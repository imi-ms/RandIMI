-- Modify global settings
ALTER TABLE settings
    ADD COLUMN gravatar_enabled boolean NOT NULL DEFAULT false,
	ADD COLUMN gravatar_option VARCHAR(255) NOT NULL DEFAULT 'MP';

-- Modify user settings
ALTER TABLE randimi_user
    ADD COLUMN gravatar_enabled boolean NOT NULL DEFAULT false;
