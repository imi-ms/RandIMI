ALTER TABLE randimi_user
    ADD COLUMN invited_by character varying(255),
    ADD CONSTRAINT randimi_user_invited_by_fkey FOREIGN KEY (invited_by) REFERENCES randimi_user (username);
