# GitHub Copilot System Instructions

You must strictly follow the backend architecture and engineering standards defined in this repository. Before generating any Java or Spring Boot code, analyze, adopt, and enforce the rules specified in the following file:

- Target File: `backend-standards.md`

## Key Enforcement Constraints:
1. **Clean Architecture Isolation:** Adhere to the Infrastructure -> Application -> Domain dependency flow. Never leak infrastructure or application concepts into the domain.
2. **Pure Domain (No Dependencies):** Absolutely NO third-party libraries, NO Spring annotations, NO JPA/Jakarta mapping annotations, and NO Lombok inside the `domain` package. Write raw Java with manual constructors, getters, and encapsulation.
3. **CQRS Pattern:** Separate commands and queries at the application layer. Do not introduce or use an in-memory command bus. Dispatch via direct constructor injection of specific handlers.
4. **Naming Conventions:** Do not use the `Service` suffix for classes. Use explicit, role-specific suffixes such as `Handler`, `Container`, `Provider`, `Registry`, `Processor`, or `Evaluator`.

Every code snippet, class definition, or refactoring advice you provide must satisfy the code review checklist defined in `backend-standards.md`.