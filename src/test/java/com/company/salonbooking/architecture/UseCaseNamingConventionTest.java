package com.company.salonbooking.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class UseCaseNamingConventionTest {

    private static com.tngtech.archunit.core.domain.JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.company.salonbooking");
    }

    @Test
    void classesEmPacotesUsecaseDevemTerSufixoUseCase() {
        ArchRule rule = classes().that().resideInAPackage("..application.usecase..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("UseCase");
        rule.check(importedClasses);
    }

    @Test
    void repositoriesNoDominioDevemSerInterfaces() {
        ArchRule rule = classes().that().resideInAPackage("..domain.repository..")
                .should().beInterfaces();
        rule.check(importedClasses);
    }

    @Test
    void controllersDevemTerSufixoController() {
        ArchRule rule = classes().that().resideInAPackage("..interfaces.rest")
                .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().haveSimpleNameEndingWith("Controller");
        rule.check(importedClasses);
    }
}