--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

--CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

--COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: acl_class; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE acl_class (
    id bigint NOT NULL,
    class character varying(255) NOT NULL,
    synonym character varying(255)
);


--
-- Name: acl_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE acl_entry (
    id bigint NOT NULL,
    ace_order integer,
    audit_failure boolean,
    audit_success boolean,
    granting boolean,
    mask integer,
    acl_object_identity bigint,
    sid bigint
);


--
-- Name: acl_object_identity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE acl_object_identity (
    id bigint NOT NULL,
    entries_inheriting boolean,
    object_id_identity bigint,
    owner_sid bigint,
    object_id_class bigint,
    parent_object bigint
);


--
-- Name: acl_sid; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE acl_sid (
    id bigint NOT NULL,
    principal boolean,
    sid character varying(255)
);


--
-- Name: audit_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE audit_entry (
    id bigint NOT NULL,
    audit_class character varying(255) NOT NULL,
    audit_type character varying(255) NOT NULL,
    content text,
    old_content text,
    reason character varying(255),
    study_id bigint NOT NULL,
    target_id bigint NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: footer_message_settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE IF NOT EXISTS footer_message_settings
(
    id                   bigint                 NOT NULL,
    current_language     character varying(255) NOT NULL,
    imprint_content      text,
    data_privacy_content text,
    support_content      text,
    settings_id          bigint                 NOT NULL
);


--
-- Name: forgot_password_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE forgot_password_token (
    id bigint NOT NULL,
    randimi_user_id bigint NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    token character varying(255) NOT NULL
);


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pseudonym_regex; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pseudonym_regex
(
    id           bigint                 NOT NULL,
    order_number integer                NOT NULL,
    regex        character varying(255) NOT NULL,
    settings_id  bigint                 NOT NULL
);


--
-- Name: pseudonym_regex_description; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pseudonym_regex_description
(
    id                 bigint                    NOT NULL,
    current_language   character varying(255)    NOT NULL,
    description        character varying(524288) NOT NULL,
    name               character varying(255)    NOT NULL,
    pseudonym_regex_id bigint                    NOT NULL
);


--
-- Name: randimi_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE randimi_user (
    id bigint NOT NULL,
    e_mail character varying(255) NOT NULL,
    enabled boolean NOT NULL,
    first_name character varying(255) NOT NULL,
    invitation_timestamp timestamp without time zone,
    invitation_token character varying(255),
    last_name character varying(255) NOT NULL,
    password character varying(255),
    username character varying(255) NOT NULL,
    sid bigint NOT NULL
);


--
-- Name: randimi_user_study; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE randimi_user_study (
    randimi_user_id bigint NOT NULL,
    study_id bigint NOT NULL
);


--
-- Name: settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE settings (
    id bigint NOT NULL,
    background_color character varying(255),
    default_language character varying(255) NOT NULL,
    highlight_color character varying(255),
    highlight_text_color character varying(255),
    mail_host character varying(255) NOT NULL,
    mail_password character varying(255),
    mail_port integer NOT NULL,
    mailsmtpauth boolean NOT NULL,
    mail_sender character varying(255) NOT NULL,
    mailtls boolean NOT NULL,
    mail_username character varying(255) NOT NULL,
    main_color character varying(255),
    main_text_color character varying(255),
    support_mail character varying(255) NOT NULL,
    support_phone character varying(255) NOT NULL
);


--
-- Name: site; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE site (
    id bigint NOT NULL,
    api_id character varying(255) NOT NULL,
    capacity integer NOT NULL,
    gui_name character varying(255) NOT NULL,
    pseudonym_regex character varying(255) NOT NULL,
    random_calls integer NOT NULL,
    seed bigint,
    study_id bigint
);


--
-- Name: subject; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE subject (
    id bigint NOT NULL,
    order_number integer NOT NULL,
    pseudonym character varying(255),
    site_id bigint,
    status character varying(255) NOT NULL,
    subject_list_id bigint NOT NULL,
    study_arm_id bigint NOT NULL,
    "timestamp" timestamp without time zone
);


--
-- Name: subject_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE subject_list (
    id bigint NOT NULL,
    remaining_assignments integer[],
    stratum_interval_code character varying(255),
    study_id bigint NOT NULL
);


--
-- Name: stratum; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE stratum (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    order_number integer NOT NULL,
    stratum_type character varying(255) NOT NULL,
    study_id bigint NOT NULL
);


--
-- Name: stratum_part_base; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE stratum_part_base (
    dtype character varying(31) NOT NULL,
    id bigint NOT NULL,
    order_number integer,
    enum_value character varying(255),
    interval_begin real,
    interval_end real,
    stratum_id bigint
);


--
-- Name: study; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE study (
    id bigint NOT NULL,
    activation_date timestamp without time zone,
    capacity integer NOT NULL,
    description character varying(524288),
    gui_name character varying(255) NOT NULL,
    max_blocksize integer,
    min_blocksize integer,
    pre_generate_subject_list boolean NOT NULL,
    pseudonym_handling character varying(255) NOT NULL,
    randomization_algorithm character varying(255) NOT NULL,
    stratified_by_site boolean
);


--
-- Name: study_arm; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE study_arm (
    id bigint NOT NULL,
    gui_name character varying(255) NOT NULL,
    order_number integer NOT NULL,
    study_id bigint NOT NULL
);


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE user_role (
    id bigint NOT NULL,
    enum_role character varying(255),
    user_id bigint
);


--
-- Name: acl_class acl_class_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_class
    ADD CONSTRAINT acl_class_pkey PRIMARY KEY (id);


--
-- Name: acl_entry acl_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT acl_entry_pkey PRIMARY KEY (id);


--
-- Name: acl_object_identity acl_object_identity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT acl_object_identity_pkey PRIMARY KEY (id);


--
-- Name: acl_sid acl_sid_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_sid
    ADD CONSTRAINT acl_sid_pkey PRIMARY KEY (id);


--
-- Name: audit_entry audit_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY audit_entry
    ADD CONSTRAINT audit_entry_pkey PRIMARY KEY (id);


--
-- Name: footer_message_settings footer_message_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY footer_message_settings
    ADD CONSTRAINT footer_message_settings_pkey PRIMARY KEY (id);


--
-- Name: forgot_password_token forgot_password_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY forgot_password_token
    ADD CONSTRAINT forgot_password_token_pkey PRIMARY KEY (id);


--
-- Name: pseudonym_regex pseudonym_regex_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pseudonym_regex
    ADD CONSTRAINT pseudonym_regex_pkey PRIMARY KEY (id);


--
-- Name: pseudonym_regex_description pseudonym_regex_description_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pseudonym_regex_description
    ADD CONSTRAINT pseudonym_regex_description_pkey PRIMARY KEY (id);


--
-- Name: randimi_user randimi_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY randimi_user
    ADD CONSTRAINT randimi_user_pkey PRIMARY KEY (id);


--
-- Name: randimi_user_study randimi_user_study_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY randimi_user_study
    ADD CONSTRAINT randimi_user_study_pkey PRIMARY KEY (randimi_user_id, study_id);


--
-- Name: subject subject_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_pkey PRIMARY KEY (id);


--
-- Name: subject_list subject_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subject_list
    ADD CONSTRAINT subject_list_pkey PRIMARY KEY (id);


--
-- Name: settings settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY settings
    ADD CONSTRAINT settings_pkey PRIMARY KEY (id);


--
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- Name: stratum_part_base stratum_part_base_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stratum_part_base
    ADD CONSTRAINT stratum_part_base_pkey PRIMARY KEY (id);


--
-- Name: stratum stratum_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stratum
    ADD CONSTRAINT stratum_pkey PRIMARY KEY (id);


--
-- Name: study_arm study_arm_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY study_arm
    ADD CONSTRAINT study_arm_pkey PRIMARY KEY (id);


--
-- Name: study study_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY study
    ADD CONSTRAINT study_pkey PRIMARY KEY (id);


--
-- Name: user_role user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (id);


--
-- Name: stratum stratum_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stratum
    ADD CONSTRAINT stratum_fkey FOREIGN KEY (study_id) REFERENCES study(id);


--
-- Name: study_arm study_arm_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY study_arm
    ADD CONSTRAINT study_arm_fkey FOREIGN KEY (study_id) REFERENCES study(id);


--
-- Name: randimi_user randimi_user_unique1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY randimi_user
    ADD CONSTRAINT randimi_user_unique1 UNIQUE (username);


--
-- Name: randimi_user randimi_user_unique2; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY randimi_user
    ADD CONSTRAINT randimi_user_unique2 UNIQUE (sid);

--
-- Name: site site_unique1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site
    ADD CONSTRAINT site_unique1 UNIQUE (api_id, study_id);

--
-- Name: site site_unique2; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site
    ADD CONSTRAINT site_unique2 UNIQUE (gui_name, study_id);


--
-- Name: acl_object_identity acl_object_identity_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT acl_object_identity_fkey FOREIGN KEY (parent_object) REFERENCES acl_object_identity(id);


--
-- Name: pseudonym_regex pseudonym_regex_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pseudonym_regex
    ADD CONSTRAINT pseudonym_regex_fkey FOREIGN KEY (settings_id) REFERENCES settings(id);


--
-- Name: pseudonym_regex_description pseudonym_regex_description_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pseudonym_regex_description
    ADD CONSTRAINT pseudonym_regex_description_fkey FOREIGN KEY (pseudonym_regex_id) REFERENCES pseudonym_regex (id);


--
-- Name: stratum_part_base stratum_part_base_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stratum_part_base
    ADD CONSTRAINT stratum_part_base_fkey FOREIGN KEY (stratum_id) REFERENCES stratum(id);


--
-- Name: user_role user_role_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_fkey FOREIGN KEY (user_id) REFERENCES randimi_user(id);


--
-- Name: acl_entry acl_entry_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT acl_entry_fkey1 FOREIGN KEY (sid) REFERENCES acl_sid(id);


--
-- Name: site site_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site
    ADD CONSTRAINT site_fkey1 FOREIGN KEY (study_id) REFERENCES study(id);


--
-- Name: site site_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site
    ADD CONSTRAINT site_fkey2 FOREIGN KEY (study_id) REFERENCES study(id);


--
-- Name: acl_object_identity acl_object_identity_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT acl_object_identity_fkey1 FOREIGN KEY (object_id_class) REFERENCES acl_class(id);


--
-- Name: subject_list subject_list_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subject_list
    ADD CONSTRAINT subject_list_fkey FOREIGN KEY (study_id) REFERENCES study(id);


--
-- Name: audit_entry audit_entry_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY audit_entry
    ADD CONSTRAINT audit_entry_fkey FOREIGN KEY (user_id) REFERENCES randimi_user(id);


--
-- Name: acl_object_identity acl_object_identity_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT acl_object_identity_fkey2 FOREIGN KEY (owner_sid) REFERENCES acl_sid(id);


--
-- Name: acl_entry acl_entry_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT acl_entry_fkey2 FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity(id);


--
-- Name: footer_message_settings footer_message_settings_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY footer_message_settings
    ADD CONSTRAINT footer_message_settings_fkey FOREIGN KEY (settings_id) REFERENCES settings (id);


--
-- Name: forgot_password_token forgot_password_token_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY forgot_password_token
    ADD CONSTRAINT forgot_password_token_fkey FOREIGN KEY (randimi_user_id) REFERENCES randimi_user(id);


--
-- Name: randimi_user randimi_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY randimi_user
    ADD CONSTRAINT randimi_user_fkey FOREIGN KEY (sid) REFERENCES acl_sid(id);


--
-- Name: randimi_user_study randimi_user_study_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY randimi_user_study
    ADD CONSTRAINT randimi_user_study_fkey1 FOREIGN KEY (randimi_user_id) REFERENCES randimi_user(id);


--
-- Name: randimi_user_study randimi_user_study_fkey2; Type: FK CONSTRAINT; Schema: public; Owner:
---

ALTER TABLE ONLY randimi_user_study
    ADD CONSTRAINT randimi_user_study_fkey2 FOREIGN KEY (study_id) REFERENCES study(id);


--
-- Name: subject subject_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_fkey1 FOREIGN KEY (study_arm_id) REFERENCES study_arm(id);


--
-- Name: subject subject_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_fkey2 FOREIGN KEY (site_id) REFERENCES site(id);


--
-- Name: subject subject_fkey3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_fkey3 FOREIGN KEY (subject_list_id) REFERENCES subject_list(id);


--
-- PostgreSQL database dump complete
--

