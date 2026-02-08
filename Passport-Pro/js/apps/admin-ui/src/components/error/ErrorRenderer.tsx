import { NetworkError } from "@passport/passport-admin-client";
import {
  useEnvironment,
  type FallbackProps,
} from "@passport/passport-ui-shared";
import {
  Alert,
  AlertActionLink,
  AlertVariant,
  PageSection,
} from "@patternfly/react-core";
import { useTranslation } from "react-i18next";

export const ErrorRenderer = ({ error }: FallbackProps) => {
  const { passport } = useEnvironment();
  const { t } = useTranslation();
  const isPermissionError =
    error instanceof NetworkError && error.response.status === 403;

  let message;
  if (isPermissionError) {
    message = t("forbiddenAdminConsole");
  } else {
    message = error.message;
  }

  return (
    <PageSection>
      <Alert
        isInline
        variant={AlertVariant.danger}
        title={message}
        actionLinks={
          isPermissionError ? (
            <AlertActionLink onClick={async () => await passport.logout()}>
              {t("signOut")}
            </AlertActionLink>
          ) : (
            <AlertActionLink onClick={() => location.reload()}>
              {t("reload")}
            </AlertActionLink>
          )
        }
      ></Alert>
    </PageSection>
  );
};
