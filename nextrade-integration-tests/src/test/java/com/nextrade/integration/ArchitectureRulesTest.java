package com.nextrade.integration;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ArchitectureRulesTest {
    private final JavaClasses classes = new ClassFileImporter().importPackages("com.nextrade");

    @Test void domainShouldNotDependOnSpring() {
        noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAPackage("org.springframework..").check(classes);
    }

    @Test void engineShouldNotDependOnSpringOrPersistence() {
        noClasses().that().resideInAPackage("..engine..").should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "..persistence..", "..api..", "..service..").check(classes);
    }

    @Test void persistenceShouldNotDependOnServiceOrApi() {
        noClasses().that().resideInAPackage("..persistence..").should().dependOnClassesThat().resideInAnyPackage("..service..", "..api..").check(classes);
    }

    @Test void apiShouldNotDependOnEngineDirectly() {
        noClasses().that().resideInAPackage("..api..").should().dependOnClassesThat().resideInAPackage("..engine..").check(classes);
    }

    @Test void noFieldInjection() {
        noFields().should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired").check(classes);
    }
}
