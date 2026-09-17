CREATE TABLE IF NOT EXISTS equipment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT,
    status INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_equipment_status ON equipment (status);
CREATE INDEX IF NOT EXISTS idx_equipment_category ON equipment (category);

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL UNIQUE,
    username TEXT NOT NULL,
    role INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS borrow_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    equipment_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    borrow_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expect_return_time DATETIME,
    actual_return_time DATETIME,
    status INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_borrow_record_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment (id) ON DELETE RESTRICT,
    CONSTRAINT fk_borrow_record_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_borrow_records_equipment
    ON borrow_records (equipment_id);
CREATE INDEX IF NOT EXISTS idx_borrow_records_user
    ON borrow_records (user_id);
CREATE INDEX IF NOT EXISTS idx_borrow_records_status
    ON borrow_records (status);
