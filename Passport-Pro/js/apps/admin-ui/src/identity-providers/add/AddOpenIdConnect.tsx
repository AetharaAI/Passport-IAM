import type IdentityProviderRepresentation from "@passport/passport-admin-client/lib/defs/identityProviderRepresentation";
import {
  ActionGroup,
  AlertVariant,
  Button,
  PageSection,
} from "@patternfly/react-core";
import { FormProvider, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAdminClient } from "../../admin-client";
import { useAlerts } from "@passport/passport-ui-shared";
import { FormAccess } from "../../components/form/FormAccess";
import { ViewHeader } from "../../components/view-header/ViewHeader";
import { useRealm } from "../../context/realm-context/RealmContext";
import { toIdentityProvider } from "../routes/IdentityProvider";
import { toIdentityProviders } from "../routes/IdentityProviders";
import { OIDCAuthentication } from "./OIDCAuthentication";
import { OIDCGeneralSettings } from "./OIDCGeneralSettings";
import { OpenIdConnectSettings } from "./OpenIdConnectSettings";

type DiscoveryIdentity = IdentityProviderRepresentation & {
  discoveryEndpoint?: string;
};

export default function AddOpenIdConnect() {
  const { adminClient } = useAdminClient();

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const isPassport = pathname.includes("passport-oidc");
  const id = `${isPassport ? "passport-" : ""}oidc`;

  const form = useForm<IdentityProviderRepresentation>({
    defaultValues: { alias: id },
    mode: "onChange",
  });
  const {
    handleSubmit,
    formState: { isDirty },
  } = form;

  const { addAlert, addError } = useAlerts();
  const { realm } = useRealm();

  const onSubmit = async (provider: DiscoveryIdentity) => {
    delete provider.discoveryEndpoint;
    try {
      await adminClient.identityProviders.create({
        ...provider,
        providerId: id,
      });
      addAlert(t("createIdentityProviderSuccess"), AlertVariant.success);
      navigate(
        toIdentityProvider({
          realm,
          providerId: id,
          alias: provider.alias!,
          tab: "settings",
        }),
      );
    } catch (error) {
      addError("createIdentityProviderError", error);
    }
  };

  return (
    <>
      <ViewHeader
        titleKey={t(
          isPassport ? "addPassportOpenIdProvider" : "addOpenIdProvider",
        )}
      />
      <PageSection variant="light">
        <FormProvider {...form}>
          <FormAccess
            role="manage-identity-providers"
            isHorizontal
            onSubmit={handleSubmit(onSubmit)}
          >
            <OIDCGeneralSettings />
            <OpenIdConnectSettings isOIDC />
            <OIDCAuthentication />
            <ActionGroup>
              <Button
                isDisabled={!isDirty}
                variant="primary"
                type="submit"
                data-testid="createProvider"
              >
                {t("add")}
              </Button>
              <Button
                variant="link"
                data-testid="cancel"
                component={(props) => (
                  <Link {...props} to={toIdentityProviders({ realm })} />
                )}
              >
                {t("cancel")}
              </Button>
            </ActionGroup>
          </FormAccess>
        </FormProvider>
      </PageSection>
    </>
  );
}
