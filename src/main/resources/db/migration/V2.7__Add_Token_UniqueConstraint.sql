ALTER TABLE ONLY forgot_password_token
    ADD CONSTRAINT forgot_password_token_unique UNIQUE (token);
