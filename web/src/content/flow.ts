import { type Adapters, BRICKS, JAVA } from '../project/bricks'

export interface FlowStep {
  file: string
  /** A substring of the line to highlight in that file. */
  line: string
  caption: string
}

const APP = `${JAVA}/application`
const S3 = `${JAVA}/adapter/out/s3`
const SQLITE = `${JAVA}/adapter/out/sqlite`

/** The journey of one loan request through the hexagon, for the adapters currently plugged in. */
export function loanFlow({ inbound, outbound }: Adapters): FlowStep[] {
  const rest = inbound === 'rest'
  const s3 = outbound === 's3'
  const inboundFile = rest ? BRICKS['adapter-rest'].mainFile : BRICKS['adapter-kafka'].mainFile

  return [
    {
      file: inboundFile,
      line: 'borrowBook.borrow(',
      caption: rest
        ? 'POST /loans arrives. The REST adapter turns the JSON body into a BorrowBookCommand and calls the driving port.'
        : 'A message arrives on library.loan-requests. The Kafka adapter turns it into a BorrowBookCommand and calls the driving port.',
    },
    {
      file: `${APP}/port/in/BorrowBookUseCase.java`,
      line: 'BorrowBookResult borrow(',
      caption: 'The driving port: an interface owned by the application. The adapter knows nothing beyond it.',
    },
    {
      file: `${APP}/service/BorrowBookService.java`,
      line: 'members.findById(',
      caption: 'The use case gathers the facts it needs: the member, the book, the loans. It asks for them through the driven ports.',
    },
    {
      file: `${APP}/port/out/LoanRepository.java`,
      line: 'findActiveByMember(',
      caption: 'A driven port, phrased in the words of the use case. Whoever implements it is invisible from here.',
    },
    {
      file: s3 ? `${S3}/S3LoanRepository.java` : `${SQLITE}/SqliteLoanRepository.java`,
      line: s3 ? 'document.read().loans()' : 'SELECT id, book_id',
      caption: s3
        ? 'The S3 adapter answers by reading library.json from the bucket.'
        : 'The SQLite adapter answers with a SQL query.',
    },
    {
      file: `${APP}/service/BorrowBookService.java`,
      line: 'policy.decide(',
      caption: 'Facts in hand, the use case lets the domain decide.',
    },
    {
      file: BRICKS.domain.mainFile,
      line: 'if (bookOnLoan)',
      caption: 'Pure business rules: book available, quota of the tier, no overdue loan. No I/O, no framework.',
    },
    {
      file: `${APP}/service/BorrowBookService.java`,
      line: 'loans.save(loan)',
      caption: 'If the loan is granted, the use case stores it, again through the LoanRepository port.',
    },
    {
      file: s3 ? `${S3}/S3LibraryDocument.java` : `${SQLITE}/SqliteLoanRepository.java`,
      line: s3 ? '.ifMatch(current.eTag())' : 'INSERT INTO loan',
      caption: s3
        ? 'The S3 adapter writes library.json back, only if nobody changed it meanwhile (If-Match on the ETag).'
        : 'The SQLite adapter inserts a row; a UNIQUE constraint guards against double loans.',
    },
    {
      file: `${APP}/service/BorrowBookService.java`,
      line: 'yield new BorrowBookResult.Granted',
      caption:
        'The domain decision stays inside: the use case answers with BorrowBookResult, the type of the driving port.',
    },
    {
      file: inboundFile,
      line: rest ? 'case BorrowBookResult.Granted' : '@SendTo',
      caption: rest
        ? 'Back in the REST adapter: the decision becomes 201 Created, or 409 with the refusal reason.'
        : 'Back in the Kafka adapter: the decision is published on library.loan-results.',
    },
  ]
}
