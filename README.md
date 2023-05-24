# Spring Boot Startup Validator

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Main Purpose

This plug-and-play library is dedicated for Spring Boot application. It enables developers to run some more complex 
validation at app's startup and, if necessary, force it to stop. It also creates an easy-to-read report after an app's 
start.

## Getting Started

To use Spring Boot Startup Validator you have to add the following dependency to your pom:
```
<dependency>
  <groupId>eu.coding-commune</groupId>
  <artifactId>startup-validator-spring-boot-starter</artifactId>
  <version>1.0.4</version>
</dependency>
```

You also have to annotate one of Spring-managed classes with annotation `@EnableStartupValidation`. It assumes that all
code to be validated is in the same package or its subpackages. If you need to customize this behavior you can pass a 
list packages to be validated e.g. `@EnableStartupValidation({"com.example.one", "com.example.two"})`.
```
@SpringBootApplication
@EnableStartupValidation
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
```

## Usage

This library put following annotations at your disposal:
### `@MustBeDefined`
the field is not null nor an empty string. Supports following arguments:
- otherwise
- message

### `@MustMatch`
the field matches passed regex. Supports following arguments:
- regex
- otherwise
- message
- secret

### `@MustSucceed`
the method runs without throwing any exception. Supports following arguments:
- otherwise
- message

### `@MustReturn`
the method returns string passed via result (utilizes toString() methods). Supports following arguments:
- result 
- otherwise
- message

<br/>

#### Arguments
Explanation for mentioned aforementioned argument:
- **otherwise**
  - type: StartupAction (see explanation below)
  - for: specifying what should happen if a validation fails
- **message**
  - type: String
  - for: specifying custom message in case of a validation fail
- **regex**
  - type: String
  - for: specifying regex to be matched in `@MustMatch`
- **secret**
  - type: Boolean
  - for: marking if a field value passed by user should be obfuscated (so it is not displayed fully in the report)
- **result**
  - type: String
  - for: specifying string to be matched by return value of method annotated with `@MustReturn`

<br/>

#### StartupAction

An enum type that allows you to choose what happens if the validation fails. As of now following values are supported
- `WARN` - warn the user in the log file but do nothing more
- `FAIL` - **default** action for all validation. Warn the user in the log fail and **actively** shut down an app.

<br/>

#### Other considerations

- A startup validator also checks for **implicitly required fields** e.g. simple int type without which Spring Boot app
wouldn't start
- If there is a problem with any implicitly required fields or an app can't start because of some other reason then
`@MustSucceed` and `@MustReturn` validations won't fire (they need working Spring context)
- Annotations can be used only on Spring managed beans. Otherwise, they won't work.
- You can autowire properties both with `@ConfigurationProperties` and `@Value`

## Examples

1. #### Validating fields using `@ConfigurationProperties`
```java
@Configuration
@ConfigurationProperties("eu.coding-commune.startup-validator.test")
public class ConfigurationPropertiesTest {

  @MustBeDefined(otherwise = StartupAction.FAIL, message = "Name must be defined!")
  private String name;

  @MustMatch(
          regex = "^(.+)@(\\S+)$",
          message = "Provided email is not valid!")
  private String email;

}
```
2. #### Validating fields using `@Value`

```java
@Configuration
public class ValueTest {

  @Value("${eu.coding-commune.startup-validator.test.additionalInfo}")
  @MustBeDefined(otherwise = StartupAction.WARN, message = "No additional info provided.")
  private String additionalInfo;

  @Value("${eu.coding-commune.startup-validator.test.implicit}")
  private int implicitlyRequired;

}
```

3. #### Validating methods - using another service

```java
@Service
public class MethodTest {

    @Autowired
    private SpringManagedService service;

    @MustSucceed(otherwise = StartupAction.WARN, message = "SpringManagedService returned an unexpected value")
    private void testMustSucceed() {
        if (!service.testService()) {
            throw new RuntimeException("SpringManagedService returned false");
        }
    }

    @MustReturn(result = "EXPECTED", otherwise = StartupAction.FAIL, message = "An unexpected value was returned. Will shut down the app!")
    private String testMustReturn() {
        return service.getStringValue();
    }

}

```

```java
@Service
public class SpringManagedService {

    public boolean testService() {
        return false;
    }

    public String getStringValue() {
        return "OTHER_THAN_EXPECTED";
    }
}
```

4. #### Report examples

```
 _____ _____ ___  ______ _____ _   _______ 
/  ___|_   _/ _ \ | ___ \_   _| | | | ___ \
\ `--.  | |/ /_\ \| |_/ / | | | | | | |_/ /
 `--. \ | ||  _  ||    /  | | | | | |  __/ 
/\__/ / | || | | || |\ \  | | | |_| | |    
\____/  \_/\_| |_/\_| \_| \_/  \___/\_|    

   ______ ___________ ___________ _____ 
   | ___ \  ___| ___ \  _  | ___ \_   _|
   | |_/ / |__ | |_/ / | | | |_/ / | |  
   |    /|  __||  __/| | | |    /  | |  
   | |\ \| |___| |   \ \_/ / |\ \  | |  
   \_| \_\____/\_|    \___/\_| \_| \_/  
Version: 1.0.4
_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _
. . . . . . . . . . . . . . . . . . . . . . . 
SEVERITY LEVEL: ERROR. Found 2 problems.
. . . . . . . . . . . . . . . . . . . . . . . 
[ERROR] | Name must be defined! | Mandatory property not configured: eu.coding-commune.startup-validator.test.name
[ERROR] | Provided email is not valid! | Regex ^(.+)@(\\S+)$ was not matched for test@test provided for property eu.coding-commune.startup-validator.test.email
. . . . . . . . . . . . . . . . . . . . . . . 
SEVERITY LEVEL: PROBABLE_ERROR. Found 1 problems.
. . . . . . . . . . . . . . . . . . . . . . . 
[PROBABLE_ERROR] |  | Value null could not be resolved for property eu.coding-commune.startup-validator.test.implicit that requires type int
. . . . . . . . . . . . . . . . . . . . . . . 
SEVERITY LEVEL: WARN. Found 1 problems.
. . . . . . . . . . . . . . . . . . . . . . . 
[WARN] | No additional info provided. | Mandatory property not configured: eu.coding-commune.startup-validator.test.additionalInfo

End of Spring Startup Report
```

```
. . . . . . . . . . . . . . . . . . . . . . . 
SEVERITY LEVEL: ERROR. Found 1 problems.
. . . . . . . . . . . . . . . . . . . . . . . 
[ERROR] | An unexpected value was returned. Will shut down the app! | Following error occurred during execution of testMustReturn() in class pl.coding.commune.startup.validator.config.MethodTest: java.lang.Exception: Expected method result: "EXPECTED", actual result: "OTHER_THAN_EXPECTED"
. . . . . . . . . . . . . . . . . . . . . . . 
SEVERITY LEVEL: WARN. Found 1 problems.
. . . . . . . . . . . . . . . . . . . . . . . 
[WARN] | SpringManagedService returned an unexpected value | Following error occurred during execution of testMustSucceed() in class pl.coding.commune.startup.validator.config.MethodTest: java.lang.RuntimeException: SpringManagedService returned false
```
