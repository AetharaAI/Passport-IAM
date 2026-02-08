import type ClientPolicyExecutorRepresentation from "./clientPolicyExecutorRepresentation.js";

/**
 * https://passport-pro.ai/docs-api/15.0/rest-api/#_clientprofilerepresentation
 */
export default interface ClientProfileRepresentation {
  description?: string;
  executors?: ClientPolicyExecutorRepresentation[];
  name?: string;
}
