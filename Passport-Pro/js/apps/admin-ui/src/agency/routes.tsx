import { lazy } from "react";
import type { Path } from "react-router-dom";
import { generateEncodedPath } from "../utils/generateEncodedPath";
import type { AppRouteObject } from "../routes";

// Agency Dashboard
export type AgencyParams = {
    realm: string;
};

const AgencyDashboard = lazy(() => import("./AgencyDashboard"));

export const AgencyDashboardRoute: AppRouteObject = {
    path: "/:realm/agency",
    element: <AgencyDashboard />,
  breadcrumb: (t) => t("agency"),
    handle: {
        access: "view-realm",
    },
};

export const toAgencyDashboard = (params: AgencyParams): Partial<Path> => ({
    pathname: generateEncodedPath(AgencyDashboardRoute.path, params),
});

// Principals List
export type PrincipalsParams = {
    realm: string;
};

const PrincipalsList = lazy(() => import("./PrincipalsList"));

export const PrincipalsListRoute: AppRouteObject = {
    path: "/:realm/agency/principals",
    element: <PrincipalsList />,
  breadcrumb: (t) => t("principals"),
    handle: {
        access: "view-realm",
    },
};

export const toPrincipalsList = (params: PrincipalsParams): Partial<Path> => ({
    pathname: generateEncodedPath(PrincipalsListRoute.path, params),
});

// Create Principal
const CreatePrincipal = lazy(() => import("./CreatePrincipal"));

export const CreatePrincipalRoute: AppRouteObject = {
    path: "/:realm/agency/principals/new",
    element: <CreatePrincipal />,
  breadcrumb: (t) => t("createPrincipal"),
    handle: {
        access: "manage-realm",
    },
};

export const toCreatePrincipal = (params: PrincipalsParams): Partial<Path> => ({
    pathname: generateEncodedPath(CreatePrincipalRoute.path, params),
});

// Principal Detail
export type PrincipalDetailParams = {
    realm: string;
    principalId: string;
    tab?: string;
};

const PrincipalDetail = lazy(() => import("./PrincipalDetail"));

export const PrincipalDetailRoute: AppRouteObject = {
    path: "/:realm/agency/principals/:principalId",
    element: <PrincipalDetail />,
  breadcrumb: (t) => t("principalDetails"),
    handle: {
        access: "view-realm",
    },
};

export const PrincipalDetailWithTabRoute: AppRouteObject = {
    ...PrincipalDetailRoute,
    path: "/:realm/agency/principals/:principalId/:tab",
};

export const toPrincipalDetail = (params: PrincipalDetailParams): Partial<Path> => {
    const path = params.tab
        ? PrincipalDetailWithTabRoute.path
        : PrincipalDetailRoute.path;
    return {
        pathname: generateEncodedPath(path, params),
    };
};

// Export all routes
const routes: AppRouteObject[] = [
    AgencyDashboardRoute,
    PrincipalsListRoute,
    CreatePrincipalRoute,
    PrincipalDetailRoute,
    PrincipalDetailWithTabRoute,
];

export default routes;
