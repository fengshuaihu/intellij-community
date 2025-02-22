// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.updateSettings.impl.pluginsAdvertisement

import com.intellij.configurationStore.FileStorageAnnotation
import com.intellij.ide.IdeBundle
import com.intellij.ide.plugins.DEPENDENCY_SUPPORT_FEATURE
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginNode
import com.intellij.ide.plugins.advertiser.PluginDataSet
import com.intellij.ide.plugins.advertiser.PluginFeatureCacheService
import com.intellij.ide.plugins.advertiser.PluginFeatureMap
import com.intellij.ide.plugins.marketplace.MarketplaceRequests
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.stateStore
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.EditorNotifications
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

internal class PluginsAdvertiserStartupActivity : ProjectActivity {
  suspend fun checkSuggestedPlugins(project: Project, includeIgnored: Boolean) {
    val application = ApplicationManager.getApplication()
    if (application.isUnitTestMode || application.isHeadlessEnvironment) {
      return
    }

    val customPlugins = loadPluginsFromCustomRepositories()

    coroutineContext.ensureActive()

    val extensionsService = PluginFeatureCacheService.getInstance()
    val oldExtensions = extensionsService.extensions

    val pluginAdvertiserService = PluginAdvertiserService.getInstance(project)
    pluginAdvertiserService.collectDependencyUnknownFeatures(includeIgnored)
    val unknownFeatures = UnknownFeaturesCollector.getInstance(project).unknownFeatures

    if (oldExtensions != null && unknownFeatures.isEmpty()) {
      if (includeIgnored) {
        coroutineContext.ensureActive()

        withContext(Dispatchers.EDT) {
          notificationGroup.createNotification(IdeBundle.message("plugins.advertiser.no.suggested.plugins"), NotificationType.INFORMATION)
            .setDisplayId("advertiser.no.plugins")
            .notify(project)
        }
      }
    }

    try {
      val customPluginIds = customPlugins.map { it.pluginId.idString }.toSet()
      if (oldExtensions == null
          || oldExtensions.isOutdated
          || includeIgnored) {
        @Suppress("DEPRECATION")
        extensionsService.extensions = PluginFeatureMap(
          getFeatureMapFromMarketPlace(customPluginIds, FileTypeFactory.FILE_TYPE_FACTORY_EP.name),
          if (oldExtensions != null) System.currentTimeMillis() else 0L,
        )
        coroutineContext.ensureActive()
        EditorNotifications.getInstance(project).updateAllNotifications()
      }

      val oldDependencies = extensionsService.dependencies
      if (oldDependencies == null
          || oldDependencies.isOutdated
          || includeIgnored) {
        extensionsService.dependencies = PluginFeatureMap(
          getFeatureMapFromMarketPlace(customPluginIds, DEPENDENCY_SUPPORT_FEATURE),
          if (oldDependencies != null) System.currentTimeMillis() else 0L,
        )
      }
      coroutineContext.ensureActive()

      if (unknownFeatures.isNotEmpty()) {
        pluginAdvertiserService.run(
          customPlugins = customPlugins,
          unknownFeatures = unknownFeatures,
          includeIgnored = includeIgnored
        )
      }

      notifyUnbundledPlugins(project)
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) {
      if (e !is ControlFlowException) {
        LOG.info(e)
      }
    }
  }

  override suspend fun execute(project: Project) {
    if (!Registry.`is`("ide.show.plugin.suggestions.on.open", true)) {
      return
    }

    checkSuggestedPlugins(project = project, includeIgnored = false)
  }
}

internal fun findSuggestedPlugins(project: Project, customRepositories: Map<String, List<PluginNode>>): List<IdeaPluginDescriptor> {
  return runBlockingMaybeCancellable {
    val application = ApplicationManager.getApplication()
    if (application.isUnitTestMode || application.isHeadlessEnvironment) {
      return@runBlockingMaybeCancellable emptyList()
    }

    val customPlugins = ArrayList<PluginNode>()
    for (value in customRepositories.values) {
      customPlugins.addAll(value)
    }

    val pluginAdvertiserService = PluginAdvertiserService.getInstance(project)
    pluginAdvertiserService.collectDependencyUnknownFeatures(true)

    val customPluginIds = customPlugins.map { it.pluginId.idString }.toSet()
    val extensionsService = PluginFeatureCacheService.getInstance()
    val oldDependencies = extensionsService.dependencies
    extensionsService.dependencies = PluginFeatureMap(
      getFeatureMapFromMarketPlace(customPluginIds, DEPENDENCY_SUPPORT_FEATURE),
      if (oldDependencies != null) System.currentTimeMillis() else 0L,
    )

    val unknownFeatures = UnknownFeaturesCollector.getInstance(project).unknownFeatures

    if (unknownFeatures.isNotEmpty()) {
      return@runBlockingMaybeCancellable pluginAdvertiserService.fetch(customPlugins, unknownFeatures, true)
    }

    return@runBlockingMaybeCancellable emptyList()
  }
}

private fun getFeatureMapFromMarketPlace(customPluginIds: Set<String>, featureType: String): Map<String, PluginDataSet> {
  val params = mapOf("featureType" to featureType)
  return MarketplaceRequests.getInstance()
    .getFeatures(params)
    .groupBy(
      { it.implementationName!! },
      { feature -> feature.toPluginData { customPluginIds.contains(it) } }
    ).mapValues {
      PluginDataSet(it.value.filterNotNull().toSet())
    }
}

private fun notifyUnbundledPlugins(project: Project) {
  val llmPluginId = PluginId.getId("com.intellij.ml.llm")

  if (PluginManagerCore.isPluginInstalled(llmPluginId) || PlatformUtils.isCommunityEdition()) {
    return
  }
  if (PropertiesComponent.getInstance().isTrueValue("dismiss.llm.not.bundled")) {
    return
  }

  val storage = ApplicationManager.getApplication().stateStore.storageManager.getStateStorage(
    FileStorageAnnotation("llm.for.code.xml", false)
  )
  if (storage.hasState("LLMSettings", false)) {
    notificationGroup.createNotification(IdeBundle.message("plugins.advertiser.unbundled.plugin.ai.assistant"), NotificationType.INFORMATION)
      .setDisplayId("advertiser.unbundled.plugin")
      .addAction(object : AnAction(IdeBundle.message("plugins.advertiser.unbundled.plugin.install")) {
        override fun actionPerformed(e: AnActionEvent) {
          installAndEnable(project, setOf(llmPluginId), true) {
          }
        }
      })
      .addAction(object : NotificationAction(IdeBundle.message("plugins.advertiser.unbundled.plugin.dismiss")) {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
          notification.expire()
          PropertiesComponent.getInstance().setValue("dismiss.llm.not.bundled", true)
        }
      })
      .notify(project)
  }
}

private val PluginFeatureMap.isOutdated: Boolean
  get() = System.currentTimeMillis() - lastUpdateTime > TimeUnit.DAYS.toMillis(1L)
