import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useNavigate } from "react-router-dom";
import { PassportSpinner, useFetch, useAlerts } from "@passport/passport-ui-shared";
import {
    Button,
    Card,
    CardBody,
    CardTitle,
    DescriptionList,
    DescriptionListDescription,
    DescriptionListGroup,
    DescriptionListTerm,
    Divider,
    Flex,
    FlexItem,
    Grid,
    GridItem,
    Label,
    PageSection,
    Tab,
    Tabs,
    TabTitleText,
    Title,
} from "@patternfly/react-core";
import {
    BuildingIcon,
    CheckCircleIcon,
    ExclamationCircleIcon,
} from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface Principal {
    id: string;
    name: string;
    type: string;
    jurisdiction: string;
    metadata?: string;
    active: boolean;
    createdAt: string;
    updatedAt?: string;
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

export function PrincipalDetail() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { id } = useParams<{ id: string }>();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [principal, setPrincipal] = useState<Principal | undefined>();
    const [activeTab, setActiveTab] = useState(0);

    useFetch(
        async () => {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals/${id}`, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
            });
            if (!response.ok) {
                if (response.status === 404) {
                    return null;
                }
                throw new Error("Failed to fetch principal");
            }
            return response.json();
        },
        (result) => {
            if (result === null) {
                navigate(`/${realm}/agency/principals`);
            } else {
                setPrincipal(result);
            }
        },
        [realm, id, adminClient]
    );

    const handleSuspend = async () => {
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals/${id}/suspend`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ reason: "Administrative action" }),
            });
            if (!response.ok) {
                throw new Error("Failed to suspend principal");
            }
            setPrincipal({ ...principal!, active: false, suspendedAt: new Date().toISOString() });
            addAlert("Principal suspended");
        } catch (error) {
            addError("Failed to suspend principal", error);
        }
    };

    const handleActivate = async () => {
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals/${id}/activate`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`
                },
            });
            if (!response.ok) {
                throw new Error("Failed to activate principal");
            }
            setPrincipal({ ...principal!, active: true, suspendedAt: undefined, suspensionReason: undefined });
            addAlert("Principal activated");
        } catch (error) {
            addError("Failed to activate principal", error);
        }
    };

    const handleDelete = async () => {
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
            addAlert("Principal deleted");
            navigate(`/${realm}/agency/principals`);
        } catch (error) {
            addError("Failed to delete principal", error);
        }
    };

    if (!principal) {
        return <PassportSpinner />;
    }

    const typeInfo = principalTypeLabels[principal.type] || { label: principal.type, color: "grey" as const };

    return (
        <PageSection>
            <Flex direction={{ default: "column" }} spaceItems={{ default: "spaceItemsLg" }}>
                {/* Header */}
                <FlexItem>
                    <Flex justifyContent={{ default: "justifyContentSpaceBetween" }} alignItems={{ default: "alignItemsCenter" }}>
                        <FlexItem>
                            <Title headingLevel="h1" size="xl">
                                <BuildingIcon /> {principal.name}
                            </Title>
                            <Flex spaceItems={{ default: "spaceItemsSm" }} className="pf-v5-u-mt-sm">
                                <FlexItem>
                                    <Label color={typeInfo.color}>{typeInfo.label}</Label>
                                </FlexItem>
                                <FlexItem>
                                    <Label
                                        color={principal.active ? "green" : "red"}
                                        icon={principal.active ? <CheckCircleIcon /> : <ExclamationCircleIcon />}
                                    >
                                        {principal.active ? "Active" : "Suspended"}
                                    </Label>
                                </FlexItem>
                                {principal.jurisdiction && (
                                    <FlexItem>
                                        <Label color="grey">{principal.jurisdiction}</Label>
                                    </FlexItem>
                                )}
                            </Flex>
                        </FlexItem>
                        <FlexItem>
                            <Flex spaceItems={{ default: "spaceItemsSm" }}>
                                {principal.active ? (
                                    <Button variant="warning" onClick={handleSuspend}>
                                        Suspend
                                    </Button>
                                ) : (
                                    <Button variant="primary" onClick={handleActivate}>
                                        Activate
                                    </Button>
                                )}
                                <Button variant="danger" onClick={handleDelete}>
                                    Delete
                                </Button>
                            </Flex>
                        </FlexItem>
                    </Flex>
                </FlexItem>

                <Divider />

                {/* Tabs */}
                <FlexItem>
                    <Tabs activeKey={activeTab} onSelect={(_, key) => setActiveTab(key as number)}>
                        <Tab eventKey={0} title={<TabTitleText>Details</TabTitleText>}>
                            <Grid hasGutter className="pf-v5-u-mt-lg">
                                <GridItem md={6}>
                                    <Card>
                                        <CardTitle>Principal Information</CardTitle>
                                        <CardBody>
                                            <DescriptionList isHorizontal isCompact>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>ID</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <code>{principal.id}</code>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Name</DescriptionListTerm>
                                                    <DescriptionListDescription>{principal.name}</DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Type</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <Label color={typeInfo.color}>{typeInfo.label}</Label>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Jurisdiction</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        {principal.jurisdiction || "Not specified"}
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Created</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        {new Date(principal.createdAt).toLocaleString()}
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                {principal.updatedAt && (
                                                    <DescriptionListGroup>
                                                        <DescriptionListTerm>Updated</DescriptionListTerm>
                                                        <DescriptionListDescription>
                                                            {new Date(principal.updatedAt).toLocaleString()}
                                                        </DescriptionListDescription>
                                                    </DescriptionListGroup>
                                                )}
                                            </DescriptionList>
                                        </CardBody>
                                    </Card>
                                </GridItem>
                                <GridItem md={6}>
                                    <Card>
                                        <CardTitle>Status</CardTitle>
                                        <CardBody>
                                            <DescriptionList isHorizontal isCompact>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Status</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <Label
                                                            color={principal.active ? "green" : "red"}
                                                            icon={principal.active ? <CheckCircleIcon /> : <ExclamationCircleIcon />}
                                                        >
                                                            {principal.active ? "Active" : "Suspended"}
                                                        </Label>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                {principal.suspendedAt && (
                                                    <>
                                                        <DescriptionListGroup>
                                                            <DescriptionListTerm>Suspended At</DescriptionListTerm>
                                                            <DescriptionListDescription>
                                                                {new Date(principal.suspendedAt).toLocaleString()}
                                                            </DescriptionListDescription>
                                                        </DescriptionListGroup>
                                                        {principal.suspensionReason && (
                                                            <DescriptionListGroup>
                                                                <DescriptionListTerm>Reason</DescriptionListTerm>
                                                                <DescriptionListDescription>
                                                                    {principal.suspensionReason}
                                                                </DescriptionListDescription>
                                                            </DescriptionListGroup>
                                                        )}
                                                    </>
                                                )}
                                            </DescriptionList>
                                        </CardBody>
                                    </Card>
                                </GridItem>
                            </Grid>
                        </Tab>
                        <Tab eventKey={1} title={<TabTitleText>Delegates</TabTitleText>}>
                            <Card className="pf-v5-u-mt-lg">
                                <CardBody>
                                    <p>Delegates for this principal will be shown here.</p>
                                </CardBody>
                            </Card>
                        </Tab>
                        <Tab eventKey={2} title={<TabTitleText>Agent Passports</TabTitleText>}>
                            <Card className="pf-v5-u-mt-lg">
                                <CardBody>
                                    <p>Agent Passports for this principal will be shown here.</p>
                                </CardBody>
                            </Card>
                        </Tab>
                    </Tabs>
                </FlexItem>
            </Flex>
        </PageSection>
    );
}

export default PrincipalDetail;
