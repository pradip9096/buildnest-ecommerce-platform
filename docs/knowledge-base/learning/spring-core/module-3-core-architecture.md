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

1

---
# Open Questions about obscure or inconspicuous topics

1. What is the application lifecycle in Spring, and how does the container manage it?
2. What is the difference between `BeanFactory` and `ApplicationContext`, and when should each be used?