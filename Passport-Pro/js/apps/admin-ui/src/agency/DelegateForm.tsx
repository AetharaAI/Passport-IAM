import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { PassportSpinner, useFetch, useAlerts } from "@passport/passport-ui-shared";
import {
    ActionGroup,
    Button,
    Form,
    FormGroup,
    FormSelect,
    FormSelectOption,
    PageSection,
    TextInput,
    Title,
} from "@patternfly/react-core";
import { UsersIcon } from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface Principal {
    id: string;
    name: string;
}

export function DelegateForm() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [principals, setPrincipals] = useState<Principal[]>([]);
    const [delegateName, setDelegateName] = useState("");
    const [principalId, setPrincipalId] = useState("");
    const [scope, setScope] = useState("");
    const [expiryDays, setExpiryDays] = useState("365");
    const [isSubmitting, setIsSubmitting] = useState(false);

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
        (result) => {
            setPrincipals(result);
            if (result.length > 0) {
                setPrincipalId(result[0].id);
            }
        },
        [realm, adminClient]
    );

    const handleSubmit = async () => {
        if (!delegateName.trim() || !principalId) {
            addError("Validation Error", new Error("Delegate Name and Principal are required"));
            return;
        }

        setIsSubmitting(true);
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/delegates`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    delegateName: delegateName.trim(),
                    principalId,
                    scope: scope.trim() || undefined,
                    expiryDays: parseInt(expiryDays) || 365,
                }),
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || "Failed to create delegate");
            }

            addAlert("Delegate created successfully");
            navigate(`/${realm}/agency`);
        } catch (error) {
            addError("Failed to create delegate", error);
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
                <UsersIcon /> Create Delegate
            </Title>
            <Form isHorizontal className="agency-form">
                <FormGroup label="Delegate Name" isRequired fieldId="delegateName">
                    <TextInput
                        id="delegateName"
                        value={delegateName}
                        onChange={(_, value) => setDelegateName(value)}
                        isRequired
                        placeholder="Enter delegate name"
                    />
                </FormGroup>
                <FormGroup label="Principal" isRequired fieldId="principalId">
                    <FormSelect
                        id="principalId"
                        value={principalId}
                        onChange={(_, value) => setPrincipalId(value)}
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
                <FormGroup label="Scope" fieldId="scope">
                    <TextInput
                        id="scope"
                        value={scope}
                        onChange={(_, value) => setScope(value)}
                        placeholder="e.g., read:users, write:roles"
                    />
                </FormGroup>
                <FormGroup label="Expiry Days" fieldId="expiryDays">
                    <TextInput
                        id="expiryDays"
                        type="number"
                        value={expiryDays}
                        onChange={(_, value) => setExpiryDays(value)}
                    />
                </FormGroup>
                <ActionGroup>
                    <Button
                        variant="primary"
                        onClick={handleSubmit}
                        isLoading={isSubmitting}
                        isDisabled={isSubmitting || !delegateName.trim() || !principalId}
                    >
                        Create Delegate
                    </Button>
                    <Button
                        variant="link"
                        onClick={() => navigate(`/${realm}/agency`)}
                    >
                        Cancel
                    </Button>
                </ActionGroup>
            </Form>
        </PageSection>
    );
}

export default DelegateForm;
