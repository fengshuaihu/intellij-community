// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ui.codeFloatingToolbar;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;

/**
 * Extension point to enable floating code toolbar for specific language.
 * @see CodeFloatingToolbar
 * @see FloatingToolbarFilter.ENABLED
 * @deprecated Use {@link FloatingToolbarCustomizer} instead.
 */
@Deprecated
public interface FloatingToolbarFilter {

  LanguageExtension<FloatingToolbarFilter> EP = new LanguageExtension<>("com.intellij.lang.floatingToolbarFilter");

  boolean isEnabled();

  static boolean isEnabledForLanguage(Language language){
    List<FloatingToolbarFilter> supporters = EP.allForLanguage(language);
    if (supporters.isEmpty()) return false;
    return ContainerUtil.all(supporters, supporter -> supporter.isEnabled());
  }

  /**
   * @deprecated Use {@link FloatingToolbarCustomizer.DefaultGroup} instead.
   */
  @Deprecated
  class ENABLED implements FloatingToolbarFilter {
    @Override
    public boolean isEnabled() {
      return true;
    }
  }
}
