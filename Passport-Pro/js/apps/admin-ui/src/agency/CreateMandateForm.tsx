import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { PassportSpinner, useFetch, useAlerts } from "@passport/passport-ui-shared";
import {
    ActionGroup,
    Button,
    Checkbox,
    Form,
    FormGroup,
    FormSelect,
    FormSelectOption,
    PageSection,
    TextArea,
    TextInput,
    Title,
} from "@patternfly/react-core";
import { KeyIcon } from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface Principal {
    id: string;
    name: string;
}

interface Delegate {
    id: string;
    agentUsername?: string;
}

// Mirror of the backend MANDATE_KINDS list. break_glass_reserved is reserved
// only — no break-glass behaviour is implemented.
const kinds = [
    "operator",
    "support",
    "integration",
    "model_route",
    "benchmark",
    "collab",
    "break_glass_reserved",
];

const isValidJson = (value: string): boolean => {
    if (!value.trim()) {
        return true;
    }
    try {
        JSON.parse(value);
        return true;
    } catch {
        return false;
    }
};

export function CreateMandateForm() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [principals, setPrincipals] = useState<Principal[]>([]);
    const [delegates, setDelegates] = useState<Delegate[]>([]);

    const [name, setName] = useState("");
    const [grantorPrincipalId, setGrantorPrincipalId] = useState("");
    const [granteeDelegateId, setGranteeDelegateId] = useState("");
    const [kind, setKind] = useState("operator");
    const [scope, setScope] = useState("");
    const [modelScope, setModelScope] = useState("");
    const [resourceScope, setResourceScope] = useState("");
    const [harnessScope, setHarnessScope] = useState("");
    const [expiryDays, setExpiryDays] = useState("30");
    const [revocable, setRevocable] = useState(true);
    const [metadata, setMetadata] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    useFetch(
        async () => {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error("Failed to fetch principals");
            }
            return response.json();
        },
        (result) => {
            setPrincipals(result);
            if (result.length > 0) {
                setGrantorPrincipalId(result[0].id);
            }
        },
        [realm, adminClient],
    );

    // Grantee delegates are scoped to the selected grantor principal.
    useFetch(
        async () => {
            if (!grantorPrincipalId) {
                return [];
            }
            const token = await adminClient.getAccessToken();
            const response = await fetch(
                `/admin/realms/${realm}/agency/principals/${grantorPrincipalId}/delegates`,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                },
            );
            if (!response.ok) {
                throw new Error("Failed to fetch delegates");
            }
            return response.json();
        },
        (result: Delegate[]) => {
            setDelegates(result);
            setGranteeDelegateId(result.length > 0 ? result[0].id : "");
        },
        [realm, adminClient, grantorPrincipalId],
    );

    const handleSubmit = async () => {
        if (!name.trim()) {
            addError("Validation Error", new Error("Mandate Name is required"));
            return;
        }
        if (!granteeDelegateId) {
            addError(
                "Validation Error",
                new Error("A grantee delegate is required — create a delegate under this principal first"),
            );
            return;
        }
        for (const [label, value] of [
            ["Model Scope", modelScope],
            ["Resource Scope", resourceScope],
            ["Harness Scope", harnessScope],
            ["Metadata", metadata],
        ] as const) {
            if (!isValidJson(value)) {
                addError("Validation Error", new Error(`${label} must be valid JSON`));
                return;
            }
        }

        setIsSubmitting(true);
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/mandates`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    name: name.trim(),
                    kind,
                    grantorPrincipalId: grantorPrincipalId || undefined,
                    granteeDelegateId,
                    scope: scope.trim() || undefined,
                    modelScope: modelScope.trim() || undefined,
                    resourceScope: resourceScope.trim() || undefined,
                    harnessScope: harnessScope.trim() || undefined,
                    expiryDays: parseInt(expiryDays) || undefined,
                    revocable,
                    metadata: metadata.trim() || undefined,
                }),
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({}));
                throw new Error(error.error || "Failed to create mandate");
            }

            addAlert("Mandate created successfully");
            navigate(`/${realm}/agency/mandates`);
        } catch (error) {
            addError("Failed to create mandate", error);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (principals.length === 0 && !isSubmitting) {
        return <PassportSpinner />;
    }

    return (
        <PageSection>
            <Title headingLevel="h1" size="xl" className="pf-v5-u-mb-lg">
                <KeyIcon /> {t("createMandate")}
            </Title>
            <Form isHorizontal className="agency-form">
                <FormGroup label="Mandate Name" isRequired fieldId="name">
                    <TextInput
                        id="name"
                        value={name}
                        onChange={(_, value) => setName(value)}
                        isRequired
                        placeholder="e.g., faraday-operator"
                    />
                </FormGroup>
                <FormGroup label="Grantor Principal" isRequired fieldId="grantorPrincipalId">
                    <FormSelect
                        id="grantorPrincipalId"
                        value={grantorPrincipalId}
                        onChange={(_, value) => setGrantorPrincipalId(value)}
                    >
                        {principals.map((principal) => (
                            <FormSelectOption
                                key={principal.id}
                                value={principal.id}
                                label={principal.name}
                            />
                        ))}
                    </FormSelect>
                </FormGroup>
                <FormGroup label="Grantee Delegate" isRequired fieldId="granteeDelegateId">
                    <FormSelect
                        id="granteeDelegateId"
                        value={granteeDelegateId}
                        onChange={(_, value) => setGranteeDelegateId(value)}
                    >
                        {delegates.length === 0 ? (
                            <FormSelectOption value="" label="No delegates for this principal" isDisabled />
                        ) : (
                            delegates.map((delegate) => (
                                <FormSelectOption
                                    key={delegate.id}
                                    value={delegate.id}
                                    label={delegate.agentUsername || delegate.id}
                                />
                            ))
                        )}
                    </FormSelect>
                </FormGroup>
                <FormGroup label="Kind" isRequired fieldId="kind">
                    <FormSelect id="kind" value={kind} onChange={(_, value) => setKind(value)}>
                        {kinds.map((k) => (
                            <FormSelectOption key={k} value={k} label={k} />
                        ))}
                    </FormSelect>
                </FormGroup>
                <FormGroup label="Capability Scope" fieldId="scope">
                    <TextInput
                        id="scope"
                        value={scope}
                        onChange={(_, value) => setScope(value)}
                        placeholder="e.g., operator:cli agent:route model:inference"
                    />
                </FormGroup>
                <FormGroup label="Model Scope (JSON)" fieldId="modelScope">
                    <TextArea
                        id="modelScope"
                        value={modelScope}
                        onChange={(_, value) => setModelScope(value)}
                        placeholder='{"allowed_classes":["general"],"policy":"deny-overrides"}'
                        autoResize
                    />
                </FormGroup>
                <FormGroup label="Resource Scope (JSON)" fieldId="resourceScope">
                    <TextArea
                        id="resourceScope"
                        value={resourceScope}
                        onChange={(_, value) => setResourceScope(value)}
                        placeholder='{"nodes":["anchor-0-lab"]}'
                        autoResize
                    />
                </FormGroup>
                <FormGroup label="Harness Scope (JSON)" fieldId="harnessScope">
                    <TextArea
                        id="harnessScope"
                        value={harnessScope}
                        onChange={(_, value) => setHarnessScope(value)}
                        placeholder='["faraday"] — explicit; empty means no harness authority'
                        autoResize
                    />
                </FormGroup>
                <FormGroup label="Expiry (days)" fieldId="expiryDays">
                    <TextInput
                        id="expiryDays"
                        type="number"
                        value={expiryDays}
                        onChange={(_, value) => setExpiryDays(value)}
                        placeholder="Leave blank for no expiry"
                    />
                </FormGroup>
                <FormGroup label="Revocable" fieldId="revocable">
                    <Checkbox
                        id="revocable"
                        label="This mandate can be revoked"
                        isChecked={revocable}
                        onChange={(_, checked) => setRevocable(checked)}
                    />
                </FormGroup>
                <FormGroup label="Metadata (JSON)" fieldId="metadata">
                    <TextArea
                        id="metadata"
                        value={metadata}
                        onChange={(_, value) => setMetadata(value)}
                        placeholder='{"note":"..."}'
                        autoResize
                    />
                </FormGroup>
                <ActionGroup>
                    <Button
                        variant="primary"
                        onClick={handleSubmit}
                        isLoading={isSubmitting}
                        isDisabled={isSubmitting || !name.trim() || !granteeDelegateId}
                    >
                        {t("createMandate")}
                    </Button>
                    <Button variant="link" onClick={() => navigate(`/${realm}/agency/mandates`)}>
                        Cancel
                    </Button>
                </ActionGroup>
            </Form>
        </PageSection>
    );
}

export default CreateMandateForm;
