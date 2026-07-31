-- Add site id
ALTER TABLE stratum_part_base
    ADD COLUMN site_id bigint;

-- Change type of stratum parts to site and set id
UPDATE stratum_part_base AS spb
SET dtype = 'StratumPartSite',
    enum_value = null,
    site_id = sit.id
FROM stratum str
    INNER JOIN study stu ON stu.id = str.study_id
    INNER JOIN site sit ON sit.study_id = stu.id
WHERE spb.stratum_id = str.id AND  str.name = 'location' AND spb.enum_value = sit.gui_name;

-- Change type of stratum to site
UPDATE stratum
SET stratum_type = 'SITE'
WHERE name = 'location';
