# Module 1: Motivation

## Topic: Problems with Traditional Java Applications

### Learning Objective

Understand the common challenges of building Java applications without the Spring Framework, and why these challenges motivated the creation of Spring.

---

# 1. What is a Traditional Java Application?

A traditional Java application is one in which the **developer is responsible for creating, configuring, connecting, and managing all application objects manually**.

Example:

```java
UserRepository repository = new UserRepository();
UserService service = new UserService(repository);
UserController controller = new UserController(service);
```

The application itself manages object creation.

---

# 2. Core Problems

## 2.1 Tight Coupling

**Definition:** A class directly depends on a specific implementation instead of an abstraction.

Example:

```java
UserService service = new UserService(new MySQLUserRepository());
```

Problem:

* Difficult to replace implementations
* Small changes ripple through the codebase
* Reduced flexibility

---

## 2.2 Manual Object Creation

Every dependency must be created explicitly.

Example:

```java
Database database = new Database();
Repository repository = new Repository(database);
Service service = new Service(repository);
```

Problems:

* Repetitive code
* Error-prone
* Difficult to maintain

---

## 2.3 Complex Dependency Management

As applications grow, dependency chains become harder to manage.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
    ↓
Connection Pool
    ↓
Configuration
```

Creating and wiring these objects manually becomes increasingly complex.

---

## 2.4 Difficult Unit Testing

Hard-coded dependencies make testing difficult.

```java
UserService service = new UserService(new MySQLRepository());
```

You cannot easily substitute a mock repository.

Using an interface instead of a concrete class improves testability, but without a container, you still have to manually assemble dependencies.

---

## 2.5 High Boilerplate Code

A significant amount of code is dedicated to:

* Creating objects
* Passing dependencies
* Managing configuration
* Initializing resources

This code supports the application but does not implement business logic.

---

## 2.6 Poor Scalability

As the number of classes increases:

* More dependencies
* More constructors
* More configuration
* Greater maintenance effort

Managing a large application manually becomes impractical.

---

## 2.7 Configuration Complexity

Configuration is often scattered across:

* Java classes
* Properties files
* XML files
* Environment-specific settings

Managing these consistently is difficult.

---

## 2.8 Lifecycle Management

Developers must manually manage:

* Object creation
* Initialization
* Cleanup
* Resource release

Examples include closing database connections or shutting down thread pools.

---

## 2.9 Code Reuse Challenges

Without a common framework, developers often duplicate code for tasks such as:

* Logging
* Exception handling
* Transactions
* Security
* Configuration

This leads to inconsistent implementations.

---

## 2.10 Maintainability Issues

As projects evolve:

* Adding features becomes slower.
* Refactoring becomes riskier.
* Understanding dependencies becomes harder.
* The codebase becomes more difficult for new developers to learn.

---

# 3. Root Cause

Most of these problems arise because **the application itself is responsible for creating and managing its objects and their relationships**.

```text
Application
    │
    ├── Create Objects
    ├── Connect Objects
    ├── Configure Objects
    ├── Manage Lifecycle
    └── Manage Resources
```

This mixes business logic with infrastructure concerns.

---

# 4. How Spring Addresses These Problems (Preview)

| Traditional Java Problem      | Spring Solution                           |
| ----------------------------- | ----------------------------------------- |
| Tight coupling                | Dependency Injection (DI)                 |
| Manual object creation        | Inversion of Control (IoC) Container      |
| Complex dependency management | Automatic dependency wiring               |
| Difficult testing             | Dependency injection enables easy mocking |
| Boilerplate code              | Framework-managed infrastructure          |
| Configuration complexity      | Centralized configuration                 |
| Lifecycle management          | Bean lifecycle management                 |
| Poor scalability              | Container-managed components              |

---

# 5. Key Takeaways

* Traditional Java applications require developers to manually create, connect, configure, and manage objects.
* As applications grow, manual dependency management becomes increasingly complex.
* Tight coupling reduces flexibility and testability.
* Boilerplate and infrastructure code distract from business logic.
* Spring addresses these issues by introducing the **IoC Container** and **Dependency Injection**, which shift object management from the application to the framework.

---

# Knowledge Dependency

```text
Traditional Java Applications
            │
            ▼
Manual Object Creation
            │
            ▼
Tight Coupling
            │
            ▼
Maintenance & Testing Problems
            │
            ▼
Need for IoC
            │
            ▼
Need for Dependency Injection
            │
            ▼
Spring Framework
```

This topic lays the conceptual foundation for the next modules on **Inversion of Control (IoC)** and **Dependency Injection (DI)**, which explain how Spring systematically solves these problems.

## Topic: Tight Coupling

## Learning Objective

Understand what **tight coupling** is, why it is a problem in traditional Java applications, and how it motivates the need for **Inversion of Control (IoC)** and **Dependency Injection (DI)**.

---

# 1. What is Coupling?

## Definition

**Coupling** is the degree of dependency between two software components (such as classes or modules).

It answers the question:

> **"How much does one class depend on another?"**

---

## Two Types of Coupling

```text
Coupling
│
├── Tight Coupling
└── Loose Coupling
```

* **Tight Coupling:** Strong dependency between components.
* **Loose Coupling:** Minimal dependency between components.

The goal of good software design is generally to achieve **loose coupling**, while recognizing that some level of coupling is always necessary.

---

# 2. What is Tight Coupling?

## Definition

**Tight coupling** occurs when one class directly depends on the **concrete implementation** of another class, making it difficult to change, replace, test, or reuse either class independently.

---

## Example

Suppose we have:

```java
public class EmailService {

    public void send(String message) {
        System.out.println("Email sent: " + message);
    }
}
```

Now another class:

```java
public class NotificationService {

    private EmailService emailService = new EmailService();

    public void notifyUser(String message) {
        emailService.send(message);
    }
}
```

Dependency:

```text
NotificationService
        │
        ▼
EmailService
```

`NotificationService` creates and owns the `EmailService` object.

---

# 3. Why is This Tight Coupling?

Because `NotificationService` knows:

* Which implementation to use
* How to create it
* When to create it

It is responsible for both **business logic** and **dependency management**.

---

# 4. Problems with Tight Coupling

## Problem 1: Difficult to Replace Implementations

Suppose the application should now send SMS instead of email.

New class:

```java
public class SmsService {

    public void send(String message) {
        System.out.println("SMS sent: " + message);
    }
}
```

Now `NotificationService` must be modified.

```java
private SmsService smsService = new SmsService();
```

Changing one component forces changes in another.

---

## Problem 2: Difficult to Test

Suppose you want to test only `NotificationService`.

It automatically creates:

```java
new EmailService()
```

You cannot easily substitute:

* Mock EmailService
* Fake EmailService
* Stub EmailService

Testing becomes more complicated because the dependency is fixed.

---

## Problem 3: Poor Reusability

`NotificationService` can work only with `EmailService`.

It cannot reuse:

* SmsService
* PushNotificationService
* WhatsAppService

without changing its code.

---

## Problem 4: Difficult Maintenance

Imagine:

```text
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Database
```

If every class manually creates the next dependency, a change in one layer may require changes across multiple classes.

---

## Problem 5: Violates the Dependency Inversion Principle (DIP)

Instead of depending on an abstraction:

```text
NotificationService
        │
        ▼
NotificationSender
```

the class depends directly on a concrete implementation:

```text
NotificationService
        │
        ▼
EmailService
```

This reduces flexibility.

---

# 5. Real-World Analogy

Imagine a TV that only works with one specific remote.

```text
TV
 │
 ▼
Samsung Remote
```

If the remote breaks, you must buy the same model.

A better design is:

```text
TV
 │
 ▼
Any Compatible Remote
```

The TV depends on a standard interface rather than one specific remote.

Similarly, software components should depend on **interfaces (abstractions)** instead of concrete implementations.

---

# 6. Characteristics of Tight Coupling

* Direct dependency on concrete classes
* Manual object creation (`new`)
* Difficult to replace implementations
* Difficult to test
* Difficult to maintain
* Low flexibility
* Low reusability

---

# 7. Better Design: Loose Coupling

Instead of:

```java
EmailService emailService = new EmailService();
```

depend on an interface:

```java
NotificationSender sender;
```

Possible implementations:

```text
NotificationSender
       │
 ┌─────┼──────────┐
 ▼     ▼          ▼
Email  SMS   Push Notification
```

The application can choose the implementation without changing the business logic.

---

# 8. How Spring Solves Tight Coupling

Instead of the class creating its dependency:

```text
NotificationService
        │
        ▼
Creates EmailService
```

Spring creates and injects the dependency:

```text
Spring Container
        │
        ▼
Creates EmailService
        │
        ▼
Injects into
NotificationService
```

The class no longer manages object creation; it only declares what it needs.

---

# 9. Before vs After Spring

### Traditional Java

```text
NotificationService
        │
Creates
        ▼
EmailService
```

### With Spring

```text
NotificationService
        ▲
Receives
        │
Spring Container
        │
Creates
        ▼
EmailService
```

Object creation is separated from business logic.

---

# 10. Key Takeaways

* **Coupling** measures how strongly software components depend on one another.
* **Tight coupling** occurs when a class depends directly on a concrete implementation and creates its own dependencies.
* Tight coupling reduces flexibility, reusability, maintainability, and testability because changes to one component often require changes to others.
* A better design is **loose coupling**, where classes depend on **abstractions (interfaces)** rather than concrete implementations.
* Spring achieves loose coupling through **Inversion of Control (IoC)** and **Dependency Injection (DI)**, allowing the framework to create and inject dependencies instead of having application classes instantiate them directly.

---

# Knowledge Dependency

```text
Traditional Java
        │
        ▼
Manual Object Creation
        │
        ▼
Tight Coupling
        │
        ▼
Maintenance & Testing Problems
        │
        ▼
Need for Loose Coupling
        │
        ▼
Inversion of Control (IoC)
        │
        ▼
Dependency Injection (DI)
        │
        ▼
Spring Framework
```

This topic establishes **why** Spring's IoC container and Dependency Injection exist. The next logical step is to study **Inversion of Control (IoC)**, which shifts object creation and dependency management from the application to the Spring container.

---

## Topic: High Dependency

## Learning Objective

Understand what **high dependency** means in software design, why it becomes a problem as applications grow, and how it motivates the need for **Inversion of Control (IoC)** and **Dependency Injection (DI)**.

> **Important distinction:** Although closely related, **high dependency** and **tight coupling** are not the same. Tight coupling describes **how** components depend on each other, while high dependency describes **how many** dependencies a component has or how extensively it relies on other components.

---

# 1. What is a Dependency?

## Definition

A **dependency** is any object, service, library, or resource that another class requires to perform its work.

Example:

```java
public class UserService {

    private UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

Here, `UserService` depends on `UserRepository`.

Dependency relationship:

```text
UserService
      │
      ▼
UserRepository
```

---

# 2. What is High Dependency?

## Definition

**High dependency** occurs when a class relies on **many other components** to perform its responsibilities.

Example:

```text
OrderService
    │
    ├── UserRepository
    ├── ProductRepository
    ├── PaymentService
    ├── InventoryService
    ├── EmailService
    ├── Logger
    ├── CacheManager
    └── Configuration
```

The more dependencies a class has, the more complex it becomes to construct, understand, test, and maintain.

---

# 3. Why Does High Dependency Occur?

As applications evolve, new features often require additional services.

For example:

### Initial version

```text
UserService
     │
     ▼
UserRepository
```

### After adding authentication

```text
UserService
     ├── UserRepository
     └── AuthenticationService
```

### After adding notifications

```text
UserService
     ├── UserRepository
     ├── AuthenticationService
     └── EmailService
```

### After adding logging, caching, and auditing

```text
UserService
     ├── UserRepository
     ├── AuthenticationService
     ├── EmailService
     ├── CacheManager
     ├── Logger
     └── AuditService
```

The dependency graph grows with the application's functionality.

---

# 4. Problems with High Dependency

## Problem 1: Complex Object Creation

Without Spring:

```java
Database db = new Database();
UserRepository repo = new UserRepository(db);
EmailService email = new EmailService();
Logger logger = new Logger();
CacheManager cache = new CacheManager();

UserService service =
    new UserService(repo, email, logger, cache);
```

The application must manually construct every required object.

---

## Problem 2: Difficult Maintenance

When one dependency changes:

```text
Database
    │
    ▼
UserRepository
    │
    ▼
UserService
```

Changes may propagate through several layers of the application.

---

## Problem 3: Difficult Testing

To test one class, you often need to create or mock all of its dependencies.

Example:

```text
UserService Test
       │
       ├── Mock Repository
       ├── Mock Email Service
       ├── Mock Cache
       ├── Mock Logger
       └── Mock Audit Service
```

The more dependencies, the more test setup is required.

---

## Problem 4: Reduced Readability

A constructor with many parameters can indicate excessive responsibilities.

```java
public UserService(
    UserRepository repository,
    EmailService emailService,
    SmsService smsService,
    CacheManager cacheManager,
    Logger logger,
    AuditService auditService,
    NotificationService notificationService) {
}
```

Such a class is harder to understand and maintain.

---

## Problem 5: Increased Change Impact

If many components depend on a particular service:

```text
InventoryService
      ▲
 ┌────┼─────┐
 │    │     │
 ▼    ▼     ▼
Order Payment Shipping
```

A change in `InventoryService` may affect multiple parts of the system.

---

# 5. High Dependency vs Tight Coupling

| High Dependency                                      | Tight Coupling                                         |
| ---------------------------------------------------- | ------------------------------------------------------ |
| Focuses on the **number and extent** of dependencies | Focuses on the **strength and nature** of dependencies |
| A class depends on many components                   | A class depends directly on specific implementations   |
| Can exist even with interfaces                       | Usually results from depending on concrete classes     |
| Increases construction and management complexity     | Reduces flexibility and replaceability                 |

A class may have **many dependencies** but still be **loosely coupled** if those dependencies are abstractions rather than concrete implementations.

---

# 6. Real-World Analogy

Imagine opening a restaurant.

To operate successfully, you depend on:

* Suppliers
* Electricity
* Water
* Staff
* Delivery partners
* Payment systems
* Internet
* Accounting software

These are **dependencies**.

As the business grows, managing all of them manually becomes increasingly difficult.

Similarly, software systems accumulate dependencies as they grow.

---

# 7. How Spring Helps

Without Spring:

```text
Application
     │
Creates Every Object
```

With Spring:

```text
Application
      │
Declares Dependencies
      │
      ▼
Spring Container
      │
Creates Objects
      │
Injects Dependencies
```

The application focuses on business logic, while Spring manages object creation and wiring.

---

# 8. Key Takeaways

* A **dependency** is any component that another component requires to perform its work.
* **High dependency** means a class relies on many other components, increasing construction, testing, and maintenance complexity.
* High dependency is a natural consequence of growing applications, but unmanaged dependencies can make systems difficult to maintain.
* **High dependency** is different from **tight coupling**: the former concerns the quantity of dependencies, while the latter concerns how those dependencies are connected.
* Spring's **IoC Container** and **Dependency Injection** centralize dependency creation and management, reducing manual wiring and improving maintainability.

---

# Knowledge Dependency

```text
Growing Application
        │
        ▼
More Features
        │
        ▼
More Dependencies
        │
        ▼
High Dependency
        │
        ▼
Complex Object Management
        │
        ▼
Need for IoC Container
        │
        ▼
Dependency Injection (DI)
        │
        ▼
Spring Framework
```

## Relationship to Previous Topic

```text
Traditional Java Applications
           │
           ├── Tight Coupling
           │       │
           │       ▼
           │  Low Flexibility
           │
           └── High Dependency
                   │
                   ▼
        Complex Object Management
                   │
                   ▼
         Spring IoC + Dependency Injection
```

**Tight coupling** explains **how components are connected**, while **high dependency** explains **how many relationships a component must manage**. Spring addresses both by centralizing object creation and dependency management in the IoC container.

---

## Topic: Difficult Testing

## Learning Objective

Understand why **traditional Java applications are often difficult to test**, how manual dependency management contributes to this problem, and how Spring's **Dependency Injection (DI)** improves testability.

> **Claim:** One of Spring's major advantages is not that it performs testing, but that it **makes applications easier to test** by reducing unnecessary dependencies.
>
> **Caveat:** Good software design is the primary factor in testability. Spring facilitates testing but does not automatically make poorly designed code testable.

---

# 1. What is Software Testing?

## Definition

**Software testing** is the process of verifying that a program behaves as expected under various conditions.

Testing helps answer questions such as:

* Does the code work correctly?
* Does it handle errors properly?
* Does a change introduce new bugs?

---

## Example

Suppose:

```text
UserService
      │
      ▼
UserRepository
```

A unit test for `UserService` should focus only on the business logic in `UserService`, not on the database.

---

# 2. Why is Testing Difficult in Traditional Java?

In traditional Java applications, classes often create their own dependencies.

Example:

```java
public class UserService {

    private UserRepository repository = new UserRepository();

}
```

Dependency:

```text
UserService
      │
Creates
      ▼
UserRepository
```

Because `UserService` creates `UserRepository` itself, the dependency cannot be easily replaced during testing.

---

# 3. Problems Caused by Manual Dependencies

## Problem 1: Cannot Isolate the Class

When testing `UserService`, the real `UserRepository` is also used.

This means the test depends on:

* Database
* Database connection
* Database configuration

instead of only the business logic.

---

## Problem 2: Slow Tests

Suppose:

```text
UserService
      │
      ▼
Database
```

Every test may require:

* Opening a database connection
* Executing SQL
* Closing the connection

This increases execution time.

---

## Problem 3: Unstable Tests

External resources may fail.

Example:

```text
Database Offline
        │
        ▼
Test Fails
```

The failure may be due to infrastructure rather than incorrect business logic.

---

## Problem 4: Difficult Test Setup

Imagine:

```text
OrderService
     │
     ├── UserRepository
     ├── ProductRepository
     ├── PaymentService
     ├── EmailService
     └── InventoryService
```

Before testing one method, every dependency must be created or configured.

This increases the complexity of test preparation.

---

## Problem 5: Difficult to Simulate Scenarios

Suppose you want to test:

* Database failure
* Payment failure
* Network timeout

If the class always creates the real dependency, these situations are difficult to simulate consistently.

---

# 4. Example

## Traditional Java

```java
public class NotificationService {

    private EmailService emailService = new EmailService();

}
```

Testing:

```text
NotificationService
        │
Uses
        ▼
Real Email Service
```

A test may send real emails or require additional configuration.

---

# 5. Better Design

Instead of creating dependencies internally:

```text
NotificationService
        │
        ▼
EmailService
```

Use an abstraction:

```text
NotificationService
        │
        ▼
NotificationSender
```

Possible implementations:

```text
NotificationSender
       │
 ┌─────┼──────────┐
 ▼     ▼          ▼
Email  SMS     Mock Sender
```

During testing, the mock implementation can replace the real one.

---

# 6. How Spring Improves Testability

With Dependency Injection:

```text
Spring Container
        │
Creates Dependency
        │
        ▼
Injects into
UserService
```

For production:

```text
UserService
      │
      ▼
Real Repository
```

For testing:

```text
UserService
      │
      ▼
Mock Repository
```

The business logic remains unchanged.

---

# 7. Unit Testing vs Integration Testing

## Unit Test

Tests one class in isolation.

```text
UserService
```

Dependencies are usually mocked or stubbed.

---

## Integration Test

Tests interactions between components.

```text
UserService
      │
      ▼
UserRepository
      │
      ▼
Database
```

Real dependencies are typically used.

---

# 8. Traditional Java vs Spring

### Traditional Java

```text
UserService
      │
Creates
      ▼
Repository
```

Testing:

```text
Must Use
Real Repository
```

---

### Spring

```text
Spring Container
      │
Injects
      ▼
Repository
      │
      ▼
UserService
```

Testing:

```text
Inject Mock Repository
```

The class being tested does not need to change.

---

# 9. Key Takeaways

* **Testing** verifies that software behaves as expected.
* Traditional Java applications are harder to test because classes often create and manage their own dependencies.
* Manually created dependencies make tests slower, harder to isolate, and more dependent on external systems.
* Depending on **interfaces (abstractions)** instead of concrete implementations improves testability.
* Spring's **Dependency Injection** allows production code to use real implementations and test code to use mock or stub implementations without modifying the business logic.

---

# Knowledge Dependency

```text
Traditional Java
        │
        ▼
Manual Object Creation
        │
        ▼
Hard-Coded Dependencies
        │
        ▼
Difficult Testing
        │
        ▼
Need for Loose Coupling
        │
        ▼
Dependency Injection (DI)
        │
        ▼
Spring Framework
```

## Relationship to Previous Topics

```text
Traditional Java Applications
            │
            ├── Tight Coupling
            ├── High Dependency
            └── Difficult Testing
                    │
                    ▼
        Need for Better Object Management
                    │
                    ▼
          Inversion of Control (IoC)
                    │
                    ▼
          Dependency Injection (DI)
                    │
                    ▼
            Spring Framework
```

The previous topics established that traditional Java applications suffer from **tight coupling** and **high dependency**. **Difficult testing** is a practical consequence of those design choices. By separating object creation from business logic through IoC and DI, Spring enables components to be tested independently with mock or fake dependencies, leading to faster, more reliable, and more maintainable tests.

---

## Topic: Manual Object Creation

## Learning Objective

Understand what **manual object creation** is, why it becomes problematic as applications grow, and how it motivates the use of **Inversion of Control (IoC)** and **Dependency Injection (DI)** in Spring.

> **Claim:** Manual object creation is one of the primary reasons traditional Java applications become difficult to maintain and scale.
>
> **Caveat:** Manually creating objects is perfectly acceptable for small programs or simple utilities. The problem emerges as applications become larger and object relationships become more complex.

---

# 1. What is an Object?

## Definition

An **object** is an instance of a class that contains state (data) and behavior (methods).

Example:

```java
UserService service = new UserService();
```

Here:

* `UserService` → Class
* `service` → Object

---

# 2. What is Object Creation?

Object creation is the process of creating an instance of a class.

In Java, this is typically done using the `new` keyword.

Example:

```java
UserRepository repository = new UserRepository();
```

Every time `new` is executed, a new object is created in memory.

---

# 3. What is Manual Object Creation?

## Definition

**Manual object creation** means the **application developer explicitly creates and connects objects using the `new` keyword.**

Example:

```java
UserRepository repository = new UserRepository();
UserService service = new UserService(repository);
UserController controller = new UserController(service);
```

Dependency graph:

```text
Application
     │
Creates
     ▼
UserRepository
     │
Creates
     ▼
UserService
     │
Creates
     ▼
UserController
```

The application is responsible for creating every object and connecting them together.

---

# 4. Why Does Manual Object Creation Become a Problem?

Consider a simple application.

```text
Controller
     │
     ▼
Service
     │
     ▼
Repository
```

Creating three objects manually is manageable.

Now consider a larger application.

```text
OrderController
       │
       ▼
OrderService
       │
       ├── UserRepository
       ├── ProductRepository
       ├── InventoryService
       ├── PaymentService
       ├── EmailService
       ├── Logger
       ├── CacheManager
       └── Configuration
```

Every dependency must be created manually.

The complexity increases rapidly.

---

# 5. Problems with Manual Object Creation

## Problem 1: Boilerplate Code

Large portions of code are dedicated to creating objects instead of implementing business logic.

Example:

```java
Database database = new Database();
UserRepository repository = new UserRepository(database);
UserService service = new UserService(repository);
UserController controller = new UserController(service);
```

The code is repetitive and adds little business value.

---

## Problem 2: Tight Coupling

When a class creates its own dependency:

```java
private UserRepository repository = new UserRepository();
```

it becomes directly coupled to that implementation.

Changing the implementation requires modifying the class.

---

## Problem 3: Complex Dependency Management

Imagine:

```text
Application
     │
     ├── Service A
     ├── Service B
     ├── Service C
     ├── Repository A
     ├── Repository B
     ├── Repository C
     └── Database
```

The application must know:

* Which object to create
* When to create it
* In what order to create it
* Which dependencies to pass

This complexity grows as the application grows.

---

## Problem 4: Difficult Testing

When dependencies are created internally:

```java
private UserRepository repository = new UserRepository();
```

they cannot be easily replaced during testing.

Using mock implementations becomes difficult.

---

## Problem 5: Difficult Maintenance

Suppose:

```text
UserService
      │
      ▼
UserRepository
```

Later:

```text
UserService
      │
      ▼
CachedUserRepository
```

Every location that manually creates `UserRepository` must be updated.

---

## Problem 6: Violates the Single Responsibility Principle (SRP)

A class should focus on its business responsibility.

Example:

```text
UserService
```

Business responsibility:

* Register users
* Validate users

Infrastructure responsibility:

* Create repositories
* Manage dependencies

Manual object creation mixes these responsibilities.

---

# 6. Real-World Analogy

Imagine building a house.

Without a contractor:

```text
Homeowner
    │
    ├── Hire plumber
    ├── Hire electrician
    ├── Hire carpenter
    ├── Hire painter
    └── Coordinate everyone
```

The homeowner manages every dependency.

With a contractor:

```text
Homeowner
      │
      ▼
Contractor
      │
      ├── Plumber
      ├── Electrician
      ├── Carpenter
      └── Painter
```

The homeowner focuses on the goal, while the contractor manages the coordination.

Similarly, Spring's IoC container acts as the "contractor" for application objects.

---

# 7. How Spring Solves Manual Object Creation

Instead of:

```text
Application
      │
Creates Objects
```

Spring manages object creation.

```text
Application
      │
Requests Objects
      │
      ▼
Spring IoC Container
      │
Creates Objects
      │
Injects Dependencies
```

The application declares what it needs, and Spring supplies it.

---

# 8. Before vs After Spring

### Traditional Java

```text
Application
     │
Creates Repository
     │
Creates Service
     │
Creates Controller
```

---

### Spring

```text
Spring Container
       │
Creates Repository
       │
Creates Service
       │
Creates Controller
       │
Injects Dependencies
```

Business classes no longer create their own dependencies.

---

# 9. Relationship to IoC

Manual object creation:

```text
Application
      │
Creates Objects
```

IoC:

```text
Spring Container
      │
Creates Objects
```

The responsibility shifts from the application to the framework.

This is the essence of **Inversion of Control**.

---

# 10. Key Takeaways

* **Object creation** is the process of instantiating classes.
* **Manual object creation** means developers explicitly create and connect objects using the `new` keyword.
* While acceptable for small applications, manual object creation becomes increasingly difficult to manage as applications grow.
* It leads to boilerplate code, tighter coupling, harder testing, more complex dependency management, and mixed responsibilities.
* Spring's **IoC Container** centralizes object creation and **Dependency Injection** supplies those objects where needed, allowing application classes to focus on business logic rather than infrastructure.

---

# Knowledge Dependency

```text
Traditional Java
        │
        ▼
Manual Object Creation
        │
        ▼
Boilerplate Code
        │
        ▼
Complex Dependency Management
        │
        ▼
Tight Coupling
        │
        ▼
Difficult Testing
        │
        ▼
Need for IoC
        │
        ▼
Dependency Injection (DI)
        │
        ▼
Spring Framework
```

## Relationship to Previous Topics

```text
Traditional Java Applications
            │
            ├── Manual Object Creation
            │          │
            │          ▼
            │    Tight Coupling
            │          │
            │          ▼
            │    Difficult Testing
            │
            └── High Dependency
                       │
                       ▼
           Need for Centralized Object Management
                       │
                       ▼
            Inversion of Control (IoC)
                       │
                       ▼
            Dependency Injection (DI)
                       │
                       ▼
                Spring Framework
```

**Manual object creation** is the foundational problem discussed in Module 1. It directly contributes to **tight coupling**, **high dependency**, and **difficult testing**. Spring addresses these challenges by introducing an **IoC Container** that creates, configures, and injects objects automatically, allowing developers to concentrate on business logic rather than object management.

---

## Topic: Boilerplate Configuration

## Learning Objective

Understand what **boilerplate configuration** is, why traditional Java applications require a large amount of repetitive configuration, and how Spring reduces this burden through **convention, dependency injection, and centralized configuration**.

> **Claim:** One of Spring's major goals is to reduce the amount of repetitive infrastructure code and configuration that developers must write.
>
> **Caveat:** Some configuration is always necessary. Spring reduces unnecessary configuration but does not eliminate configuration altogether.

---

# 1. What is Configuration?

## Definition

**Configuration** is the process of defining **how an application should be assembled, initialized, and connected**.

Configuration includes:

* Creating objects
* Connecting dependencies
* Database settings
* Security settings
* External properties
* Resource initialization

Configuration tells the application **how** to run, rather than **what** business problem to solve.

---

# 2. What is Boilerplate?

## Definition

**Boilerplate** is repetitive, predictable code or configuration that is necessary for the program to work but contributes little or no business value.

Examples include:

* Repeated object creation
* Repeated configuration files
* Repeated initialization code
* Repeated dependency wiring

---

# 3. What is Boilerplate Configuration?

## Definition

**Boilerplate configuration** is the repetitive setup required to configure an application before the actual business logic can execute.

Instead of writing business features, developers spend time writing infrastructure code.

---

# 4. Example Without Spring

Suppose an application has:

```text id="htaz9n"
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Database
```

The developer manually writes:

```java id="y88uxl"
Database database = new Database();

UserRepository repository =
        new UserRepository(database);

UserService service =
        new UserService(repository);

UserController controller =
        new UserController(service);
```

Most of this code is not business logic—it is configuration.

---

# 5. Why is Boilerplate Configuration a Problem?

## Problem 1: Repetitive Code

Many applications repeatedly perform tasks such as:

* Creating objects
* Connecting objects
* Initializing resources

This repetition increases code volume without adding new functionality.

---

## Problem 2: Difficult Maintenance

Suppose:

```text id="jlwm8m"
UserRepository
```

changes to

```text id="0xmhdo"
CachedUserRepository
```

Every location that creates `UserRepository` must be updated manually.

---

## Problem 3: Error-Prone

Developers may accidentally:

* Forget to initialize an object
* Pass incorrect dependencies
* Create objects in the wrong order
* Duplicate configuration

Manual configuration increases the likelihood of mistakes.

---

## Problem 4: Configuration Scattered Across the Application

Without a centralized approach, configuration may be spread across:

* Java classes
* XML files
* Properties files
* Environment variables

This makes the application harder to understand and maintain.

---

## Problem 5: Reduced Productivity

Developers spend time writing infrastructure code instead of implementing business features.

For example:

```text id="k56ts0"
Time Spent
│
├── Business Logic
└── Configuration
```

The larger the application, the greater the configuration effort.

---

# 6. Real-World Analogy

Imagine buying a new computer.

Without automation:

* Install the operating system
* Install drivers
* Configure networking
* Install software
* Configure security
* Configure user accounts

Every computer requires nearly the same setup.

Modern operating systems automate most of these repetitive tasks.

Similarly, Spring automates much of an application's repetitive configuration.

---

# 7. How Spring Reduces Boilerplate Configuration

Instead of manually creating and connecting every object:

```text id="ksjlwm"
Application
      │
Creates Everything
```

Spring uses:

* IoC Container
* Dependency Injection
* Component Scanning
* Auto-configuration (via Spring Boot)

Result:

```text id="yjlwm"
Application
      │
Declares Components
      │
      ▼
Spring Container
      │
Creates Objects
      │
Connects Objects
      │
Manages Lifecycle
```

Developers describe **what** they need, and Spring handles **how** it is assembled.

---

# 8. Traditional Java vs Spring

### Traditional Java

```text id="hpdwpf"
Developer
      │
Creates Objects
      │
Connects Objects
      │
Configures Objects
      │
Maintains Configuration
```

---

### Spring Framework

```text id="0vrz5j"
Developer
      │
Declares Components
      │
      ▼
Spring Container
      │
Creates Beans
      │
Injects Dependencies
      │
Manages Configuration
```

---

# 9. Spring Boot Goes Further

Spring Framework reduces configuration.

Spring Boot reduces it even more.

Traditional Spring:

```text id="eqbdsi"
Developer
      │
Configuration
      │
      ▼
Application
```

Spring Boot:

```text id="y5ck1i"
Convention
      │
      ▼
Auto Configuration
      │
      ▼
Application
```

Spring Boot follows the principle:

> **Convention over Configuration**

Meaning sensible defaults are provided, so developers write configuration only when customization is needed.

---

# 10. Key Takeaways

* **Configuration** defines how an application is assembled and initialized.
* **Boilerplate** refers to repetitive infrastructure code or configuration that adds little business value.
* **Boilerplate configuration** is the repeated setup required to create objects, connect dependencies, and initialize application components.
* Excessive boilerplate makes applications more difficult to maintain, more error-prone, and less productive to develop.
* Spring reduces boilerplate through **IoC**, **Dependency Injection**, centralized configuration, and component scanning. Spring Boot further reduces configuration through **auto-configuration** and **convention over configuration**.

---

# Knowledge Dependency

```text id="k6p4ki"
Traditional Java
        │
        ▼
Manual Configuration
        │
        ▼
Boilerplate Configuration
        │
        ▼
Large Amount of Repetitive Code
        │
        ▼
Maintenance Problems
        │
        ▼
Need for IoC Container
        │
        ▼
Dependency Injection (DI)
        │
        ▼
Spring Framework
        │
        ▼
Spring Boot Auto-Configuration
```

## Relationship to Previous Topics

```text id="d4mtvx"
Traditional Java Applications
            │
            ├── Manual Object Creation
            ├── Tight Coupling
            ├── High Dependency
            ├── Difficult Testing
            └── Boilerplate Configuration
                     │
                     ▼
       Excessive Infrastructure Code
                     │
                     ▼
      Need for Centralized Management
                     │
                     ▼
       Inversion of Control (IoC)
                     │
                     ▼
      Dependency Injection (DI)
                     │
                     ▼
           Spring Framework
                     │
                     ▼
      Spring Boot (Convention over Configuration)
```

Boilerplate configuration is the cumulative effect of the previous problems. When developers manually create objects, manage dependencies, and wire components together, repetitive configuration grows rapidly. Spring addresses this by centralizing configuration in the **IoC Container**, while Spring Boot builds on that foundation with **auto-configuration**, allowing developers to focus primarily on business logic.

---
# Open Questions
1. What is connection between loose coupling, association, and abstraction? 
**Claim:** **Abstraction, association, and loose coupling are related but operate at different levels of software design.** They work together to reduce dependency and make systems easier to understand, change, and test.

The relationship can be understood as:

```text
Abstraction
      ↓
Defines "what" is exposed
      ↓
Association
      ↓
Defines "who knows whom"
      ↓
Loose Coupling
      ↓
Defines "how dependent they are"
```

Think of them as answering three different questions:

| Concept        | Fundamental Question                       | Focus                         |
| -------------- | ------------------------------------------ | ----------------------------- |
| Abstraction    | What should be visible?                    | Hide implementation details   |
| Association    | Which objects should be connected?         | Relationships between objects |
| Loose Coupling | How much should they depend on each other? | Minimize dependencies         |

---

## 1. Abstraction: Hide unnecessary details

Abstraction exposes only the essential behavior while hiding implementation.

Example:

```java
interface PaymentService {
    void pay(double amount);
}
```

A customer only knows:

> "I can call `pay()`."

It does **not** know:

* Stripe
* Razorpay
* PayPal
* Database
* HTTP calls

The implementation is hidden.

```text
Customer
    │
    ▼
PaymentService
    │
    ▼
Implementation Hidden
```

Abstraction reduces the amount of knowledge required.

---

## 2. Association: Objects are connected

Association simply means **one object knows about another object**.

Example:

```java
class OrderService {

    private PaymentService paymentService;

}
```

Here,

```
OrderService --------> PaymentService
```

This arrow is an **association**.

Association says nothing about coupling.

It only says:

> "These two objects communicate."

---

## 3. Loose Coupling: Minimize dependency

Loose coupling asks:

> "How strongly are these objects connected?"

Consider:

```java
class OrderService {

    private RazorpayService payment;

}
```

Now the dependency is:

```
OrderService
      │
      ▼
RazorpayService
```

If Razorpay changes,

```
OrderService
        ↓
Must change
```

This is **tight coupling**.

Instead:

```java
class OrderService {

    private PaymentService payment;

}
```

Now:

```
OrderService
        │
        ▼
PaymentService
        │
 ┌──────┴─────────┐
 ▼                ▼
Stripe       Razorpay
```

The dependency is weaker.

---

# How abstraction creates loose coupling

Without abstraction

```
OrderService
      │
      ▼
StripePaymentService
```

Dependency is concrete.

With abstraction

```
OrderService
      │
      ▼
PaymentService
      │
      ▼
StripePaymentService
```

Now the service depends on a contract rather than an implementation.

This is the foundation of **Dependency Inversion**.

---

# How association fits in

Association always exists if objects collaborate.

Example:

```
Student ------ Course
```

This is an association.

It can be

* tightly coupled
* loosely coupled

depending on **how** the association is implemented.

Example:

### Tight association

```java
class OrderService {

    private StripePaymentService payment;

}
```

Association exists.

Coupling is high.

---

### Loose association

```java
class OrderService {

    private PaymentService payment;

}
```

Association still exists.

Coupling is low.

The association did not change.

Only the dependency changed.

---

# Complete relationship

```text
              Abstraction
                    │
       hides implementation
                    │
                    ▼
        Objects communicate
          through interfaces
                    │
                    ▼
             Association
                    │
      defines relationship
                    │
                    ▼
           Loose Coupling
                    │
       reduces dependency
                    │
                    ▼
 Easier maintenance and testing
```

---

# Real-world analogy

Imagine hiring a taxi.

### Abstraction

You know only:

> "I need a ride."

You do not care about:

* engine type
* transmission
* fuel injection

---

### Association

You have a relationship with the taxi driver.

```
Passenger ---- Driver
```

That is the association.

---

### Loose coupling

Tomorrow you can use:

* Uber
* Ola
* Local taxi

without changing your travel plan.

```
Passenger
      │
      ▼
Ride Service
      │
 ┌────┴─────┐
 ▼          ▼
Uber       Ola
```

The passenger depends on the **service**, not a specific driver or company.

---

# Spring Framework connection

Spring's Inversion of Control (IoC) container uses all three concepts together:

```text
Application
      │
      ▼
Interfaces (Abstraction)
      │
      ▼
Dependency Injection
      │
      ▼
Association created by Spring
      │
      ▼
Loose Coupling
```

Instead of creating objects manually,

```java
PaymentService payment = new StripePaymentService();
```

Spring injects an implementation:

```java
@Autowired
private PaymentService payment;
```

The class is associated with `PaymentService`, while the framework supplies the concrete implementation. This keeps the association intact but makes the dependency loosely coupled.

---

## 80/20 takeaway

```text
Abstraction
    ↓
Hide implementation and expose only the contract.

Association
    ↓
Represent the relationship between collaborating objects.

Loose Coupling
    ↓
Ensure those relationships depend on abstractions rather than concrete implementations.

Together:
Abstraction → enables loose coupling.
Association → provides communication between objects.
Loose coupling → makes those associations flexible, maintainable, and testable.
```

This progression—from **abstraction** to **association** to **loose coupling**—is the core design philosophy behind object-oriented design, the SOLID principles (especially the Dependency Inversion Principle), and frameworks like Spring that emphasize dependency injection.

---
2. What is the business logic? What is the difference between business reponsiblities and infrastructure responsibilities?
**Claim:** **Business logic** defines **what the system should do to satisfy business requirements**, whereas **infrastructure** defines **how the system technically performs those operations**. The distinction is based on **responsibility**, not complexity or code location. Business responsibilities express domain rules and decisions; infrastructure responsibilities provide technical capabilities that enable those rules to execute.

A useful mental model is:

```text
Business Requirement
        │
        ▼
Business Logic
(What decision should be made?)
        │
        ▼
Infrastructure
(How is the decision executed technically?)
```

For example, in an e-commerce application:

```text
Business Requirement:
"A customer can purchase a product if it is in stock."
```

This decomposes into:

```text
Business Responsibility
───────────────────────
✓ Verify product availability
✓ Calculate total price
✓ Apply discount rules
✓ Validate purchase eligibility
✓ Reserve inventory

Infrastructure Responsibility
─────────────────────────────
✓ Query the database
✓ Send HTTP requests
✓ Store order records
✓ Send email notifications
✓ Process payment gateway API
✓ Write logs
✓ Cache data
✓ Handle transactions
```

The key distinction is that **business responsibilities answer "What should happen?"**, while **infrastructure responsibilities answer "How is it accomplished technically?"**

### Business Logic

Business logic consists of **domain-specific rules, policies, constraints, calculations, and decisions** that would remain valid even if the underlying technology changed.

Examples:

* Calculate a 10% discount for premium customers.
* Reject orders when inventory is unavailable.
* Prevent withdrawal beyond the account balance.
* Allow only managers to approve refunds above ₹10,000.

Notice that none of these statements mention:

* MySQL
* Spring Boot
* REST APIs
* Docker
* Kafka

These technologies merely implement the rules; they do not define them.

---

### Infrastructure Responsibilities

Infrastructure provides **technical services** required to execute business logic but does not determine business decisions.

Typical responsibilities include:

* Database access
* File storage
* Network communication
* Authentication mechanisms
* Logging and monitoring
* Caching
* Messaging
* Transaction management
* External API integration

For example:

```text
Business:
"Save the order."

Infrastructure:
"Insert the order into MySQL using JPA."
```

The decision to save the order belongs to the business; the mechanism for persisting it belongs to the infrastructure.

---

### Comparison

| Aspect           | Business Responsibilities                   | Infrastructure Responsibilities                       |
| ---------------- | ------------------------------------------- | ----------------------------------------------------- |
| Primary question | **What should happen?**                     | **How is it technically done?**                       |
| Concern          | Domain behavior                             | Technical implementation                              |
| Defined by       | Business requirements                       | Technology stack                                      |
| Changes when     | Business policies change                    | Technology changes                                    |
| Examples         | Pricing, validation, eligibility, workflows | Database, HTTP, logging, messaging, security, caching |

---

### Why the Separation Matters

Separating these responsibilities follows the **Separation of Concerns** principle.

If business logic directly depends on infrastructure:

```text
Business Logic
       │
       ▼
MySQL
```

changing the database, messaging system, or framework may require modifying business rules.

Instead:

```text
Business Logic
       │
       ▼
Repository Interface
       │
       ▼
MySQL Repository
```

the business layer depends only on an abstraction. The infrastructure implements that abstraction, enabling **loose coupling**, easier testing, and greater maintainability.

---

### Spring Boot Example

```java
public class OrderService {

    private final OrderRepository repository;
    private final PaymentGateway gateway;

    public void placeOrder(Order order) {

        // Business responsibilities
        validateInventory(order);
        calculateTotal(order);
        applyDiscount(order);

        // Infrastructure responsibilities
        repository.save(order);
        gateway.processPayment(order);
    }
}
```

Here:

**Business responsibilities**

* Validate inventory
* Calculate total
* Apply discounts

**Infrastructure responsibilities**

* Persist data
* Communicate with the payment provider

The service coordinates both, but the responsibilities remain conceptually distinct.

---

### Caveat

Some operations combine business and infrastructure concerns. For example, "send an order confirmation email" is a **business requirement**, while **SMTP communication, email templates, retries, and network transmission** are infrastructure concerns. A well-designed system separates these by allowing the business layer to express the intent ("send confirmation") while the infrastructure layer handles the technical execution.

### 80/20 Takeaway

```text
Business Requirements
        │
        ▼
Business Logic
• Business rules
• Decisions
• Policies
• Validation
• Calculations
        │
        ▼
Infrastructure
• Database
• APIs
• Messaging
• Logging
• Security
• Caching
```

**In essence, business responsibilities define the system's behavior from the perspective of the business domain ("what" and "why"), whereas infrastructure responsibilities provide the technical mechanisms that realize that behavior ("how"). This separation is a foundational architectural principle underlying layered architecture, Clean Architecture, Domain-Driven Design, and Spring applications because it preserves loose coupling, improves testability, and isolates business knowledge from technology-specific concerns.**

**Claim: What is the business logic? What is the difference between business reponsiblities and infrastructure responsibilities?**
---
**Claim:** **Yes—but with an important qualification.** In application architecture, **the business logic should be the center of attention**, because it represents the purpose and value of the software. The rest of the system—frameworks, databases, messaging, logging, APIs, and other infrastructure—exists primarily to support and enable the business logic, not to define it.

The architecture can be visualized as concentric layers:

```text
                Business Requirements
                        │
                        ▼
              Business Logic (Core)
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
 Persistence       Communication    Security
 (Database)         (REST/Kafka)    (Auth)
        ▼               ▼               ▼
          Infrastructure & Frameworks
```

The business logic answers questions such as:

* What problem is the system solving?
* What rules must be enforced?
* What decisions should be made?

Infrastructure answers questions such as:

* Where is the data stored?
* How is the email sent?
* Which database is used?
* How is authentication implemented?

From the perspective of software architecture, the dependency should ideally flow **toward the business logic**, not away from it:

```text
Presentation
      │
      ▼
Application Layer
      │
      ▼
Business Logic
      ▲
      │
Infrastructure
```

Notice that **business logic should not depend directly on infrastructure**. Instead, infrastructure depends on abstractions defined by the business or application layer. This is the essence of **Dependency Inversion** and is why frameworks like Spring emphasize **Dependency Injection**.

For example:

```java
public class OrderService {

    private final OrderRepository repository;
}
```

`OrderService` knows only about the `OrderRepository` interface (an abstraction), not whether the implementation uses MySQL, PostgreSQL, MongoDB, or an in-memory database. The business remains focused on **"save the order"**, while the infrastructure decides **how** it is saved.

### Why make business logic the center?

If technology changes:

```text
MySQL  → PostgreSQL
REST   → gRPC
Kafka  → RabbitMQ
Spring → Quarkus
```

the business rules often remain unchanged:

```text
✓ Calculate discount
✓ Validate inventory
✓ Approve payment
✓ Create order
```

This stability makes the business layer the most valuable and enduring part of the application.

### Caveat

This principle applies primarily to **business applications** (e-commerce, banking, healthcare, ERP, CRM, etc.). Not all software is business-centric. For example:

* A compiler centers on language semantics.
* A game engine centers on rendering and physics.
* A database centers on storage and query execution.
* An operating system centers on resource management.

In these systems, the "core" is not business logic but the software's **domain logic**—the fundamental rules of the problem domain. Thus, a more general architectural principle is:

> **The center of attention should be the domain's core logic, while infrastructure should remain a supporting concern.**

### 80/20 Takeaway

```text
Core (Most Important)
─────────────────────
• Business rules
• Domain decisions
• Policies
• Workflows

Supporting Layers
─────────────────
• Database
• Framework
• HTTP
• Messaging
• Logging
• Security
• Caching
```

**In short, architecture should be organized around the problem the software solves, not the technologies used to solve it. In business applications, this means the business logic is the center of attention, and infrastructure exists to serve, isolate, and preserve that core.**

**Claim: So, Spring helps us to focus on core logic?**
**Claim:** **Yes. One of Spring's primary goals is to let developers focus on the application's core (business) logic by handling much of the technical infrastructure automatically.**

**Caveat:** Spring does **not** eliminate infrastructure responsibilities. Instead, it **manages and abstracts** many of them, allowing you to write less infrastructure code and concentrate on business behavior.

Without Spring, a significant portion of your code is devoted to technical concerns:

```text
Application
├── Business Logic
├── Object Creation
├── Dependency Management
├── Database Connection
├── Transaction Management
├── Security
├── Configuration
├── Logging
└── Error Handling
```

With Spring, many of these infrastructure concerns are provided by the framework:

```text
Application
├── Business Logic   ← Your primary focus
│
└── Spring Framework
    ├── Dependency Injection (IoC)
    ├── Bean Lifecycle
    ├── Transaction Management
    ├── Security
    ├── Configuration
    ├── Database Integration
    ├── REST Infrastructure
    └── AOP (Logging, Auditing, etc.)
```

Consider a simple service:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void placeOrder(Order order) {
        validate(order);
        calculateTotal(order);
        repository.save(order);
    }
}
```

Here:

* **Your responsibility (business logic):**

  * Validate the order.
  * Calculate the total.
  * Define when an order should be saved.

* **Spring's responsibility (infrastructure):**

  * Create `OrderRepository`.
  * Inject it into `OrderService`.
  * Manage object lifecycles.
  * Configure the database connection.
  * Manage transactions (if `@Transactional` is used).

Notice that you express **what** should happen, while Spring handles much of **how** the supporting infrastructure works.

This is why Spring embodies the principle of **Inversion of Control (IoC)**:

```text
Without Spring

Your Code
    │
    ▼
Creates and manages infrastructure


With Spring

Your Code
    │
    ▼
Declares business logic
    ▲
    │
Spring manages infrastructure
```

### Trade-off

Spring reduces boilerplate and infrastructure code, but it introduces its own concepts and abstractions (such as IoC, Dependency Injection, beans, and annotations). Therefore, there is an initial learning curve. However, for medium and large applications, the reduction in complexity and improved maintainability generally outweigh this cost.

### 80/20 Takeaway

```text
Business Logic
    ↓
"What should the system do?"

Spring
    ↓
"How can the infrastructure support it?"

Goal:
Spend most of your effort designing business rules,
not wiring objects or managing technical details.
```

This is why Spring is often described as an **application framework rather than merely a library**: it provides the infrastructure so that your code can remain centered on the application's core business logic.
---

3. What is the application framework? 
**Claim:** **An application framework is a reusable software platform that provides the common structure, infrastructure, and lifecycle of an application, allowing developers to focus primarily on implementing the application's domain or business logic rather than rebuilding technical foundations.**

**Caveat:** A framework is **not** the application itself. It does not solve your business problem; instead, it provides the environment and services in which your business logic executes.

At a high level:

```text
Business Requirements
        │
        ▼
Your Application
        │
        ▼
Application Framework
        │
        ▼
Operating System
```

Think of building a house.

* **Business logic** is the furniture and rooms designed for the owner's needs.
* **The framework** is the foundation, plumbing, electrical wiring, and structural support.
* **You customize the house**, but you don't rebuild the engineering every time.

Similarly, when building an e-commerce application:

```text
Your Code
─────────
✓ Order processing
✓ Inventory rules
✓ Discount calculation
✓ Payment validation

Framework
─────────
✓ Object creation
✓ Dependency injection
✓ HTTP request handling
✓ Database integration
✓ Security
✓ Configuration
✓ Transaction management
✓ Logging
```

The framework handles the repetitive technical work so you can focus on solving the business problem.

---

## Why is it called a "framework"?

The word **framework** literally means **a supporting structure or skeleton**.

An application framework provides the application's skeleton:

```text
            Application
        ┌─────────────────┐
        │  Business Logic │  ← You write
        ├─────────────────┤
        │  Framework      │  ← Reusable foundation
        └─────────────────┘
```

Instead of starting from an empty project, you start with a predefined structure.

---

## What does an application framework provide?

Most frameworks provide services such as:

| Responsibility         | Provided by Framework |
| ---------------------- | --------------------- |
| Project structure      | ✓                     |
| Object lifecycle       | ✓                     |
| Dependency management  | ✓                     |
| Request handling       | ✓                     |
| Configuration          | ✓                     |
| Security               | ✓                     |
| Transaction management | ✓                     |
| Database integration   | ✓                     |
| Error handling         | ✓                     |

These are **cross-cutting infrastructure concerns** that many applications need.

---

## Why is Spring called an application framework?

Spring provides infrastructure for building Java applications.

Without Spring:

```java
public static void main(String[] args) {
    OrderRepository repository = new MySqlOrderRepository();
    OrderService service = new OrderService(repository);
}
```

You manually create and connect every object.

With Spring:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

Spring automatically:

* Creates the objects (beans)
* Connects dependencies (Dependency Injection)
* Manages their lifecycle
* Configures the infrastructure

You focus on the business logic.

---

## Framework vs Library

This is the most important distinction.

| Library                            | Framework                                           |
| ---------------------------------- | --------------------------------------------------- |
| You call the library.              | The framework calls your code.                      |
| You control the program flow.      | The framework controls the program flow.            |
| Provides reusable functions.       | Provides the application's structure and lifecycle. |
| Example: Java Collections, Jackson | Spring Framework                                    |

This difference is known as **Inversion of Control (IoC)**.

```text
Library

Your Code
     │
     ▼
Library


Framework

Framework
     │
     ▼
Your Code
```

The framework decides **when** and **how** your code is executed.

---

## Relationship to business logic

The architecture can be viewed as layers:

```text
Business Requirements
        │
        ▼
Business Logic
        │
        ▼
Application Framework
        │
        ▼
Java Platform (JVM)
        │
        ▼
Operating System
```

The framework's purpose is to **support** the business logic, not replace it.

---

## 80/20 Takeaway

```text
Business Logic
    │
    ▼
"What should the application do?"

Application Framework
    │
    ▼
"How should the application run?"

Spring
    │
    ▼
Provides reusable infrastructure so you can focus on the application's core logic.
```

In software engineering terms, **an application framework is a reusable architectural foundation that manages an application's lifecycle and common infrastructure services while providing extension points where developers implement domain-specific or business-specific behavior.** This is why Spring is called an application framework: it supplies the infrastructure and execution model, allowing your code to remain focused on the application's core purpose rather than its technical plumbing.


---
3. what is the Convention over Configuration?

---

* What is the difference between business logic and infrastructure code? What is the difference between business logic and application logic? What is the difference between business logic and domain logic? What is the difference between business logic and presentation logic? What is the difference between business logic and service layer? What is the difference between business logic and application layer? What is the difference between business logic and data access layer? What is the difference between business logic and persistence layer? What is the difference between business logic and infrastructure layer? What is the difference between business logic and cross-cutting concerns? What is the difference between business logic and technical concerns? What is the difference between business logic and non-functional requirements? What is the difference between business logic and functional requirements?
* What is the difference between business logic and business rules? What is the difference between business logic and business processes? What is the difference between business logic and business workflows? What is the difference between business logic and business operations? What is the difference between business logic and business functions? What is the difference between business logic and business services? What is the difference between business logic and business components? What is the difference between business logic and business modules? What is the difference between business logic and business layers? What is the difference between business logic and business tiers? What is the difference between business logic and business architecture? What is the difference between business logic and enterprise architecture?