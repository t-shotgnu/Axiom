# Backend Engineering Standards & Architecture Guide
**Technology Stack:** Java 17+ / Spring Boot 3.x
**Architectural Paradigms:** Clean Architecture (CA), Domain-Driven Design (DDD), CQRS (Lightweight)

---

## 1. Architectural Layers (Clean Architecture)

We strictly enforce a three-layer Clean Architecture pattern. Governance of dependencies flows inwards: **Infrastructure -> Application -> Domain**. Elements of an outer layer must never be exposed or leaked into an inner layer.

+-------------------+-------------------------+-------------------------+
| **Domain Layer**  | **Application Layer**   | **Infrastructure Layer** |
+-------------------+-------------------------+-------------------------+
| - Pure Java       | - Orchestration         | - Technical Implementation |
| - No Frameworks   | - CQRS Commands/Queries | - Spring Configurations |
| - No Lombok       | - Depends on Domain     | - Depends on Application & Domain |
+-------------------+-------------------------+-------------------------+

### 1.1 Domain Layer
* **Core Logic:** Contains the absolute business truth, invariants, rules, and logic.
* **Dependency Rule:** Zero external dependencies. Complete isolation.
* **Framework Agnostic:** No Spring annotations, no persistence configurations, no third-party utilities.

### 1.2 Application Layer
* **Orchestration:** Orchestrates use cases, handles transactions, security, and coordinates domain entities.
* **Communication:** Orchestrates CQRS Commands and Queries.
* **Dependencies:** Depends exclusively on the Domain Layer. Communicates with Infrastructure via interfaces (Inversion of Control).

### 1.3 Infrastructure Layer
* **Technical Implementation:** Database configurations, Spring MVC configuration, concrete repository implementations (Spring Data, JPA), message brokers (Kafka/RabbitMQ), and external HTTP clients.
* **Dependencies:** Depends directly on Application and Domain layers.

---

## 2. Domain-Driven Design (DDD) & Zero-Dependency Domain

The Domain Layer is sacred. To prevent technical contamination of business rules, the domain must remain pure Java.

### 2.1 The Zero-Dependency Rule
* **No Frameworks:** No Spring (`@Component`, `@Service`, `@Transactional`).
* **No Persistence Annotations:** No Jakarta/JPA (`@Entity`, `@Table`, `@Id`, `@Column`). If using Hibernate, mapping must be handled via Infrastructure-specific Entity DTOs and database mappers.
* **No Third-Party Libraries:** No Guava, no Apache Commons, no Jackson annotations (`@JsonProperty`).
* **No Lombok:** Absolute ban on Lombok (`@Data`, `@Getter`, `@NoArgsConstructor`, etc.) inside the Domain Layer. All constructor initialization, getters, and encapsulation mechanisms must be written in raw Java. This guarantees readability, type safety, and prevents compilation magic from masking architectural flaws.

### 2.2 Aggregates & Invariants
* An Aggregate Root must ensure all inner boundary invariants are satisfied.
* Aggregates can only reference other Aggregates by their Identity (ID), never by object references.
* Modification of the Aggregate state must happen exclusively through expressive domain methods (e.g., `activate()`, `assignInventory()`), never via setters.

### 2.3 Example: Pure Domain Aggregate Root (No Lombok, No Frameworks)

```java
package com.company.project.domain.order;

import com.company.project.domain.exception.DomainRuleException;
import java.util.UUID;
import java.math.BigDecimal;

public final class Order {

    private final UUID id;
    private final UUID customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;

    public Order(UUID id, UUID customerId, BigDecimal totalAmount) {
        if (id == null || customerId == null) {
            throw new DomainRuleException("Order and Customer identities cannot be null.");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainRuleException("Initial total amount must be greater than zero.");
        }
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
    }

    public void complete() {
        if (this.status != OrderStatus.PENDING) {
            throw new DomainRuleException("Only PENDING orders can be completed.");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void applyDiscount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainRuleException("Discount amount must be positive.");
        }
        if (this.totalAmount.compareTo(discountAmount) <= 0) {
            throw new DomainRuleException("Discount cannot exceed or equal the total order amount.");
        }
        this.totalAmount = this.totalAmount.subtract(discountAmount);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
```

3. CQRS (Command Query Responsibility Segregation)
We decouple read operations from write operations at the Application Layer level. We intentionally do not use an in-memory command bus (such as MediatR clones or Axon Framework) to avoid overhead, excessive abstraction tracing, and loss of compile-time type-safety.

3.1 Architectural Split
Commands: Change state. Do not return data (except for mutations returning generated IDs or metadata when absolutely necessary).
Queries: Fetch state. Read-only. Must not alter any state.

3.2 Dispatching and Handling
Instead of a dynamic/reflected bus, dispatching is done via explicit constructor dependency injection of specific handlers.
Handlers are cleanly split into single-responsibility classes implementing simple functional interfaces or concrete structures.

3.3 Example: Command Flow
```Java
package com.company.project.application.order.command;

import java.util.UUID;
import java.math.BigDecimal;

public record CreateOrderCommand(UUID customerId, BigDecimal amount) {}
```

```Java
package com.company.project.application.order.command;

import com.company.project.application.common.annotation.UseCase;
import com.company.project.domain.order.Order;
import com.company.project.domain.order.OrderRepository;
import java.util.UUID;

@Service
public final class CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    public CreateOrderCommandHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public UUID handle(CreateOrderCommand command) {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, command.customerId(), command.amount());
        
        orderRepository.save(order);
        return orderId;
    }
}
```
4. Class Naming Conventions & Anti-Patterns4.1 The "Service" Suffix BanUsing the Service suffix on every business class is an anti-pattern. It leads to bloated classes containing unrelated procedural operations. We choose highly expressive, role-specific suffixes instead.SuffixArchitectural Intent / PurposeExample**HandlerProcesses a specific CQRS Command or Query.RegisterCustomerCommandHandler**ContainerHolds, manages, and encapsulates state, context, or data collections temporarily.SecurityContextContainer**ProviderFetches external data, configurations, or parameters from environmental sources.ExchangeRateProvider**RegistryManages runtime registration, maps, dynamic lookups, or factories of specific types.PaymentGatewayRegistry**ProcessorPerforms algorithmic computations or sequence processing pipelines on raw data.TransactionPayloadProcessor**EvaluatorInspects current state against explicit policy guidelines or rule matrixes.RiskThresholdEvaluator**AdapterImplements an application interface to bridge external technical layers.SqlOrderRepositoryAdapter4.2 Prohibited ConventionsOrderService: Split this into CreateOrderCommandHandler, CancelOrderCommandHandler, or domain models.OrderServiceImpl: Interfaces must not have a single artificial implementation named with an Impl suffix. Use descriptive names like JpaOrderRepositoryAdapter implementing OrderRepository.5. Verification Checklist for Code ReviewsDuring a Pull Request review, verify the following conditions fail the build or merge checklist:Is there any Lombok import (import lombok...) inside the domain package? -> REJECTDoes a class in the domain package reference Spring annotations (@Component, @Service)? -> REJECTAre read operations using the same mutation paths/repositories as writes, violating CQRS? -> REJECTIs there a class named *Service without an extremely concrete, non-procedural justification? -> REJECTIs the infrastructure layer communicating directly with the domain without mapping entities? -> REJECT