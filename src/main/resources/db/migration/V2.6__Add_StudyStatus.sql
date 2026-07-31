ALTER TABLE study
    ADD COLUMN status character varying(255) NOT NULL DEFAULT 'CREATED';

UPDATE study
SET status = 'ACTIVE'
WHERE activation_date IS NOT NULL;

ALTER TABLE study
    ALTER COLUMN status DROP DEFAULT;
