package com.aredondocharro.ClothingStore.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.aredondocharro.ClothingStore",
        importOptions = { ImportOption.DoNotIncludeTests.class }
)
class ArchitectureRulesTest {

    // Dominio no depende de Spring ni JPA ni infraestructura
    @ArchTest
    static final ArchRule domain_no_spring_jpa_infra =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..", "jakarta.persistence..", "..infrastructure.."
                    );

    // Application no depende de Spring
    @ArchTest
    static final ArchRule application_no_spring =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

    // Dominio no puede ver infraestructura (doble protección)
    @ArchTest
    static final ArchRule domain_cannot_see_infrastructure =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

    // Aísla bounded contexts (ejemplos)
    @ArchTest
    static final ArchRule identity_no_depends_on_security_infra =
            noClasses().that().resideInAPackage("..identity..")
                    .should().dependOnClassesThat().resideInAnyPackage("..security.infrastructure..");

    @ArchTest
    static final ArchRule security_no_depends_on_identity_infra =
            noClasses().that().resideInAPackage("..security..")
                    .should().dependOnClassesThat().resideInAnyPackage("..identity.infrastructure..");
}
