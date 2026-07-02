import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { PassportSpinner, useFetch, useAlerts } from "@passport/passport-ui-shared";
import {
    Button,
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
import { EllipsisVIcon, KeyIcon } from "@patternfly/react-icons";
import { Table, Th, Tr, Td, Thead, Tbody } from "@patternfly/react-table";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface Mandate {
    id: string;
    name?: string;
    kind?: string;
    grantorPrincipalId?: string;
    principalName?: string;
    delegateId?: string;
    delegateName?: string;
    status?: string;
    validUntil?: string;
    revocable?: boolean;
    createdAt?: string;
}

const statusColor = (status?: string): "green" | "red" | "orange" | "grey" => {
    switch (status) {
        case "active":
            return "green";
        case "revoked":
            return "red";
        case "expired":
            return "orange";
        default:
            return "grey";
    }
};

export function MandatesList() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [mandates, setMandates] = useState<Mandate[] | undefined>();
    const [openMenuId, setOpenMenuId] = useState<string | null>(null);

    useFetch(
        async () => {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/mandates`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error("Failed to fetch mandates");
            }
            return response.json();
        },
        (result) => setMandates(result),
        [realm, adminClient],
    );

    const handleRevoke = async (mandate: Mandate) => {
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(
                `/admin/realms/${realm}/agency/mandates/${mandate.id}?reason=${encodeURIComponent("Administrative revocation")}`,
                {
                    method: "DELETE",
                    headers: { Authorization: `Bearer ${token}` },
                },
            );
            if (!response.ok) {
                throw new Error("Failed to revoke mandate");
            }
            setMandates((mandates ?? []).map((m) => (m.id === mandate.id ? { ...m, status: "revoked" } : m)));
            addAlert("Mandate revoked successfully");
        } catch (error) {
            addError("Failed to revoke mandate", error);
        }
    };

    if (mandates === undefined) {
        return <PassportSpinner />;
    }

    if (mandates.length === 0) {
        return (
            <PageSection>
                <EmptyState>
                    <EmptyStateHeader
                        titleText="No mandates"
                        headingLevel="h2"
                        icon={<EmptyStateIcon icon={KeyIcon} />}
                    />
                    <EmptyStateBody>
                        A mandate is a scoped, revocable, time-boxed authority grant:
                        Principal → Delegate → Mandate → Agent Passport.
                    </EmptyStateBody>
                    <EmptyStateFooter>
                        <EmptyStateActions>
                            <Button
                                variant="primary"
                                component={(props: any) => (
                                    <Link {...props} to={`/${realm}/agency/mandates/new`} />
                                )}
                            >
                                {t("createMandate")}
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
                            <KeyIcon /> {t("mandates")}
                        </Title>
                    </ToolbarItem>
                    <ToolbarItem align={{ default: "alignRight" }}>
                        <Button
                            variant="primary"
                            component={(props: any) => (
                                <Link {...props} to={`/${realm}/agency/mandates/new`} />
                            )}
                        >
                            {t("createMandate")}
                        </Button>
                    </ToolbarItem>
                </ToolbarContent>
            </Toolbar>
            <Table aria-label="Mandates list">
                <Thead>
                    <Tr>
                        <Th>Name</Th>
                        <Th>Kind</Th>
                        <Th>Grantor Principal</Th>
                        <Th>Grantee Delegate</Th>
                        <Th>Status</Th>
                        <Th>Expires</Th>
                        <Th>Revocable</Th>
                        <Th>Created</Th>
                        <Th></Th>
                    </Tr>
                </Thead>
                <Tbody>
                    {mandates.map((mandate) => (
                        <Tr key={mandate.id}>
                            <Td>
                                <Link to={`/${realm}/agency/mandates/${mandate.id}`}>
                                    {mandate.name || mandate.id}
                                </Link>
                            </Td>
                            <Td>{mandate.kind ? <Label color="blue">{mandate.kind}</Label> : "—"}</Td>
                            <Td>{mandate.principalName || "—"}</Td>
                            <Td>{mandate.delegateName || mandate.delegateId || "—"}</Td>
                            <Td>
                                <Label color={statusColor(mandate.status)}>
                                    {mandate.status || "unknown"}
                                </Label>
                            </Td>
                            <Td>
                                {mandate.validUntil
                                    ? new Date(mandate.validUntil).toLocaleDateString()
                                    : "Never"}
                            </Td>
                            <Td>{mandate.revocable === false ? "No" : "Yes"}</Td>
                            <Td>
                                {mandate.createdAt
                                    ? new Date(mandate.createdAt).toLocaleDateString()
                                    : "—"}
                            </Td>
                            <Td isActionCell>
                                <Dropdown
                                    isOpen={openMenuId === mandate.id}
                                    onOpenChange={(isOpen) => setOpenMenuId(isOpen ? mandate.id : null)}
                                    toggle={(toggleRef) => (
                                        <MenuToggle
                                            ref={toggleRef}
                                            variant="plain"
                                            onClick={() =>
                                                setOpenMenuId(openMenuId === mandate.id ? null : mandate.id)
                                            }
                                            isExpanded={openMenuId === mandate.id}
                                        >
                                            <EllipsisVIcon />
                                        </MenuToggle>
                                    )}
                                >
                                    <DropdownList>
                                        <DropdownItem
                                            key="view"
                                            onClick={() =>
                                                navigate(`/${realm}/agency/mandates/${mandate.id}`)
                                            }
                                        >
                                            View Details
                                        </DropdownItem>
                                        <DropdownItem
                                            key="revoke"
                                            onClick={() => handleRevoke(mandate)}
                                            isDanger
                                            isDisabled={
                                                mandate.revocable === false || mandate.status === "revoked"
                                            }
                                        >
                                            Revoke
                                        </DropdownItem>
                                    </DropdownList>
                                </Dropdown>
                            </Td>
                        </Tr>
                    ))}
                </Tbody>
            </Table>
        </PageSection>
    );
}

export default MandatesList;
