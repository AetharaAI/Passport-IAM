package org.passport.testsuite.theme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.Config.Scope;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.resource.AccountResourceProvider;
import org.passport.services.resource.AccountResourceProviderFactory;

import org.jboss.resteasy.reactive.NoCache;

public class CustomAccountResourceProviderFactory implements AccountResourceProviderFactory, AccountResourceProvider {
  public static final String ID = "ext-custom-account-console";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public AccountResourceProvider create(PassportSession session) {
    return this;
  }

  @Override
  public Object getResource() {
    return this;
  }

  @GET
  @NoCache
  @Produces(MediaType.TEXT_HTML)
  public Response getMainPage() {
    return Response.ok().entity("<html><head><title>Account</title></head><body><h1>Custom Account Console</h1></body></html>").build();
  }
  
  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(PassportSessionFactory factory) {}

  @Override
  public void close() {}
}
