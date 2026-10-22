# Spring Boot 2.7 → 3.3 / Java 11 → 21 migration, step by step

A small but realistic JWT-secured REST service migrated from a legacy stack to a current one, **one breaking change per commit**, with the same test suite passing on both ends.

| | `main` (legacy) | `migrated` |
|---|---|---|
| Spring Boot | 2.7.18 (EOL) | 3.3.5 |
| Java | 11 | 21 |
| Spring Security | 5.7 — `WebSecurityConfigurerAdapter`, `antMatchers()` | 6.3 — `SecurityFilterChain`, lambda DSL, `requestMatchers()` |
| Jakarta EE | `javax.persistence` / `javax.servlet` / `javax.validation` | `jakarta.*` |
| Hibernate | 5.6 | 6.5 |
| API docs | Springfox 3 (abandoned, needs 2 startup hacks) | springdoc-openapi 2.6 |
| JWT | jjwt 0.11 | jjwt 0.12 |
| DTOs | POJOs with getters | Java records |
| Request threads | platform threads | virtual threads (`spring.threads.virtual.enabled`) |
| `GET /api/authors` | **21 SQL statements** (N+1) | **1 SQL statement** (`@EntityGraph`) |
| Tests | 3 pass | 3 pass |

## Run it

```bash
# legacy
git checkout main && mvn test            # JDK 11 or 17 (Boot 2.7 + Hibernate 5 are not certified on 21)

# migrated
git checkout migrated && mvn test        # needs JDK 21

mvn spring-boot:run
curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"username":"admin","password":"admin"}'
curl -s localhost:8080/api/authors -H "Authorization: Bearer <token>"
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html` · H2 console: `http://localhost:8080/h2-console`

## Migration log — `git log main..migrated`

Each commit is self-contained; you can stop after any step and still have a buildable branch from step 2 onward.

| Step | Breaking change hit | Fix |
|---|---|---|
| 1 | Bumping the parent POM alone: ~40 compile errors | Nothing yet — this commit documents the blast radius |
| 2 | `javax.persistence`, `javax.servlet`, `javax.validation` packages gone (Jakarta EE 10) | Mechanical import rename. Hibernate 6 reads `jakarta.persistence` with no mapping changes for this schema |
| 3 | `WebSecurityConfigurerAdapter` removed; `antMatchers()` removed; `authenticationManagerBean()` gone | `SecurityFilterChain` bean + lambda DSL; `AuthenticationConfiguration.getAuthenticationManager()`. **Gotcha:** with the H2 console on the classpath there are two servlets, and `requestMatchers(String)` throws *"This method cannot decide whether these patterns are Spring MVC patterns or not"* at startup — use `antMatcher(...)` explicitly |
| 4 | Springfox has no Boot 3 release | springdoc-openapi; deleted the `Docket`, the `WebMvcRequestHandlerProvider` `BeanPostProcessor` hack and the `spring.mvc.pathmatch.matching-strategy=ant_path_matcher` override |
| 5 | jjwt 0.11 API deprecated | `subject()/issuedAt()/expiration()`, `signWith(key)` without `SignatureAlgorithm`, `parser().verifyWith().parseSignedClaims().getPayload()` |
| 6 | — (improvement) | DTOs → records; virtual threads via one property |
| 7 | — (improvement) | `@EntityGraph(attributePaths = "books")` collapses 21 statements into 1. Hibernate 6 de-duplicates parent rows automatically, so no `DISTINCT` needed (Hibernate 5 required it) |

### Other Boot 3 changes that did **not** bite this project but usually do

- **Actuator**: `/actuator/**` paths unchanged, but `management.metrics.export.*` → `management.<system>.metrics.export.*`; `httptrace` → `httpexchanges`
- **Hibernate 6**: `@Type(type = "...")` string types removed; `IDENTITY` on MySQL batching; `LocalDateTime` now mapped to `TIMESTAMP(6)` (schema diff on Flyway validate)
- **Spring Data 3**: `findById` on a `Specification` now wants `Specification<T>` generics; `Pageable.unpaged()` semantics; `@Query` with native SQL and `nativeQuery = true` on Hibernate 6 is stricter about aliases
- **Trailing-slash matching** is off by default in Spring MVC 6: `/api/authors/` no longer matches `/api/authors`
- **`spring.factories` auto-configuration** → `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- **Logback/Log4j2 1.x**, **Freemarker**, **Thymeleaf 2**, **Ehcache 2** need major-version bumps

## How this was verified

`AuthorApiTest` runs the same three scenarios on both branches: unauthenticated → 401, login → JWT → 200 with 20 authors × 5 books, and a Hibernate `Statistics` assertion on the number of prepared statements for the listing endpoint (21 on `main`, 1 on `migrated`).

## About

Portfolio project by **Naveen (Patsamatla Ramakrishnam Raju)** — Java/Spring Boot developer specialising in legacy modernisation. I do this migration on real codebases; the free first step is a repo audit with a written plan and effort estimate.
