package org.passport.testframework;

import java.util.List;

import org.passport.testframework.clustering.LoadBalancerSupplier;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.server.ClusteredPassportServerSupplier;

public class ClusteringTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new ClusteredPassportServerSupplier(), new LoadBalancerSupplier());
    }
}
