DO
$$
    DECLARE
        v_study_id BIGINT;
        v_stratum_id BIGINT;
        v_stratum_number INTEGER;
        v_site_name character varying(255);
    BEGIN
        -- Set parameter
        SELECT 27
        INTO v_study_id;

        SELECT gui_name
        INTO v_site_name
        FROM site
        WHERE study_id = v_study_id;

        SELECT COUNT(*)
        INTO v_stratum_number
        FROM stratum
        WHERE study_id = v_study_id;

        -- Set study to stratified by site
        UPDATE study
        SET stratified_by_site = true
        WHERE id = v_study_id;

        -- Create stratum
        INSERT INTO stratum(id, name, order_number, stratum_type, study_id)
        VALUES (nextval('hibernate_sequence'), 'location', v_stratum_number, 'ENUM', v_study_id)
        RETURNING id INTO v_stratum_id;

        INSERT INTO stratum_part_base(dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
        VALUES ('StratumPartEnumeration', nextval('hibernate_sequence'), 0, v_site_name, NULL, NULL, v_stratum_id);

        UPDATE subject_list
        SET stratum_interval_code = CONCAT(stratum_interval_code, '_', 'location-', v_site_name)
        WHERE study_id = v_study_id;
    END
$$;
