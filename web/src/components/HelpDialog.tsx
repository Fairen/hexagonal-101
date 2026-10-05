import { useRef } from 'react'

/** The "?" button of the header and the modal describing what the project does. */
export function HelpButton() {
  const dialog = useRef<HTMLDialogElement>(null)

  return (
    <>
      <button
        type="button"
        className="icon-button"
        aria-label="About this project"
        title="About this project"
        aria-haspopup="dialog"
        onClick={() => dialog.current?.showModal()}
      >
        <i className="codicon codicon-question" aria-hidden="true" />
      </button>

      <dialog
        ref={dialog}
        className="help-dialog"
        aria-labelledby="help-title"
        // A click on the backdrop lands on the dialog element itself: close.
        onClick={(event) => event.target === dialog.current && dialog.current.close()}
      >
        <div className="help-content">
          <header className="help-header">
            <h2 id="help-title">About this project</h2>
            <form method="dialog">
              <button type="submit" className="help-close" aria-label="Close">
                <i className="codicon codicon-close" aria-hidden="true" />
              </button>
            </form>
          </header>

          <p className="help-lead">
            A small but real Spring Boot service, built with hexagonal architecture: a library that lends books.
            The code compiles and is tested; this site shows it, it does not run it.
          </p>

          <section>
            <h3>Borrowing a book</h3>
            <p>A member asks to borrow a book. The library refuses when:</p>
            <ol>
              <li>the book is already on loan to someone,</li>
              <li>the member already holds as many books as their tier allows,</li>
              <li>the member has a loan past its due date.</li>
            </ol>
            <p>Otherwise the loan is granted, with a due date that depends on the member&apos;s tier:</p>
            <table>
              <thead>
                <tr>
                  <th scope="col">Tier</th>
                  <th scope="col">Books at once</th>
                  <th scope="col">Loan duration</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Standard</td>
                  <td>3</td>
                  <td>14 days</td>
                </tr>
                <tr>
                  <td>Premium</td>
                  <td>5</td>
                  <td>28 days</td>
                </tr>
              </tbody>
            </table>
            <p>An unknown member or book is reported as not found.</p>
          </section>

          <section>
            <h3>Two ways in</h3>
            <dl>
              <dt>REST</dt>
              <dd>
                <code>POST /loans {'{"memberId", "bookId"}'}</code> answers <code>201</code> with the loan,{' '}
                <code>409</code> with the refusal reason, or <code>404</code>.
              </dd>
              <dt>Kafka</dt>
              <dd>
                A message on <code>library.loan-requests</code> gets its answer on <code>library.loan-results</code>:{' '}
                GRANTED, REFUSED, NOT_FOUND or INVALID.
              </dd>
            </dl>
          </section>

          <section>
            <h3>Two ways to store</h3>
            <dl>
              <dt>S3</dt>
              <dd>
                Books, members and loans in one <code>library.json</code> file in a bucket. Concurrent writes are
                safe: a write only succeeds if nobody changed the file meanwhile.
              </dd>
              <dt>SQLite</dt>
              <dd>Three tables, created on startup. The database itself forbids lending the same book twice.</dd>
            </dl>
          </section>

          <section>
            <h3>Sample data</h3>
            <ul>
              <li>
                <code>m-1</code> Ada Lovelace, standard, no loan: can borrow any free book (<code>b-1</code>,{' '}
                <code>b-6</code> to <code>b-8</code>).
              </li>
              <li>
                <code>m-2</code> Linus Torvalds, standard, holds 3 books: quota reached.
              </li>
              <li>
                <code>m-3</code> Grace Hopper, premium, one loan overdue since February: refused until returned.
              </li>
              <li>
                <code>b-2</code> to <code>b-5</code> are already on loan.
              </li>
            </ul>
          </section>

          <section>
            <h3>Using this site</h3>
            <ul>
              <li>Open a file in the explorer: its brick lights up in the diagram, with an explanation below.</li>
              <li>Click a brick of the diagram to open its main file.</li>
              <li>
                Click <strong>⇄ plug in</strong> on an adapter to swap REST ↔ Kafka or S3 ↔ SQLite: only two lines of{' '}
                <code>application.yml</code> change.
              </li>
              <li>
                <strong>Follow a loan request</strong> walks you through every layer, file by file.
              </li>
            </ul>
          </section>
        </div>
      </dialog>
    </>
  )
}
