-- Study
ALTER TABLE study
    ADD COLUMN api_id             character varying(255),
    ADD COLUMN synchronize_api_id boolean;

UPDATE study
SET api_id             = id,
    synchronize_api_id = false;

ALTER TABLE study
    ALTER COLUMN api_id SET NOT NULL,
    ALTER COLUMN synchronize_api_id SET NOT NULL,
    ADD CONSTRAINT study_unique_gui_name UNIQUE (gui_name),
    ADD CONSTRAINT study_unique_api_id UNIQUE (api_id);

-- Site
ALTER TABLE site
    ADD COLUMN synchronize_api_id boolean;

UPDATE site
SET synchronize_api_id = false;

ALTER TABLE site
    ALTER COLUMN synchronize_api_id SET NOT NULL;

-- StudyArm
ALTER TABLE study_arm
    ADD COLUMN api_id character varying(255),
    ADD COLUMN synchronize_api_id boolean;

UPDATE study_arm
SET api_id             = gui_name,
    synchronize_api_id = true;

ALTER TABLE study_arm
    ALTER COLUMN api_id SET NOT NULL,
    ALTER COLUMN synchronize_api_id SET NOT NULL,
    ADD CONSTRAINT study_arm_unique_gui_name UNIQUE (study_id, gui_name),
    ADD CONSTRAINT study_arm_unique_api_id UNIQUE (study_id, api_id);

-- Stratum
ALTER TABLE stratum
    ADD COLUMN api_id             character varying(255),
    ADD COLUMN synchronize_api_id boolean;

UPDATE stratum
SET api_id             = name,
    synchronize_api_id = true;

ALTER TABLE stratum
    ALTER COLUMN api_id SET NOT NULL,
    ALTER COLUMN synchronize_api_id SET NOT NULL,
    ADD CONSTRAINT stratum_unique_name UNIQUE (study_id, name),
    ADD CONSTRAINT stratum_unique_api_id UNIQUE (study_id, api_id);

-- StratumPart
ALTER TABLE stratum_part_base
    ADD COLUMN api_id             character varying(255),
    ADD COLUMN synchronize_api_id boolean;

UPDATE stratum_part_base
SET api_id             = enum_value,
    synchronize_api_id = true
WHERE dtype = 'StratumPartEnumeration';
