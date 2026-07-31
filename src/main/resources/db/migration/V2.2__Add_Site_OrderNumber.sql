-- Add column for order number
ALTER TABLE site
    ADD COLUMN order_number integer NOT NULL default 0;

ALTER TABLE site
    ALTER COLUMN order_number DROP DEFAULT;

-- Set the order number of all sites per study
DO
$$
    DECLARE
        v_study_entry public.study;
    BEGIN
        FOR v_study_entry IN SELECT * FROM study
            LOOP
                CREATE SEQUENCE site_order_number START WITH 0 MINVALUE 0;

                UPDATE site
                SET order_number = nextval('site_order_number')
                WHERE study_id = v_study_entry.id;

                DROP SEQUENCE site_order_number;
            END LOOP;
    END
$$;
