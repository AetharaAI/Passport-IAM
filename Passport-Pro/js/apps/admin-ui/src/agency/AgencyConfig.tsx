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
    Title,
} from "@patternfly/react-core";
import { CogIcon } from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

const complianceModes = [
    { value: "NONE", label: "None (Internal)" },
    { value: "LOI_1", label: "LOI-1 (Self-Attested)" },
    { value: "LOI_2", label: "LOI-2 (Cryptographic)" },
    { value: "LOI_3", label: "LOI-3 (Government/TPM)" },
];

const auditLevels = [
    { value: "NONE", label: "None" },
    { value: "BASIC", label: "Basic" },
    { value: "STANDARD", label: "Standard" },
    { value: "STRICT", label: "Strict (All Actions Signed)" },
];

export function AgencyConfig() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [complianceMode, setComplianceMode] = useState("NONE");
    const [auditLevel, setAuditLevel] = useState("STANDARD");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

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
        (result) => {
            setComplianceMode(result.complianceMode || "NONE");
            setAuditLevel(result.auditLevel || "STANDARD");
            setIsLoading(false);
        },
        [realm, adminClient]
    );

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/config`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    complianceMode,
                    auditLevel,
                }),
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || "Failed to update agency configuration");
            }

            addAlert("Agency configuration updated successfully");
            navigate(`/${realm}/agency`);
        } catch (error) {
            addError("Failed to update agency configuration", error);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isLoading) {
        return <PassportSpinner />;
    }

    return (
        <PageSection>
            <Title headingLevel="h1" size="xl" className="pf-v5-u-mb-lg">
                <CogIcon /> Agency Configuration
            </Title>
            <Form isHorizontal className="agency-form">
                <FormGroup label="Compliance Mode" fieldId="complianceMode">
                    <FormSelect
                        id="complianceMode"
                        value={complianceMode}
                        onChange={(_, value) => setComplianceMode(value)}
                    >
                        {complianceModes.map((option) => (
                            <FormSelectOption
                                key={option.value}
                                value={option.value}
                                label={option.label}
                            />
                        ))}
                    </FormSelect>
                </FormGroup>
                <FormGroup label="Audit Level" fieldId="auditLevel">
                    <FormSelect
                        id="auditLevel"
                        value={auditLevel}
                        onChange={(_, value) => setAuditLevel(value)}
                    >
                        {auditLevels.map((option) => (
                            <FormSelectOption
                                key={option.value}
                                value={option.value}
                                label={option.label}
                            />
                        ))}
                    </FormSelect>
                </FormGroup>
                <ActionGroup>
                    <Button
                        variant="primary"
                        onClick={handleSubmit}
                        isLoading={isSubmitting}
                        isDisabled={isSubmitting}
                    >
                        Save Configuration
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

export default AgencyConfig;
