-- Same sample data as the S3 adapter's library-seed.json.
INSERT INTO book (id, title, author) VALUES
    ('b-1', 'Domain-Driven Design', 'Eric Evans'),
    ('b-2', 'Clean Architecture', 'Robert C. Martin'),
    ('b-3', 'Refactoring', 'Martin Fowler'),
    ('b-4', 'Growing Object-Oriented Software, Guided by Tests', 'Steve Freeman, Nat Pryce'),
    ('b-5', 'Working Effectively with Legacy Code', 'Michael Feathers'),
    ('b-6', 'The Pragmatic Programmer', 'David Thomas, Andrew Hunt'),
    ('b-7', 'Patterns of Enterprise Application Architecture', 'Martin Fowler'),
    ('b-8', 'Implementing Domain-Driven Design', 'Vaughn Vernon');

INSERT INTO member (id, name, tier) VALUES
    ('m-1', 'Ada Lovelace', 'STANDARD'),
    ('m-2', 'Linus Torvalds', 'STANDARD'),
    ('m-3', 'Grace Hopper', 'PREMIUM');

INSERT INTO loan (id, book_id, member_id, borrowed_on, due_on) VALUES
    ('l-1', 'b-2', 'm-2', '2026-09-25', '2026-10-09'),
    ('l-2', 'b-3', 'm-2', '2026-09-25', '2026-10-09'),
    ('l-3', 'b-4', 'm-2', '2026-09-25', '2026-10-09'),
    ('l-4', 'b-5', 'm-3', '2026-01-05', '2026-02-02');
