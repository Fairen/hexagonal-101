package dev.hexagonal101.library;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The dependency rule of the hexagon, checked on the compiled classes.
 * <p>
 * Every package shares the same classpath, so nothing but this test stops the domain
 * from importing Spring: if one of these rules breaks, the build fails.
 */
@AnalyzeClasses(packages = "dev.hexagonal101.library", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_depends_on_nothing = classes()
            .that().resideInAPackage("..library.domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("..library.domain..", "java..", "org.jspecify..");

    @ArchTest
    static final ArchRule application_depends_only_on_the_domain = classes()
            .that().resideInAPackage("..library.application..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..library.application..", "..library.domain..", "java..", "org.jspecify..");

    @ArchTest
    static final ArchRule adapters_do_not_know_each_other = slices()
            .matching("dev.hexagonal101.library.adapter.(*).(*)..")
            .should().notDependOnEachOther();

    @ArchTest
    static final ArchRule adapters_use_ports_not_services = noClasses()
            .that().resideInAPackage("..library.adapter..")
            .should().dependOnClassesThat().resideInAPackage("..library.application.service..");

    @ArchTest
    static final ArchRule driving_adapters_only_see_the_driving_port = noClasses()
            .that().resideInAPackage("..library.adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..library.domain..", "..library.application.port.out..", "..library.application.service..");

    @ArchTest
    static final ArchRule driven_adapters_do_not_call_use_cases = noClasses()
            .that().resideInAPackage("..library.adapter.out..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..library.application.port.in..", "..library.application.service..");

    @ArchTest
    static final ArchRule driven_ports_are_interfaces = classes()
            .that().resideInAPackage("..library.application.port.out..")
            .and().areTopLevelClasses()
            .should().beInterfaces();

    @ArchTest
    static final ArchRule the_hexagon_has_no_framework_annotation = noClasses()
            .that().resideInAnyPackage("..library.domain..", "..library.application..")
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beMetaAnnotatedWith("org.springframework.stereotype.Component");

    @ArchTest
    static final ArchRule only_bootstrap_knows_bootstrap = noClasses()
            .that().resideOutsideOfPackage("..library.bootstrap..")
            .should().dependOnClassesThat().resideInAPackage("..library.bootstrap..");
}
