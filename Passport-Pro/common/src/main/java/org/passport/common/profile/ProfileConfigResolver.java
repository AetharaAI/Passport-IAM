package org.passport.common.profile;

import org.passport.common.Profile;

public interface ProfileConfigResolver {

    Profile.ProfileName getProfileName();

    FeatureConfig getFeatureConfig(String feature);

    public enum FeatureConfig {
        ENABLED,
        DISABLED,
        UNCONFIGURED
    }

}
