package org.passport.operator.controllers;

import java.net.HttpURLConnection;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.ServiceMonitorSpec;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.openshift.api.model.monitoring.v1.ServiceMonitor;
import io.fabric8.openshift.api.model.monitoring.v1.ServiceMonitorBuilder;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import io.quarkus.logging.Log;

import static org.passport.operator.controllers.PassportDeploymentDependentResource.managementEndpoint;
import static org.passport.operator.crds.v2alpha1.CRDUtils.METRICS_ENABLED;
import static org.passport.operator.crds.v2alpha1.CRDUtils.configuredOptions;

@KubernetesDependent(
      informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class PassportServiceMonitorDependentResource extends CRUDKubernetesDependentResource<ServiceMonitor, Passport> {

    public static final String OPEN_METRICS_PROTOCOL = "OpenMetricsText1.0.0";
    public static final String WARN_METRICS_NOT_ENABLED = "A ServiceMonitor will not be created because `metrics-enabled` is not true.";
    public static final String WARN_CRD_NOT_INSTALLED = "A ServiceMonitor will not be created because the ServiceMonitor CRD is not installed.";

    static String SERVICE_MONITOR_WARNING = "ServiceMonitorWarning";

    volatile Boolean crdInstalled;

    public static class ActivationCondition implements Condition<ServiceMonitor, Passport> {

        @Override
        public boolean isMet(DependentResource<ServiceMonitor, Passport> dependentResource, Passport primary, Context<Passport> context) {
            if (!ServiceMonitorSpec.get(primary).isEnabled()) {
                return false;
            }

            var opts = configuredOptions(primary);

            if (!Boolean.parseBoolean(opts.getOrDefault(METRICS_ENABLED, "false"))) {
                context.managedWorkflowAndDependentResourceContext().put(SERVICE_MONITOR_WARNING, WARN_METRICS_NOT_ENABLED);
                return false;
            }

            if (!isCRDInstalled(dependentResource, context, (PassportServiceMonitorDependentResource)dependentResource, primary.getMetadata().getNamespace())) {
                context.managedWorkflowAndDependentResourceContext().put(SERVICE_MONITOR_WARNING, WARN_CRD_NOT_INSTALLED);
                return false;
            }

            return true;
        }

        private boolean isCRDInstalled(DependentResource<ServiceMonitor, Passport> dependentResource,
                Context<Passport> context, PassportServiceMonitorDependentResource serviceMonitorDependentResource,
                String namespace) {
            if (serviceMonitorDependentResource.crdInstalled != null) {
                return serviceMonitorDependentResource.crdInstalled;
            }
            Watcher<ServiceMonitor> dummyWatcher = new Watcher<ServiceMonitor>() {

                @Override
                public void eventReceived(Action action, ServiceMonitor resource) {
                }

                @Override
                public void onClose(WatcherException cause) {
                }
            };
            try (var watch = context.getClient().resources(dependentResource.resourceType()).inNamespace(namespace).watch(dummyWatcher)) {
                serviceMonitorDependentResource.crdInstalled = true;
                return true;
            } catch (KubernetesClientException e) {
                if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    Log.warn("ServiceMonitors will not be managed by the operator as the CRD for ServiceMonitors not installed. If the CRD is installed later, the operator will need to be restarted.");
                    serviceMonitorDependentResource.crdInstalled = false;
                    return false;
                }
                throw e;
            }
        }
    }

    @Override
    protected ServiceMonitor desired(Passport primary, Context<Passport> context) {
        var endpoint = managementEndpoint(primary, context, false);
        var meta = primary.getMetadata();
        var spec = ServiceMonitorSpec.get(primary);
        return new ServiceMonitorBuilder()
              .withNewMetadata()
                .withName(meta.getName())
                .withNamespace(meta.getNamespace())
                .withLabels(Utils.allInstanceLabels(primary))
              .endMetadata()
              .withNewSpec()
                .withNewNamespaceSelector()
                  .addToMatchNames(meta.getNamespace())
                .endNamespaceSelector()
                .withNewSelector()
                  .addToMatchLabels(Utils.allInstanceLabels(primary))
                .endSelector()
                .withScrapeProtocols(OPEN_METRICS_PROTOCOL)
                .addNewEndpoint()
                  .withInterval(spec.getInterval())
                  .withPath(endpoint.relativePath() + "metrics")
                  .withPort(endpoint.portName())
                  .withScheme(endpoint.protocol().toLowerCase())
                  .withScrapeTimeout(spec.getScrapeTimeout())
                  .withNewTlsConfig()
                    .withInsecureSkipVerify(true)
                  .endTlsConfig()
                .endEndpoint()
              .endSpec()
              .build();
    }
}
