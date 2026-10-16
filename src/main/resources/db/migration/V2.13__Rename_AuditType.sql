-- Fix for migration V2.10 where audit_type was incorrectly migrated from DEALLOCATED_SUBJECT to RELEASED_SUBJECT
UPDATE audit_entry
SET audit_type = 'RELEASE_SUBJECT'
WHERE audit_type = 'DEALLOCATE_SUBJECT';
