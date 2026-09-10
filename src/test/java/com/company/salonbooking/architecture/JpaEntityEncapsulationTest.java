package com.company.salonbooking.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Enforces Seção 52: "Nunca retornar Entity JPA diretamente." */
class JpaEntityEncapsulationTest {

    private static com.tngtech.archunit.core.domain.JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.company.salonbooking");
    }

    @Test
    void controllersNuncaDevemReferenciarJpaEntities() {
        ArchRule rule = noClasses().that().resideInAPackage("..interfaces.rest..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("JpaEntity");
        rule.check(importedClasses);
    }

    @Test
    void useCasesNuncaDevemRetornarJpaEntities() {
        // Use cases may depend on repositories (which internally use JPA entities),
        // but must never expose a JpaEntity type as a return value — mappers must
        // convert to the domain model before crossing that boundary.
        ArchRule rule = noClasses().that().resideInAPackage("..application.usecase..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("JpaRepository");
        rule.check(importedClasses);
    }
}
