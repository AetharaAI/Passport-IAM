import {
  createNamedContext,
  useRequiredContext,
} from "@passport/passport-ui-shared";
import { Groups } from "@passport/passport-admin-client";
import { PropsWithChildren } from "react";

export const GroupsResourceContext = createNamedContext<Groups | undefined>(
  "GroupsResourceContext",
  undefined,
);

export const useGroupResource = () => useRequiredContext(GroupsResourceContext);

type GroupsContextProps = PropsWithChildren & {
  value: Groups;
};
export const GroupResourceContext = ({
  value,
  children,
}: GroupsContextProps) => {
  return (
    <GroupsResourceContext.Provider value={value}>
      {children}
    </GroupsResourceContext.Provider>
  );
};
