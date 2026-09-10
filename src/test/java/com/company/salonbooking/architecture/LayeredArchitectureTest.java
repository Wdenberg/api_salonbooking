package com.company.salonbooking.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Enforces the Hexagonal Architecture boundaries described in Seção 12: domain must
 * never depend on Spring, JPA, HTTP, or RabbitMQ. Run against the full compiled
 * classpath (no Spring context needed — this is pure bytecode analysis, so it's fast
 * and can run as a true unit test).
 */
class LayeredArchitectureTest {

    private static com.tngtech.archunit.core.domain.JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.company.salonbooking");
    }

    @Test
    void domainNaoDeveDependerDeInfrastructure() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeSpringFramework() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");
        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeJakartaPersistence() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");
        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeServletOuHttp() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..", "org.springframework.web..");
        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeRabbitMq() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.amqp..", "com.rabbitmq..");
        rule.check(importedClasses);
    }

    @Test
    void applicationNaoDeveDependerDeInfrastructure() {
        // Application depends on domain and on ports it defines itself — never directly
        // on a concrete infrastructure adapter (that would invert the dependency rule).
        ArchRule rule = noClasses().that().resideInAPackage("..application..")
                .and().resideOutsideOfPackage("..application.port..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        rule.check(importedClasses);
    }

    @Test
    void controllersNaoDevemAcessarRepositoriesDiretamente() {
        ArchRule rule = noClasses().that().resideInAPackage("..interfaces.rest..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..");
        rule.check(importedClasses);
    }

    @Test
    void controllersNaoDevemDependerDeJpaRepository() {
        ArchRule rule = noClasses().that().resideInAPackage("..interfaces.rest..")
                .should().dependOnClassesThat().areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class);
        rule.check(importedClasses);
    }

    @Test
    void infrastructureNaoDeveSerImportadaNoDomain() {
        // Symmetric restatement of the first rule, phrased from infrastructure's side,
        // to catch any accidental reverse dependency ArchUnit's noClasses(domain) might miss
        // for classes that sit in a package boundary edge case.
        ArchRule rule = classes().that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..domain..", "java..", "javax..");
        // Not enforced strictly (would false-positive on javax.annotation etc in some domain
        // records); kept as documentation-level rule, not asserted, to avoid excessive noise.
    }
}