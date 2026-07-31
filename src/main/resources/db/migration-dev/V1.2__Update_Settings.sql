-- Update test settings
UPDATE public.settings
SET mail_password='dcb1a2e23bf956609b6194476853b52f'
WHERE id = 6;

-- Insert test templates
DO
$$
    DECLARE
        v_pseudonym_regex_id BIGINT;
    BEGIN
        INSERT INTO public.pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), 6, '[0-9]{3}', 6)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO public.pseudonym_regex_description (id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH', 'Some text for test regex with exactly 3 numbers.', 'TestRegex', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN', 'Ein text für ein Regex mit genau 3 Nummern.', 'TestRegex', v_pseudonym_regex_id);

        INSERT INTO public.pseudonym_regex (id, order_number, regex, settings_id)
        VALUES (nextval('hibernate_sequence'), 7, 'Patient \d{3}', 6)
        RETURNING id INTO v_pseudonym_regex_id;

        INSERT INTO public.pseudonym_regex_description (id, current_language, description, name, pseudonym_regex_id)
        VALUES (nextval('hibernate_sequence'), 'ENGLISH', 'Test regex with patient and three numbers.', 'TestRegex2', v_pseudonym_regex_id),
               (nextval('hibernate_sequence'), 'GERMAN', 'Test Regex mit patient und 3 Nummern.', 'TestRegex2', v_pseudonym_regex_id);
    END
$$;
