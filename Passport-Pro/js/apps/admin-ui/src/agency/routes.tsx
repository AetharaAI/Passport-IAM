import { lazy } from "react";
import type { Path } from "react-router-dom";
import { generateEncodedPath } from "../utils/generateEncodedPath";
import type { AppRouteObject } from "../routes";

// Agency Dashboard
export type AgencyParams = {
    realm: string;
};

const AgencyDashboard = lazy(() => import("./AgencyDashboard"));

// Agency Configuration
const AgencyConfig = lazy(() => import("./AgencyConfig"));

export const AgencyConfigRoute: AppRouteObject = {
    path: "/:realm/agency/configure",
    element: <AgencyConfig />,
    breadcrumb: (t) => t("agencyConfiguration"),
    handle: {
        access: "manage-realm",
    },
};

export const toAgencyConfig = (params: AgencyParams): Partial<Path> => ({
    pathname: generateEncodedPath(AgencyConfigRoute.path, params),
});

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

// Delegates
const DelegateForm = lazy(() => import("./DelegateForm"));

export const CreateDelegateRoute: AppRouteObject = {
    path: "/:realm/agency/delegates/new",
    element: <DelegateForm />,
    breadcrumb: (t) => t("createDelegate"),
    handle: {
        access: "manage-realm",
    },
};

export const toCreateDelegate = (params: PrincipalsParams): Partial<Path> => ({
    pathname: generateEncodedPath(CreateDelegateRoute.path, params),
});

// Mint Passport
const MintPassportForm = lazy(() => import("./MintPassportForm"));

export const MintPassportRoute: AppRouteObject = {
    path: "/:realm/agency/passports/mint",
    element: <MintPassportForm />,
    breadcrumb: (t) => t("mintPassport"),
    handle: {
        access: "manage-realm",
    },
};

export const toMintPassport = (params: PrincipalsParams): Partial<Path> => ({
    pathname: generateEncodedPath(MintPassportRoute.path, params),
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
    AgencyConfigRoute,
    PrincipalsListRoute,
    CreatePrincipalRoute,
    CreateDelegateRoute,
    MintPassportRoute,
    PrincipalDetailRoute,
    PrincipalDetailWithTabRoute,
];

export default routes;
