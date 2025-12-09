-- ShedLock distributed scheduler locking table
-- Ensures only one instance of a scheduled task runs at a time

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);

-- Index for efficient lock cleanup queries
CREATE INDEX IF NOT EXISTS idx_shedlock_lock_until ON shedlock(lock_until);

-- Add comments for documentation
COMMENT ON TABLE shedlock IS 'Distributed lock table for scheduled task coordination across multiple application instances';
COMMENT ON COLUMN shedlock.name IS 'Unique name of the scheduled task/lock';
COMMENT ON COLUMN shedlock.lock_until IS 'Timestamp until which the lock is valid';
COMMENT ON COLUMN shedlock.locked_at IS 'Timestamp when the lock was acquired';
COMMENT ON COLUMN shedlock.locked_by IS 'Identifier of the instance holding the lock (hostname + thread)';
