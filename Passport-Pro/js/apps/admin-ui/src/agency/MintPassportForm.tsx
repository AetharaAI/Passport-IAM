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
    TextArea,
    TextInput,
    Title,
} from "@patternfly/react-core";
import { RobotIcon } from "@patternfly/react-icons";

import { useRealm } from "../context/realm-context/RealmContext";
import { useAdminClient } from "../admin-client";

import "./agency.css";

interface Principal {
    id: string;
    name: string;
}

const tiers = [
    { value: "tpm", label: "Tier 1 (Hardware TPM)" },
    { value: "dns", label: "Tier 2.5 (DNS-Anchored)" },
    { value: "software", label: "Tier 3 (Software HSM)" },
    { value: "dev", label: "Tier 4 (Development Key)" },
];

export function MintPassportForm() {
    const { t } = useTranslation();
    const { realm } = useRealm();
    const { adminClient } = useAdminClient();
    const { addAlert, addError } = useAlerts();
    const navigate = useNavigate();

    const [principals, setPrincipals] = useState<Principal[]>([]);
    const [agentName, setAgentName] = useState("");
    const [principalId, setPrincipalId] = useState("");
    const [tier, setTier] = useState("software");
    const [publicKeyPem, setPublicKeyPem] = useState("");
    const [mandate, setMandate] = useState("{}");
    const [machinePassportId, setMachinePassportId] = useState("");
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
        if (!agentName.trim() || !principalId || !publicKeyPem.trim() || !mandate.trim()) {
            addError("Validation Error", new Error("Agent Name, Principal, Public Key, and Mandate are required"));
            return;
        }

        try {
            JSON.parse(mandate);
        } catch (e) {
            addError("Validation Error", new Error("Mandate must be valid JSON"));
            return;
        }

        setIsSubmitting(true);
        try {
            const token = await adminClient.getAccessToken();
            const response = await fetch(`/admin/realms/${realm}/agency/passports/mint`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    agentName: agentName.trim(),
                    principalId,
                    tier,
                    publicKeyPem: publicKeyPem.trim(),
                    mandate: JSON.parse(mandate),
                    machinePassportId: machinePassportId.trim() || undefined,
                }),
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || "Failed to mint passport");
            }

            addAlert("Agent Passport minted successfully");
            navigate(`/${realm}/agency`);
        } catch (error) {
            addError("Failed to mint passport", error);
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
                <RobotIcon /> Mint Agent Passport
            </Title>
            <Form isHorizontal className="agency-form">
                <FormGroup label="Agent Name" isRequired fieldId="agentName">
                    <TextInput
                        id="agentName"
                        value={agentName}
                        onChange={(_, value) => setAgentName(value)}
                        isRequired
                        placeholder="e.g., omni-agent-001"
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
                <FormGroup label="Trust Tier" isRequired fieldId="tier">
                    <FormSelect
                        id="tier"
                        value={tier}
                        onChange={(_, value) => setTier(value)}
                    >
                        {tiers.map((option) => (
                            <FormSelectOption
                                key={option.value}
                                value={option.value}
                                label={option.label}
                            />
                        ))}
                    </FormSelect>
                </FormGroup>
                <FormGroup label="Public Key (PEM)" isRequired fieldId="publicKeyPem">
                    <TextArea
                        id="publicKeyPem"
                        value={publicKeyPem}
                        onChange={(_, value) => setPublicKeyPem(value)}
                        isRequired
                        placeholder="Paste ECDSA P-256 public key in PEM format"
                        autoResize
                    />
                </FormGroup>
                <FormGroup label="Mandate (JSON)" isRequired fieldId="mandate">
                    <TextArea
                        id="mandate"
                        value={mandate}
                        onChange={(_, value) => setMandate(value)}
                        isRequired
                        placeholder='{"scope": ["read", "write"], "constraints": {}}'
                        autoResize
                    />
                </FormGroup>
                <FormGroup label="Machine Passport ID" fieldId="machinePassportId">
                    <TextInput
                        id="machinePassportId"
                        value={machinePassportId}
                        onChange={(_, value) => setMachinePassportId(value)}
                        placeholder="Optional: Machine identity linkage"
                    />
                </FormGroup>
                <ActionGroup>
                    <Button
                        variant="primary"
                        onClick={handleSubmit}
                        isLoading={isSubmitting}
                        isDisabled={isSubmitting || !agentName.trim() || !principalId || !publicKeyPem.trim()}
                    >
                        Mint Passport
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

export default MintPassportForm;
