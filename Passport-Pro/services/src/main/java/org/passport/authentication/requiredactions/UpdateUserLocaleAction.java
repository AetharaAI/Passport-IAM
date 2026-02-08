package org.passport.authentication.requiredactions;

import org.passport.Config;
import org.passport.authentication.RequiredActionContext;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.locale.LocaleSelectorProvider;
import org.passport.locale.LocaleUpdaterProvider;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.UserModel;

public class UpdateUserLocaleAction implements RequiredActionProvider, RequiredActionFactory {

    @Override
    public String getDisplayText() {
        return "Update User Locale";
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        String userRequestedLocale = context.getAuthenticationSession().getAuthNote(LocaleSelectorProvider.USER_REQUEST_LOCALE);
        if (userRequestedLocale != null) {
            LocaleUpdaterProvider updater = context.getSession().getProvider(LocaleUpdaterProvider.class);
            updater.updateUsersLocale(context.getUser(), userRequestedLocale);
        } else {
            String userLocale = context.getUser().getFirstAttribute(UserModel.LOCALE);

            if (userLocale != null) {
                LocaleUpdaterProvider updater = context.getSession().getProvider(LocaleUpdaterProvider.class);
                updater.updateLocaleCookie(userLocale);
            } else {
                LocaleUpdaterProvider updater = context.getSession().getProvider(LocaleUpdaterProvider.class);
                updater.expireLocaleCookie();
            }
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
    }

    @Override
    public void processAction(RequiredActionContext context) {
    }

    @Override
    public RequiredActionProvider create(PassportSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "update_user_locale";
    }

}
