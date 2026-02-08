import {
  Dropdown,
  DropdownList,
  DropdownProps,
  MenuToggle,
} from "@patternfly/react-core";
import { EllipsisVIcon } from "@patternfly/react-icons";
import { ReactNode, useState } from "react";

type PassportDropdownProps = Omit<DropdownProps, "toggle"> & {
  "data-testid"?: string;
  isKebab?: boolean;
  title?: ReactNode;
  dropDownItems: ReactNode[];
};

export const PassportDropdown = ({
  isKebab = false,
  title,
  dropDownItems,
  ...rest
}: PassportDropdownProps) => {
  const [open, setOpen] = useState(false);

  return (
    <Dropdown
      {...rest}
      popperProps={{
        position: "right",
      }}
      onOpenChange={(isOpen) => setOpen(isOpen)}
      toggle={(ref) => (
        <MenuToggle
          data-testid={`${rest["data-testid"]}-toggle`}
          ref={ref}
          onClick={() => setOpen(!open)}
          isExpanded={open}
          variant={isKebab ? "plain" : "default"}
        >
          {isKebab ? <EllipsisVIcon /> : title}
        </MenuToggle>
      )}
      isOpen={open}
    >
      <DropdownList>{dropDownItems}</DropdownList>
    </Dropdown>
  );
};
