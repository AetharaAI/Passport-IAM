package org.passport.testframework.ui.webdriver;


import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.ui.annotations.InjectWebDriver;

import org.openqa.selenium.WebDriver;

public abstract class AbstractWebDriverSupplier implements Supplier<ManagedWebDriver, InjectWebDriver> {

    @Override
    public ManagedWebDriver getValue(InstanceContext<ManagedWebDriver, InjectWebDriver> instanceContext) {
        return new ManagedWebDriver(getWebDriver());
    }

    @Override
    public boolean compatible(InstanceContext<ManagedWebDriver, InjectWebDriver> a, RequestedInstance<ManagedWebDriver, InjectWebDriver> b) {
        return true;
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public void close(InstanceContext<ManagedWebDriver, InjectWebDriver> instanceContext) {
        instanceContext.getValue().driver().quit();
    }

    public abstract WebDriver getWebDriver();

}
