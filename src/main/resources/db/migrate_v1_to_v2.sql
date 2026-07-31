-- TODO remaining_assignments beachten nicht deallocation

-- Execute this skript as randomuser
SET search_path = public, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;

-- Change path of dto classes
UPDATE acl_class
SET synonym = 'de.unimuenster.imi.randimi.dto.study.StudyDTO'
WHERE synonym = 'de.wwu.imi.randimi.dto.StudyDTO';

UPDATE acl_class
SET class = 'de.unimuenster.imi.randimi.model.study.Study'
WHERE class = 'de.wwu.imi.randimi.model.study.Study';

-- Update acl
ALTER TABLE acl_entry
    RENAME CONSTRAINT "FKh21ds1gnbca4ra2kvbcdyrh0v" TO audit_entry_fkey1;
ALTER TABLE acl_entry
    RENAME CONSTRAINT "FK3bw8mjhtthsdkefpytbd3460l" TO audit_entry_fkey2;

ALTER TABLE acl_object_identity
    RENAME CONSTRAINT "FKaw7v33k3lchp9ua1rpwmayt8q" TO audit_entry_fkey;
ALTER TABLE acl_object_identity
    RENAME CONSTRAINT "FKh8c49x2r40j84iq1sg1i4wh9p" TO audit_entry_fkey1;
ALTER TABLE acl_object_identity
    RENAME CONSTRAINT "FKlh3n6tlkqmopr3pajpol0vd5l" TO audit_entry_fkey2;

-- Rename audit entry columns
ALTER TABLE "AuditEntry"
    RENAME TO audit_entry;

ALTER TABLE audit_entry
    RENAME COLUMN "auditClass" TO audit_class;

ALTER TABLE audit_entry
    RENAME COLUMN "auditType" TO audit_type;

ALTER TABLE audit_entry
    RENAME COLUMN "dtoContent" TO old_content;

ALTER TABLE audit_entry
    RENAME COLUMN "studyId" TO study_id;

ALTER TABLE audit_entry
    RENAME COLUMN "targetId" TO target_id;

UPDATE audit_entry
SET target_id = study_id
WHERE audit_class = 'STUDY';

UPDATE audit_entry
SET audit_class = 'SUBJECT'
WHERE audit_class = 'RANDOMIZATION_ENTRY';

ALTER TABLE audit_entry
    ALTER COLUMN target_id SET NOT NULL;

ALTER TABLE audit_entry
    RENAME CONSTRAINT "AuditEntry_pkey" TO audit_entry_pkey;

ALTER TABLE audit_entry
    RENAME CONSTRAINT "FKdb9x49wc2gd05wvm97k5jwjh3" TO audit_entry_fkey;

-- Update User
ALTER TABLE "User"
    RENAME TO randimi_user;

ALTER TABLE randimi_user
    RENAME COLUMN "eMail" TO e_mail;
ALTER TABLE randimi_user
    RENAME COLUMN "firstName" TO first_name;
ALTER TABLE randimi_user
    RENAME COLUMN "lastName" TO last_name;

ALTER TABLE randimi_user
    RENAME CONSTRAINT "User_pkey" TO randimi_user_pkey;
ALTER TABLE randimi_user
    RENAME CONSTRAINT "uk_jreodf78a7pl5qidfh43axdfb" TO randimi_user_unique1;
ALTER TABLE randimi_user
    RENAME CONSTRAINT "uk_i6630setqmvrtjt2eqqr1giqj" TO randimi_user_unique2;
ALTER TABLE randimi_user
    RENAME CONSTRAINT "FKjbo6jqessdosh7xsy7uu3jclt" TO randimi_user_fkey;

-- Update UserRole
ALTER TABLE "UserRole"
    RENAME TO user_role;

ALTER TABLE user_role
    RENAME COLUMN "enumRole" TO enum_role;

ALTER TABLE user_role
    RENAME CONSTRAINT "UserRole_pkey" TO user_role_pkey;

-- Update forgot_password_token
ALTER TABLE "ForgotPasswordToken"
    RENAME TO forgot_password_token;

DELETE
FROM forgot_password_token;

ALTER TABLE forgot_password_token
    ADD COLUMN randimi_user_id bigint NOT NULL default 0,
    DROP COLUMN "user";

ALTER TABLE forgot_password_token
    ADD CONSTRAINT forgot_password_token_fkey FOREIGN KEY (randimi_user_id) REFERENCES randimi_user (id);

ALTER TABLE forgot_password_token
    ALTER COLUMN randimi_user_id DROP DEFAULT;

ALTER TABLE forgot_password_token
    RENAME CONSTRAINT "ForgotPasswordToken_pkey" TO forgot_password_token_pkey;

-- Add new color options to, remove mail signature
ALTER TABLE "Settings"
    RENAME TO settings;

ALTER TABLE settings
    RENAME COLUMN "defaultLanguage" TO default_language;

ALTER TABLE settings
    RENAME COLUMN "mailHost" TO mail_host;

ALTER TABLE settings
    RENAME COLUMN "mailPassword" TO mail_password;

ALTER TABLE settings
    RENAME COLUMN "mailPort" TO mail_port;

ALTER TABLE settings
    RENAME COLUMN "mailSMTPAuth" TO mailsmtpauth;

ALTER TABLE settings
    RENAME COLUMN "mailSender" TO mail_sender;

ALTER TABLE settings
    RENAME COLUMN "mailTLS" TO mailtls;

ALTER TABLE settings
    RENAME COLUMN "mailUsername" TO mail_username;

ALTER TABLE settings
    RENAME COLUMN "supportMail" TO support_mail;

ALTER TABLE settings
    RENAME COLUMN "supportPhone" TO support_phone;

ALTER TABLE settings
    ADD COLUMN background_color     character varying(255),
    ADD COLUMN highlight_color      character varying(255),
    ADD COLUMN highlight_text_color character varying(255),
    ADD COLUMN main_color           character varying(255),
    ADD COLUMN main_text_color      character varying(255),
    DROP COLUMN "mailSignatureAddress",
    DROP COLUMN "mailSignaturePhone";

ALTER TABLE settings
    RENAME CONSTRAINT "Settings_pkey" TO settings_pkey;

-- Update language
UPDATE settings
SET default_language = 'GERMAN'
WHERE default_language = 'de_DE';

UPDATE settings
SET default_language = 'ENGLISH'
WHERE default_language = 'en_US';

-- Update color settings
UPDATE settings
SET background_color     = '#FFFFFF',
    highlight_color      = '#97bf0d',
    highlight_text_color = '#FFFFFF',
    main_color           = '#00632e',
    main_text_color      = '#000000';

-- Add new table for footer message settings
CREATE TABLE footer_message_settings
(
    id                   bigint                 NOT NULL,
    current_language     character varying(255) NOT NULL,
    imprint_content      text,
    data_privacy_content text,
    support_content      text,
    settings_id          bigint                 NOT NULL,
    CONSTRAINT footer_message_settings_pkey PRIMARY KEY (id),
    CONSTRAINT footer_message_settings_fkey FOREIGN KEY (settings_id) REFERENCES settings (id)
);

INSERT INTO footer_message_settings (id, current_language, imprint_content, data_privacy_content, support_content,
                                     settings_id)
VALUES (nextval('hibernate_sequence'), 'GERMAN', 'Impressum', 'Datenschutz', 'Hilfe', 5),
       (nextval('hibernate_sequence'), 'ENGLISH', 'Imprint', 'Data Privacy', 'Support', 5);

-- Add table for description and update pseudonym regex templates and
ALTER TABLE "PseudonymRegex"
    RENAME TO pseudonym_regex;

CREATE TABLE pseudonym_regex_description
(
    id                 bigint                    NOT NULL,
    current_language   character varying(255)    NOT NULL,
    description        character varying(524288) NOT NULL,
    name               character varying(255)    NOT NULL,
    pseudonym_regex_id bigint                    NOT NULL,
    CONSTRAINT pseudonym_regex_description_pkey PRIMARY KEY (id),
    CONSTRAINT pseudonym_regex_description_fkey FOREIGN KEY (pseudonym_regex_id) REFERENCES pseudonym_regex (id)
);

INSERT INTO pseudonym_regex_description (id, current_language, description, name, pseudonym_regex_id)
SELECT nextval('hibernate_sequence'), 'GERMAN', description, name, id
FROM pseudonym_regex;

INSERT INTO pseudonym_regex_description (id, current_language, description, name, pseudonym_regex_id)
SELECT nextval('hibernate_sequence'), 'ENGLISH', description, name, id
FROM pseudonym_regex;

ALTER TABLE pseudonym_regex
    ADD COLUMN order_number integer NOT NULL default 0,
    DROP COLUMN description,
    DROP COLUMN name;

ALTER TABLE pseudonym_regex
    ALTER COLUMN order_number DROP DEFAULT;

ALTER TABLE pseudonym_regex
    RENAME CONSTRAINT "PseudonymRegex_pkey" TO pseudonym_regex_pkey;

ALTER TABLE pseudonym_regex
    RENAME CONSTRAINT "FKmafub2u6p59wrlxwk004yj1ma" TO pseudonym_regex_fkey;

CREATE SEQUENCE pseudonym_regex_order_number START WITH 0 MINVALUE 0;

UPDATE pseudonym_regex
SET order_number = nextval('pseudonym_regex_order_number');

DO
$$
    DECLARE
        v_pseudonym_regex_id BIGINT;
    BEGIN
        INSERT INTO pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), nextval('pseudonym_regex_order_number'), '.*', 5)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH',
                E'Matches any pseudonym.\n\n".": Matches any character\n"*": The preceding expression may occur zero or more times',
                'Any pseudonym', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN',
                E'Akzeptiert jedes Pseudonym.\n\n".": Akzeptiert ein beliebiges Zeichen\n"*": Der voranstehende Ausdruck darf beliebig oft vorkommen',
                'Beliebiges Pseudonym', v_pseudonym_regex_id);

        INSERT INTO pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), nextval('pseudonym_regex_order_number'), '\d{2}_\d{3}', 5)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH',
                E'Matches two digits followed by an underscore and three digits, e.g. 27_914.\n\n"\\d": Matches one digit\n"{n}": The preceding expression may occur exactly n times',
                'Numbers', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN',
                E'Akzeptiert zwei Ziffern gefolgt von einem Unterstrich und drei Ziffern, z. B. 27_914.\n\n"\\d": Akzeptiert eine Ziffer\n"{n}":  Der voranstehende Ausdruck darf genau n mal vorkommen',
                'Nummern', v_pseudonym_regex_id);


        INSERT INTO pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), nextval('pseudonym_regex_order_number'), '.+\d{2}_\d{3}', 5)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH',
                E'Matches a not empty prefix followed by two digits, an underscore and three digits, e.g. ukm27_914.\n\n".": Matches any character\n"+": The preceding expression may occur one or more times\n"\\d": Matches one digit\n"{n}": The preceding expression may occur exactly n times',
                'Prefix followed by numbers', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN',
                E'Akzeptiert einen nicht leeren Präfix gefolgt von zwei Ziffern, einem Unterstrich und drei Ziffern, z. B. ukm27_914.\n\n".": Akzeptiert ein beliebiges Zeichen\n"+": Der voranstehende Ausdruck muss mindestens einmal vorkommen\n"\\d": Akzeptiert eine Ziffer\n"{n}":  Der voranstehende Ausdruck muss genau n mal vorkommen',
                'Präfix gefolgt von Zahlen', v_pseudonym_regex_id);


        INSERT INTO pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), nextval('pseudonym_regex_order_number'), '.{5}', 5)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH',
                E'Matches 5 arbitrary characters, e.g. h_2g4.\n\n".": Matches any character\n"{n}": The preceding expression may occur exactly n times',
                'Any pseudonym fixed size', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN',
                E'Akzeptiert 5 beliebige Zeichen, z. B. h_2g4.\n\n".": Akzeptiert ein beliebiges Zeichen\n"{n}":  Der voranstehende Ausdruck darf genau n mal vorkommen',
                'Beliebiges Pseudonym fester Größe', v_pseudonym_regex_id);


        INSERT INTO pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), nextval('pseudonym_regex_order_number'), '[a-zA-Z]\d[a-zA-Z]\d', 5)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH',
                E'Matches a letter followed by a digit, a letter and a digit, e.g. b2u8.\n\n"[]": Matches a single character that is contained within the brackets\n"[a-z]": Specifies a range of characters from "a" to "z"\n"\\d": Matches one digit',
                'Alternating between letters and digits', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN',
                E'Akzeptiert einen Buchstaben gefolgt von einer Ziffer, einem Buchstaben und einer Ziffer, z. B. b2u8.\n\n"[]": Akzeptiert ein Zeichen, dass in den Klammern enthalten ist\n"[a-z]": Spezifiziert einen Bereich von Buchstaben von "a" bis "z"\n"\\d": Akzeptiert eine Ziffer',
                'Abwechselnd Buchstaben und Ziffern', v_pseudonym_regex_id);


        INSERT INTO pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), nextval('pseudonym_regex_order_number'), '.{2,7}', 5)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH',
                E'Matches 2 to 7 arbitrary characters, e.g. 3ay_.\n\n".": Matches any character\n"{m,n}": The preceding expression may occur between m and n times',
                'Any pseudonym of length 2 to 7', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN',
                E'Akzeptiert 2 bis 7 beliebige Zeichen, z. B. 3ay_.\n\n".": Akzeptiert ein beliebiges Zeichen\n"{m,n}": Der voranstehende Ausdruck dar m bis n mal vorkommen',
                'Beliebiges Pseudonym der Länge 2 bis 7', v_pseudonym_regex_id);
    END
$$;

DROP SEQUENCE pseudonym_regex_order_number;

-- Create site
CREATE TABLE site
(
    id              bigint                 NOT NULL,
    api_id          character varying(255) NOT NULL,
    capacity        integer                NOT NULL,
    gui_name        character varying(255) NOT NULL,
    pseudonym_regex character varying(255) NOT NULL,
    random_calls    integer                NOT NULL,
    seed            bigint,
    study_id        bigint,
    CONSTRAINT site_pkey PRIMARY KEY (id),
    CONSTRAINT site_unique1 UNIQUE (api_id, study_id),
    CONSTRAINT site_unique2 UNIQUE (gui_name, study_id)
);

-- Update study
ALTER TABLE "Study"
    RENAME TO study;

ALTER TABLE study
    ADD COLUMN pre_generate_subject_list boolean NOT NULL default true,
    ADD COLUMN stratified_by_site boolean default false;

ALTER TABLE study
    ALTER COLUMN pre_generate_subject_list DROP DEFAULT;
ALTER TABLE study
    ALTER COLUMN stratified_by_site DROP DEFAULT;

ALTER TABLE study
    RENAME COLUMN "activationDate" TO activation_date;

ALTER TABLE study
    RENAME COLUMN "guiName" TO gui_name;

ALTER TABLE study
    RENAME COLUMN "maxBlocksize" TO max_blocksize;

ALTER TABLE study
    RENAME COLUMN "minBlocksize" TO min_blocksize;

ALTER TABLE study
    RENAME COLUMN "pseudonymHandling" TO pseudonym_handling;

ALTER TABLE study
    RENAME COLUMN "randomizationAlgorithm" TO randomization_algorithm;

ALTER TABLE study
    RENAME COLUMN "studySize" TO capacity;

ALTER TABLE study
    RENAME CONSTRAINT "Study_pkey" TO study_pkey;

-- Update stratum
ALTER TABLE "Stratum"
    RENAME TO stratum;

ALTER TABLE stratum
    RENAME COLUMN "orderNumber" TO order_number;

ALTER TABLE stratum
    RENAME COLUMN "stratumType" TO stratum_type;

ALTER TABLE stratum
    RENAME CONSTRAINT "Stratum_pkey" TO stratum_pkey;

ALTER TABLE stratum
    RENAME CONSTRAINT "FKdhf7kso7n9r1eo47sma76oflq" TO stratum_fkey;

UPDATE stratum
SET stratum_type = 'ENUM'
WHERE stratum_type = 'INTERVAL';

-- Update stratum_part_base
ALTER TABLE "StratumPartBase"
    RENAME TO stratum_part_base;

ALTER TABLE stratum_part_base
    RENAME COLUMN "DTYPE" TO dtype;

ALTER TABLE stratum_part_base
    RENAME COLUMN "orderNumber" TO order_number;

ALTER TABLE stratum_part_base
    RENAME COLUMN "intervalBegin" TO interval_begin;

ALTER TABLE stratum_part_base
    RENAME COLUMN "intervalEnd" TO interval_end;

ALTER TABLE stratum_part_base
    ADD COLUMN enum_value character varying(255);

ALTER TABLE stratum_part_base
    RENAME CONSTRAINT "StratumPartBase_pkey" TO stratum_part_base_pkey;

ALTER TABLE stratum_part_base
    RENAME CONSTRAINT "FKgvyyoo689welariykv8egp61h" TO stratum_part_base_fkey;

INSERT INTO stratum_part_base(dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
SELECT 'StratumPartEnumeration', nextval('hibernate_sequence'), NULL, spe."enumList", NULL, NULL, spb.stratum_id
FROM stratum_part_base spb
         INNER JOIN "StratumPartEnumeration_enumList" spe ON spb.id = spe."StratumPartEnumeration_id";

DROP TABLE "StratumPartEnumeration_enumList";

DO
$$
    DECLARE
        stratum bigint;
    BEGIN
        FOR stratum IN SELECT DISTINCT stratum_id FROM stratum_part_base WHERE dtype = 'StratumPartEnumeration'
            LOOP
                CREATE SEQUENCE spb_order_number START WITH 0 MINVALUE 0;

                UPDATE stratum_part_base
                SET order_number = nextval('spb_order_number')
                WHERE stratum_id = stratum AND enum_value IS NOT NULL;

                DROP SEQUENCE spb_order_number;
            END LOOP;
    END
$$;

UPDATE stratum_part_base
SET dtype          = 'StratumPartEnumeration',
    enum_value     = interval_begin || '-' || interval_end,
    interval_begin = NULL,
    interval_end   = NULL
WHERE dtype = 'StratumPartInterval';

DELETE
FROM stratum_part_base
WHERE enum_value IS NULL;

-- Update study_arm
ALTER TABLE "StudyArm"
    RENAME TO study_arm;

ALTER TABLE study_arm
    RENAME COLUMN "guiName" TO gui_name;

ALTER TABLE study_arm
    RENAME COLUMN "orderNumber" TO order_number;

ALTER TABLE study_arm
    RENAME CONSTRAINT "StudyArm_pkey" TO study_arm_pkey;

ALTER TABLE study_arm
    RENAME CONSTRAINT "FKtmwlhf026ouev29igqpqam1e8" TO study_arm_fkey;

-- Rename to subject
ALTER TABLE "RandomizationEntry"
    RENAME TO subject;

ALTER TABLE subject
    RENAME COLUMN "orderNumber" TO order_number;

ALTER TABLE subject
    RENAME COLUMN "randomizationList_id" TO subject_list_id;

ALTER TABLE subject
    RENAME COLUMN "studyArm_id" TO study_arm_id;

ALTER TABLE subject
    ADD COLUMN site_id bigint,
    ADD COLUMN status  character varying(255) NOT NULL default 'PRE_GENERATED';

ALTER TABLE subject
    ALTER COLUMN status DROP DEFAULT;
ALTER TABLE subject
    ALTER COLUMN site_id DROP DEFAULT;

ALTER TABLE subject
    RENAME CONSTRAINT "RandomizationEntry_pkey" TO subject_pkey;

ALTER TABLE subject
    RENAME CONSTRAINT "FKpa78chsithnraminfoofo0vg8" TO subject_fkey1;

ALTER TABLE subject
    RENAME CONSTRAINT "FK7c7nn325spf9hcdftysr9kkxe" TO subject_fkey3;

ALTER TABLE "RandomizationList"
    RENAME TO subject_list;

ALTER TABLE subject_list
    RENAME COLUMN "stratumIntervalCode" TO stratum_interval_code;

ALTER TABLE subject_list
    ADD COLUMN remaining_assignments integer[];

ALTER TABLE subject_list
    RENAME CONSTRAINT "RandomizationList_pkey" TO subject_list_pkey;

ALTER TABLE subject_list
    RENAME CONSTRAINT "FK8u5mdymck8tsp0c8972y2g5b8" TO subject_list_fkey;

--UPDATE subject_list
--SET remaining_assignments = subquery.remaining_assignments
--FROM (SELECT array_agg(number_subjects) AS remaining_assignments, subsubquery.subject_list_id
--      FROM (SELECT count(s) AS number_subjects, sl.id AS subject_list_id
--            FROM subject_list sl
--                     INNER JOIN subject s ON sl.id = s.subject_list_id
--                     INNER JOIN study_arm sa ON s.study_arm_id = sa.id
--            WHERE s.pseudonym is NULL
--            GROUP BY s.study_arm_id, sl.id, sa.order_number
--            ORDER BY sa.order_number) AS subsubquery
--      GROUP BY subsubquery.subject_list_id) as subquery
--WHERE subject_list.id = subquery.subject_list_id;

--DELETE
--FROM subject
--WHERE pseudonym IS NULL

UPDATE subject_list
SET remaining_assignments = subquery.remaining_assignments
FROM (SELECT array_agg(number_subjects) AS remaining_assignments, subsubquery.subject_list_id
      FROM (SELECT 0 AS number_subjects, sl.id AS subject_list_id
            FROM subject_list sl
                     INNER JOIN subject s ON sl.id = s.subject_list_id
                     INNER JOIN study_arm sa ON s.study_arm_id = sa.id
            WHERE s.pseudonym is NULL
            GROUP BY s.study_arm_id, sl.id, sa.order_number
            ORDER BY sa.order_number) AS subsubquery
      GROUP BY subsubquery.subject_list_id) as subquery
WHERE subject_list.id = subquery.subject_list_id;

UPDATE subject_list
SET stratum_interval_code = ''
WHERE stratum_interval_code IS NULL;

UPDATE subject
SET status = 'ACTIVE'
WHERE deleted IS FALSE AND pseudonym IS NOT NULL;

UPDATE subject
SET status = 'DELETED'
WHERE deleted IS TRUE;

INSERT INTO site
SELECT nextval('hibernate_sequence'), location, capacity, location, "pseudonymRegex", "randomCalls", seed, id
FROM (SELECT DISTINCT st.capacity,
                      s.location,
                      st."pseudonymRegex",
                      st."randomCalls",
                      st.seed,
                      st.id
      FROM subject s
               join subject_list sl ON s.subject_list_id = sl.id
               join study st ON sl.study_id = st.id
      WHERE s.location IS NOT NULL) as sss;

DO
$$
    DECLARE
        acl_class_id bigint;
    BEGIN
        INSERT INTO acl_class
        VALUES(nextval('hibernate_sequence'), 'de.unimuenster.imi.randimi.model.study.Site', 'de.unimuenster.imi.randimi.dto.study.SiteDTO')
        RETURNING id INTO acl_class_id;

        INSERT INTO acl_object_identity
        SELECT nextval('hibernate_sequence'), TRUE, si.id, aoi.owner_sid, acl_class_id, NULL::bigint
        FROM site si
                 INNER JOIN study st on st.id = si.study_id
                 INNER JOIN acl_object_identity aoi ON aoi.object_id_identity = st.id;
    END
$$;

UPDATE subject
SET site_id = si.id
FROM site si
WHERE subject.location = si.gui_name;

ALTER TABLE subject
    ADD CONSTRAINT subject_fkey2 FOREIGN KEY (site_id) REFERENCES site (id);

ALTER TABLE study
    DROP COLUMN blocksizes,
    DROP COLUMN "pseudonymRegex",
    DROP COLUMN "randomCalls",
    DROP COLUMN seed;

ALTER TABLE subject
    DROP COLUMN location,
    DROP COLUMN deleted;

-- Create randimi_user_study
CREATE TABLE randimi_user_study
(
    randimi_user_id bigint NOT NULL,
    study_id        bigint NOT NULL,
    CONSTRAINT randimi_user_study_pkey PRIMARY KEY (randimi_user_id, study_id),
    CONSTRAINT randimi_user_study_fkey1 FOREIGN KEY (randimi_user_id) REFERENCES randimi_user (id),
    CONSTRAINT randimi_user_study_fkey2 FOREIGN KEY (study_id) REFERENCES study (id)
);

INSERT INTO randimi_user_study
SELECT DISTINCT ru.id, s.id
FROM acl_entry ae
         JOIN randimi_user ru ON ae.sid = ru.sid
         JOIN acl_object_identity aoi ON ae.acl_object_identity = aoi.id
         JOIN study s ON s.id = aoi.object_id_identity;

-- Add permission for randomizer
DELETE
FROM acl_entry ae
WHERE ae.mask = 0
  AND NOT EXISTS(SELECT *
                 FROM acl_object_identity aoi
                          INNER JOIN acl_entry ae2 ON aoi.id = ae2.acl_object_identity
                 WHERE aoi.object_id_class = 4
                   AND ae.acl_object_identity = aoi.id
                   AND ae2.acl_object_identity = aoi.id
                   AND ae2.mask = 6);

INSERT INTO acl_entry(id, ace_order, audit_failure, audit_success, granting, mask, acl_object_identity, sid)
SELECT nextval('hibernate_sequence'), 1, FALSE, FALSE, TRUE, 0, si_aoi.id, ae.sid
FROM acl_entry ae
         INNER JOIN acl_object_identity s_aoi ON ae.acl_object_identity = s_aoi.id
         INNER JOIN study s ON s.id = s_aoi.object_id_identity
         INNER JOIN site si ON si.study_id = s.id
         INNER JOIN acl_object_identity si_aoi ON si.id = si_aoi.object_id_identity
WHERE ae.mask = 1;

INSERT INTO acl_entry(id, ace_order, audit_failure, audit_success, granting, mask, acl_object_identity, sid)
SELECT nextval('hibernate_sequence'), 1, FALSE, FALSE, TRUE, 1, si_aoi.id, ae.sid
FROM acl_entry ae
         INNER JOIN acl_object_identity s_aoi ON ae.acl_object_identity = s_aoi.id
         INNER JOIN study s ON s.id = s_aoi.object_id_identity
         INNER JOIN site si ON si.study_id = s.id
         INNER JOIN acl_object_identity si_aoi ON si.id = si_aoi.object_id_identity
WHERE ae.mask = 1;

DELETE
FROM acl_entry ae
WHERE ae.mask = 1
  AND EXISTS(SELECT *
             FROM acl_object_identity aoi
             WHERE aoi.object_id_class = 4
               AND ae.acl_object_identity = aoi.id);

ALTER TABLE footer_message_settings
OWNER TO randomuser;

ALTER TABLE pseudonym_regex_description
OWNER TO randomuser;

ALTER TABLE site
OWNER TO randomuser;

ALTER TABLE randimi_user_study
OWNER TO randomuser;
