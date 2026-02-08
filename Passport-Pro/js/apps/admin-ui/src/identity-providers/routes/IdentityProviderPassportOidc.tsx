import { lazy } from "react";
import type { Path } from "react-router-dom";
import { generateEncodedPath } from "../../utils/generateEncodedPath";
import type { AppRouteObject } from "../../routes";

export type IdentityProviderPassportOidcParams = { realm: string };

const AddOpenIdConnect = lazy(() => import("../add/AddOpenIdConnect"));

export const IdentityProviderPassportOidcRoute: AppRouteObject = {
  path: "/:realm/identity-providers/passport-oidc/add",
  element: <AddOpenIdConnect />,
  breadcrumb: (t) => t("addPassportOpenIdProvider"),
  handle: {
    access: "manage-identity-providers",
  },
};

export const toIdentityProviderPassportOidc = (
  params: IdentityProviderPassportOidcParams,
): Partial<Path> => ({
  pathname: generateEncodedPath(IdentityProviderPassportOidcRoute.path, params),
});
