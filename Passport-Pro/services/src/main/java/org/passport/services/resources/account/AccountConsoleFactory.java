package org.passport.services.resources.account;

import java.io.IOException;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;

import org.passport.Config.Scope;
import org.passport.models.ClientModel;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.services.resource.AccountResourceProvider;
import org.passport.services.resource.AccountResourceProviderFactory;
import org.passport.theme.Theme;

/**
 * Provides the {@code default} {@link AccountConsole} implementation backed by the
 * {@code account} management client.
 */
public class AccountConsoleFactory implements AccountResourceProviderFactory {

  @Override
  public String getId() {
    return "default";
  }

  @Override
  public AccountResourceProvider create(PassportSession session) {
    RealmModel realm = session.getContext().getRealm();
    ClientModel client = getAccountManagementClient(realm);
    Theme theme = getTheme(session);
    return createAccountConsole(session, client, theme);
  }

  protected AccountConsole createAccountConsole(PassportSession session, ClientModel client, Theme theme) {
    return new AccountConsole(session, client, theme);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(PassportSessionFactory factory) {}

  @Override
  public void close() {}

  protected Theme getTheme(PassportSession session) {
    try {
      return session.theme().getTheme(Theme.Type.ACCOUNT);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  protected  ClientModel getAccountManagementClient(RealmModel realm) {
    ClientModel client = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
    if (client == null || !client.isEnabled()) {
      throw new NotFoundException("account management not enabled");
    }
    return client;
  }
}
