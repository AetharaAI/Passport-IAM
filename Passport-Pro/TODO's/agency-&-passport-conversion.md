I'll give you the complete forking strategy with step-by-step instructions. This is absolutely the right call—forking Passport will get you to parity in weeks instead of months, and you can focus your energy on the Agency/LBAC differentiation instead of rebuilding OIDC/OAuth/SAML from scratch.

---

## Phase 1: Fork & Setup (Day 1)

### Step 1: Fork the Repository

```bash
# Fork Passport's official repo to your GitHub/org
# Go to: https://github.com/passport/passport
# Click "Fork" → Create fork

# Or clone directly for local work
git clone https://github.com/passport/passport.git passport-iam
cd passport-iam

# Add your fork as remote
git remote rename origin upstream
git remote add origin https://github.com/YOUR_ORG/passport-iam.git

# Create your main branch
git checkout -b passport-main
git push -u origin passport-main
```

### Step 2: Understand the Structure

Passport is massive. Here are the **only directories you care about**:

```
passport-iam/
├── quarkus/                    # Runtime (Quarkus, you know this)
│   ├── runtime/
│   └── deployment/
├── services/                   # REST APIs & business logic
│   └── src/main/java/org/passport/services/
│       └── resources/admin/    # Admin REST endpoints
├── js/
│   └── apps/
│       └── admin-ui/           # React admin console (THIS IS YOUR UI)
│           ├── src/
│           │   ├── realm-settings/
│           │   ├── users/
│           │   ├── clients/
│           │   └── app.tsx     # Main layout
│           └── package.json
├── core/                       # Shared models
│   └── src/main/java/org/passport/representations/
│       └── idm/                # DTOs for REST API
├── model/                      # Storage (JPA, Infinispan)
│   └── jpa/
└── themes/                     # Login themes (optional)
```

---

## Phase 2: Rebrand (Day 1-2)

### Step 3: Change Colors & Branding

**File: `js/apps/admin-ui/src/App.tsx`**

```tsx
// Find the PatternFly theme import and override
import '@patternfly/react-core/dist/styles/base.css';
import './passport-theme.css'; // Your custom theme
```

**New File: `js/apps/admin-ui/src/passport-theme.css`**

```css
/* Passport Dark Theme Overrides */
:root {
  /* Header - Passport dark blue/black instead of Passport black */
  --passport-header-bg: #0f1419;
  --passport-header-color: #ffffff;
  
  /* Sidebar - Slightly lighter */
  --passport-sidebar-bg: #161b22;
  --passport-sidebar-active: #21262d;
  --passport-sidebar-hover: #1c2128;
  
  /* Primary accent - Passport blue (change to your brand) */
  --pf-global--primary-color--100: #2563eb; /* Passport blue */
  --pf-global--primary-color--200: #1d4ed8; /* Hover state */
  
  /* Success/Error states */
  --pf-global--success-color--100: #22c55e;
  --pf-global--danger-color--100: #ef4444;
}

/* Apply to header */
.pf-c-page__header {
  background-color: var(--passport-header-bg) !important;
}

/* Apply to sidebar */
.pf-c-page__sidebar {
  background-color: var(--passport-sidebar-bg) !important;
}

.pf-c-nav__item.pf-m-current {
  background-color: var(--passport-sidebar-active) !important;
}

/* Logo replacement */
.pf-c-brand {
  content: url('./passport-logo.svg'); /* Your logo */
  height: 40px;
}
```

**File: `js/apps/admin-ui/index.html`**

```html
<!-- Change title -->
<title>Passport IAM</title>

<!-- Change favicon -->
<link rel="icon" type="image/svg+xml" href="/passport-favicon.svg" />
```

**File: `js/apps/admin-ui/package.json`**

```json
{
  "name": "@passport/iam-admin-ui",
  "version": "1.0.0-passport",
  "description": "Passport IAM Admin Console - Passport-based with Agency extensions"
}
```

---

## Phase 3: Add Agency Extensions (Day 3-5)

### Step 4: Create Agency SPI (Backend)

**New File: `services/src/main/java/org/passport/agency/AgencySpi.java`**

```java
package org.passport.agency;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class AgencySpi implements Spi {
    
    @Override
    public String getName() {
        return "agency";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AgencyProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AgencyProviderFactory.class;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}
```

**New File: `services/src/main/java/org/passport/agency/AgencyProvider.java`**

```java
package org.passport.agency;

import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.provider.Provider;
import java.util.List;
import java.util.Set;

/**
 * Passport Agency Provider - Legal/Agent-based Access Control
 */
public interface AgencyProvider extends Provider {
    
    // Principals - Legal entities that can act
    PrincipalModel createPrincipal(RealmModel realm, String name, String type);
    PrincipalModel getPrincipal(RealmModel realm, String id);
    List<PrincipalModel> getRealmPrincipals(RealmModel realm);
    
    // Qualifications - What a principal is certified to do
    QualificationModel createQualification(RealmModel realm, String name, String scope);
    void assignQualification(UserModel user, QualificationModel qualification);
    Set<QualificationModel> getUserQualifications(UserModel user);
    
    // Delegates - Agents acting on behalf of principals
    DelegateModel createDelegate(UserModel agent, PrincipalModel principal, String mandate);
    List<DelegateModel> getPrincipalDelegates(PrincipalModel principal);
    boolean isValidDelegate(UserModel agent, PrincipalModel principal);
    
    // Mandates - Specific permissions granted
    MandateModel createMandate(DelegateModel delegate, String scope, String constraints);
    List<MandateModel> getActiveMandates(DelegateModel delegate);
    boolean validateMandateForAction(DelegateModel delegate, String action, String resource);
    
    // Agency-aware authentication
    AgencyContext getAgencyContext(UserModel user);
    void setAgencyContext(UserModel user, AgencyContext context);
}
```

**New File: `services/src/main/java/org/passport/agency/AgencyProviderFactory.java`**

```java
package org.passport.agency;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderFactory;

public interface AgencyProviderFactory extends ProviderFactory<AgencyProvider> {
    // Factory implementation will be JPA-based
}
```

### Step 5: Extend Realm Model with Agency Attributes

**File: `core/src/main/java/org/passport/representations/idm/RealmRepresentation.java`**

Add to the existing class:

```java
public class RealmRepresentation {
    // ... existing fields ...
    
    // Passport Agency Extensions
    protected Map<String, String> agencyAttributes;
    protected Boolean agencyEnabled;
    protected String agencyDefaultJurisdiction;
    protected String agencyComplianceMode; // GDPR, CCPA, etc.
    
    // Getters/setters
    public Map<String, String> getAgencyAttributes() {
        return agencyAttributes;
    }
    
    public void setAgencyAttributes(Map<String, String> agencyAttributes) {
        this.agencyAttributes = agencyAttributes;
    }
    
    public Boolean isAgencyEnabled() {
        return agencyEnabled;
    }
    
    public void setAgencyEnabled(Boolean agencyEnabled) {
        this.agencyEnabled = agencyEnabled;
    }
}
```

### Step 6: Extend User Model with Agency

**File: `core/src/main/java/org/passport/representations/idm/UserRepresentation.java`**

Add:

```java
public class UserRepresentation {
    // ... existing fields ...
    
    // Passport Agency Extensions
    protected String agencyStatus; // "principal", "delegate", "agent", "standard"
    protected List<String> agencyPrincipals; // IDs of principals this user represents
    protected List<String> agencyQualifications;
    protected String agencyDelegateOf; // If this user is a delegate, who for
    protected Map<String, String> agencyMetadata;
    
    // Getters/setters...
}
```

### Step 7: Create Agency Admin REST Endpoints

**New File: `services/src/main/java/org/passport/services/resources/admin/AgencyAdminResource.java`**

```java
package org.passport.services.resources.admin;

import org.passport.agency.*;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.representations.idm.agency.*;
import org.passport.services.ErrorResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin REST API for Agency/LBAC management
 * Path: /admin/realms/{realm}/agency
 */
@Path("/admin/realms/{realm}/agency")
public class AgencyAdminResource {
    
    private final PassportSession session;
    private final RealmModel realm;
    private final AgencyProvider agency;
    private final AdminPermissionEvaluator auth;
    
    public AgencyAdminResource(PassportSession session, RealmModel realm, 
                               AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
        this.agency = session.getProvider(AgencyProvider.class);
    }
    
    // ===== PRINCIPALS =====
    
    @GET
    @Path("/principals")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PrincipalRepresentation> getPrincipals() {
        auth.realm().requireViewRealm();
        return agency.getRealmPrincipals(realm).stream()
            .map(this::toRepresentation)
            .collect(Collectors.toList());
    }
    
    @POST
    @Path("/principals")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPrincipal(PrincipalRepresentation rep) {
        auth.realm().requireManageRealm();
        PrincipalModel principal = agency.createPrincipal(realm, rep.getName(), rep.getType());
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder()
            .path(principal.getId()).build())
            .entity(toRepresentation(principal))
            .build();
    }
    
    @GET
    @Path("/principals/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PrincipalRepresentation getPrincipal(@PathParam("id") String id) {
        auth.realm().requireViewRealm();
        PrincipalModel principal = agency.getPrincipal(realm, id);
        if (principal == null) {
            throw new NotFoundException("Principal not found");
        }
        return toRepresentation(principal);
    }
    
    // ===== DELEGATES =====
    
    @GET
    @Path("/users/{userId}/delegates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DelegateRepresentation> getUserDelegates(@PathParam("userId") String userId) {
        auth.users().requireViewUser(userId);
        UserModel user = session.users().getUserById(realm, userId);
        // Implementation...
        return List.of();
    }
    
    @POST
    @Path("/users/{userId}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDelegate(@PathParam("userId") String userId, 
                                   DelegateRepresentation rep) {
        auth.users().requireManageUser(userId);
        // Implementation...
        return Response.ok().build();
    }
    
    // ===== MANDATES =====
    
    @GET
    @Path("/mandates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MandateRepresentation> getMandates(
            @QueryParam("delegateId") String delegateId,
            @QueryParam("principalId") String principalId,
            @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly) {
        auth.realm().requireViewRealm();
        // Implementation with filtering...
        return List.of();
    }
    
    @POST
    @Path("/mandates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMandate(MandateRepresentation rep) {
        auth.realm().requireManageRealm();
        // Validate mandate against legal constraints
        if (!isValidMandate(rep)) {
            return ErrorResponse.error("Invalid mandate specification", Response.Status.BAD_REQUEST);
        }
        // Implementation...
        return Response.created(...).build();
    }
    
    @POST
    @Path("/mandates/{id}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationResponse validateMandate(@PathParam("id") String id,
                                               ValidationRequest request) {
        // Real-time mandate validation for access decisions
        boolean valid = agency.validateMandateForAction(...);
        return new ValidationResponse(valid, valid ? null : "Mandate insufficient for requested action");
    }
    
    // ===== REALM CONFIG =====
    
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public AgencyRealmConfig getRealmConfig() {
        auth.realm().requireViewRealm();
        AgencyRealmConfig config = new AgencyRealmConfig();
        config.setEnabled(Boolean.parseBoolean(realm.getAttribute("agency.enabled")));
        config.setDefaultJurisdiction(realm.getAttribute("agency.defaultJurisdiction"));
        config.setComplianceMode(realm.getAttribute("agency.complianceMode"));
        // Load other config...
        return config;
    }
    
    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRealmConfig(AgencyRealmConfig config) {
        auth.realm().requireManageRealm();
        realm.setAttribute("agency.enabled", String.valueOf(config.isEnabled()));
        realm.setAttribute("agency.defaultJurisdiction", config.getDefaultJurisdiction());
        realm.setAttribute("agency.complianceMode", config.getComplianceMode());
        return Response.noContent().build();
    }
    
    // Helper methods...
    private PrincipalRepresentation toRepresentation(PrincipalModel model) {
        PrincipalRepresentation rep = new PrincipalRepresentation();
        rep.setId(model.getId());
        rep.setName(model.getName());
        rep.setType(model.getType());
        rep.setCreatedAt(model.getCreatedAt());
        rep.setActive(model.isActive());
        return rep;
    }
    
    private boolean isValidMandate(MandateRepresentation rep) {
        // Legal validation logic - jurisdiction, scope constraints, etc.
        return true;
    }
}
```

### Step 8: Register Agency SPI

**New File: `services/src/main/resources/META-INF/services/org.passport.provider.Spi`**

Add line:
```
org.passport.agency.AgencySpi
```

**New File: `services/src/main/resources/META-INF/services/org.passport.agency.AgencyProviderFactory`**

Add line:
```
org.passport.agency.jpa.JpaAgencyProviderFactory
```

---

## Phase 4: Frontend Agency UI (Day 5-7)

### Step 9: Add Agency Navigation

**File: `js/apps/admin-ui/src/components/nav/AdminNav.tsx`**

Add to the navigation structure:

```tsx
import {
  Nav,
  NavGroup,
  NavItem,
  NavList
} from '@patternfly/react-core';
import { 
  UsersIcon, 
  BlueprintIcon,  // For Agency
  CertificateIcon, // For Qualifications
  ShareAltIcon,    // For Delegates
  FileContractIcon // For Mandates
} from '@patternfly/react-icons';

export const AdminNav = () => {
  const { realm } = useRealm();
  const agencyEnabled = realm.attributes?.['agency.enabled'] === 'true';
  
  return (
    <Nav>
      {/* Existing groups... */}
      
      <NavGroup title="Manage">
        <NavItem to={`/${realm}/users`} icon={<UsersIcon />}>
          Users
        </NavItem>
        {/* ... other items ... */}
      </NavGroup>
      
      {/* PASSPORT AGENCY SECTION */}
      {agencyEnabled && (
        <NavGroup title="Agency">
          <NavItem to={`/${realm}/agency/principals`} icon={<BlueprintIcon />}>
            Principals
          </NavItem>
          <NavItem to={`/${realm}/agency/qualifications`} icon={<CertificateIcon />}>
            Qualifications
          </NavItem>
          <NavItem to={`/${realm}/agency/delegates`} icon={<ShareAltIcon />}>
            Delegates
          </NavItem>
          <NavItem to={`/${realm}/agency/mandates`} icon={<FileContractIcon />}>
            Mandates
          </NavItem>
        </NavGroup>
      )}
      
      <NavGroup title="Configure">
        <NavItem to={`/${realm}/realm-settings`}>
          Realm settings
        </NavItem>
        {/* ... */}
      </NavGroup>
    </Nav>
  );
};
```

### Step 10: Create Agency Pages

**New File: `js/apps/admin-ui/src/agency/PrincipalsList.tsx`**

```tsx
import { useState, useEffect } from 'react';
import { useRealm } from '../context/RealmContext';
import {
  PageSection,
  PageSectionVariants,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarItem,
  Button,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  ActionsColumn,
  Label,
  Modal,
  Form,
  FormGroup,
  TextInput,
  Select,
  SelectOption
} from '@patternfly/react-core';
import { PlusCircleIcon, BlueprintIcon } from '@patternfly/react-icons';
import { useFetch } from '../utils/useFetch';

interface Principal {
  id: string;
  name: string;
  type: 'individual' | 'organization' | 'system';
  status: 'active' | 'suspended' | 'revoked';
  createdAt: string;
  jurisdiction: string;
}

export const PrincipalsList = () => {
  const { realm } = useRealm();
  const [principals, setPrincipals] = useState<Principal[]>([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newPrincipal, setNewPrincipal] = useState({ name: '', type: 'organization', jurisdiction: '' });
  
  useFetch(
    () => fetch(`/admin/realms/${realm}/agency/principals`),
    setPrincipals,
    [realm]
  );
  
  const columns = ['Name', 'Type', 'Jurisdiction', 'Status', 'Created'];
  
  const rowActions = (principal: Principal) => [
    { title: 'View Details', onClick: () => navigate(`/agency/principals/${principal.id}`) },
    { title: 'Edit', onClick: () => editPrincipal(principal) },
    { title: 'Manage Delegates', onClick: () => navigate(`/agency/principals/${principal.id}/delegates`) },
    { isSeparator: true },
    { title: 'Suspend', onClick: () => suspendPrincipal(principal.id) },
    { title: 'Revoke', onClick: () => revokePrincipal(principal.id), isDanger: true }
  ];
  
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'green';
      case 'suspended': return 'orange';
      case 'revoked': return 'red';
      default: return 'grey';
    }
  };
  
  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <Title headingLevel="h1">
          <BlueprintIcon /> Principals
        </Title>
        <p>Legal entities authorized to act within this realm.</p>
      </PageSection>
      
      <PageSection>
        <Toolbar>
          <ToolbarContent>
            <ToolbarItem>
              <Button 
                variant="primary" 
                icon={<PlusCircleIcon />}
                onClick={() => setIsCreateModalOpen(true)}
              >
                Create Principal
              </Button>
            </ToolbarItem>
          </ToolbarContent>
        </Toolbar>
        
        <Table aria-label="Principals" borders isStriped>
          <Thead>
            <Tr>
              {columns.map(col => <Th key={col}>{col}</Th>)}
              <Th screenReaderText="Actions" />
            </Tr>
          </Thead>
          <Tbody>
            {principals.map(principal => (
              <Tr key={principal.id}>
                <Td>
                  <a href={`/agency/principals/${principal.id}`}>
                    {principal.name}
                  </a>
                </Td>
                <Td>{principal.type}</Td>
                <Td>{principal.jurisdiction}</Td>
                <Td>
                  <Label color={getStatusColor(principal.status)}>
                    {principal.status}
                  </Label>
                </Td>
                <Td>{new Date(principal.createdAt).toLocaleDateString()}</Td>
                <Td isActionCell>
                  <ActionsColumn items={rowActions(principal)} />
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      </PageSection>
      
      {/* Create Principal Modal */}
      <Modal
        title="Create New Principal"
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        actions={[
          <Button key="create" variant="primary" onClick={handleCreate}>
            Create
          </Button>,
          <Button key="cancel" variant="link" onClick={() => setIsCreateModalOpen(false)}>
            Cancel
          </Button>
        ]}
      >
        <Form>
          <FormGroup label="Principal Name" isRequired>
            <TextInput
              value={newPrincipal.name}
              onChange={val => setNewPrincipal({...newPrincipal, name: val})}
              placeholder="e.g., Acme Corporation"
            />
          </FormGroup>
          <FormGroup label="Type">
            <Select
              value={newPrincipal.type}
              onChange={val => setNewPrincipal({...newPrincipal, type: val})}
              options={[
                <SelectOption key="individual" value="individual">Individual</SelectOption>,
                <SelectOption key="organization" value="organization">Organization</SelectOption>,
                <SelectOption key="system" value="system">System</SelectOption>
              ]}
            />
          </FormGroup>
          <FormGroup label="Jurisdiction">
            <TextInput
              value={newPrincipal.jurisdiction}
              onChange={val => setNewPrincipal({...newPrincipal, jurisdiction: val})}
              placeholder="e.g., US-CA, EU-DE"
            />
          </FormGroup>
        </Form>
      </Modal>
    </>
  );
};
```

### Step 11: Add Agency Tab to Realm Settings

**File: `js/apps/admin-ui/src/realm-settings/RealmSettings.tsx`**

Add tab:

```tsx
import { useState } from 'react';
import {
  Tabs,
  Tab,
  TabTitleText,
  TabTitleIcon
} from '@patternfly/react-core';
import { 
  CogIcon, 
  SignInIcon, 
  EnvelopeIcon, 
  PaletteIcon,
  KeyIcon,
  CalendarIcon,
  GlobeIcon,
  ShieldAltIcon,
  ClockIcon,
  TokenIcon,
  FileContractIcon, // For Agency
  UserIcon
} from '@patternfly/react-icons';

// Import your new component
import { AgencyRealmSettings } from '../agency/AgencyRealmSettings';

export const RealmSettings = () => {
  const [activeTab, setActiveTab] = useState(0);
  
  const tabs = [
    { eventKey: 0, title: 'General', icon: <CogIcon />, component: <GeneralSettings /> },
    { eventKey: 1, title: 'Login', icon: <SignInIcon />, component: <LoginSettings /> },
    { eventKey: 2, title: 'Email', icon: <EnvelopeIcon />, component: <EmailSettings /> },
    { eventKey: 3, title: 'Themes', icon: <PaletteIcon />, component: <ThemesSettings /> },
    { eventKey: 4, title: 'Keys', icon: <KeyIcon />, component: <KeysSettings /> },
    { eventKey: 5, title: 'Events', icon: <CalendarIcon />, component: <EventsSettings /> },
    { eventKey: 6, title: 'Localization', icon: <GlobeIcon />, component: <LocalizationSettings /> },
    { eventKey: 7, title: 'Security defenses', icon: <ShieldAltIcon />, component: <SecurityDefenses /> },
    { eventKey: 8, title: 'Sessions', icon: <ClockIcon />, component: <SessionsSettings /> },
    { eventKey: 9, title: 'Tokens', icon: <TokenIcon />, component: <TokenSettings /> },
    { eventKey: 10, title: 'Client policies', icon: <FileContractIcon />, component: <ClientPolicies /> },
    { eventKey: 11, title: 'User profile', icon: <UserIcon />, component: <UserProfile /> },
    // PASSPORT AGENCY TAB
    { eventKey: 12, title: 'Agency', icon: <BlueprintIcon />, component: <AgencyRealmSettings /> },
  ];
  
  return (
    <Tabs 
      activeKey={activeTab} 
      onSelect={(event, eventKey) => setActiveTab(eventKey as number)}
      isBox={false}
      mountOnEnter
      unmountOnExit
    >
      {tabs.map(tab => (
        <Tab 
          key={tab.eventKey}
          eventKey={tab.eventKey}
          title={
            <>
              <TabTitleIcon>{tab.icon}</TabTitleIcon>
              <TabTitleText>{tab.title}</TabTitleText>
            </>
          }
        >
          {tab.component}
        </Tab>
      ))}
    </Tabs>
  );
};
```

**New File: `js/apps/admin-ui/src/agency/AgencyRealmSettings.tsx`**

```tsx
import { useState, useEffect } from 'react';
import { useRealm } from '../context/RealmContext';
import {
  PageSection,
  Form,
  FormGroup,
  Switch,
  TextInput,
  Select,
  SelectOption,
  ActionGroup,
  Button,
  Alert
} from '@patternfly/react-core';
import { SaveIcon, UndoIcon } from '@patternfly/react-icons';

interface AgencyConfig {
  enabled: boolean;
  defaultJurisdiction: string;
  complianceMode: 'gdpr' | 'ccpa' | 'hipaa' | 'sox' | 'custom';
  requireMandateForDelegation: boolean;
  maxDelegationDepth: number;
  auditLevel: 'minimal' | 'standard' | 'comprehensive';
}

export const AgencyRealmSettings = () => {
  const { realm, updateRealm } = useRealm();
  const [config, setConfig] = useState<AgencyConfig>({
    enabled: false,
    defaultJurisdiction: '',
    complianceMode: 'gdpr',
    requireMandateForDelegation: true,
    maxDelegationDepth: 3,
    auditLevel: 'standard'
  });
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  
  useEffect(() => {
    // Load existing config
    fetch(`/admin/realms/${realm}/agency/config`)
      .then(res => res.json())
      .then(data => setConfig(data))
      .catch(() => {/* Use defaults */});
  }, [realm]);
  
  const handleSave = async () => {
    setIsSaving(true);
    setSaveError(null);
    
    try {
      // Update realm attributes
      await updateRealm({
        attributes: {
          ...realm.attributes,
          'agency.enabled': String(config.enabled),
          'agency.defaultJurisdiction': config.defaultJurisdiction,
          'agency.complianceMode': config.complianceMode,
          'agency.requireMandate': String(config.requireMandateForDelegation),
          'agency.maxDepth': String(config.maxDelegationDepth),
          'agency.auditLevel': config.auditLevel
        }
      });
      
      // Update agency config
      await fetch(`/admin/realms/${realm}/agency/config`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      });
      
    } catch (error) {
      setSaveError(error.message);
    } finally {
      setIsSaving(false);
    }
  };
  
  return (
    <PageSection>
      <Form>
        {saveError && (
          <Alert variant="danger" title={saveError} isInline />
        )}
        
        <FormGroup label="Enable Agency Framework">
          <Switch
            id="agency-enabled"
            isChecked={config.enabled}
            onChange={checked => setConfig({...config, enabled: checked})}
            label="Agency features enabled"
            labelOff="Agency features disabled"
          />
        </FormGroup>
        
        {config.enabled && (
          <>
            <FormGroup label="Default Jurisdiction" fieldId="jurisdiction">
              <TextInput
                id="jurisdiction"
                value={config.defaultJurisdiction}
                onChange={val => setConfig({...config, defaultJurisdiction: val})}
                placeholder="e.g., US-CA, EU-DE, UK"
              />
            </FormGroup>
            
            <FormGroup label="Compliance Mode" fieldId="compliance">
              <Select
                id="compliance"
                value={config.complianceMode}
                onChange={val => setConfig({...config, complianceMode: val})}
                options={[
                  <SelectOption key="gdpr" value="gdpr">GDPR (EU)</SelectOption>,
                  <SelectOption key="ccpa" value="ccpa">CCPA (California)</SelectOption>,
                  <SelectOption key="hipaa" value="hipaa">HIPAA (Healthcare)</SelectOption>,
                  <SelectOption key="sox" value="sox">SOX (Financial)</SelectOption>,
                  <SelectOption key="custom" value="custom">Custom Policy</SelectOption>
                ]}
              />
            </FormGroup>
            
            <FormGroup label="Delegation Requirements">
              <Switch
                id="require-mandate"
                isChecked={config.requireMandateForDelegation}
                onChange={checked => setConfig({...config, requireMandateForDelegation: checked})}
                label="Require explicit mandate for all delegations"
              />
            </FormGroup>
            
            <FormGroup label="Maximum Delegation Depth" fieldId="max-depth">
              <TextInput
                id="max-depth"
                type="number"
                value={config.maxDelegationDepth}
                onChange={val => setConfig({...config, maxDelegationDepth: parseInt(val)})}
                min={1}
                max={10}
              />
            </FormGroup>
            
            <FormGroup label="Audit Logging Level" fieldId="audit">
              <Select
                id="audit"
                value={config.auditLevel}
                onChange={val => setConfig({...config, auditLevel: val})}
                options={[
                  <SelectOption key="minimal" value="minimal">Minimal (Errors only)</SelectOption>,
                  <SelectOption key="standard" value="standard">Standard (Access events)</SelectOption>,
                  <SelectOption key="comprehensive" value="comprehensive">Comprehensive (All actions)</SelectOption>
                ]}
              />
            </FormGroup>
          </>
        )}
        
        <ActionGroup>
          <Button 
            variant="primary" 
            icon={<SaveIcon />}
            onClick={handleSave}
            isLoading={isSaving}
          >
            Save
          </Button>
          <Button 
            variant="link" 
            icon={<UndoIcon />}
            onClick={() => window.location.reload()}
          >
            Revert
          </Button>
        </ActionGroup>
      </Form>
    </PageSection>
  );
};
```

---

## Phase 5: Build & Deploy (Day 7-8)

### Step 12: Build Commands

```bash
# Build the entire project (takes 10-15 minutes first time)
cd passport-iam
mvn clean install -DskipTests -Dquarkus.build.native=false

# Build just the admin UI (faster for UI dev)
cd js/apps/admin-ui
npm install
npm run build

# Run in dev mode (hot reload)
npm run dev  # Runs on localhost:5173

# Build Quarkus distribution
cd quarkus
mvn clean package -DskipTests
```

### Step 13: Docker Build

**New File: `Dockerfile.passport`**

```dockerfile
FROM quay.io/passport/passport:22.0.5 as builder

# Enable health and metrics
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure database
ENV KC_DB=postgres

WORKDIR /opt/passport

# Add your custom providers
COPY target/passport-agency-provider.jar /opt/passport/providers/
COPY themes/passport /opt/passport/themes/passport

RUN /opt/passport/bin/kc.sh build

FROM quay.io/passport/passport:22.0.5
COPY --from=builder /opt/passport/ /opt/passport/

# Passport branding
ENV KC_SPI_THEME_DEFAULT=passport

ENTRYPOINT ["/opt/passport/bin/kc.sh"]
```

**Build & run**:
```bash
docker build -f Dockerfile.passport -t passport-iam:latest .
docker run -p 8080:8080 \
  -e PASSPORT_ADMIN=admin \
  -e PASSPORT_ADMIN_PASSWORD=admin \
  -e KC_DB_URL=jdbc:postgresql://db:5432/passport \
  passport-iam:latest start-dev
```

---

## Summary: What You Get

| Feature | Status |
|---------|--------|
| Full Passport 22+ feature set | ✅ Inherited |
| OIDC/OAuth2/SAML/LDAP | ✅ Inherited |
| Admin UI with proper layout | ✅ Inherited + themed |
| Agency/LBAC extensions | ✅ Your additions |
| Import/export Passport realms | ✅ Native |
| Passport branding | ✅ Custom CSS |
| Docker deployment | ✅ Ready |

---

## Next Steps for Kimi Code

When you switch to Kimi Code, have it:

1. **Generate the JPA entities** for `Principal`, `Delegate`, `Mandate`, `Qualification`
2. **Implement the AgencyProvider** with full CRUD operations
3. **Add the authentication flow** for Agency verification (custom authenticator)
4. **Create the protocol mapper** to inject Agency claims into JWTs
5. **Build the remaining UI pages**: Qualifications, Delegates, Mandates lists and detail views

Want me to generate any of these specific files in full detail now, or is this enough to get your fork started?