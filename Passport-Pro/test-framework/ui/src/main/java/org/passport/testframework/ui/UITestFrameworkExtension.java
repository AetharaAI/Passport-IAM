package org.passport.testframework.ui;

import java.util.List;
import java.util.Map;

import org.passport.testframework.TestFrameworkExtension;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.ui.page.PageSupplier;
import org.passport.testframework.ui.webdriver.ChromeHeadlessWebDriverSupplier;
import org.passport.testframework.ui.webdriver.ChromeWebDriverSupplier;
import org.passport.testframework.ui.webdriver.FirefoxHeadlessWebDriverSupplier;
import org.passport.testframework.ui.webdriver.FirefoxWebDriverSupplier;
import org.passport.testframework.ui.webdriver.HtmlUnitWebDriverSupplier;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;


public class UITestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new HtmlUnitWebDriverSupplier(),
                new ChromeHeadlessWebDriverSupplier(),
                new ChromeWebDriverSupplier(),
                new FirefoxHeadlessWebDriverSupplier(),
                new FirefoxWebDriverSupplier(),
                new PageSupplier()
        );
    }

    @Override
    public Map<Class<?>, String> valueTypeAliases() {
        return Map.of(
                ManagedWebDriver.class, "browser"
        );
    }

}
