import type UserRepresentation from "@passport/passport-admin-client/lib/defs/userRepresentation";

import { fetchAdminUI } from "../../context/auth/admin-ui-endpoint";
import PassportAdminClient from "@passport/passport-admin-client";

type IDQuery = {
  id: string;
  type: string;
};

type PaginatingQuery = IDQuery & {
  first: number;
  max: number;
  search?: string;
};

type EffectiveClientRolesQuery = IDQuery;

type Query = Partial<Omit<PaginatingQuery, "adminClient">> & {
  endpoint: string;
};

type ClientRole = {
  id: string;
  role: string;
  description?: string;
  client: string;
  clientId: string;
};

const fetchEndpoint = async (
  adminClient: PassportAdminClient,
  { id, type, first, max, search, endpoint }: Query,
): Promise<any> =>
  fetchAdminUI(
    adminClient,
    `/ui-ext/${endpoint}/${type}/${encodeURIComponent(id!)}`,
    {
      first: (first || 0).toString(),
      max: (max || 10).toString(),
      search: search || "",
    },
  );

export const getAvailableClientRoles = (
  adminClient: PassportAdminClient,
  query: PaginatingQuery,
): Promise<ClientRole[]> =>
  fetchEndpoint(adminClient, { ...query, endpoint: "available-roles" });

export const getEffectiveClientRoles = (
  adminClient: PassportAdminClient,
  query: EffectiveClientRolesQuery,
): Promise<ClientRole[]> =>
  fetchEndpoint(adminClient, { ...query, endpoint: "effective-roles" });

type UserQuery = {
  lastName?: string;
  firstName?: string;
  email?: string;
  username?: string;
  emailVerified?: boolean;
  idpAlias?: string;
  idpUserId?: string;
  enabled?: boolean;
  briefRepresentation?: boolean;
  exact?: boolean;
  q?: string;
};

export type BruteUser = UserRepresentation & {
  bruteForceStatus?: Record<string, object>;
};

export const findUsers = (
  adminClient: PassportAdminClient,
  query: UserQuery,
): Promise<BruteUser[]> =>
  fetchAdminUI(
    adminClient,
    "ui-ext/brute-force-user",
    query as Record<string, string>,
  );

export const fetchUsedBy = (
  adminClient: PassportAdminClient,
  query: PaginatingQuery,
): Promise<string[]> =>
  fetchEndpoint(adminClient, {
    ...query,
    endpoint: "authentication-management",
  });
