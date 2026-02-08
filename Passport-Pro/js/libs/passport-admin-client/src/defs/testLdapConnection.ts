/**
 * https://passport-pro.ai/docs-api/11.0/rest-api/#_testldapconnectionrepresentation
 */

export default interface TestLdapConnectionRepresentation {
  action?: string;
  connectionUrl?: string;
  bindDn?: string;
  bindCredential?: string;
  useTruststoreSpi?: string;
  connectionTimeout?: string;
  componentId?: string;
  startTls?: string;
  authType?: string;
}
