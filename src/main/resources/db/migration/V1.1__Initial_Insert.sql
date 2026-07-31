-- Initial user
INSERT INTO public.acl_sid (id, principal, sid) VALUES
	(3, true, 'ADMIN');

INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username, invitation_timestamp, invitation_token, sid) VALUES 
	(1, 'randimi@uni-muenster.de', true, '$2a$10$ktXRYklVAt/G5Cg/sWu0de95Chd4LAWhvenTEjQvx.SDGiL2Ls8Mq', 'RandIMI', 'Admin', 'ADMIN', NULL, NULL, 3);

INSERT INTO public.user_role (id, enum_role, user_id) VALUES 
	(2, 'ROLE_ADMIN', 1);

INSERT INTO public.acl_class (id, class, synonym) VALUES 
	(4, 'de.unimuenster.imi.randimi.model.study.Study', 'de.unimuenster.imi.randimi.dto.study.StudyDTO'),
	(5, 'de.unimuenster.imi.randimi.model.study.Site', 'de.unimuenster.imi.randimi.dto.study.SiteDTO');

INSERT INTO public.settings (id, background_color, default_language, highlight_color, highlight_text_color, mail_host,
                             mail_password, mail_port, mailsmtpauth, mail_sender, mailtls, mail_username, main_color,
                             main_text_color, support_mail, support_phone)
VALUES (6, '#FFFFFF', 'GERMAN', '#97bf0d', '#FFFFFF', 'secmail.uni-muenster.de', NULL, 587, true,
        'randimi@uni-muenster.de', true, 'randimi', '#00632e', '#000000',
        'randimi@uni-muenster.de', '+49 (0)251 83 52526');

-- Pseudonym regex templates
INSERT INTO public.pseudonym_regex (id, order_number, regex, settings_id)
VALUES (7, 0, '.*', 6),
       (8, 1, '\d{2}_\d{3}', 6),
       (9, 2, '.+\d{2}_\d{3}', 6),
       (10, 3, '.{5}', 6),
       (11, 4, '[a-zA-Z]\d[a-zA-Z]\d', 6),
       (12, 5, '.{2,7}', 6);

INSERT INTO public.pseudonym_regex_description(id, current_language, description, name, pseudonym_regex_id)
VALUES (13, 'ENGLISH',
        E'Matches any pseudonym.\n\n".": Matches any character\n"*": The preceding expression may occur zero or more times',
        'Any pseudonym', 7),
       (14, 'GERMAN',
        E'Akzeptiert jedes Pseudonym.\n\n".": Akzeptiert ein beliebiges Zeichen\n"*": Der voranstehende Ausdruck darf beliebig oft vorkommen',
        'Beliebiges Pseudonym', 7),
       (15, 'ENGLISH',
        E'Matches two digits followed by an underscore and three digits, e.g. 27_914.\n\n"\\d": Matches one digit\n"{n}": The preceding expression may occur exactly n times',
        'Numbers', 8),
       (16, 'GERMAN',
        E'Akzeptiert zwei Ziffern gefolgt von einem Unterstrich und drei Ziffern, z. B. 27_914.\n\n"\\d": Akzeptiert eine Ziffer\n"{n}":  Der voranstehende Ausdruck darf genau n mal vorkommen',
        'Nummern', 8),
       (17, 'ENGLISH',
        E'Matches a not empty prefix followed by two digits, an underscore and three digits, e.g. ukm27_914.\n\n".": Matches any character\n"+": The preceding expression may occur one or more times\n"\\d": Matches one digit\n"{n}": The preceding expression may occur exactly n times',
        'Prefix followed by numbers', 9),
       (18, 'GERMAN',
        E'Akzeptiert einen nicht leeren Präfix gefolgt von zwei Ziffern, einem Unterstrich und drei Ziffern, z. B. ukm27_914.\n\n".": Akzeptiert ein beliebiges Zeichen\n"+": Der voranstehende Ausdruck muss mindestens einmal vorkommen\n"\\d": Akzeptiert eine Ziffer\n"{n}":  Der voranstehende Ausdruck muss genau n mal vorkommen',
        'Präfix gefolgt von Zahlen', 9),
       (19, 'ENGLISH',
        E'Matches 5 arbitrary characters, e.g. h_2g4.\n\n".": Matches any character\n"{n}": The preceding expression may occur exactly n times',
        'Any pseudonym fixed size', 10),
       (20, 'GERMAN',
        E'Akzeptiert 5 beliebige Zeichen, z. B. h_2g4.\n\n".": Akzeptiert ein beliebiges Zeichen\n"{n}":  Der voranstehende Ausdruck darf genau n mal vorkommen',
        'Beliebiges Pseudonym fester Größe', 10),
       (21, 'ENGLISH',
        E'Matches a letter followed by a digit, a letter and a digit, e.g. b2u8.\n\n"[]": Matches a single character that is contained within the brackets\n"[a-z]": Specifies a range of characters from "a" to "z"\n"\\d": Matches one digit',
        'Alternating between letters and digits', 11),
       (22, 'GERMAN',
        E'Akzeptiert einen Buchstaben gefolgt von einer Ziffer, einem Buchstaben und einer Ziffer, z. B. b2u8.\n\n"[]": Akzeptiert ein Zeichen, dass in den Klammern enthalten ist\n"[a-z]": Spezifiziert einen Bereich von Buchstaben von "a" bis "z"\n"\\d": Akzeptiert eine Ziffer',
        'Abwechselnd Buchstaben und Ziffern', 11),
       (23, 'ENGLISH',
        E'Matches 2 to 7 arbitrary characters, e.g. 3ay_.\n\n".": Matches any character\n"{m,n}": The preceding expression may occur between m and n times',
        'Any pseudonym of length 2 to 7', 12),
       (24, 'GERMAN',
        E'Akzeptiert 2 bis 7 beliebige Zeichen, z. B. 3ay_.\n\n".": Akzeptiert ein beliebiges Zeichen\n"{m,n}": Der voranstehende Ausdruck dar m bis n mal vorkommen',
        'Beliebiges Pseudonym der Länge 2 bis 7', 12);

INSERT INTO footer_message_settings (id, current_language, imprint_content, data_privacy_content, support_content,
                                     settings_id)
VALUES (25, 'GERMAN', 'Impressum', 'Datenschutz', 'Hilfe', 6),
       (26, 'ENGLISH', 'Imprint', 'Data Privacy', 'Support', 6);


-- Update SEQUENCE
SELECT setval('hibernate_sequence', 27, false);


