package org.passport.testframework.ui.page;

import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.ui.annotations.InjectPage;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;

public class PageSupplier  implements Supplier<AbstractPage, InjectPage> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<AbstractPage, InjectPage> instanceContext) {
        return DependenciesBuilder.create(ManagedWebDriver.class).build();
    }

    @Override
    public AbstractPage getValue(InstanceContext<AbstractPage, InjectPage> instanceContext) {
        ManagedWebDriver webDriver = instanceContext.getDependency(ManagedWebDriver.class);
        return webDriver.page().createPage(instanceContext.getRequestedValueType());
    }

    @Override
    public boolean compatible(InstanceContext<AbstractPage, InjectPage> a, RequestedInstance<AbstractPage, InjectPage> b) {
        return true;
    }

}
