package org.passport.theme;

import org.passport.models.ThemeManager;
import org.passport.provider.ProviderFactory;

/**
 */
public interface ThemeManagerFactory extends ProviderFactory<ThemeManager> {
  void clearCache();
}
