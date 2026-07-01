package com.example.buildnest_ecommerce.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Enforces that service-package test classes use PIT-visible naming.
 *
 * PIT's targetTests pattern only picks up *ImplTest and *ServiceTest in service packages.
 * A class named FooTest (without the Impl/Service suffix) is silently excluded from
 * mutation analysis — it runs and appears in JaCoCo coverage but kills zero mutations.
 *
 * This rule fails the build with a clear message if such a class is detected, so the
 * naming trap is caught at PR time rather than silently degrading the mutation score.
 */
class PitNamingConventionTest {

    private static final String BASE_PACKAGE = "com.example.buildnest_ecommerce";

    @Test
    void serviceTestClassesMustUseImplTestOrServiceTestSuffix() {
        JavaClasses testClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE + ".service");

        classes()
                .that().haveSimpleNameEndingWith("Test")
                .and().haveSimpleNameNotEndingWith("ImplTest")
                .and().haveSimpleNameNotEndingWith("ServiceTest")
                .and().haveSimpleNameNotEndingWith("IntegrationTest")
                .and().haveSimpleNameNotEndingWith("E2ETest")
                .and().haveSimpleNameNotEndingWith("StressTest")
                .and().haveSimpleNameNotEndingWith("ReliabilityTest")
                .should(new ArchCondition<>("not exist in a service package") {
                    @Override
                    public void check(
                            com.tngtech.archunit.core.domain.JavaClass item,
                            ConditionEvents events) {
                        String msg = String.format(
                                "Test class '%s' in a service package must be named '*ImplTest' or " +
                                "'*ServiceTest' so PIT mutation testing picks it up. " +
                                "Rename it to '%sImplTest' (if testing a concrete *Impl class) " +
                                "or '%sServiceTest' (if testing an interface/abstract service).",
                                item.getName(),
                                item.getSimpleName().replaceAll("Test$", ""),
                                item.getSimpleName().replaceAll("Test$", ""));
                        events.add(SimpleConditionEvent.violated(item, msg));
                    }
                })
                .allowEmptyShould(true)
                .check(testClasses);
    }
}
