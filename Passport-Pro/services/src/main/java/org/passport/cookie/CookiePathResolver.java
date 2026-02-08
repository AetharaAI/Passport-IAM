package org.passport.cookie;

import org.passport.models.PassportContext;
import org.passport.services.resources.RealmsResource;

class CookiePathResolver {

    private final PassportContext context;
    private String realmPath;

    private String requestPath;

    CookiePathResolver(PassportContext context) {
        this.context = context;
    }

    String resolvePath(CookieType cookieType) {
        switch (cookieType.getPath()) {
            case REALM:
                if (realmPath == null) {
                    realmPath = RealmsResource.realmBaseUrl(context.getUri()).path("/").build(context.getRealm().getName()).getRawPath();
                }
                return realmPath;
            case REQUEST:
                if (requestPath == null) {
                    requestPath = context.getUri().getRequestUri().getRawPath();
                }
                return requestPath;
            default:
                throw new IllegalArgumentException("Unsupported enum value " + cookieType.getPath().name());
        }
    }

}
