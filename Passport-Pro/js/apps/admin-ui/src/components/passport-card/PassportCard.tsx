import {
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  CardTitle,
  Dropdown,
  DropdownList,
  Flex,
  FlexItem,
  Label,
  MenuToggle,
  MenuToggleElement,
} from "@patternfly/react-core";
import { ReactElement, useState } from "react";
import { Link, To } from "react-router-dom";

import "./passport-card.css";
import { EllipsisVIcon } from "@patternfly/react-icons";

export type PassportCardProps = {
  title: string;
  dropdownItems?: ReactElement[];
  labelText?: string;
  labelColor?: any;
  footerText?: string;
  to: To;
};

export const PassportCard = ({
  title,
  dropdownItems,
  labelText,
  labelColor,
  footerText,
  to,
}: PassportCardProps) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const onDropdownToggle = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  return (
    <Card isSelectable isClickable>
      <CardHeader
        actions={{
          actions: dropdownItems ? (
            <Dropdown
              popperProps={{
                position: "right",
              }}
              onOpenChange={onDropdownToggle}
              toggle={(ref: React.Ref<MenuToggleElement>) => (
                <MenuToggle
                  ref={ref}
                  onClick={onDropdownToggle}
                  variant="plain"
                  data-testid={`${title}-dropdown`}
                >
                  <EllipsisVIcon />
                </MenuToggle>
              )}
              isOpen={isDropdownOpen}
            >
              <DropdownList>{dropdownItems}</DropdownList>
            </Dropdown>
          ) : undefined,
          hasNoOffset: false,
          className: undefined,
        }}
      >
        <CardTitle data-testid="passport-card-title">
          <Link to={to}>{title}</Link>
        </CardTitle>
      </CardHeader>
      <CardBody />
      <CardFooter>
        <Flex>
          <FlexItem className="passport--passport-card__footer">
            {footerText && footerText}
          </FlexItem>
          <FlexItem>
            {labelText && (
              <Label color={labelColor || "gray"}>{labelText}</Label>
            )}
          </FlexItem>
        </Flex>
      </CardFooter>
    </Card>
  );
};
