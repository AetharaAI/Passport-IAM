import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";
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
    Switch,
    Title,
} from "@patternfly/react-core";
import {
    BuildingIcon,
    CubesIcon,
    KeyIcon,
    RobotIcon,
    ShieldAltIcon,
    UsersIcon,
} from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface AgencyConfig {
    enabled: boolean;
    defaultJurisdiction: string;
    complianceMode: string;
    mandatesRequired: boolean;
    defaultMandateValidityDays: number;
    qualificationsEnforced: boolean;
    auditLevel: string;
    agentPassportsEnabled: boolean;
    maxPassportsPerPrincipal: number;
    principalCount?: number;
    delegateCount?: number;
    mandateCount?: number;
    passportCount?: number;
}

/**
 * Agency Dashboard - Main overview for Passport-Pro Agency/LBAC features
 */
export function AgencyDashboard() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();

    const [config, setConfig] = useState<AgencyConfig | undefined>();

    useFetch(
        async () => {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/config`, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
            });
            if (!response.ok) {
                throw new Error("Failed to fetch agency config");
            }
            return response.json();
        },
        (result) => setConfig(result),
        [realm, adminClient]
    );

    const toggleAgency = async (enabled: boolean) => {
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/config`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ enabled }),
            });
            if (!response.ok) {
                throw new Error("Failed to update agency config");
            }
            setConfig({ ...config!, enabled });
            addAlert("Agency " + (enabled ? "enabled" : "disabled") + " successfully");
        } catch (error) {
            addError("Failed to update agency config", error);
        }
    };

    if (!config) {
        return <PassportSpinner />;
    }

    const stats = [
        {
            icon: <BuildingIcon />,
            label: "Principals",
            value: config.principalCount ?? 0,
            link: `/${realm}/agency/principals`,
            color: "blue",
        },
        {
            icon: <UsersIcon />,
            label: "Active Delegates",
            value: config.delegateCount ?? 0,
            link: `/${realm}/agency/delegates`,
            color: "green",
        },
        {
            icon: <KeyIcon />,
            label: "Active Mandates",
            value: config.mandateCount ?? 0,
            link: `/${realm}/agency/mandates`,
            color: "purple",
        },
        {
            icon: <RobotIcon />,
            label: "Agent Passports",
            value: config.passportCount ?? 0,
            link: `/${realm}/agency/passports`,
            color: "orange",
        },
    ];

    return (
        <PageSection className="agency-dashboard">
            <Flex direction={{ default: "column" }} spaceItems={{ default: "spaceItemsLg" }}>
                {/* Header */}
                <FlexItem>
                    <Flex justifyContent={{ default: "justifyContentSpaceBetween" }} alignItems={{ default: "alignItemsCenter" }}>
                        <FlexItem>
                            <Title headingLevel="h1" size="xl">
                                <ShieldAltIcon /> Agency / LBAC
                            </Title>
                            <p className="agency-subtitle">
                                Legal-Based Access Control for delegated authorities and AI agent passports
                            </p>
                        </FlexItem>
                        <FlexItem>
                            <Switch
                                id="agency-enabled"
                                label="Agency Enabled"
                                labelOff="Agency Disabled"
                                isChecked={config.enabled}
                                onChange={(_, checked) => toggleAgency(checked)}
                            />
                        </FlexItem>
                    </Flex>
                </FlexItem>

                {!config.enabled && (
                    <FlexItem>
                        <Card className="agency-disabled-notice">
                            <CardBody>
                                <Flex alignItems={{ default: "alignItemsCenter" }} spaceItems={{ default: "spaceItemsMd" }}>
                                    <FlexItem>
                                        <CubesIcon className="pf-v5-u-font-size-lg" />
                                    </FlexItem>
                                    <FlexItem>
                                        <strong>Agency features are disabled for this realm.</strong>
                                        <p>Enable Agency to manage principals, delegates, mandates, and agent passports.</p>
                                    </FlexItem>
                                </Flex>
                            </CardBody>
                        </Card>
                    </FlexItem>
                )}

                {config.enabled && (
                    <>
                        {/* Stats Grid */}
                        <FlexItem>
                            <Grid hasGutter>
                                {stats.map((stat) => (
                                    <GridItem key={stat.label} sm={12} md={6} lg={3}>
                                        <Card isCompact className={`agency-stat-card agency-stat-${stat.color}`}>
                                            <CardBody>
                                                <Link to={stat.link} className="agency-stat-link">
                                                    <Flex alignItems={{ default: "alignItemsCenter" }} spaceItems={{ default: "spaceItemsMd" }}>
                                                        <FlexItem className="agency-stat-icon">
                                                            {stat.icon}
                                                        </FlexItem>
                                                        <FlexItem>
                                                            <div className="agency-stat-value">{stat.value}</div>
                                                            <div className="agency-stat-label">{stat.label}</div>
                                                        </FlexItem>
                                                    </Flex>
                                                </Link>
                                            </CardBody>
                                        </Card>
                                    </GridItem>
                                ))}
                            </Grid>
                        </FlexItem>

                        <Divider />

                        {/* Configuration Overview */}
                        <FlexItem>
                            <Grid hasGutter>
                                <GridItem md={6}>
                                    <Card>
                                        <CardTitle>Configuration</CardTitle>
                                        <CardBody>
                                            <DescriptionList isHorizontal isCompact>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Default Jurisdiction</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        {config.defaultJurisdiction || "Not set"}
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Compliance Mode</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <Label color={config.complianceMode === "NONE" ? "grey" : "blue"}>
                                                            {config.complianceMode}
                                                        </Label>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Mandates Required</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <Label color={config.mandatesRequired ? "green" : "grey"}>
                                                            {config.mandatesRequired ? "Yes" : "No"}
                                                        </Label>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Audit Level</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <Label>{config.auditLevel}</Label>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                            </DescriptionList>
                                        </CardBody>
                                    </Card>
                                </GridItem>
                                <GridItem md={6}>
                                    <Card>
                                        <CardTitle>Agent Passports</CardTitle>
                                        <CardBody>
                                            <DescriptionList isHorizontal isCompact>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Status</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        <Label color={config.agentPassportsEnabled ? "green" : "grey"}>
                                                            {config.agentPassportsEnabled ? "Enabled" : "Disabled"}
                                                        </Label>
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Max per Principal</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        {config.maxPassportsPerPrincipal}
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                                <DescriptionListGroup>
                                                    <DescriptionListTerm>Default Validity</DescriptionListTerm>
                                                    <DescriptionListDescription>
                                                        {config.defaultMandateValidityDays} days
                                                    </DescriptionListDescription>
                                                </DescriptionListGroup>
                                            </DescriptionList>
                                            <Divider className="pf-v5-u-my-md" />
                                            <Button
                                                variant="secondary"
                                                component={(props: any) => <Link {...props} to={`/${realm}/agency/config`} />}
                                            >
                                                Configure Agency
                                            </Button>
                                        </CardBody>
                                    </Card>
                                </GridItem>
                            </Grid>
                        </FlexItem>

                        {/* Quick Actions */}
                        <FlexItem>
                            <Card>
                                <CardTitle>Quick Actions</CardTitle>
                                <CardBody>
                                    <Flex spaceItems={{ default: "spaceItemsMd" }}>
                                        <FlexItem>
                                            <Button
                                                variant="primary"
                                                icon={<BuildingIcon />}
                                                component={(props: any) => <Link {...props} to={`/${realm}/agency/principals/new`} />}
                                            >
                                                Create Principal
                                            </Button>
                                        </FlexItem>
                                        <FlexItem>
                                            <Button
                                                variant="secondary"
                                                icon={<UsersIcon />}
                                                component={(props: any) => <Link {...props} to={`/${realm}/agency/delegates/new`} />}
                                            >
                                                Create Delegate
                                            </Button>
                                        </FlexItem>
                                        {config.agentPassportsEnabled && (
                                            <FlexItem>
                                                <Button
                                                    variant="tertiary"
                                                    icon={<RobotIcon />}
                                                    component={(props: any) => <Link {...props} to={`/${realm}/agency/passports/mint`} />}
                                                >
                                                    Mint Agent Passport
                                                </Button>
                                            </FlexItem>
                                        )}
                                    </Flex>
                                </CardBody>
                            </Card>
                        </FlexItem>
                    </>
                )}
            </Flex>
        </PageSection>
    );
}

export default AgencyDashboard;
