import type ClientProfileRepresentation from "./clientProfileRepresentation.js";

/**
 * https://passport-pro.ai/docs-api/15.0/rest-api/#_clientprofilesrepresentation
 */
export default interface ClientProfilesRepresentation {
  globalProfiles?: ClientProfileRepresentation[];
  profiles?: ClientProfileRepresentation[];
}
