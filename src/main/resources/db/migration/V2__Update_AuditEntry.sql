ALTER TABLE audit_entry
    ADD COLUMN username character varying(255);

UPDATE audit_entry AS ae
SET username = rs.username
FROM randimi_user AS rs
WHERE ae.user_id = rs.id;

ALTER TABLE audit_entry
    ALTER COLUMN username SET NOT NULL;

ALTER TABLE audit_entry
    DROP COLUMN user_id;
