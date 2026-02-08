import type { ServerInfoRepresentation } from "@passport/passport-admin-client/lib/defs/serverInfoRepesentation";
import {
  createNamedContext,
  PassportSpinner,
  useFetch,
  useRequiredContext,
} from "@passport/passport-ui-shared";
import { PropsWithChildren, useState } from "react";
import { useAdminClient } from "../../admin-client";
import { sortProviders } from "../../util";

export const ServerInfoContext = createNamedContext<
  ServerInfoRepresentation | undefined
>("ServerInfoContext", undefined);

export const useServerInfo = () => useRequiredContext(ServerInfoContext);

export const useLoginProviders = () =>
  sortProviders(useServerInfo().providers!["login-protocol"].providers);

export const ServerInfoProvider = ({ children }: PropsWithChildren) => {
  const { adminClient } = useAdminClient();
  const [serverInfo, setServerInfo] = useState<ServerInfoRepresentation>();

  useFetch(() => adminClient.serverInfo.find(), setServerInfo, []);

  if (!serverInfo) {
    return <PassportSpinner />;
  }

  return (
    <ServerInfoContext.Provider value={serverInfo}>
      {children}
    </ServerInfoContext.Provider>
  );
};
