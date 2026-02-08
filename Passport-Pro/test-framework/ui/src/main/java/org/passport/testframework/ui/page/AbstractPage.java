package org.passport.testframework.ui.page;

import org.passport.testframework.ui.webdriver.ManagedWebDriver;

public abstract class AbstractPage {

    protected final ManagedWebDriver driver;

    public AbstractPage(ManagedWebDriver driver) {
        this.driver = driver;
    }

    public abstract String getExpectedPageId();

    public void assertCurrent() {
        driver.waiting().waitForPage(this);
    }
}
