import PassportAdminClient from "@passport/passport-admin-client";
import {
  createNamedContext,
  useRequiredContext,
} from "@passport/passport-ui-shared";
import type Passport from "keycloak-js";
import type { Environment } from "./environment";

export type AdminClientProps = {
  passport: Passport;
  adminClient: PassportAdminClient;
};

export const AdminClientContext = createNamedContext<
  AdminClientProps | undefined
>("AdminClientContext", undefined);

export const useAdminClient = () => useRequiredContext(AdminClientContext);

export async function initAdminClient(
  passport: Passport,
  environment: Environment,
) {
  const adminClient = new PassportAdminClient();

  adminClient.setConfig({ realmName: environment.realm });
  adminClient.baseUrl = environment.adminBaseUrl;
  adminClient.registerTokenProvider({
    async getAccessToken() {
      try {
        await passport.updateToken(5);
      } catch {
        await passport.login();
      }

      return passport.token;
    },
  });

  return adminClient;
}
