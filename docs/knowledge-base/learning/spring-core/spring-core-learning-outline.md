# Spring Core Learning Outline

## Claim

The most effective way to learn Spring Core is to progress from **why Spring exists**, to **how the IoC container works**, and finally to **how beans are configured, managed, and interact**. Each topic builds upon the previous one.

**Caveat:** This outline covers **Spring Core** only. It intentionally excludes Spring Boot, Spring MVC, Spring Data JPA, and Spring Security except where brief comparisons aid understanding.

---

# Level 0 — Prerequisites

Before Spring Core, be comfortable with:

* Java Fundamentals
* Object-Oriented Programming (OOP)
* Interfaces & Polymorphism
* Constructors
* Exceptions
* Collections
* Generics (basic)
* Maven or Gradle (basic)

---

# Level 1 — Why Spring Exists (The Problem)

## Module 1: Motivation

* Problems with traditional Java applications
* Tight coupling
* High dependency
* Difficult testing
* Manual object creation
* Boilerplate configuration

**Outcome:** Understand *why* Spring was created before learning *how* it solves these problems.

---

# Level 2 — Spring Fundamentals

## Module 2: Introduction to Spring

* What is Spring?
* What is Spring Framework?
* Features of Spring
* Spring Modules
* Spring Ecosystem
* Spring vs Spring Boot

---

## Module 3: Core Architecture

* Container
* Bean
* Context
* Metadata
* Configuration

Understand the relationship:

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

# Level 3 — Inversion of Control

## Module 4: Inversion of Control (IoC)

* What is IoC?
* Traditional object creation
* IoC principle
* Benefits
* Real-world analogy

---

## Module 5: IoC Container

* BeanFactory
* ApplicationContext
* Differences
* Container responsibilities
* Bean lookup

---

# Level 4 — Dependency Injection

## Module 6: Dependency Injection (DI)

* What is DI?
* Constructor Injection
* Setter Injection
* Field Injection
* Why Constructor Injection is preferred
* Advantages

Practice identifying dependencies between classes.

---

# Level 5 — Spring Beans

## Module 7: Bean Fundamentals

* What is a Bean?
* Bean Definition
* Bean Naming
* Bean Creation
* Bean Registration

---

## Module 8: Bean Lifecycle

* Instantiation
* Dependency Injection
* Initialization
* Usage
* Destruction

Understand:

```text
Create
    ↓
Inject
    ↓
Initialize
    ↓
Use
    ↓
Destroy
```

---

## Module 9: Bean Scopes

* Singleton
* Prototype
* Request
* Session
* Application
* WebSocket

Focus first on Singleton and Prototype.

---

# Level 6 — Configuration

## Module 10: Bean Configuration

Historical evolution:

* XML Configuration
* Java Configuration
* Annotation Configuration

---

## Module 11: Java Configuration

* `@Configuration`
* `@Bean`

---

## Module 12: Component Scanning

* `@Component`
* `@ComponentScan`

---

# Level 7 — Stereotype Annotations

## Module 13: Component Stereotypes

* `@Component`
* `@Service`
* `@Repository`
* `@Controller`

Understand their semantic roles.

---

# Level 8 — Dependency Resolution

## Module 14: Autowiring

* `@Autowired`
* Constructor Injection
* Setter Injection
* Field Injection

---

## Module 15: Qualifiers

* `@Qualifier`
* `@Primary`

Handle multiple implementations.

---

# Level 9 — Bean Lifecycle Customization

## Module 16: Lifecycle Hooks

* `@PostConstruct`
* `@PreDestroy`
* Initialization callbacks
* Destruction callbacks

---

# Level 10 — External Configuration

## Module 17: Properties

* Property files
* Environment
* `@Value`

---

# Level 11 — Advanced Bean Management

## Module 18: Profiles

* `@Profile`
* Development profile
* Production profile

---

## Module 19: Conditional Beans

* Conditional creation
* Bean selection

---

# Level 12 — Spring Expression Language (Basic)

## Module 20: SpEL

* Expressions
* Property access
* Method invocation
* Collections

Only basic usage is necessary initially.

---

# Level 13 — Events

## Module 21: Spring Events

* Event Publisher
* Event Listener
* Custom Events

---

# Level 14 — Resources

## Module 22: Resource Abstraction

* Resource
* ResourceLoader

---

# Level 15 — Validation

## Module 23: Bean Validation (Introduction)

* Validation concept
* Validator interface

(Advanced validation belongs with Spring MVC.)

---

# Level 16 — Aspect-Oriented Programming (Introduction)

## Module 24: AOP Basics

* Cross-cutting concerns
* Advice
* Join Point
* Pointcut
* Aspect

Focus on concepts only; advanced AOP can be learned later.

---

# Level 17 — Mini Project

Build a console application that demonstrates:

* Bean creation
* Dependency Injection
* Component scanning
* Configuration class
* Bean lifecycle
* Bean scopes
* Profiles
* Properties

---

# Knowledge Dependency Graph

```text
Java
    │
    ▼
Why Spring?
    │
    ▼
Spring Architecture
    │
    ▼
IoC
    │
    ▼
IoC Container
    │
    ▼
Dependency Injection
    │
    ▼
Beans
    │
    ▼
Bean Lifecycle
    │
    ▼
Bean Scopes
    │
    ▼
Configuration
    │
    ▼
Component Scanning
    │
    ▼
Autowiring
    │
    ▼
Qualifiers
    │
    ▼
Properties
    │
    ▼
Profiles
    │
    ▼
Events
    │
    ▼
Resources
    │
    ▼
AOP (Introduction)
```

# Pareto Learning Order (80/20)

If your goal is to become productive with Spring quickly, prioritize these modules:

1. Why Spring Exists
2. Spring Architecture
3. Inversion of Control (IoC)
4. IoC Container
5. Dependency Injection (DI)
6. Bean Fundamentals
7. Bean Lifecycle
8. Bean Scopes
9. Java Configuration (`@Configuration`, `@Bean`)
10. Component Scanning (`@Component`, `@ComponentScan`)
11. Stereotype Annotations (`@Service`, `@Repository`, `@Controller`)
12. Autowiring (`@Autowired`)
13. `@Qualifier` and `@Primary`

These topics form the conceptual foundation for almost every Spring-based technology, including Spring Boot, Spring MVC, Spring Data JPA, and Spring Security. Advanced features such as events, resources, SpEL, and AOP are valuable but can be learned after you are comfortable with the core container and dependency injection model.
