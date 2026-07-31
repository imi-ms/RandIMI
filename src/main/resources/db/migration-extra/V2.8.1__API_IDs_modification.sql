-- Study
UPDATE study
SET api_id             = api_id || ' - API ID',
    synchronize_api_id = false;

-- Site
UPDATE site
SET api_id = api_id || ' - API ID',
    synchronize_api_id = false;

-- StudyArm
UPDATE study_arm
SET api_id             = api_id || ' - API ID',
    synchronize_api_id = false;

-- Stratum
UPDATE stratum
SET api_id             = api_id || ' - API ID',
    synchronize_api_id = false;

-- StratumPart
UPDATE stratum_part_base
SET api_id             = api_id || ' - API ID',
    synchronize_api_id = false;
