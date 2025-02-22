// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.github.pullrequest.ui.details.model.impl

import com.intellij.collaboration.ui.codereview.details.model.CodeReviewChangesViewModelBase
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.plugins.github.api.data.GHCommit
import org.jetbrains.plugins.github.api.data.GHUser
import org.jetbrains.plugins.github.pullrequest.GHPRStatisticsCollector
import org.jetbrains.plugins.github.pullrequest.data.service.GHPRSecurityService
import org.jetbrains.plugins.github.pullrequest.ui.GHSimpleLoadingModel
import org.jetbrains.plugins.github.pullrequest.ui.getResultFlow
import org.jetbrains.plugins.github.pullrequest.ui.toolwindow.GHPRDiffController

internal class GHPRCommitsViewModel(
  scope: CoroutineScope,
  private val project: Project,
  commitsLoadingModel: GHSimpleLoadingModel<List<GHCommit>>,
  securityService: GHPRSecurityService,
  private val diffBridge: GHPRDiffController
) : CodeReviewChangesViewModelBase<GHCommit>() {
  val ghostUser: GHUser = securityService.ghostUser

  override val reviewCommits: StateFlow<List<GHCommit>> = commitsLoadingModel.getResultFlow()
    .map { commits -> commits?.asReversed() ?: listOf() }
    .stateIn(scope, SharingStarted.Eagerly, listOf())

  override fun commitHash(commit: GHCommit): String {
    return commit.abbreviatedOid
  }

  override fun selectCommit(index: Int) {
    if (index > -1) {
      diffBridge.activeTree = GHPRDiffController.ActiveTree.COMMITS
    }
    else {
      diffBridge.activeTree = GHPRDiffController.ActiveTree.FILES
    }
    super.selectCommit(index)
    GHPRStatisticsCollector.logDetailsCommitChosen(project)
  }

  override fun selectNextCommit() {
    super.selectNextCommit()
    GHPRStatisticsCollector.logDetailsNextCommitChosen(project)
  }

  override fun selectPreviousCommit() {
    super.selectPreviousCommit()
    GHPRStatisticsCollector.logDetailsPrevCommitChosen(project)
  }
}