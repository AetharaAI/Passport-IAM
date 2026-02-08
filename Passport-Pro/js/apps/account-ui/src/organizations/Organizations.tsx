import OrganizationRepresentation from "@passport/passport-admin-client/lib/defs/organizationRepresentation";
import {
  ErrorBoundaryProvider,
  PassportSpinner,
  ListEmptyState,
  OrganizationTable,
  useEnvironment,
} from "@passport/passport-ui-shared";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { getUserOrganizations } from "../api/methods";
import { Page } from "../components/page/Page";
import { Environment } from "../environment";
import { usePromise } from "../utils/usePromise";

export const Organizations = () => {
  const { t } = useTranslation();
  const context = useEnvironment<Environment>();

  const [userOrgs, setUserOrgs] = useState<OrganizationRepresentation[]>([]);

  usePromise(
    (signal) => getUserOrganizations({ signal, context }),
    setUserOrgs,
  );

  if (!userOrgs) {
    return <PassportSpinner />;
  }

  return (
    <Page title={t("organizations")} description={t("organizationDescription")}>
      <ErrorBoundaryProvider>
        <OrganizationTable
          link={({ children }) => <span>{children}</span>}
          loader={userOrgs}
        >
          <ListEmptyState
            message={t("emptyUserOrganizations")}
            instructions={t("emptyUserOrganizationsInstructions")}
          />
        </OrganizationTable>
      </ErrorBoundaryProvider>
    </Page>
  );
};

export default Organizations;
