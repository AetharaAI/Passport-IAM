package org.passport.test.migration;

public class AddPassportIntegrationTestRewrite extends TestRewrite {

    @Override
    public void rewrite() {
        addImport("org.passport.testframework.annotations.PassportIntegrationTest");

        int classDeclaration = findClassDeclaration();
        content.add(classDeclaration, "@PassportIntegrationTest");

        info(classDeclaration,"Added @PassportIntegrationTest");
    }

}
