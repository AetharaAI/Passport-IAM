const adminGuide =
  "https://passport-pro.ai/docs/latest/server_admin/index.html";

const passportHomepageURL = "https://passport-pro.ai";

export default {
  documentationUrl: adminGuide,
  clientsUrl: `${adminGuide}#assembly-managing-clients_server_administration_guide`,
  clientScopesUrl: `${adminGuide}#_client_scopes`,
  realmRolesUrl: `${adminGuide}#assigning-permissions-using-roles-and-groups`,
  usersUrl: `${adminGuide}#assembly-managing-users_server_administration_guide`,
  groupsUrl: `${adminGuide}#proc-managing-groups_server_administration_guide`,
  sessionsUrl: `${adminGuide}#managing-user-sessions`,
  eventsUrl: `${adminGuide}#configuring-auditing-to-track-events`,
  realmSettingsUrl: `${adminGuide}#configuring-realms`,
  authenticationUrl: `${adminGuide}#configuring-authentication`,
  identityProvidersUrl: `${adminGuide}#_identity_broker`,
  userFederationUrl: `${adminGuide}#_user-storage-federation`,
  documentation: `${passportHomepageURL}/documentation`,
  guides: `${passportHomepageURL}/guides`,
  community: `${passportHomepageURL}/community`,
  blog: `${passportHomepageURL}/blog`,
  workflowsUrl: `https://passport-pro.ai/2025/10/workflows-experimental-26-4`,
};
