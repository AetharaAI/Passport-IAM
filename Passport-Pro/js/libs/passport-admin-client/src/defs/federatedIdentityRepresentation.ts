/**
 * https://passport-pro.ai/docs-api/11.0/rest-api/index.html#_federatedidentityrepresentation
 */

export default interface FederatedIdentityRepresentation {
  identityProvider?: string;
  userId?: string;
  userName?: string;
}
