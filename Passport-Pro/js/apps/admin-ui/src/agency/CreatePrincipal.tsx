import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { useAlerts } from "@passport/passport-ui-shared";
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
import { BuildingIcon } from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

const principalTypes = [
    { value: "natural-person", label: "Natural Person" },
    { value: "corporation", label: "Corporation" },
    { value: "partnership", label: "Partnership" },
    { value: "trust", label: "Trust" },
    { value: "government-agency", label: "Government Agency" },
    { value: "non-profit", label: "Non-Profit" },
];

export function CreatePrincipal() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [type, setType] = useState("corporation");
    const [jurisdiction, setJurisdiction] = useState("");
    const [metadata, setMetadata] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        if (!name.trim()) {
            addError("Validation Error", new Error("Name is required"));
            return;
        }

        setIsSubmitting(true);
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/principals`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    name: name.trim(),
                    type,
                    jurisdiction: jurisdiction.trim() || undefined,
                    metadata: metadata.trim() || undefined,
                }),
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || "Failed to create principal");
            }

            const created = await response.json();
            addAlert("Principal created successfully");
            navigate(`/${realm}/agency/principals/${created.id}`);
        } catch (error) {
            addError("Failed to create principal", error);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <PageSection>
            <Title headingLevel="h1" size="xl" className="pf-v5-u-mb-lg">
                <BuildingIcon /> Create Principal
            </Title>
            <Form isHorizontal>
                <FormGroup label="Name" isRequired fieldId="name">
                    <TextInput
                        id="name"
                        value={name}
                        onChange={(_, value) => setName(value)}
                        isRequired
                        placeholder="Enter principal name"
                    />
                </FormGroup>
                <FormGroup label="Type" isRequired fieldId="type">
                    <FormSelect
                        id="type"
                        value={type}
                        onChange={(_, value) => setType(value)}
                    >
                        {principalTypes.map((option) => (
                            <FormSelectOption
                                key={option.value}
                                value={option.value}
                                label={option.label}
                            />
                        ))}
                    </FormSelect>
                </FormGroup>
                <FormGroup label="Jurisdiction" fieldId="jurisdiction">
                    <TextInput
                        id="jurisdiction"
                        value={jurisdiction}
                        onChange={(_, value) => setJurisdiction(value)}
                        placeholder="e.g., US, UK, EU"
                    />
                </FormGroup>
                <FormGroup label="Metadata (JSON)" fieldId="metadata">
                    <TextInput
                        id="metadata"
                        value={metadata}
                        onChange={(_, value) => setMetadata(value)}
                        placeholder='{"key": "value"}'
                    />
                </FormGroup>
                <ActionGroup>
                    <Button
                        variant="primary"
                        onClick={handleSubmit}
                        isLoading={isSubmitting}
                        isDisabled={isSubmitting || !name.trim()}
                    >
                        Create Principal
                    </Button>
                    <Button
                        variant="link"
                        onClick={() => navigate(`/${realm}/agency/principals`)}
                    >
                        Cancel
                    </Button>
                </ActionGroup>
            </Form>
        </PageSection>
    );
}

export default CreatePrincipal;
