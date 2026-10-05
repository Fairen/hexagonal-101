import type { BrickId } from '../project/bricks'

export interface BrickInfo {
  title: string
  role: string
  text: string
}

export interface FileInfo {
  text: string
  /** Substrings: every line containing one of them is highlighted in the editor. */
  keyLines: string[]
}

export const BRICK_INFO: Record<BrickId, BrickInfo> = {
  domain: {
    title: 'Domain',
    role: 'The core of the hexagon',
    text:
      'The business rules of the library: who may borrow which book, and until when. Plain Java: no framework, ' +
      'no I/O, no annotation. It does not know that HTTP, Kafka, S3 or SQL exist. That is why it never changes ' +
      'when you swap an adapter.',
  },
  'port-in': {
    title: 'Driving port',
    role: 'Application · inbound',
    text:
      'What the application offers to the outside world: BorrowBookUseCase, with its own input (BorrowBookCommand) ' +
      'and output (BorrowBookResult). The REST controller and the Kafka listener only see this package: ' +
      'not the service behind it, not the domain model. The domain stays inside the hexagon.',
  },
  'use-case': {
    title: 'Use case',
    role: 'Application',
    text:
      'BorrowBookService implements the driving port. It orchestrates: gather the facts through the driven ports, ' +
      'let the domain decide, save the outcome, and translate the domain decision into the result of the port. ' +
      'Plain Java as well: the bootstrap package hands it to Spring, but the class itself has no idea.',
  },
  'port-out': {
    title: 'Driven ports',
    role: 'Application · outbound',
    text:
      'What the application needs from the outside world, in its own words: BookCatalog, MemberDirectory, ' +
      'LoanRepository. The application defines these interfaces and the infrastructure implements them: ' +
      'dependencies point inwards. Watch the vocabulary: S3 and SQLite are not ports, they are adapters. ' +
      'The port is the interface, an adapter is one implementation of it.',
  },
  'adapter-rest': {
    title: 'REST adapter',
    role: 'Driving adapter',
    text:
      'Translates HTTP into calls of the driving port: JSON body → BorrowBookCommand → BorrowBookResult → status code ' +
      '(201, 409, 404). It holds no business rule: replace it and the library still lends books the same way.',
  },
  'adapter-kafka': {
    title: 'Kafka adapter',
    role: 'Driving adapter',
    text:
      'Same job as the REST controller, different transport. A message from library.loan-requests becomes a ' +
      'command; the decision is published to library.loan-results. Kafka has no caller waiting for an answer, so ' +
      'replying through a topic is handled here, in the adapter. The use case does not change.',
  },
  'adapter-s3': {
    title: 'S3 adapter',
    role: 'Driven adapter',
    text:
      'Implements the three driven ports on top of a single library.json object in a bucket. Writes are ' +
      'optimistic: a conditional PutObject (If-Match on the ETag) guarantees that a concurrent write is never lost.',
  },
  'adapter-sqlite': {
    title: 'SQLite adapter',
    role: 'Driven adapter',
    text:
      'Implements the very same three ports with SQL (Spring JdbcClient) on a schema created by Flyway. ' +
      'A UNIQUE constraint makes the database refuse a second loan of the same book.',
  },
  bootstrap: {
    title: 'Bootstrap',
    role: 'Composition root',
    text:
      'The configurator (or composition root): it assembles the hexagon but contains no business logic. ' +
      'It is the only package that knows every other one. It imports the four adapters; two properties of ' +
      'application.yml decide which ones start. Plugging another adapter means changing one line here, ' +
      'and nothing in the domain or the application.',
  },
}

const VALUE_OBJECT: FileInfo = {
  text:
    'A value object: an immutable record that validates itself in its constructor. A MemberId cannot be ' +
    'passed where a BookId is expected, which two Strings would happily allow.',
  keyLines: ['public record', 'throw new IllegalArgumentException'],
}

const FILE_INFO: Record<string, FileInfo> = {
  // Build
  'pom.xml': {
    text:
      'A single Maven module: the layers of the hexagon are packages under src/main/java. Every library is on ' +
      'the classpath of every package, so the compiler does not protect the domain: ArchitectureTest (ArchUnit) ' +
      'does, and fails the build if the domain or the application ever imports Spring, S3, Kafka or JDBC.',
    keyLines: ['<artifactId>spring-boot-starter-webmvc</artifactId>', '<artifactId>spring-boot-starter-kafka</artifactId>',
      '<artifactId>s3</artifactId>', '<artifactId>sqlite-jdbc</artifactId>', 'enforced by ArchitectureTest'],
  },
  'compose.yaml': {
    text:
      'Local infrastructure to run the application for real: an S3-compatible server (S3Mock) and a Kafka broker. ' +
      'The website itself never runs the code.',
    keyLines: ['image:'],
  },

  // Domain
  'LoanPolicy.java': {
    text:
      'The three lending rules, in the order they apply, then the due date computed from the tier. ' +
      'The result is a LoanDecision value: saying no is a normal business outcome, not an exception.',
    keyLines: ['public LoanDecision decide(', 'if (bookOnLoan)', 'maxActiveLoans()', 'isOverdue(today)', 'loanDuration()'],
  },
  'LoanDecision.java': {
    text:
      'A sealed interface with two cases, Granted and Refused. The use case handles it with an exhaustive switch. ' +
      'It never leaves the hexagon: adapters receive BorrowBookResult, the type of the driving port.',
    keyLines: ['sealed interface', 'record Granted', 'record Refused'],
  },
  'MembershipTier.java': {
    text: 'The policy parameters live with the concept they describe: STANDARD holds 3 books for 14 days, PREMIUM 5 for 28.',
    keyLines: ['STANDARD(', 'PREMIUM('],
  },
  'Loan.java': {
    text: 'A book held by a member. The record protects its own invariant (a loan cannot be due before it starts) and knows when it is overdue.',
    keyLines: ['isOverdue', 'dueOn.isBefore(borrowedOn)'],
  },
  'RefusalReason.java': {
    text: 'Why the library said no, in the domain. The use case maps it to the Reason of the driving port; adapters only ever see that one.',
    keyLines: ['BOOK_ALREADY_ON_LOAN', 'LOAN_QUOTA_REACHED', 'OVERDUE_LOANS'],
  },
  'Member.java': { text: 'A member and their tier. A plain record: no JPA annotation, no JSON annotation.', keyLines: ['public record Member'] },
  'Book.java': { text: 'A book of the catalog. A plain record: no JPA annotation, no JSON annotation.', keyLines: ['public record Book'] },
  'MemberId.java': VALUE_OBJECT,
  'BookId.java': VALUE_OBJECT,
  'LoanId.java': VALUE_OBJECT,

  // Application
  'BorrowBookUseCase.java': {
    text:
      'The driving port. Notice where it lives: in the application package, not in the REST adapter. ' +
      'Its input and output types live next to it, so a caller needs nothing else from the hexagon.',
    keyLines: ['public interface BorrowBookUseCase', 'BorrowBookResult borrow('],
  },
  'BorrowBookCommand.java': {
    text:
      'The input of the use case: an intent, with no trace of HTTP or Kafka. It carries plain values, not domain ' +
      'objects: building MemberId and BookId is the use case\'s job, so adapters do not depend on the domain.',
    keyLines: ['public record BorrowBookCommand', 'throw new IllegalArgumentException'],
  },
  'BorrowBookResult.java': {
    text:
      'The output of the use case, owned by the port. The domain has its own LoanDecision; this type is what the ' +
      'outside world sees. The domain model can change without breaking a single adapter.',
    keyLines: ['sealed interface BorrowBookResult', 'record Granted', 'record Refused', 'enum Reason'],
  },
  'MemberNotFoundException.java': {
    text: 'Part of the contract of the port. Each driving adapter translates it: 404 over REST, a NOT_FOUND message over Kafka.',
    keyLines: ['extends RuntimeException'],
  },
  'BookNotFoundException.java': {
    text: 'Part of the contract of the port. Each driving adapter translates it: 404 over REST, a NOT_FOUND message over Kafka.',
    keyLines: ['extends RuntimeException'],
  },
  'BookCatalog.java': {
    text: 'A driven port: "find a book". The S3 adapter reads it from JSON, the SQLite adapter from a table.',
    keyLines: ['public interface BookCatalog', 'findById('],
  },
  'MemberDirectory.java': {
    text: 'A driven port: "find a member". The S3 adapter reads it from JSON, the SQLite adapter from a table.',
    keyLines: ['public interface MemberDirectory', 'findById('],
  },
  'LoanRepository.java': {
    text:
      'A driven port, in the words of the use case: "loans of a member", "is this book on loan". ' +
      'Not "SELECT", not "GetObject": those belong to the adapters.',
    keyLines: ['public interface LoanRepository', 'findActiveByMember(', 'isBookOnLoan(', 'void save('],
  },
  'BorrowBookService.java': {
    text:
      'Gather the facts, let the domain decide, persist, then answer in the terms of the port. Its dependencies ' +
      'are interfaces (ports) received in the constructor: tests replace them with in-memory fakes, production ' +
      'with S3 or SQLite adapters.',
    keyLines: ['implements BorrowBookUseCase', 'members.findById(', 'policy.decide(', 'loans.save(loan)', 'yield new BorrowBookResult.Granted'],
  },

  // REST adapter
  'RestAdapterConfiguration.java': {
    text: 'The switch of this adapter: it is loaded only when library.adapters.inbound is "rest".',
    keyLines: ['@ConditionalOnProperty', '@Import('],
  },
  'LoanController.java': {
    text:
      'HTTP in, port call, HTTP out. The controller only knows the driving port (interface, command, result): ' +
      'never the service behind it, never the domain. It maps each case of the result to a status code.',
    keyLines: ['private final BorrowBookUseCase borrowBook', 'borrowBook.borrow(', 'case BorrowBookResult.Granted', 'case BorrowBookResult.Refused'],
  },
  'LoanRequest.java': {
    text: 'The JSON body: an HTTP detail. It is converted into a BorrowBookCommand right away, so the JSON format can change without touching the core.',
    keyLines: ['BorrowBookCommand toCommand()'],
  },
  'LoanResponse.java': {
    text: 'The JSON returned on success, mapped from the result of the port. The JSON format belongs to the adapter.',
    keyLines: ['static LoanResponse from('],
  },
  'RestExceptionHandler.java': {
    text: 'Translates the exceptions of the port into RFC 9457 problem details (404, 400).',
    keyLines: ['@ExceptionHandler'],
  },

  // Kafka adapter
  'KafkaAdapterConfiguration.java': {
    text: 'The switch of this adapter: it is loaded only when library.adapters.inbound is "kafka". It also declares the two topics.',
    keyLines: ['@ConditionalOnProperty', 'TopicBuilder.name('],
  },
  'LoanRequestListener.java': {
    text:
      'The Kafka twin of the REST controller: message in, port call, message out. @SendTo publishes the return ' +
      'value on the reply topic: that is how an asynchronous transport answers.',
    keyLines: ['@KafkaListener', '@SendTo', 'borrowBook.borrow('],
  },
  'LoanRequestMessage.java': {
    text: 'The message format of library.loan-requests, converted into the same BorrowBookCommand as over REST.',
    keyLines: ['BorrowBookCommand toCommand()'],
  },
  'LoanResultMessage.java': {
    text: 'The message format of library.loan-results: the Kafka equivalent of the HTTP response, mapped from the result of the port.',
    keyLines: ['enum Status', 'static LoanResultMessage of('],
  },

  // S3 adapter
  'S3AdapterConfiguration.java': {
    text: 'Loaded only when library.adapters.outbound is "s3". Builds the S3 client and the three port implementations.',
    keyLines: ['@ConditionalOnProperty', 'S3BookCatalog s3BookCatalog', 'S3MemberDirectory s3MemberDirectory', 'S3LoanRepository s3LoanRepository'],
  },
  'S3Properties.java': {
    text: 'The library.s3.* settings of application.yml, bound to a typed record.',
    keyLines: ['@ConfigurationProperties'],
  },
  'S3LibraryDocument.java': {
    text:
      'All the S3 specifics in one place. Every write is read-modify-write on library.json, protected by ' +
      'If-Match: if someone else wrote in between, the ETag differs, S3 answers 412 and we retry.',
    keyLines: ['.ifMatch(current.eTag())', '.ifNoneMatch("*")', 'isConcurrentWrite(e)'],
  },
  'LibraryJson.java': {
    text: 'The JSON layout of library.json, kept apart from the domain model: storage format and business model evolve independently.',
    keyLines: ['toDomain()'],
  },
  'S3BookCatalog.java': {
    text: 'Implements the BookCatalog port by reading library.json.',
    keyLines: ['implements BookCatalog'],
  },
  'S3MemberDirectory.java': {
    text: 'Implements the MemberDirectory port by reading library.json.',
    keyLines: ['implements MemberDirectory'],
  },
  'S3LoanRepository.java': {
    text: 'Implements the LoanRepository port on library.json. Same interface as the SQLite version, different technology.',
    keyLines: ['implements LoanRepository', 'document.read().loans()', 'document.update('],
  },
  'library-seed.json': {
    text:
      'Sample data, identical to the SQLite seed. Try it: m-1 can borrow b-1; m-2 has reached their quota; ' +
      'm-3 has an overdue loan; b-2 to b-5 are already on loan.',
    keyLines: ['"loans"'],
  },

  // SQLite adapter
  'SqliteAdapterConfiguration.java': {
    text:
      'Loaded only when library.adapters.outbound is "sqlite". The adapter owns its infrastructure: data source, ' +
      'Flyway migrations, JdbcClient. None of it exists when S3 is plugged in.',
    keyLines: ['@ConditionalOnProperty', 'initMethod = "migrate"', 'JdbcClient.create('],
  },
  'SqliteProperties.java': {
    text: 'The library.sqlite.* settings of application.yml.',
    keyLines: ['@ConfigurationProperties'],
  },
  'SqliteBookCatalog.java': {
    text: 'Implements the BookCatalog port with one SQL query.',
    keyLines: ['implements BookCatalog', 'SELECT'],
  },
  'SqliteMemberDirectory.java': {
    text: 'Implements the MemberDirectory port with one SQL query.',
    keyLines: ['implements MemberDirectory', 'SELECT'],
  },
  'SqliteLoanRepository.java': {
    text: 'Implements the LoanRepository port with SQL. Same interface as the S3 version: the use case cannot tell them apart.',
    keyLines: ['implements LoanRepository', 'SELECT id, book_id', 'SELECT EXISTS', 'INSERT INTO loan'],
  },
  'V1__schema.sql': {
    text: 'The schema, applied by Flyway on startup. The UNIQUE constraint on book_id is a last line of defence against double loans.',
    keyLines: ['CREATE TABLE', 'UNIQUE'],
  },
  'V2__seed.sql': {
    text: 'Sample data, identical to library-seed.json of the S3 adapter.',
    keyLines: ['INSERT INTO'],
  },

  // Bootstrap
  'LibraryApplication.java': {
    text:
      'The composition root, Cockburn\'s configurator: it lists the pieces and contains no logic. The hexagon\'s ' +
      'classes have no Spring annotation, so they are listed here and Spring builds them through their constructor. ' +
      'The four adapters are listed too; each switches itself on or off from application.yml.',
    keyLines: ['@Import({', 'BorrowBookService.class', 'LoanPolicy.class', 'AdapterConfiguration.class', 'Clock clock()'],
  },
  'application.yml': {
    text:
      'The two lines that plug the adapters. Swap an adapter in the diagram: these are the only lines of ' +
      'the whole project that change.',
    keyLines: ['inbound:', 'outbound:'],
  },
}

/** Files whose name is shared by several packages are only looked up by their full path. */
const AMBIGUOUS_NAMES = new Set(['package-info.java'])

export function fileInfo(path: string): FileInfo | undefined {
  const name = path.slice(path.lastIndexOf('/') + 1)
  return FILE_INFO[path] ?? (AMBIGUOUS_NAMES.has(name) ? undefined : FILE_INFO[name])
}
