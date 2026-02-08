import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { PassportSpinner, useFetch, useAlerts } from "@passport/passport-ui-shared";
import {
    Button,
    ButtonVariant,
    Dropdown,
    DropdownItem,
    DropdownList,
    EmptyState,
    EmptyStateHeader,
    EmptyStateIcon,
    EmptyStateBody,
    EmptyStateActions,
    EmptyStateFooter,
    Label,
    MenuToggle,
    PageSection,
    Title,
    Toolbar,
    ToolbarContent,
    ToolbarItem,
} from "@patternfly/react-core";
import {
    BuildingIcon,
    EllipsisVIcon,
    SearchIcon,
} from "@patternfly/react-icons";
import { Table, Th, Tr, Td, Thead, Tbody } from "@patternfly/react-table";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

type PrincipalType = "NATURAL_PERSON" | "CORPORATION" | "PARTNERSHIP" | "TRUST" | "GOVERNMENT_AGENCY" | "NON_PROFIT";

interface Principal {
    id: string;
    name: string;
    type: string;
    jurisdiction: string;
    active: boolean;
    createdAt: string;
    suspendedAt?: string;
    suspensionReason?: string;
}

const principalTypeLabels: Record<string, { label: string; color: "blue" | "cyan" | "green" | "orange" | "purple" }> = {
    "natural-person": { label: "Natural Person", color: "blue" },
    "corporation": { label: "Corporation", color: "green" },
    "partnership": { label: "Partnership", color: "cyan" },
    "trust": { label: "Trust", color: "purple" },
    "government-agency": { label: "Government Agency", color: "orange" },
    "non-profit": { label: "Non-Profit", color: "green" },
};

export function PrincipalsList() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [principals, setPrincipals] = useState<Principal[]>([]);
    const [openMenuId, setOpenMenuId] = useState<string | null>(null);

    useFetch(
        async () => {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals`, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
            });
            if (!response.ok) {
                throw new Error("Failed to fetch principals");
            }
            return response.json();
        },
        (result) => setPrincipals(result),
        [realm, adminClient]
    );

    const handleDelete = async (id: string) => {
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals/${id}`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${token}`
                },
            });
            if (!response.ok) {
                throw new Error("Failed to delete principal");
            }
            setPrincipals(principals.filter(p => p.id !== id));
            addAlert("Principal deleted successfully");
        } catch (error) {
            addError("Failed to delete principal", error);
        }
    };

    if (principals === undefined) {
        return <PassportSpinner />;
    }

    if (principals.length === 0) {
        return (
            <PageSection>
                <EmptyState>
                    <EmptyStateHeader
                        titleText="No principals"
                        headingLevel="h2"
                        icon={<EmptyStateIcon icon={BuildingIcon} />}
                    />
                    <EmptyStateBody>
                        Principals represent legal entities that can delegate authority to agents.
                    </EmptyStateBody>
                    <EmptyStateFooter>
                        <EmptyStateActions>
                            <Button
                                variant="primary"
                                component={(props: any) => <Link {...props} to={`/${realm}/agency/principals/new`} />}
                            >
                                Create Principal
                            </Button>
                        </EmptyStateActions>
                    </EmptyStateFooter>
                </EmptyState>
            </PageSection>
        );
    }

    return (
        <PageSection>
            <Toolbar>
                <ToolbarContent>
                    <ToolbarItem>
                        <Title headingLevel="h1" size="xl">
                            <BuildingIcon /> Principals
                        </Title>
                    </ToolbarItem>
                    <ToolbarItem align={{ default: "alignRight" }}>
                        <Button
                            variant="primary"
                            component={(props: any) => <Link {...props} to={`/${realm}/agency/principals/new`} />}
                        >
                            Create Principal
                        </Button>
                    </ToolbarItem>
                </ToolbarContent>
            </Toolbar>
            <Table aria-label="Principals list">
                <Thead>
                    <Tr>
                        <Th>Name</Th>
                        <Th>Type</Th>
                        <Th>Jurisdiction</Th>
                        <Th>Status</Th>
                        <Th>Created</Th>
                        <Th></Th>
                    </Tr>
                </Thead>
                <Tbody>
                    {principals.map((principal) => {
                        const typeInfo = principalTypeLabels[principal.type] || { label: principal.type, color: "grey" as const };
                        return (
                            <Tr key={principal.id}>
                                <Td>
                                    <Link to={`/${realm}/agency/principals/${principal.id}`}>
                                        {principal.name}
                                    </Link>
                                </Td>
                                <Td>
                                    <Label color={typeInfo.color}>
                                        {typeInfo.label}
                                    </Label>
                                </Td>
                                <Td>{principal.jurisdiction || "—"}</Td>
                                <Td>
                                    <Label color={principal.active ? "green" : "red"}>
                                        {principal.active ? "Active" : "Suspended"}
                                    </Label>
                                </Td>
                                <Td>{new Date(principal.createdAt).toLocaleDateString()}</Td>
                                <Td isActionCell>
                                    <Dropdown
                                        isOpen={openMenuId === principal.id}
                                        onOpenChange={(isOpen) => setOpenMenuId(isOpen ? principal.id : null)}
                                        toggle={(toggleRef) => (
                                            <MenuToggle
                                                ref={toggleRef}
                                                variant="plain"
                                                onClick={() => setOpenMenuId(openMenuId === principal.id ? null : principal.id)}
                                                isExpanded={openMenuId === principal.id}
                                            >
                                                <EllipsisVIcon />
                                            </MenuToggle>
                                        )}
                                    >
                                        <DropdownList>
                                            <DropdownItem
                                                key="view"
                                                onClick={() => navigate(`/${realm}/agency/principals/${principal.id}`)}
                                            >
                                                View Details
                                            </DropdownItem>
                                            <DropdownItem
                                                key="delete"
                                                onClick={() => handleDelete(principal.id)}
                                                isDanger
                                            >
                                                Delete
                                            </DropdownItem>
                                        </DropdownList>
                                    </Dropdown>
                                </Td>
                            </Tr>
                        );
                    })}
                </Tbody>
            </Table>
        </PageSection>
    );
}

export default PrincipalsList;
