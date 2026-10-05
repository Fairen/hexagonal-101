package dev.hexagonal101.library.adapter.out.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;

import dev.hexagonal101.library.application.port.out.LoanRepository;
import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.LoanId;
import dev.hexagonal101.library.domain.MemberId;

/** {@link LoanRepository} port, implemented with SQL on the {@code loan} table. */
class SqliteLoanRepository implements LoanRepository {

    private final JdbcClient jdbc;

    SqliteLoanRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Loan> findActiveByMember(MemberId memberId) {
        return jdbc.sql("SELECT id, book_id, member_id, borrowed_on, due_on FROM loan WHERE member_id = :memberId")
                .param("memberId", memberId.value())
                .query((row, rowNum) -> toLoan(row))
                .list();
    }

    @Override
    public boolean isBookOnLoan(BookId bookId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM loan WHERE book_id = :bookId)")
                .param("bookId", bookId.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public void save(Loan loan) {
        jdbc.sql("""
                INSERT INTO loan (id, book_id, member_id, borrowed_on, due_on)
                VALUES (:id, :bookId, :memberId, :borrowedOn, :dueOn)
                """)
                .param("id", loan.id().value())
                .param("bookId", loan.bookId().value())
                .param("memberId", loan.memberId().value())
                .param("borrowedOn", loan.borrowedOn().toString())
                .param("dueOn", loan.dueOn().toString())
                .update();
    }

    private static Loan toLoan(ResultSet row) throws SQLException {
        return new Loan(
                new LoanId(row.getString("id")),
                new BookId(row.getString("book_id")),
                new MemberId(row.getString("member_id")),
                LocalDate.parse(row.getString("borrowed_on")),
                LocalDate.parse(row.getString("due_on")));
    }
}
