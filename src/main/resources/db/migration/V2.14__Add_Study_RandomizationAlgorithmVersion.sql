-- Existing studies keep using the first version of their randomization algorithm
ALTER TABLE study
    ADD COLUMN randomization_algorithm_version INTEGER NOT NULL DEFAULT 1;

ALTER TABLE study
    ALTER COLUMN randomization_algorithm_version DROP DEFAULT;
