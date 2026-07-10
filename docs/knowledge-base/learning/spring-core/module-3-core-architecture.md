# Module 3: Core Architecture

## Topic: Container

### Learning Objective

Understand the role of the Spring Container in managing application objects (beans) and how it facilitates Dependency Injection and Inversion of Control.

---

## What is a Container?

A **container** is the core component of the Spring Framework that **creates, configures, manages, and destroys objects (called beans)** throughout the application's lifecycle.

Instead of developers manually creating objects using `new`, the Spring container takes responsibility for managing them. 

> **Definition:**
> A Spring container is the runtime environment that manages the lifecycle and dependencies of Spring beans.

---

## Why is a Container Needed?

In a traditional Java application:

* Developers manually create objects.
* Developers manually connect dependent objects.
* Developers manually manage object lifecycles.

This leads to:

* Tight coupling
* More boilerplate code
* Difficult testing
* Hard-to-maintain applications

The Spring container solves these problems by automating object management. 

---

## What Does the Container Do?

The Spring container is responsible for:

1. **Creating beans**
2. **Configuring beans**
3. **Injecting dependencies**
4. **Managing bean lifecycle**
5. **Providing beans when requested**

In short:

```text
Container
    │
    ├── Creates beans
    ├── Configures beans
    ├── Injects dependencies
    ├── Manages lifecycle
    └── Provides beans
```

---

## How Does the Container Know What to Manage?

The container reads **configuration metadata**, which tells it:

* Which classes should become beans
* How beans should be created
* How beans depend on each other
* Bean scopes
* Lifecycle information

Configuration metadata can be provided using:

* XML configuration
* Java configuration
* Annotations

---

## Where Does the Container Store Objects?

The container stores managed objects internally as **Spring Beans**.

```text
Spring Container
       │
       ├── UserService Bean
       ├── UserRepository Bean
       ├── EmailService Bean
       └── PaymentService Bean
```

Whenever the application needs one of these objects, it asks the container instead of creating it directly.

---

## Real-World Analogy

Imagine a **hotel**.

You don't build the room yourself.

Instead:

* The hotel prepares the room.
* Cleans it.
* Maintains it.
* Gives it to you when needed.

Similarly:

* You don't create objects.
* The Spring container creates, configures, maintains, and provides them.

---

## Traditional Java vs Spring Container

### Traditional Java

```text
Developer
      │
      ▼
new UserRepository()
      │
      ▼
new UserService(repository)
```

The developer is responsible for everything.

---

### Spring

```text
Developer
      │
      ▼
Spring Container
      │
      ├── Creates UserRepository
      ├── Creates UserService
      └── Injects UserRepository into UserService
```

The container manages everything.

---

## Relationship with Other Core Concepts

```text
Application
      │
      ▼
Spring Container
      │
      ▼
ApplicationContext
      │
      ▼
Beans
```

* **Container** is the engine that manages objects.
* **ApplicationContext** is the most commonly used implementation of the Spring container.
* **Beans** are the objects managed by the container. 

---

## Benefits of Using a Container

* Eliminates manual object creation
* Reduces coupling
* Enables Dependency Injection
* Simplifies testing
* Centralizes object management
* Improves maintainability
* Promotes reusable and modular code

---

## Key Takeaways (80/20)

* A **Spring container** manages application objects called **beans**.
* It **creates, configures, injects dependencies into, manages the lifecycle of, and provides beans**.
* It replaces manual object creation with automatic management.
* It is the foundation of **IoC (Inversion of Control)** and **Dependency Injection (DI)**.
* **ApplicationContext** is the most commonly used Spring container. 
---

**Claim: Injecting dependencies into beans means injecting one object into another object, correct?**
---
Yes, exactly.

More precisely:

> **Injecting dependencies into beans means providing one object (the dependency) to another object (the dependent bean) so that it can perform its responsibilities.**

For example:

```java
class UserRepository {
}

class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Here:

* `UserService` is a **bean**.
* `UserRepository` is another **bean**.
* `UserService` **depends on** `UserRepository`.
* The Spring container **injects the `UserRepository` object into the `UserService` object**.

Conceptually:

```text
UserRepository object
          │
          │ Injected by Spring
          ▼
UserService object
```

Or, from the container's perspective:

```text
Spring Container
      │
      ├── Creates UserRepository object
      ├── Creates UserService object
      └── Injects UserRepository into UserService
```

### Important distinction

The container **does not inject classes**; it injects **objects (bean instances)**.

So, a more accurate statement is:

> **Dependency Injection means injecting one bean (object) into another bean (object).**

This is the essence of Dependency Injection in Spring and forms the foundation of the IoC container.
---

## Topic: Bean

---

### Learning Objective

Understand what a Spring Bean is, how it is defined, and how the Spring container manages its lifecycle and dependencies.

---

## What is a Bean?

A **bean** is an **object that is created, configured, and managed by the Spring container**.

> **Definition:**
> A Spring bean is an object whose lifecycle is managed by the Spring container.

Not every object in your application is a bean. An object becomes a bean **only if the Spring container manages it**. 

---

## Why Do We Need Beans?

Without Spring:

* Developers manually create objects using `new`.
* Developers manually connect dependent objects.
* Developers manually manage object lifecycles.

With Spring:

* The container creates objects.
* The container injects dependencies.
* The container manages the object lifecycle.

Beans are the mechanism through which Spring manages application objects.

---

## What Makes an Object a Bean?

An object is a Spring bean if it is:

* Created by the Spring container
* Configured by the Spring container
* Managed by the Spring container

Creating an object with `new` alone does **not** make it a Spring bean.

---

## Example

### Traditional Java

```java
UserRepository repository = new UserRepository();
UserService service = new UserService(repository);
```

Here:

* `repository` is an object.
* `service` is an object.
* Neither is a Spring bean because Spring did not create or manage them.

---

### Spring

```text
Spring Container
       │
       ├── Creates UserRepository bean
       ├── Creates UserService bean
       └── Injects UserRepository into UserService
```

Here:

* `UserRepository` is a bean.
* `UserService` is a bean.
* Both are managed by the Spring container.

---

## Bean vs Object

| Object                                      | Bean                                      |
| ------------------------------------------- | ----------------------------------------- |
| Any instance of a class                     | An object managed by the Spring container |
| Created using `new` or by a framework       | Created by the Spring container           |
| Lifecycle managed by the developer          | Lifecycle managed by the Spring container |
| May not participate in Dependency Injection | Can participate in Dependency Injection   |

> **Key point:** Every Spring bean is an object, but not every object is a Spring bean.

---

## Relationship with the Container

The Spring container manages beans throughout their lifecycle.

```text
Application
      │
      ▼
Spring Container
      │
      ├── Bean A
      ├── Bean B
      ├── Bean C
      └── Bean D
```

The container:

* Creates beans
* Configures beans
* Injects dependencies
* Provides beans when requested
* Destroys beans when they are no longer needed

---

## Real-World Analogy

Imagine a **library**.

* A **book** represents a bean.
* The **library** represents the Spring container.

You don't create or manage the books yourself. The library:

* Stores them
* Organizes them
* Maintains them
* Gives you the required book when requested

Similarly, the Spring container manages beans and provides them to your application.

---

## Common Ways to Define Beans

Spring can create beans using:

* XML configuration
* Java configuration (`@Bean`)
* Component scanning (`@Component`, `@Service`, `@Repository`, `@Controller`)

These approaches are covered in later modules. 

---

## Relationship with Other Core Concepts

```text
Application
      │
      ▼
Spring Container
      │
      ▼
Beans
      │
      ▼
Dependencies
```

* The **container** manages **beans**.
* Beans can depend on other beans.
* The container injects those dependencies automatically.

---

## Key Takeaways (80/20)

* A **bean** is an **object managed by the Spring container**.
* The container **creates, configures, injects dependencies into, manages, and destroys** beans.
* An object created with `new` is **not** a Spring bean unless it is managed by the Spring container.
* **Every bean is an object, but not every object is a bean.**
* Beans are the fundamental building blocks of Spring applications.
---

## Topic: Context
---

### Learning Objective

Understand the role of the Spring ApplicationContext in managing beans and how it provides a central point for accessing application components.

---

## What is Context?

In Spring, a **context** is the **runtime environment that holds and manages all the beans of an application**.

In practice, when people say "Spring Context," they usually mean the **ApplicationContext**, which is the most commonly used Spring container.

> **Definition:**
> The Spring Context (ApplicationContext) is the central container that stores, manages, and provides access to all Spring beans.

---

## Why Do We Need a Context?

An application may contain dozens or hundreds of beans.

The context provides a central place to:

* Store beans
* Manage bean lifecycles
* Inject dependencies
* Provide beans when requested
* Maintain application-wide configuration

Without a context, every object would need to be created and managed manually.

---

## What Does the Context Contain?

The Spring Context contains:

* Beans
* Bean definitions
* Configuration metadata
* Dependency relationships
* Bean lifecycle information

Conceptually:

```text
ApplicationContext
        │
        ├── UserService Bean
        ├── UserRepository Bean
        ├── EmailService Bean
        ├── PaymentService Bean
        └── Configuration Metadata
```

---

## How Does It Work?

When an application starts:

1. Spring creates the **ApplicationContext**.
2. The context reads the configuration metadata.
3. The context creates all required beans.
4. The context injects dependencies between beans.
5. The application requests beans from the context whenever needed.

```text
Application Starts
        │
        ▼
ApplicationContext Created
        │
        ▼
Read Configuration
        │
        ▼
Create Beans
        │
        ▼
Inject Dependencies
        │
        ▼
Application Uses Beans
```

---

## Context vs Container

These terms are closely related but not identical.

| Container                                                            | Context (ApplicationContext)                                                       |
| -------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| General concept that manages beans                                   | The most commonly used implementation of the Spring container                      |
| Creates and manages beans                                            | Creates, manages, and provides access to beans with additional enterprise features |
| Includes implementations like `BeanFactory` and `ApplicationContext` | A specific implementation built on top of `BeanFactory`                            |

> **Key point:** Every `ApplicationContext` is a Spring container, but not every Spring container is an `ApplicationContext`.

---

## Getting a Bean from the Context

Instead of creating an object yourself:

```java
UserService service = new UserService();
```

You ask the context for it:

```java
UserService service = context.getBean(UserService.class);
```

The context returns the already managed bean.

---

## Real-World Analogy

Imagine a **company office**.

* The **office** represents the Spring Context.
* The **employees** represent beans.

The office:

* Keeps records of all employees.
* Assigns work.
* Coordinates interactions.
* Provides the right employee when needed.

Similarly, the Spring Context:

* Knows every bean.
* Manages their relationships.
* Supplies the required bean to the application.

---

## Relationship with Other Core Concepts

```text
Application
      │
      ▼
ApplicationContext
      │
      ├── Bean A
      ├── Bean B
      ├── Bean C
      └── Bean D
             │
             ▼
      Dependency Injection
```

* The **ApplicationContext** is the central container.
* It manages all **beans**.
* It performs **Dependency Injection** between beans.

---

## Context vs Bean

| Context                                             | Bean                                    |
| --------------------------------------------------- | --------------------------------------- |
| Manages application objects                         | An application object managed by Spring |
| Holds multiple beans                                | Represents a single managed object      |
| Responsible for lifecycle and dependency management | Participates in dependency injection    |

---

## Key Takeaways (80/20)

* The **Spring Context** usually refers to the **ApplicationContext**.
* It is the central runtime environment that **stores, manages, and provides access to Spring beans**.
* It reads configuration metadata, creates beans, injects dependencies, and manages bean lifecycles.
* Applications obtain managed objects (beans) from the **ApplicationContext** instead of creating them manually.
* **ApplicationContext** is the most commonly used implementation of the Spring container.

---

**Claim: So, the spring container is an abstract concept, and context (ApplicationContext) is an implementation of it?**
Yes, that's the right mental model, with one small refinement.

> **The Spring container is a conceptual term that refers to the component responsible for managing beans. `BeanFactory` and `ApplicationContext` are concrete implementations of that concept.**

Think of it like this:

```text
Spring Container (Concept)
           │
           ├── BeanFactory
           │      (Basic container)
           │
           └── ApplicationContext
                  (Advanced container)
```

### What is the Spring container?

"Spring container" is **not a class**. It is a **general term** for the component that:

* Creates beans
* Configures beans
* Injects dependencies
* Manages bean lifecycles
* Provides beans to the application

Any implementation that performs these responsibilities is considered a Spring container.

### What is `BeanFactory`?

`BeanFactory` is the **most basic implementation** of the Spring container.

It provides the core IoC and Dependency Injection capabilities.

### What is `ApplicationContext`?

`ApplicationContext` builds upon `BeanFactory` and adds many enterprise features, such as:

* Component scanning
* Event publishing
* Internationalization (i18n)
* Resource loading
* Environment and property support

Because of these additional capabilities, **almost all modern Spring applications use `ApplicationContext` instead of `BeanFactory`**.

### Hierarchy

A more accurate hierarchy is:

```text
Spring Container (Concept)
           │
           ▼
     BeanFactory (Interface)
           │
           ▼
 ApplicationContext (Interface)
           │
           ▼
Concrete Implementations
(ClassPathXmlApplicationContext,
AnnotationConfigApplicationContext,
GenericApplicationContext, ...)
```

### Key Takeaways

* ✅ **Spring container** is a **concept**, not a specific class.
* ✅ **`BeanFactory`** is the basic container interface.
* ✅ **`ApplicationContext`** is a more feature-rich container interface that extends `BeanFactory`.
* ✅ In practice, when developers say "Spring container," they almost always mean an `ApplicationContext`.

This distinction becomes especially useful in the next module, **IoC Container**, where you'll study `BeanFactory` and `ApplicationContext` in depth.
---

**Expound on your point: BeanFactory and ApplicationContext are the interfaces.**
---

This is an important distinction because it illustrates how Spring itself applies good object-oriented design principles.

Let's build the idea step by step.

---

## Step 1: What is an Interface?

In Java, an **interface defines a contract**, not an implementation.

It specifies **what operations are available**, but not **how they are performed**.

For example:

```java
interface Vehicle {
    void start();
}
```

This interface says:

> "Any class implementing `Vehicle` must provide a `start()` method."

But it doesn't explain **how** to start.

Different implementations can provide different behavior.

```java
class Car implements Vehicle {
    public void start() {
        System.out.println("Starting car...");
    }
}

class Bike implements Vehicle {
    public void start() {
        System.out.println("Starting bike...");
    }
}
```

Notice:

* `Vehicle` is **not an object**.
* `Vehicle` is **not an implementation**.
* It is simply a contract.

---

## Step 2: Apply the Same Idea to Spring

Spring follows the same design philosophy.

Instead of creating one concrete container class, Spring defines an interface.

### BeanFactory

```java
public interface BeanFactory {
    Object getBean(String name);
    ...
}
```

This interface defines operations like:

* get a bean
* check whether a bean exists
* retrieve bean metadata

But it does **not** specify how these operations are implemented.

---

## Step 3: Who Implements BeanFactory?

Spring provides concrete classes that implement the interface.

For example:

```text
BeanFactory (Interface)
        ▲
        │
        │ implements
        │
DefaultListableBeanFactory
```

`DefaultListableBeanFactory` contains the actual code that:

* stores beans
* creates beans
* injects dependencies
* returns beans

So:

```text
BeanFactory
     │
     │ Contract
     ▼
DefaultListableBeanFactory
     │
     │ Actual implementation
     ▼
Creates and manages beans
```

---

## Step 4: What About ApplicationContext?

`ApplicationContext` is also an interface.

In simplified form:

```java
public interface ApplicationContext extends BeanFactory {
}
```

Notice something important.

It **extends** `BeanFactory`.

That means:

* Everything a `BeanFactory` can do...
* An `ApplicationContext` can also do...

...plus additional features.

Conceptually:

```text
BeanFactory
      ▲
      │ extends
      │
ApplicationContext
```

So the hierarchy is:

```text
ApplicationContext
       IS-A
BeanFactory
```

---

## Step 5: Who Implements ApplicationContext?

Spring provides several implementations.

For example:

```text
ApplicationContext (Interface)
            ▲
            │
            ├───────────────┐
            │               │
AnnotationConfigApplicationContext
ClassPathXmlApplicationContext
GenericApplicationContext
```

These are actual classes.

When your application starts, you create one of these classes.

For example:

```java
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);
```

Notice the left and right sides.

```java
ApplicationContext
```

is an interface.

```java
AnnotationConfigApplicationContext
```

is the implementation.

This is a common Java design pattern called **programming to an interface**.

---

## Step 6: Why Does Spring Use Interfaces?

Suppose Spring had only one class:

```text
SpringContainer
```

Every application would depend directly on that class.

Instead, Spring defines interfaces.

This provides:

* Loose coupling
* Flexibility
* Easier testing
* Interchangeable implementations
* Better extensibility

For example:

```java
ApplicationContext context = ...
```

The rest of your code doesn't care whether `context` is actually:

* `AnnotationConfigApplicationContext`
* `ClassPathXmlApplicationContext`
* `GenericApplicationContext`

As long as it satisfies the `ApplicationContext` contract, your code works.

---

## Step 7: Real-World Analogy

Imagine a **remote control**.

The remote defines buttons:

* Power
* Volume
* Channel

This is like an **interface**.

Different TVs implement those buttons differently.

```text
Remote Control (Interface)

        ▲
        │
        ├──────────────┐
        │              │
Samsung TV      Sony TV
```

You don't care how the TV turns on internally.

You only know that pressing the **Power** button works.

Spring works the same way.

---

## Complete Picture

```text
          Spring Container (Concept)
                    │
                    ▼
          BeanFactory (Interface)
                    │
                    ▼
      ApplicationContext (Interface)
                    │
      ┌─────────────┼─────────────────┐
      ▼             ▼                 ▼
AnnotationConfigApplicationContext
ClassPathXmlApplicationContext
GenericApplicationContext
```

* **Spring Container** is the **concept** of a component that manages beans.
* **`BeanFactory`** is the **basic interface** defining the container contract.
* **`ApplicationContext`** is a **more capable interface** that extends `BeanFactory`.
* **Concrete classes** such as `AnnotationConfigApplicationContext` implement `ApplicationContext` and contain the actual logic for creating, configuring, and managing beans.

This layered design is a textbook example of **abstraction** in object-oriented programming: users interact with interfaces (contracts), while Spring provides interchangeable implementations behind those interfaces.
---

## Topic: Metadata

### Learning Objective

Understand what metadata is, why the Spring container needs metadata, and how metadata tells Spring how to create, configure, and manage beans.

---

## What is Metadata?

**Metadata means "data about data."**

In Spring:

> **Metadata is configuration information that describes how the Spring container should create, configure, connect, and manage beans.**

The container itself does not automatically know your application structure. Metadata provides the instructions.

---

## Why Does Spring Need Metadata?

The Spring container has responsibilities:

* Create beans
* Configure beans
* Inject dependencies
* Manage lifecycle

But it needs answers:

* Which classes should become beans?
* How should objects be created?
* Which dependencies are required?
* What is the bean scope?
* What lifecycle methods should run?

Metadata provides these answers.

---

## Simple Example Without Spring

Developer manually gives instructions:

```java
UserRepository repository = new UserRepository();

UserService service = new UserService(repository);
```

The developer decides:

```text
Create UserRepository
          │
          ▼
Create UserService
          │
          ▼
Inject UserRepository
```

The instructions are inside Java code.

---

## With Spring Metadata

You describe the relationship:

```java
@Service
class UserService {

    private UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}


@Repository
class UserRepository {
}
```

The annotations are metadata.

They tell Spring:

```text
Metadata
    │
    ├── UserService is a bean
    ├── UserRepository is a bean
    └── UserService needs UserRepository
```

Then:

```text
Metadata
    │
    ▼
Spring Container
    │
    ▼
Creates and connects beans
```

---

# Types of Spring Metadata

Spring supports three major styles.

## 1. XML Metadata (Older Approach)

External configuration file:

```xml
<bean id="userService"
      class="com.example.UserService"/>
```

Meaning:

> Create a bean from the UserService class.

---

## 2. Java Configuration Metadata

Using Java classes:

```java
@Configuration
class AppConfig {

    @Bean
    UserService userService() {
        return new UserService();
    }
}
```

Meaning:

* `@Configuration` → this class contains bean definitions
* `@Bean` → this method creates a bean

---

## 3. Annotation Metadata (Modern Approach)

Using annotations directly on classes:

```java
@Service
class UserService {
}
```

Meaning:

> Register UserService as a Spring bean.

Common metadata annotations:

```text
@Component
@Service
@Repository
@Controller

@Autowired
@Bean
@Configuration
```

---

# Metadata vs Bean

| Metadata                    | Bean                            |
| --------------------------- | ------------------------------- |
| Instructions for Spring     | Actual object managed by Spring |
| Exists before bean creation | Created using metadata          |
| Describes what to create    | The created instance            |

Example:

```text
@Service annotation
        │
        ▼
 Metadata
        │
        ▼
Spring Container
        │
        ▼
 UserService Bean
```

---

# Metadata vs Container

```text
Configuration Metadata
          │
          │ provides instructions
          ▼
Spring Container
          │
          │ creates/manages
          ▼
        Beans
```

* Metadata = instructions
* Container = manager/executor
* Bean = managed object

---

# Real-World Analogy

Think of building a house.

**Blueprint = Metadata**

It describes:

* What rooms exist
* How rooms connect
* Materials needed

**Builder = Spring Container**

Uses the blueprint to create the house.

**House = Beans**

The final created objects.

```text
Blueprint
    │
    ▼
Builder
    │
    ▼
House
```

Similarly:

```text
Metadata
    │
    ▼
Spring Container
    │
    ▼
Beans
```

---

# Complete Spring Core Relationship

```text
Application
     │
     ▼
Configuration Metadata
     │
     ▼
ApplicationContext
(Spring Container)
     │
     ▼
Beans
     │
     ▼
Dependencies
```

---

# Key Takeaways (80/20)

* **Metadata is information that tells Spring how to manage objects.**
* The Spring container reads metadata before creating beans.
* Metadata answers: "What beans exist and how should they be connected?"
* Metadata can come from XML, Java configuration, or annotations.
* Metadata is the blueprint, the container is the builder, and beans are the created objects. 
---
**Q. Expound on what is the difference between Java Configuration metadata and Annotation metadata?**
---
**Learning Objective:** Understand the difference between **Java Configuration metadata** and **Annotation-based metadata** in Spring, why both exist, and when each approach is used.

---

## Core Idea

Both **Java Configuration** and **Annotations** provide **metadata** to the Spring container.

They both answer:

> "Which objects should become beans, and how should Spring manage them?"

The difference is **where you place the instructions**.

```text
Configuration Metadata
          │
          ├── Java Configuration
          │       → Instructions in separate config classes
          │
          └── Annotation Configuration
                  → Instructions directly on application classes
```

---

# 1. Java Configuration (`@Configuration` + `@Bean`)

## Meaning

With Java Configuration, you create a **separate configuration class** that explicitly tells Spring how to create beans.

Example:

```java
@Configuration
class AppConfig {

    @Bean
    UserService userService() {
        return new UserService();
    }
}
```

Here:

* `@Configuration` tells Spring:

  > "This class contains bean creation instructions."

* `@Bean` tells Spring:

  > "Call this method and register the returned object as a bean."

Flow:

```text
AppConfig class
      │
      ▼
@Bean methods
      │
      ▼
Spring Container
      │
      ▼
Creates Beans
```

---

## Characteristics

Java Configuration is **explicit**.

You manually describe:

* Which objects become beans
* How objects are created
* Constructor arguments
* Initialization logic

Example:

```java
@Bean
PaymentService paymentService() {
    return new PaymentService(
        new StripeClient()
    );
}
```

You control object creation.

---

# 2. Annotation Configuration (`@Component`, `@Service`, etc.)

## Meaning

With annotation configuration, you mark the class itself and let Spring automatically discover it.

Example:

```java
@Service
class UserService {

}
```

You are telling Spring:

> "When scanning, find this class and create a bean automatically."

Flow:

```text
@Service
   │
   ▼
Component Scanning
   │
   ▼
Spring Container
   │
   ▼
Creates UserService Bean
```

---

## Characteristics

Annotation configuration is **automatic**.

Spring discovers classes using:

```java
@ComponentScan
```

Then it searches for:

```text
@Component
@Service
@Repository
@Controller
```

and creates beans automatically.

---

# Main Difference

| Java Configuration                 | Annotation Configuration         |
| ---------------------------------- | -------------------------------- |
| Bean defined outside the class     | Bean defined on the class itself |
| Uses `@Configuration` + `@Bean`    | Uses stereotype annotations      |
| Explicit creation                  | Automatic discovery              |
| Developer controls object creation | Spring creates automatically     |
| More flexible                      | Less boilerplate                 |
| Good for external libraries        | Good for your own classes        |

---

# Example Comparison

Suppose we have:

```java
class EmailService {

}
```

## Java Configuration approach

Class stays clean:

```java
class EmailService {

}
```

Configuration elsewhere:

```java
@Configuration
class AppConfig {

    @Bean
    EmailService emailService() {
        return new EmailService();
    }
}
```

Spring sees:

```text
@Bean
  ↓
Create EmailService
```

---

## Annotation approach

Put metadata directly on the class:

```java
@Service
class EmailService {

}
```

Spring sees:

```text
@Service
   ↓
Automatically create EmailService
```

---

# When to Use Which?

## Use Annotation Configuration for your own application classes:

Example:

```text
Your Code

UserController
UserService
UserRepository
```

Usually:

```java
@Controller
@Service
@Repository
```

Because you own the source code and can add annotations.

---

## Use Java Configuration for objects you cannot modify:

Example:

Third-party library class:

```java
class StripeClient {

}
```

You cannot add:

```java
@Component
class StripeClient {}
```

because it is not your code.

So:

```java
@Configuration
class PaymentConfig {

    @Bean
    StripeClient stripeClient() {
        return new StripeClient();
    }
}
```

---

# Real-World Analogy

Think about employee registration.

## Java Configuration

Manager manually registers employees:

```text
Employee List
      │
      ▼
Manager registers John
      ▼
Company System
```

Explicit control.

---

## Annotation Configuration

Employees wear ID badges:

```text
Employee with Badge
          │
          ▼
Scanner detects badge
          │
          ▼
Company System registers employee
```

Automatic discovery.

---

# Key Takeaways (80/20)

* Both approaches provide **metadata** to the Spring container.
* **Java Configuration (`@Configuration`, `@Bean`)** = manually tell Spring how to create beans.
* **Annotation Configuration (`@Component`, `@Service`, etc.)** = mark classes and let Spring discover them automatically.
* Use **annotations for your own classes**.
* Use **Java Configuration for third-party classes or when you need more creation control**.
* Modern Spring applications commonly use both together.
---

## Topic: Configuration

### Learning Objective

Understand what configuration means in Spring, why configuration exists, and how it provides instructions that allow the Spring container to create and manage beans.

---

## What is Configuration?

In Spring:

> **Configuration is the process of providing metadata (instructions) that tells the Spring container how to create, configure, connect, and manage beans.**

The container performs the work, but configuration tells it **what to do**.

Simple relationship:

```text
Configuration
      │
      │ provides metadata
      ▼
Spring Container
      │
      │ creates/manages
      ▼
Beans
```

---

## Why Do We Need Configuration?

The Spring container is powerful, but it does not automatically know:

* Which classes should become beans?
* How should beans be created?
* Which beans depend on each other?
* What settings should beans use?
* How long should beans exist?

Configuration provides these answers.

---

## Traditional Java Without Configuration

Developer controls object creation:

```java
UserRepository repository = new UserRepository();

UserService service =
        new UserService(repository);
```

Responsibilities:

```text
Developer
    │
    ├── Creates objects
    ├── Connects dependencies
    └── Manages lifecycle
```

The application code contains object management logic.

---

# Spring With Configuration

You provide instructions:

```java
@Configuration
class AppConfig {

    @Bean
    UserRepository userRepository() {
        return new UserRepository();
    }


    @Bean
    UserService userService() {
        return new UserService(userRepository());
    }
}
```

Spring does the work:

```text
Configuration
      │
      ▼
Spring Container
      │
      ├── Creates UserRepository Bean
      ├── Creates UserService Bean
      └── Injects UserRepository into UserService
```

---

# Types of Spring Configuration

Spring evolved through three major configuration styles.

---

## 1. XML Configuration (Old Approach)

Configuration is written in XML files.

Example:

```xml
<bean id="userService"
      class="com.example.UserService"/>
```

Meaning:

> Create and manage a UserService bean.

Flow:

```text
XML File
   │
   ▼
Spring Container
   │
   ▼
Bean
```

---

# 2. Java Configuration (Modern Explicit Approach)

Uses:

* `@Configuration`
* `@Bean`

Example:

```java
@Configuration
class AppConfig {

    @Bean
    UserService userService() {
        return new UserService();
    }
}
```

Meaning:

* `AppConfig` contains configuration.
* `userService()` creates a bean.
* Spring manages the returned object.

Flow:

```text
@Configuration Class
          │
          ▼
@Bean Methods
          │
          ▼
Spring Container
          │
          ▼
Beans
```

---

# 3. Annotation Configuration (Modern Automatic Approach)

Uses annotations directly on classes.

Example:

```java
@Service
class UserService {

}
```

Spring scans:

```java
@ComponentScan
```

and automatically creates beans.

Flow:

```text
@Component / @Service
          │
          ▼
Component Scan
          │
          ▼
Spring Container
          │
          ▼
Bean
```

---

# Configuration vs Metadata

They are closely related.

| Configuration                     | Metadata                                       |
| --------------------------------- | ---------------------------------------------- |
| The way instructions are provided | The actual instructions/data                   |
| Process/source                    | Information itself                             |
| XML, Java classes, annotations    | Bean definitions, dependencies, lifecycle info |

Example:

```java
@Service
class UserService {}
```

* Annotation configuration = the approach
* `@Service` information = metadata

---

# Configuration vs Container

| Configuration      | Container             |
| ------------------ | --------------------- |
| Gives instructions | Executes instructions |
| Defines beans      | Creates beans         |
| Static information | Runtime component     |

Relationship:

```text
Configuration (Plan)
          │
          ▼
Container (Executor)
          │
          ▼
Beans (Objects)
```

---

# Real-World Analogy

Imagine building a restaurant.

**Configuration = Recipe**

Defines:

* Ingredients needed
* Preparation steps
* Cooking instructions

**Chef = Spring Container**

Uses the recipe.

**Food = Beans**

Final created objects.

```text
Recipe
  │
  ▼
Chef
  │
  ▼
Food
```

Spring:

```text
Configuration
       │
       ▼
Container
       │
       ▼
Beans
```

---

# Complete Core Architecture Flow

```text
Application
     │
     ▼
Configuration
     │
     ▼
Metadata
     │
     ▼
ApplicationContext
(Spring Container)
     │
     ▼
Beans
     │
     ▼
Dependency Injection
```

---

# Key Takeaways (80/20)

* **Configuration tells Spring what objects to create and how to manage them.**
* The Spring container uses configuration metadata to build the application.
* Configuration removes object creation responsibility from developers.
* Three main styles:

  * XML Configuration
  * Java Configuration (`@Configuration`, `@Bean`)
  * Annotation Configuration (`@Component`, `@Service`, etc.)
* **Configuration is the plan, container is the executor, beans are the result.**
---

# Understanding the Relationship Between Application, ApplicationContext, and Beans in Spring Core

---

Simple example showing:

```text
Application
      │
      ▼
ApplicationContext
      │
      ▼
Beans
```

---

### 1. Bean (Object managed by Spring)

```java
public class UserService {

    public void registerUser() {
        System.out.println("User registered");
    }
}
```

`UserService` is a normal Java class.

---

### 2. Configuration (Tell Spring to create Bean)

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public UserService userService() {
        return new UserService();
    }
}
```

Here:

* `@Configuration` → provides metadata
* `@Bean` → tells Spring: "Create and manage this object"

---

### 3. Application creates ApplicationContext

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MyApplication {

    public static void main(String[] args) {

        ApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        UserService service =
                context.getBean(UserService.class);

        service.registerUser();
    }
}
```

---

### Relationship in Code

```text
MyApplication
      │
      │ creates
      ▼
ApplicationContext context
      │
      │ reads
      ▼
AppConfig (@Configuration)
      │
      │ creates
      ▼
UserService Bean
```

Runtime view:

```text
Application
(MyApplication.java)
        │
        ▼
ApplicationContext
(AnnotationConfigApplicationContext)
        │
        ▼
Bean
(UserService object)
```

Key idea:

* **Application** starts Spring.
* **ApplicationContext** is the Spring container implementation.
* **ApplicationContext reads configuration and manages beans.**
* **Beans are the objects your application actually uses.**
---

---
# Open Questions about obscure or inconspicuous topics

1. What is the application lifecycle in Spring, and how does the container manage it?
2. What is the difference between `BeanFactory` and `ApplicationContext`, and when should each be used?
3. What is the programming to an interface?
4. What is the difference between ApplicationContext implementation in Spring Boot and Spring Framework?
5. What is the component scanning process in Spring, and how does it relate to bean creation?
6. Do we use java configuration in Spring Boot? If yes, then why instead of annotation configuration?
7. What is the AppConfig class in spring framework? why don't seem to see it in spring boot, instead I seem to see for example public class CivilEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CivilEcommerceApplication.class, args);
	}

}?
8. What is the application classes and configuration classes in Spring? How are they different?
  