-- Create table to link subject lists with stratum parts
CREATE TABLE subject_lists_stratum_parts (
    stratum_part_base_id bigint NOT NULL,
    subject_list_id      bigint NOT NULL,
    CONSTRAINT subject_lists_stratum_parts_fkey1 FOREIGN KEY (stratum_part_base_id) REFERENCES stratum_part_base (id),
    CONSTRAINT subject_lists_stratum_parts_fkey2 FOREIGN KEY (subject_list_id) REFERENCES subject_list (id)
);

-- Insert links
INSERT INTO subject_lists_stratum_parts(stratum_part_base_id, subject_list_id)
SELECT spb.id, sl.id
FROM subject_list sl
    JOIN study stu ON stu.id = sl.study_id
    JOIN stratum str ON str.study_id = stu.id
    JOIN stratum_part_base spb ON spb.stratum_id = str.id
WHERE sl.stratum_interval_code LIKE '%' || str.name || '-' || spb.enum_value || '%'
    OR sl.stratum_interval_code LIKE '%' || str.name || '-' || TO_CHAR(spb.interval_begin, 'FM9999999990.0') || '-' || TO_CHAR(spb.interval_end, 'FM9999999990.0') || '%';

-- Remove old stratum interval code
ALTER TABLE subject_list
    DROP COLUMN stratum_interval_code;
