import type WhoAmIRepresentation from "../defs/whoAmIRepresentation.js";
import type PassportAdminClient from "../index.js";
import Resource from "./resource.js";

export class WhoAmI extends Resource<{ realm?: string }> {
  constructor(client: PassportAdminClient) {
    super(client, {
      path: "/admin/{realm}/console",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }

  public find = this.makeRequest<
    { currentRealm: string },
    WhoAmIRepresentation
  >({
    method: "GET",
    path: "/whoami",
    queryParamKeys: ["currentRealm"],
  });
}
