# Spring Framework 101 (Pareto Principle – Learn the 20% That Gives 80% of the Value)

## Claim

You do **not** need to learn the entire Spring ecosystem first. Start with the **Spring Framework core**, then move to **Spring Boot**, and finally learn production-ready features.

---

# 1. What is Spring?

**Spring** is a Java framework that helps you build applications by reducing boilerplate code and managing object creation, dependencies, configuration, and application lifecycle.

Without Spring:

```java
UserRepository repo = new UserRepository();
UserService service = new UserService(repo);
```

You manually create every object.

With Spring:

```java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

Spring creates the objects and connects them automatically.

---

# 2. Why Spring?

Without Spring:

* Manual object creation
* Tight coupling
* Difficult testing
* Difficult configuration
* Large amount of boilerplate

Spring provides:

* Dependency Injection
* Inversion of Control
* Bean Lifecycle Management
* Configuration Management
* Integration with databases, web, security, messaging, etc.

---

# 3. Spring Ecosystem

```text
Spring Ecosystem
│
├── Spring Framework (Core)
│   ├── IoC Container
│   ├── Dependency Injection
│   ├── Beans
│   ├── Configuration
│   ├── AOP
│   ├── Data Access
│   └── Web MVC
│
├── Spring Boot
│
├── Spring Security
│
├── Spring Data JPA
│
├── Spring Cloud
│
└── Spring Testing
```

Think of **Spring Framework** as the engine, while **Spring Boot** makes the engine easy to use.

---

# 4. Core Concepts

## 1. Inversion of Control (IoC)

Normally:

```text
Application
      ↓
Creates Objects
```

Spring:

```text
Application
      ↓
Requests Objects
      ↓
Spring Creates Objects
```

Spring owns object creation.

---

## 2. Dependency Injection (DI)

Suppose:

```text
UserController
      ↓
UserService
      ↓
UserRepository
```

Without DI:

```java
UserService service = new UserService(new UserRepository());
```

With DI:

```java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

Spring injects the repository automatically.

---

## 3. Bean

A **Bean** is simply an object managed by Spring.

```java
@Service
public class UserService {}
```

Spring creates:

```text
Bean
    ↓
UserService Object
```

Not every Java object is a bean.

---

## 4. IoC Container

The IoC Container:

* Creates beans
* Stores beans
* Injects dependencies
* Manages lifecycle

```text
Application
      ↓
IoC Container
      ↓
Beans
```

---

# 5. Bean Lifecycle

```text
Start Application
        ↓
Create Bean
        ↓
Inject Dependencies
        ↓
Initialize Bean
        ↓
Application Uses Bean
        ↓
Destroy Bean
```

---

# 6. Common Annotations

| Annotation        | Purpose                                                |
| ----------------- | ------------------------------------------------------ |
| `@Component`      | Generic Spring bean                                    |
| `@Service`        | Business logic                                         |
| `@Repository`     | Data access                                            |
| `@Controller`     | MVC controller                                         |
| `@RestController` | REST API controller                                    |
| `@Configuration`  | Configuration class                                    |
| `@Bean`           | Creates a bean manually                                |
| `@Autowired`      | Inject dependency (constructor injection is preferred) |

---

# 7. Spring Bean Scopes

| Scope     | Meaning                  |
| --------- | ------------------------ |
| Singleton | One instance (default)   |
| Prototype | New object every request |
| Request   | One per HTTP request     |
| Session   | One per user session     |

---

# 8. Project Layering

```text
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

Responsibilities:

### Controller

Receives HTTP requests.

### Service

Contains business rules.

### Repository

Communicates with the database.

---

# 9. Configuration

Instead of XML, modern Spring primarily uses annotations.

Example:

```java
@Configuration
public class AppConfig {

    @Bean
    public UserService userService(UserRepository repository) {
        return new UserService(repository);
    }
}
```

---

# 10. Spring MVC Request Flow

```text
Browser
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
    ↓
Repository
    ↓
Service
    ↓
Controller
    ↓
JSON Response
```

---

# 11. Spring vs Spring Boot

| Spring Framework               | Spring Boot                    |
| ------------------------------ | ------------------------------ |
| Core framework                 | Built on Spring Framework      |
| More manual configuration      | Auto-configuration             |
| More setup                     | Faster development             |
| Flexible                       | Convention over configuration  |
| Used by Spring Boot internally | Simplifies Spring applications |

---

# 12. Typical Request Flow in a Spring Boot Application

```text
Client
   │
   ▼
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
   │
   ▼
Repository
   │
   ▼
Service
   │
   ▼
Controller
   │
   ▼
JSON Response
```

---

# 13. Learning Roadmap

```text
Java
   ↓
OOP
   ↓
Maven / Gradle
   ↓
Spring Core
   ↓
Dependency Injection
   ↓
Spring Boot
   ↓
Spring MVC
   ↓
REST APIs
   ↓
Spring Data JPA
   ↓
Spring Security
   ↓
Testing
   ↓
Docker
   ↓
CI/CD
```

---

# 14. Common Interview Questions

1. What is Spring?
2. What is IoC?
3. What is Dependency Injection?
4. What is a Bean?
5. What is the IoC Container?
6. What is the difference between `@Component`, `@Service`, and `@Repository`?
7. Why is constructor injection preferred over field injection?
8. What is bean scope?
9. What is the difference between Spring and Spring Boot?
10. Explain the request flow from Controller to Database.

---

# Mental Model

```text
Spring Framework
│
├── IoC Container
│     ├── Creates Beans
│     ├── Injects Dependencies
│     └── Manages Lifecycle
│
├── Dependency Injection
│
├── Configuration
│
├── Spring MVC
│
├── Data Access
│
└── AOP
        │
        ▼
Spring Boot
        │
        ▼
REST APIs
        │
        ▼
Database
        │
        ▼
Production Application
```

**Caveat:** This tutorial focuses on the foundational 20% of Spring Framework concepts that provide the greatest leverage. Topics such as AOP, transaction management, event handling, validation, caching, messaging, reactive programming, and advanced bean configuration are important but are best learned after you're comfortable with Spring Core and Spring Boot.
