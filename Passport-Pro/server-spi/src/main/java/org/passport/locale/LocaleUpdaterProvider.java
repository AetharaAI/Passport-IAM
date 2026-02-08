package org.passport.locale;

import org.passport.models.UserModel;
import org.passport.provider.Provider;

public interface LocaleUpdaterProvider extends Provider {

    void updateUsersLocale(UserModel user, String locale);

    void updateLocaleCookie(String locale);

    void expireLocaleCookie();

}
