# Java SE CDI JPA Weld Project Template

A project template demonstrating **CDI** (Weld SE), **JPA** (Hibernate ORM), and **JUnit Jupiter** integration in a plain Java SE environment — no application server required.

## Architecture

```
src/main/java
└── org.os890.cdi.template
    ├── EntityManagerProducer          CDI producer for request-scoped EntityManager
    ├── persistence/
    │   ├── ConfigEntry                JPA entity (key-value config)
    │   ├── ConfigRepository           Repository interface
    │   └── SimplifiedConfigRepository JPA implementation
    └── service/
        ├── ConfigService              Service interface
        └── SimplifiedConfigService    Transactional implementation

src/test/java
└── org.os890.cdi.test
    ├── SimpleTest                     Integration test (JUnit Jupiter + Weld)
    ├── SimpleTestBeanClasses          Composite stereotype annotation
    ├── mock/
    │   ├── TestConfigBundle           Meta-annotation for test bean registration
    │   └── TestConfigService          Request-scoped test override
    ├── jpa/
    │   ├── JpaTestBeanClasses         Meta-annotation for JPA test beans
    │   ├── TestEntityManagerProducer  In-memory HSQLDB entity manager
    │   └── TestPersistenceUnitInfo    Programmatic persistence unit config
    └── weld/
        ├── cdi/
        │   ├── CdiContainer           Container control SPI
        │   ├── CdiContainerLoader     ServiceLoader-based factory
        │   ├── ContextControl         CDI context lifecycle control
        │   └── impl/
        │       ├── WeldContainerControl  Weld SE container implementation
        │       └── WeldContextControl    Weld context lifecycle management
        └── junit/
            └── WeldExtension          JUnit Jupiter extension for CDI tests
```

## Usage

### Running a CDI + JPA test

Annotate your test class with `@ExtendWith(WeldExtension.class)` and declare which beans to register:

```java
@SimpleTestBeanClasses
@ExtendWith(WeldExtension.class)
class MyTest {

    @Inject
    private ConfigService configService;

    @Test
    void crud() {
        ConfigEntry entry = configService.save(new ConfigEntry("key", "value"));
        assertNotNull(configService.load(entry.getId()));
    }
}
```

### Registering custom beans

Use the `@WeldExtension.CdiBeanClass` annotation (repeatable) to declare which CDI beans are available:

```java
@WeldExtension.CdiBeanClass({MyService.class, MyRepository.class})
@ExtendWith(WeldExtension.class)
class CustomTest { ... }
```

Annotations can be composed into meta-annotations for reuse (see `TestConfigBundle`, `JpaTestBeanClasses`).

### Programmatic bean lookup

```java
MyService service = WeldExtension.getContextualReference(MyService.class, false);
```

Pass `true` as the second argument if the bean is optional (returns `null` instead of throwing).

## Requirements

| Component        | Version          |
|------------------|------------------|
| Java             | 25+              |
| Maven            | 3.6.3+           |
| CDI API          | 4.1.0            |
| Hibernate ORM    | 7.0.0.Final      |
| Weld SE          | 6.0.1.Final      |
| JUnit Jupiter    | 5.12.2           |
| H2 (production)  | 2.3.232          |
| HSQLDB (test)    | 2.7.4            |

## Code Quality

All quality plugins run automatically during every build:

| Plugin                     | Phase      | Purpose                                            |
|----------------------------|------------|----------------------------------------------------|
| `maven-compiler-plugin`    | compile    | `-Xlint:all` with `failOnWarning=true`             |
| `maven-enforcer-plugin`    | validate   | Java 25+, Maven 3.6.3+, dependency convergence, banned `javax.*` |
| `maven-checkstyle-plugin`  | validate   | Code style enforcement (Checkstyle 10.23.1)        |
| `apache-rat-plugin`        | validate   | Apache License 2.0 header verification             |
| `maven-surefire-plugin`    | test       | Unit / integration test execution                  |
| `jacoco-maven-plugin`      | test       | Code coverage reporting                            |
| `maven-javadoc-plugin`     | (on demand)| Javadoc generation                                 |

## Building

```bash
# Compile, run quality checks, and execute tests
mvn clean verify

# Generate Javadoc
mvn javadoc:javadoc

# Generate coverage report (target/site/jacoco/index.html)
mvn clean verify   # JaCoCo runs automatically
```

## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
