CREATE TABLE book (
    id     TEXT PRIMARY KEY,
    title  TEXT NOT NULL,
    author TEXT NOT NULL
);

CREATE TABLE member (
    id   TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    tier TEXT NOT NULL CHECK (tier IN ('STANDARD', 'PREMIUM'))
);

-- A book can only be held by one member at a time: the database enforces it too.
CREATE TABLE loan (
    id          TEXT PRIMARY KEY,
    book_id     TEXT NOT NULL UNIQUE REFERENCES book (id),
    member_id   TEXT NOT NULL REFERENCES member (id),
    borrowed_on TEXT NOT NULL,
    due_on      TEXT NOT NULL
);

CREATE INDEX loan_member_id ON loan (member_id);
