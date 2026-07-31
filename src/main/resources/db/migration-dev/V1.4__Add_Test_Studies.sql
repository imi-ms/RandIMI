-- inactive study
DO
$$
    DECLARE
        v_site_id    BIGINT;
        v_study_id   BIGINT;
        v_stratum_id BIGINT;
    BEGIN
        -- study
        INSERT INTO public.study (id, activation_date, capacity, description, gui_name, max_blocksize, min_blocksize, pre_generate_subject_list,
                                  pseudonym_handling, randomization_algorithm, stratified_by_site)
        VALUES (nextval('hibernate_sequence'), NULL, 24, 'This is an inactive study.', 'Inactive Study', 2, 2, false, 'UNIQUE_IN_LOCATION', 'BLOCKED', false)
        RETURNING id INTO v_study_id;

        -- acl
        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_study_id, 3, 4, NULL);

        -- study_arm
        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'First arm of inactive study', 0, v_study_id),
               (nextval('hibernate_sequence'), 'Second arm of inactive study', 1, v_study_id);

        -- site
        INSERT INTO public.site (id, api_id, capacity, gui_name, pseudonym_regex, random_calls, seed, study_id)
        VALUES (nextval('hibernate_sequence'), 'Site of inactive study' ,100, 'Site of inactive study', '.*', 0, 0, v_study_id)
        RETURNING id INTO v_site_id;

        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_site_id, 3, 5, NULL);

        -- stratum gender
        INSERT INTO public.stratum (id, name, order_number, stratum_type, study_id)
        VALUES (nextval('hibernate_sequence'), 'gender', 0, 'ENUM', v_study_id)
        RETURNING id INTO v_stratum_id;

        INSERT INTO public.stratum_part_base (dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
        VALUES ('StratumPartEnumeration', nextval('hibernate_sequence'), 0, 'm', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 1, 'w', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 2, 'd', NULL, NULL, v_stratum_id);

        -- audit entry create
        INSERT INTO public.audit_entry (id, audit_class, audit_type, content, old_content, reason, study_id, target_id,
                                        timestamp, user_id)
        VALUES (nextval('hibernate_sequence'), 'STUDY', 'CREATE', '', NULL, NULL, v_study_id, 0, '2000-12-24 18:00:00.123', 1);
    END
$$;

-- active study
DO
$$
    DECLARE
        v_active_test_user_id           BIGINT;
        v_active_test_user_sid          BIGINT;
        v_api_test_user_id              BIGINT;
        v_api_test_user_sid             BIGINT;
        v_study_id                      BIGINT;
        v_acl_object_identity_study_id  BIGINT;
        v_acl_object_identity_site_id_1 BIGINT;
        v_site_id_1                     BIGINT;
        v_site_id_2                     BIGINT;
        v_stratum_id                    BIGINT;
        v_study_arm_id_0                BIGINT;
        v_study_arm_id_1                BIGINT;
        v_subject_list_id               BIGINT;
    BEGIN
        -- get active test user
        SELECT public.randimi_user.id, public.randimi_user.sid
        INTO v_active_test_user_id, v_active_test_user_sid
        FROM public.randimi_user
        WHERE username = 'ACTIVE_TEST_USER';

        -- get api user
        SELECT public.randimi_user.id, public.randimi_user.sid
        INTO v_api_test_user_id, v_api_test_user_sid
        FROM public.randimi_user
        WHERE username = 'API_TEST_USER';

        -- study
        INSERT INTO public.study (id, activation_date, capacity, description, gui_name, max_blocksize, min_blocksize, pre_generate_subject_list,
                                  pseudonym_handling, randomization_algorithm, stratified_by_site)
        VALUES (nextval('hibernate_sequence'), '2000-12-24 18:00:00.123', 120, 'This is an active study.', 'Active Study', 2, 2, false, 'UNIQUE_IN_LOCATION', 'BLOCKED', false)
        RETURNING id INTO v_study_id;

        -- acl
        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_study_id, 3, 4, NULL)
        RETURNING id INTO v_acl_object_identity_study_id;

        -- study user
        INSERT INTO public.randimi_user_study(randimi_user_id, study_id)
        VALUES (v_active_test_user_id, v_study_id),
               (v_api_test_user_id, v_study_id);

        -- permission READ_STUDY for active_test_user
        -- permission GET_NOTIFICATION for active_test_user
        -- permission READ_STUDY for api_test_user
        INSERT INTO public.acl_entry (id, ace_order, audit_failure, audit_success, granting, mask, acl_object_identity, sid)
        VALUES (nextval('hibernate_sequence'), 1, false, false, true, 4, v_acl_object_identity_study_id, v_active_test_user_sid),
               (nextval('hibernate_sequence'), 1, false, false, true, 9, v_acl_object_identity_study_id, v_active_test_user_sid),
               (nextval('hibernate_sequence'), 1, false, false, true, 4, v_acl_object_identity_study_id, v_api_test_user_sid);

        -- study_arm
        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'First arm of active study', 0, v_study_id)
        RETURNING id INTO v_study_arm_id_0;

        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'Second arm of active study', 1, v_study_id)
        RETURNING id INTO v_study_arm_id_1;

        -- site
        INSERT INTO public.site (id, api_id, capacity, gui_name, pseudonym_regex, random_calls, seed, study_id)
        VALUES (nextval('hibernate_sequence'), 'First Site of active study', 60, 'First Site of active study', '.*', 2, 1, v_study_id)
        RETURNING id INTO v_site_id_1;

        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_site_id_1, 3, 5, NULL)
        RETURNING id INTO v_acl_object_identity_site_id_1;

        INSERT INTO public.site (id, api_id, capacity, gui_name, pseudonym_regex, random_calls, seed, study_id)
        VALUES (nextval('hibernate_sequence'), 'Second Site of active study', 60, 'Second Site of active study', '.*', 0, 2, v_study_id)
        RETURNING id INTO v_site_id_2;

        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_site_id_2, 3, 5, NULL);

        -- permission READ_SUBJECT for active_test_user
        -- permission CREATE_SUBJECT for active_test_user
        -- permission READ_SUBJECT for api_test_user
        -- permission CREATE_SUBJECT for api_test_user
        INSERT INTO public.acl_entry (id, ace_order, audit_failure, audit_success, granting, mask, acl_object_identity, sid)
        VALUES (nextval('hibernate_sequence'), 1, false, false, true, 0, v_acl_object_identity_site_id_1, v_active_test_user_sid),
               (nextval('hibernate_sequence'), 1, false, false, true, 1, v_acl_object_identity_site_id_1, v_active_test_user_sid),
               (nextval('hibernate_sequence'), 1, false, false, true, 0, v_acl_object_identity_site_id_1, v_api_test_user_sid),
               (nextval('hibernate_sequence'), 1, false, false, true, 1, v_acl_object_identity_site_id_1, v_api_test_user_sid);

        -- stratum gender
        INSERT INTO public.stratum (id, name, order_number, stratum_type, study_id)
        VALUES (nextval('hibernate_sequence'), 'gender', 0, 'ENUM', v_study_id)
        RETURNING id INTO v_stratum_id;

        INSERT INTO public.stratum_part_base (dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
        VALUES ('StratumPartEnumeration', nextval('hibernate_sequence'), 0, 'm', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 1, 'w', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 2, 'd', NULL, NULL, v_stratum_id);

        -- stratum age group
        INSERT INTO public.stratum (id, name, order_number, stratum_type, study_id)
        VALUES (nextval('hibernate_sequence'), 'age group', 1, 'ENUM', v_study_id)
        RETURNING id INTO v_stratum_id;

        INSERT INTO public.stratum_part_base (dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
        VALUES ('StratumPartEnumeration', nextval('hibernate_sequence'), 0, '0 - 17', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 1, '18 - 100', NULL, NULL, v_stratum_id);

        -- audit entry create
        -- audit entry activate
        INSERT INTO public.audit_entry (id, audit_class, audit_type, content, old_content, reason, study_id, target_id, timestamp, user_id)
        VALUES (nextval('hibernate_sequence'), 'STUDY', 'CREATE', '', NULL, NULL, v_study_id, 0, '2000-12-24 18:00:00.123', 1),
               (nextval('hibernate_sequence'), 'STUDY', 'ACTIVATE', '', NULL, NULL, v_study_id, 0, '2000-12-24 18:00:00.123', 1);

        -- subject_list
        INSERT INTO public.subject_list (id, remaining_assignments, stratum_interval_code, study_id)
        VALUES (nextval('hibernate_sequence'), '{0, 0}', 'gender-m_age group-0 - 17', v_study_id)
        RETURNING id INTO v_subject_list_id;

        -- subjects
        INSERT INTO public.subject (id, order_number, pseudonym, site_id, status, subject_list_id, study_arm_id, timestamp)
        VALUES (nextval('hibernate_sequence'), 1, 'pseudonym1', v_site_id_1, 'ACTIVE', v_subject_list_id, v_study_arm_id_0, '2000-12-24 18:00:00.123'),
               (nextval('hibernate_sequence'), 2, 'pseudonym2', v_site_id_2, 'ACTIVE', v_subject_list_id, v_study_arm_id_1, '2000-12-24 18:00:00.123');

        INSERT INTO public.subject_list (id, remaining_assignments, stratum_interval_code, study_id)
        VALUES (nextval('hibernate_sequence'), '{0, 0}', 'gender-w_age group-0 - 17', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'gender-d_age group-0 - 17', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'gender-m_age group-18 - 100', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'gender-w_age group-18 - 100', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'gender-d_age group-18 - 100', v_study_id);
    END
$$;

-- Stratified by site study
DO
$$
    DECLARE
        v_api_test_user_id              BIGINT;
        v_api_test_user_sid             BIGINT;
        v_study_id                      BIGINT;
        v_acl_object_identity_study_id  BIGINT;
        v_acl_object_identity_site_id_1 BIGINT;
        v_site_id_1                     BIGINT;
        v_site_id_2                     BIGINT;
        v_stratum_id                    BIGINT;
        v_study_arm_id_0                BIGINT;
        v_study_arm_id_1                BIGINT;
    BEGIN
        -- get api user
        SELECT public.randimi_user.id, public.randimi_user.sid
        INTO v_api_test_user_id, v_api_test_user_sid
        FROM public.randimi_user
        WHERE username = 'API_TEST_USER';

        -- study
        INSERT INTO public.study (id, activation_date, capacity, description, gui_name, max_blocksize, min_blocksize, pre_generate_subject_list,
                                  pseudonym_handling, randomization_algorithm, stratified_by_site)
        VALUES (nextval('hibernate_sequence'), '1997-7-24 9:21:00.123', 100, 'This is an study stratified by site.', 'Stratified By Site Study', 6, 2, false, 'UNIQUE_IN_LOCATION', 'BLOCKED', true)
        RETURNING id INTO v_study_id;

        -- acl
        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_study_id, 3, 4, NULL)
        RETURNING id INTO v_acl_object_identity_study_id;

        -- study user
        INSERT INTO public.randimi_user_study(randimi_user_id, study_id)
        VALUES (v_api_test_user_id, v_study_id);

        -- permission READ_STUDY for api_test_user
        INSERT INTO public.acl_entry (id, ace_order, audit_failure, audit_success, granting, mask, acl_object_identity, sid)
        VALUES (nextval('hibernate_sequence'), 1, false, false, true, 4, v_acl_object_identity_study_id, v_api_test_user_sid);

        -- study_arm
        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'First arm', 0, v_study_id)
        RETURNING id INTO v_study_arm_id_0;

        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'Second arm', 1, v_study_id)
        RETURNING id INTO v_study_arm_id_1;

        -- site
        INSERT INTO public.site (id, api_id, capacity, gui_name, pseudonym_regex, random_calls, seed, study_id)
        VALUES (nextval('hibernate_sequence'), 'First Site', 12, 'First Site', '.*', 0, 1, v_study_id)
        RETURNING id INTO v_site_id_1;

        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_site_id_1, 3, 5, NULL)
        RETURNING id INTO v_acl_object_identity_site_id_1;

        INSERT INTO public.site (id, api_id, capacity, gui_name, pseudonym_regex, random_calls, seed, study_id)
        VALUES (nextval('hibernate_sequence'), 'Second Site', 12, 'Second Site', '.*', 0, 2, v_study_id)
        RETURNING id INTO v_site_id_2;

        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_site_id_2, 3, 5, NULL);

        -- permission READ_SUBJECT for api_test_user
        -- permission CREATE_SUBJECT for api_test_user
        INSERT INTO public.acl_entry (id, ace_order, audit_failure, audit_success, granting, mask, acl_object_identity, sid)
        VALUES (nextval('hibernate_sequence'), 1, false, false, true, 0, v_acl_object_identity_site_id_1, v_api_test_user_sid),
               (nextval('hibernate_sequence'), 1, false, false, true, 1, v_acl_object_identity_site_id_1, v_api_test_user_sid);

        -- stratum gender
        INSERT INTO public.stratum (id, name, order_number, stratum_type, study_id)
        VALUES (nextval('hibernate_sequence'), 'gender', 0, 'ENUM', v_study_id)
        RETURNING id INTO v_stratum_id;

        INSERT INTO public.stratum_part_base (dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
        VALUES ('StratumPartEnumeration', nextval('hibernate_sequence'), 0, 'm', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 1, 'w', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 2, 'd', NULL, NULL, v_stratum_id);

        -- stratum location
        INSERT INTO public.stratum (id, name, order_number, stratum_type, study_id)
        VALUES (nextval('hibernate_sequence'), 'location', 1, 'ENUM', v_study_id)
        RETURNING id INTO v_stratum_id;

        INSERT INTO public.stratum_part_base (dtype, id, order_number, enum_value, interval_begin, interval_end, stratum_id)
        VALUES ('StratumPartEnumeration', nextval('hibernate_sequence'), 0, 'First Site', NULL, NULL, v_stratum_id),
               ('StratumPartEnumeration', nextval('hibernate_sequence'), 1, 'Second Site', NULL, NULL, v_stratum_id);

        -- audit entry create
        -- audit entry activate
        INSERT INTO public.audit_entry (id, audit_class, audit_type, content, old_content, reason, study_id, target_id, timestamp, user_id)
        VALUES (nextval('hibernate_sequence'), 'STUDY', 'CREATE', '', NULL, NULL, v_study_id, 0, '2000-12-24 18:00:00.123', 1),
               (nextval('hibernate_sequence'), 'STUDY', 'ACTIVATE', '', NULL, NULL, v_study_id, 0, '2000-12-24 18:00:00.123', 1);

        -- subject_list
        INSERT INTO public.subject_list (id, remaining_assignments, stratum_interval_code, study_id)
        VALUES (nextval('hibernate_sequence'), '{0, 0}', 'location-First Site_gender-m', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'location-First Site_gender-w', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'location-First Site_gender-d', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'location-Second Site_gender-m', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'location-Second Site_gender-w', v_study_id),
               (nextval('hibernate_sequence'), '{0, 0}', 'location-Second Site_gender-d', v_study_id);
    END
$$;

-- pre generated study
DO
$$
    DECLARE
        v_site_id         BIGINT;
        v_study_arm_id_0 BIGINT;
        v_study_arm_id_1 BIGINT;
        v_study_id        BIGINT;
        v_subject_list_id BIGINT;
    BEGIN
        -- study
        INSERT INTO public.study (id, activation_date, capacity, description, gui_name, max_blocksize, min_blocksize, pre_generate_subject_list,
                                  pseudonym_handling, randomization_algorithm, stratified_by_site)
        VALUES (nextval('hibernate_sequence'), NULL, 10, 'This is a pre generated study.', 'Pre-Generated Study', 2, 2, true, 'UNIQUE_IN_LOCATION', 'BLOCKED', false)
        RETURNING id INTO v_study_id;

        -- acl
        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class,
                                                parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_study_id, 3, 4, NULL);

        -- study_arm
        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'First arm of pre-generated study', 0, v_study_id)
        RETURNING id INTO v_study_arm_id_0;

        INSERT INTO public.study_arm (id, gui_name, order_number, study_id)
        VALUES (nextval('hibernate_sequence'), 'Second arm of pre-generated study', 1, v_study_id)
        RETURNING id INTO v_study_arm_id_1;

        -- site
        INSERT INTO public.site (id, api_id, capacity, gui_name, pseudonym_regex, random_calls, seed, study_id)
        VALUES (nextval('hibernate_sequence'), 'Site of pre-generated study' ,10, 'Site of pre-generated study', '.*', 0, 0, v_study_id)
        RETURNING id INTO v_site_id;

        INSERT INTO public.acl_object_identity (id, entries_inheriting, object_id_identity, owner_sid, object_id_class, parent_object)
        VALUES (nextval('hibernate_sequence'), true, v_site_id, 3, 5, NULL);

        -- audit entry create
        INSERT INTO public.audit_entry (id, audit_class, audit_type, content, old_content, reason, study_id, target_id,
                                        timestamp, user_id)
        VALUES (nextval('hibernate_sequence'), 'STUDY', 'CREATE', '', NULL, NULL, v_study_id, 0, '2000-12-24 18:00:00.123', 1);

        -- subject list
        INSERT INTO public.subject_list (id, remaining_assignments, stratum_interval_code, study_id)
        VALUES (nextval('hibernate_sequence'), '{0, 0}', 'gender-m_age group-0.0-17.0', v_study_id)
        RETURNING id INTO v_subject_list_id;

        -- subjects
        INSERT INTO public.subject (id, order_number, pseudonym, site_id, status, subject_list_id, study_arm_id, timestamp)
        VALUES (nextval('hibernate_sequence'), 1, 'pseudonym1', v_site_id, 'ACTIVE', v_subject_list_id, v_study_arm_id_0, '2000-12-24 18:00:00.123'),
               (nextval('hibernate_sequence'), 2, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_0, NULL),
               (nextval('hibernate_sequence'), 3, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_1, NULL),
               (nextval('hibernate_sequence'), 4, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_1, NULL),
               (nextval('hibernate_sequence'), 5, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_0, NULL),
               (nextval('hibernate_sequence'), 6, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_1, NULL),
               (nextval('hibernate_sequence'), 7, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_0, NULL),
               (nextval('hibernate_sequence'), 8, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_1, NULL),
               (nextval('hibernate_sequence'), 9, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_1, NULL),
               (nextval('hibernate_sequence'), 10, NULL, NULL, 'PRE_GENERATED', v_subject_list_id, v_study_arm_id_0, NULL);
    END
$$;
