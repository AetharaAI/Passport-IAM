package org.passport.testsuite.user.profile;

import java.util.Map;

import org.passport.models.PassportSession;
import org.passport.models.UserModel;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.userprofile.DeclarativeUserProfileProvider;
import org.passport.userprofile.UserProfile;
import org.passport.userprofile.UserProfileContext;

public class CustomUserProfileProvider extends DeclarativeUserProfileProvider {

    public CustomUserProfileProvider(PassportSession session, CustomUserProfileProviderFactory factory) {
        super(session, factory);
        UPConfig upConfig = getConfiguration();

        upConfig.getAttribute(UserModel.FIRST_NAME).setRequired(null);
        upConfig.getAttribute(UserModel.LAST_NAME).setRequired(null);
        upConfig.getAttribute(UserModel.EMAIL).setRequired(null);

        setConfiguration(upConfig);
    }

    @Override
    public UserProfile create(UserProfileContext context, UserModel user) {
        return this.create(context, user.getAttributes(), user);
    }

    @Override
    public UserProfile create(UserProfileContext context, Map<String, ?> attributes, UserModel user) {
        return super.create(context, attributes, user);
    }

    @Override
    public UserProfile create(UserProfileContext context, Map<String, ?> attributes) {
        return this.create(context, attributes, (UserModel) null);
    }

}
