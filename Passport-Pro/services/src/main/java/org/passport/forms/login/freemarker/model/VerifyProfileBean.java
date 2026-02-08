package org.passport.forms.login.freemarker.model;

import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;

import org.passport.models.PassportSession;
import org.passport.models.UserModel;
import org.passport.userprofile.UserProfile;
import org.passport.userprofile.UserProfileContext;
import org.passport.userprofile.UserProfileProvider;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class VerifyProfileBean extends AbstractUserProfileBean {

    private final UserModel user;

    public VerifyProfileBean(UserModel user, MultivaluedMap<String, String> formData, PassportSession session) {
        super(formData);
        this.user = user;
        init(session, false);
    }

    @Override
    protected UserProfile createUserProfile(UserProfileProvider provider) {
        return provider.create(UserProfileContext.UPDATE_PROFILE, user);
    }

    @Override
    protected Stream<String> getAttributeDefaultValues(String name){
        return user.getAttributeStream(name);
    }
    
    @Override 
    public String getContext() {
        return UserProfileContext.UPDATE_PROFILE.name();
    }

}
