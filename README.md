# Hexagonal 101

A website for learning hexagonal architecture (ports and adapters) from a **real** Java project.

- Browse the source code of a small Spring Boot service in a Monaco editor.
- An interactive hexagon highlights the brick the open file belongs to. Click a brick to open its code.
- Plug in other adapters from the diagram: **REST ↔ Kafka** on the driving side and **S3 (JSON) ↔ SQLite** on the driven side. Only two lines of `application.yml` change. The domain and the use cases do not.
- Follow a loan request step by step through the layers.

The website never runs the Java code: it displays it. The code is still real: it compiles, and CI tests it.

## The business case

A library lends books. `POST /loans {memberId, bookId}` (or a message on `library.loan-requests`) is refused when:

1. the book is already on loan,
2. the member holds as many books as their tier allows (STANDARD: 3, PREMIUM: 5),
3. the member has an overdue loan.

Otherwise the loan is granted, due in 14 days (STANDARD) or 28 days (PREMIUM).

## Repository layout

```
java/                         single Maven module, Java 25, Spring Boot 4.1
  pom.xml
  compose.yaml                S3Mock + Kafka, to run the app locally
  src/main/java/dev/hexagonal101/library/
    domain/                   business rules, plain Java
    application/port/in/      driving port (use case interface)
    application/port/out/     driven ports (BookCatalog, MemberDirectory, LoanRepository)
    application/service/      use case implementation
    adapter/in/rest/          driving adapter: Spring MVC
    adapter/in/kafka/         driving adapter: Spring for Apache Kafka, reply topic
    adapter/out/s3/           driven adapter: library.json in S3, conditional writes (If-Match ETag)
    adapter/out/sqlite/       driven adapter: JdbcClient + Flyway
    bootstrap/                Spring Boot application and wiring
  src/main/resources/         application.yml, S3 seed, SQLite migrations
  src/test/java/...           tests, including ArchitectureTest (ArchUnit) which enforces the dependency rule
web/                          the website: React + Vite + TypeScript + Monaco
```

## Java project

```bash
cd java
./mvnw verify                      # unit, ArchUnit and Testcontainers tests (needs Docker)

docker compose up -d               # S3Mock on :9090 and Kafka on :9092
./mvnw spring-boot:run

curl -X POST localhost:8080/loans -H 'Content-Type: application/json' \
     -d '{"memberId":"m-1","bookId":"b-1"}'
```

To swap adapters, run it with `--library.adapters.inbound=kafka` and/or `--library.adapters.outbound=sqlite`.

Sample data: `m-1` can borrow; `m-2` has reached their quota; `m-3` has an overdue loan; `b-2` to `b-5` are on loan.

## Website

```bash
cd web
npm install
npm run dev        # http://localhost:5173/hexagonal-101/
npm run build      # static site in web/dist
```

The site imports the sources from `../java` at build time (`web/src/project/sources.ts`). Change the Java code and the site shows the new version. The explanations and key lines live in `web/src/content/annotations.ts`, and the steps of the request animation live in `web/src/content/flow.ts`. Key lines are matched by substring, not by line number, so they survive code edits.

Pushing to `master` deploys the site to GitHub Pages (`.github/workflows/deploy.yml`).
