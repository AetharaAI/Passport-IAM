/**
 * https://passport-pro.ai/docs-api/11.0/rest-api/index.html#_rolesrepresentation
 */

import type RoleRepresentation from "./roleRepresentation.js";

export default interface RolesRepresentation {
  realm?: RoleRepresentation[];
  client?: { [index: string]: RoleRepresentation[] };
  application?: { [index: string]: RoleRepresentation[] };
}
