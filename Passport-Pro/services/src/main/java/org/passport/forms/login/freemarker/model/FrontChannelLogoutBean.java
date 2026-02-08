package org.passport.forms.login.freemarker.model;

import java.util.List;

import org.passport.models.PassportSession;
import org.passport.protocol.oidc.FrontChannelLogoutHandler;

public class FrontChannelLogoutBean {

    private final FrontChannelLogoutHandler logoutInfo;

    public FrontChannelLogoutBean(PassportSession session) {
        logoutInfo = FrontChannelLogoutHandler.current(session);
    }

    public String getLogoutRedirectUri() {
        return logoutInfo.getLogoutRedirectUri();
    }

    public List<FrontChannelLogoutHandler.ClientInfo> getClients() {
        return logoutInfo.getClients();
    }

}
