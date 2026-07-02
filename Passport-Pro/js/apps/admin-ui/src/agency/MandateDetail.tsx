import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { PassportSpinner, useFetch, useAlerts } from "@passport/passport-ui-shared";
import {
    Button,
    Card,
    CardBody,
    CardTitle,
    CodeBlock,
    CodeBlockCode,
    DescriptionList,
    DescriptionListDescription,
    DescriptionListGroup,
    DescriptionListTerm,
    Label,
    PageSection,
    Title,
} from "@patternfly/react-core";
import { KeyIcon } from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface Mandate {
    id: string;
    name?: string;
    kind?: string;
    principalName?: string;
    grantorPrincipalId?: string;
    delegateId?: string;
    delegateName?: string;
    status?: string;
    scope?: string;
    modelScope?: string;
    resourceScope?: string;
    harnessScope?: string;
    metadata?: string;
    revocable?: boolean;
    validFrom?: string;
    validUntil?: string;
    createdAt?: string;
}

const prettyJson = (value?: string): string => {
    if (!value) {
        return "—";
    }
    try {
        return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
        return value;
    }
};

export function MandateDetail() {
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const { mandateId } = useParams<{ mandateId: string }>();

    const [mandate, setMandate] = useState<Mandate | undefined>();

    useFetch(
        async () => {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/mandates/${mandateId}`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error("Failed to fetch mandate");
            }
            return response.json();
        },
        (result) => setMandate(result),
        [realm, adminClient, mandateId],
    );

    const handleRevoke = async () => {
        if (!mandate) {
            return;
        }
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
            addAlert("Mandate revoked successfully");
            setMandate({ ...mandate, status: "revoked" });
        } catch (error) {
            addError("Failed to revoke mandate", error);
        }
    };

    if (!mandate) {
        return <PassportSpinner />;
    }

    return (
        <PageSection>
            <Title headingLevel="h1" size="xl" className="pf-v5-u-mb-lg">
                <KeyIcon /> {mandate.name || mandate.id}
            </Title>
            <Card>
                <CardTitle>Mandate</CardTitle>
                <CardBody>
                    <DescriptionList isHorizontal>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Name</DescriptionListTerm>
                            <DescriptionListDescription>{mandate.name || "—"}</DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Kind</DescriptionListTerm>
                            <DescriptionListDescription>
                                {mandate.kind ? <Label color="blue">{mandate.kind}</Label> : "—"}
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Grantor Principal</DescriptionListTerm>
                            <DescriptionListDescription>
                                {mandate.principalName || mandate.grantorPrincipalId || "—"}
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Grantee Delegate</DescriptionListTerm>
                            <DescriptionListDescription>
                                {mandate.delegateName || mandate.delegateId || "—"}
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Status</DescriptionListTerm>
                            <DescriptionListDescription>
                                <Label
                                    color={
                                        mandate.status === "active"
                                            ? "green"
                                            : mandate.status === "revoked"
                                              ? "red"
                                              : "orange"
                                    }
                                >
                                    {mandate.status || "unknown"}
                                </Label>
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Capability Scope</DescriptionListTerm>
                            <DescriptionListDescription>{mandate.scope || "—"}</DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Revocable</DescriptionListTerm>
                            <DescriptionListDescription>
                                {mandate.revocable === false ? "No" : "Yes"}
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Expires</DescriptionListTerm>
                            <DescriptionListDescription>
                                {mandate.validUntil
                                    ? new Date(mandate.validUntil).toLocaleString()
                                    : "Never"}
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Created</DescriptionListTerm>
                            <DescriptionListDescription>
                                {mandate.createdAt ? new Date(mandate.createdAt).toLocaleString() : "—"}
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                    </DescriptionList>
                </CardBody>
            </Card>

            <Card className="pf-v5-u-mt-md">
                <CardTitle>Scope Envelopes</CardTitle>
                <CardBody>
                    <DescriptionList>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Model Scope</DescriptionListTerm>
                            <DescriptionListDescription>
                                <CodeBlock>
                                    <CodeBlockCode>{prettyJson(mandate.modelScope)}</CodeBlockCode>
                                </CodeBlock>
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Resource Scope</DescriptionListTerm>
                            <DescriptionListDescription>
                                <CodeBlock>
                                    <CodeBlockCode>{prettyJson(mandate.resourceScope)}</CodeBlockCode>
                                </CodeBlock>
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Harness Scope</DescriptionListTerm>
                            <DescriptionListDescription>
                                <CodeBlock>
                                    <CodeBlockCode>{prettyJson(mandate.harnessScope)}</CodeBlockCode>
                                </CodeBlock>
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                            <DescriptionListTerm>Metadata</DescriptionListTerm>
                            <DescriptionListDescription>
                                <CodeBlock>
                                    <CodeBlockCode>{prettyJson(mandate.metadata)}</CodeBlockCode>
                                </CodeBlock>
                            </DescriptionListDescription>
                        </DescriptionListGroup>
                    </DescriptionList>
                </CardBody>
            </Card>

            <div className="pf-v5-u-mt-md">
                <Button
                    variant="danger"
                    onClick={handleRevoke}
                    isDisabled={mandate.revocable === false || mandate.status === "revoked"}
                >
                    Revoke Mandate
                </Button>{" "}
                <Button
                    variant="link"
                    component={(props: any) => <Link {...props} to={`/${realm}/agency/mandates`} />}
                >
                    Back to Mandates
                </Button>
            </div>
        </PageSection>
    );
}

export default MandateDetail;
