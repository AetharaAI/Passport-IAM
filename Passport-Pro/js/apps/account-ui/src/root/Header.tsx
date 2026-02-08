import {
  PassportMasthead,
  label,
  useEnvironment,
} from "@passport/passport-ui-shared";
import { Button } from "@patternfly/react-core";
import { ExternalLinkSquareAltIcon } from "@patternfly/react-icons";
import { useTranslation } from "react-i18next";
import { useHref } from "react-router-dom";

import { environment } from "../environment";
import { joinPath } from "../utils/joinPath";

import style from "./header.module.css";

const ReferrerLink = () => {
  const { t } = useTranslation();

  return environment.referrerUrl ? (
    <Button
      data-testid="referrer-link"
      component="a"
      href={environment.referrerUrl.replace("_hash_", "#")}
      variant="link"
      icon={<ExternalLinkSquareAltIcon />}
      iconPosition="right"
      isInline
    >
      {t("backTo", {
        app: label(t, environment.referrerName, environment.referrerUrl),
      })}
    </Button>
  ) : null;
};

export const Header = () => {
  const { environment, passport } = useEnvironment();
  const { t } = useTranslation();

  const brandImage = environment.logo || "passport-logo.svg";
  const logoUrl = environment.logoUrl ? environment.logoUrl : "/";
  const internalLogoHref = useHref(logoUrl);

  // User can indicate that he wants an internal URL by starting it with "/"
  const indexHref = logoUrl.startsWith("/") ? internalLogoHref : logoUrl;

  return (
    <PassportMasthead
      data-testid="page-header"
      passport={passport}
      features={{ hasManageAccount: false }}
      brand={{
        href: indexHref,
        src: joinPath(environment.resourceUrl, brandImage),
        alt: t("logo"),
        className: style.brand,
      }}
      toolbarItems={[<ReferrerLink key="link" />]}
    />
  );
};
