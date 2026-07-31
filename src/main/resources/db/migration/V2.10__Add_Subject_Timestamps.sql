ALTER TABLE subject
    RENAME COLUMN "timestamp" TO "randomization_timestamp";


ALTER TABLE subject
    ADD COLUMN deletion_timestamp TIMESTAMP without time zone,
    ADD COLUMN release_timestamp TIMESTAMP without time zone;

UPDATE subject AS s
SET deletion_timestamp = ae.timestamp
FROM audit_entry ae
WHERE ae.target_id = s.id AND ae.audit_class = 'SUBJECT' AND ae.audit_type = 'DELETE';

UPDATE subject AS s
SET release_timestamp = ae.timestamp
FROM audit_entry ae
WHERE ae.target_id = s.id AND ae.audit_class = 'SUBJECT' AND ae.audit_type = 'DEALLOCATE_SUBJECT';

UPDATE subject
SET status = 'RELEASED'
WHERE status = 'DEALLOCATED';

UPDATE audit_entry
SET audit_type = 'RELEASED_SUBJECT'
WHERE audit_type = 'DEALLOCATED_SUBJECT';
