// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.idea.quickfix

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.idea.base.util.names.FqNames
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.checkers.OptInNames

internal object OptInFixesUtils {
    fun annotationFqName(diagnostic: Diagnostic) = when (diagnostic.factory) {
        OPT_IN_USAGE -> OPT_IN_USAGE.cast(diagnostic).a
        OPT_IN_USAGE_ERROR -> OPT_IN_USAGE_ERROR.cast(diagnostic).a
        OPT_IN_OVERRIDE -> OPT_IN_OVERRIDE.cast(diagnostic).a
        OPT_IN_OVERRIDE_ERROR -> OPT_IN_OVERRIDE_ERROR.cast(diagnostic).a
        else -> null
    }

    fun optInFqName(moduleDescriptor: ModuleDescriptor) = OptInNames.OPT_IN_FQ_NAME.takeIf { annotationExists(moduleDescriptor, it) }
        ?: FqNames.OptInFqNames.OLD_USE_EXPERIMENTAL_FQ_NAME

    fun annotationExists(moduleDescriptor: ModuleDescriptor, fqName: FqName): Boolean =
        moduleDescriptor.resolveClassByFqName(fqName, NoLookupLocation.FROM_IDE) != null

}
