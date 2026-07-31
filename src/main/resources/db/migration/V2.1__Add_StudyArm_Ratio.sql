ALTER TABLE study_arm
    ADD COLUMN ratio integer;

UPDATE study_arm AS ae
SET ratio = 1;

ALTER TABLE study_arm
    ALTER COLUMN ratio SET NOT NULL;
