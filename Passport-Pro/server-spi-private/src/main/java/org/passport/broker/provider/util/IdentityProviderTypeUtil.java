package org.passport.broker.provider.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.passport.broker.provider.ClientAssertionIdentityProvider;
import org.passport.broker.provider.ExchangeExternalToken;
import org.passport.broker.provider.IdentityProvider;
import org.passport.broker.provider.JWTAuthorizationGrantProvider;
import org.passport.broker.provider.UserAuthenticationIdentityProvider;
import org.passport.broker.social.SocialIdentityProvider;
import org.passport.models.IdentityProviderCapability;
import org.passport.models.IdentityProviderModel;
import org.passport.models.IdentityProviderType;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderFactory;

public class IdentityProviderTypeUtil {

    private IdentityProviderTypeUtil() {
    }

    public static List<IdentityProviderType> listTypesFromFactory(PassportSession session, String factoryId) {
        PassportSessionFactory sf = session.getPassportSessionFactory();
        ProviderFactory<?> factory = sf.getProviderFactory(IdentityProvider.class, factoryId);
        if (factory == null) {
            return List.of();
        }
        Class<?> providerType = getType(factory);
        return Arrays.stream(IdentityProviderType.values())
                .filter(t -> !t.equals(IdentityProviderType.ANY) && toTypeClass(t).isAssignableFrom(providerType))
                .collect(Collectors.toList());
    }

    public static List<String> listFactoriesByCapability(PassportSession session, IdentityProviderCapability capability) {
        Set<IdentityProviderType> types = Arrays.stream(IdentityProviderType.values()).filter(t -> t.getCapabilities().contains(capability)).collect(Collectors.toSet());
        return listFactoriesByTypes(session, types);
    }

    public static List<String> listFactoriesByType(PassportSession session, IdentityProviderType type) {
        return listFactoriesByTypes(session, Set.of(type));
    }

    private static List<String> listFactoriesByTypes(PassportSession session, Set<IdentityProviderType> types) {
        PassportSessionFactory sf = session.getPassportSessionFactory();

        Stream<ProviderFactory> factories = sf.getProviderFactoriesStream(IdentityProvider.class);
        if (types.contains(IdentityProviderType.ANY) || types.contains(IdentityProviderType.USER_AUTHENTICATION) || types.contains(IdentityProviderType.JWT_AUTHORIZATION_GRANT)) {
            factories = Stream.concat(factories, sf.getProviderFactoriesStream(SocialIdentityProvider.class));
        }

        Set<Class<?>> typeClasses = types.stream().map(IdentityProviderTypeUtil::toTypeClass).collect(Collectors.toSet());

        return factories.filter(f -> typeClasses.stream().anyMatch(t -> t.isAssignableFrom(getType(f))))
                .map(ProviderFactory::getId)
                .toList();
    }

    private static Class<?> getType(ProviderFactory<?> f) {
        try {
            return f.getClass().getMethod("create", PassportSession.class, IdentityProviderModel.class).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> toTypeClass(IdentityProviderType type) {
        return switch (type) {
            case USER_AUTHENTICATION -> UserAuthenticationIdentityProvider.class;
            case CLIENT_ASSERTION -> ClientAssertionIdentityProvider.class;
            case EXCHANGE_EXTERNAL_TOKEN -> ExchangeExternalToken.class;
            case JWT_AUTHORIZATION_GRANT -> JWTAuthorizationGrantProvider.class;
            case ANY -> IdentityProvider.class;
        };
    }

}
