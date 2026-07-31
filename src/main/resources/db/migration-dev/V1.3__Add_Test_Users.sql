-- admin
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
        v_randimi_user_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'TESTADMIN')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'randimi@uni-muenster.de', true,
                '$2a$10$AtpzSnoGHEQu38/vEa0h8e/MYiUuSE8AJ0XoxW42PHGyC7QK3z8Lm', 'AdminFirstName', 'AdminLastName',
                'TESTADMIN', NULL, NULL, v_acl_sid_id)
        RETURNING id INTO v_randimi_user_id;

        -- user role
        INSERT INTO public.user_role (id, enum_role, user_id)
        VALUES (nextval('hibernate_sequence'), 'ROLE_ADMIN', v_randimi_user_id);
    END
$$;

-- Insert test local manager
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
        v_randimi_user_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'TEST_LOCAL_MANAGER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        -- password: password
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'local_manager.test_user@example.com', true,
                '$2a$10$nIPVux.cCivKUUkROIecHOfUho.AIv.Msm0opOUV2s4XayK2lz332', 'Local-Manger',
                'Test-User', 'TEST_LOCAL_MANAGER', NULL, NULL, v_acl_sid_id)
        RETURNING id INTO v_randimi_user_id;

        -- user role
        INSERT INTO public.user_role (id, enum_role, user_id)
        VALUES (nextval('hibernate_sequence'), 'ROLE_LOCAL_MANAGER', v_randimi_user_id);
    END
$$;

-- Insert test study manager
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
        v_randimi_user_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'TESTSTUDYMANAGER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        -- password: SecretStudyManager
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'randimi@uni-muenster.de', true, '$2a$10$s7.T5nhTtqWQ/KPPowQbMuzjd0yzDJyPUh4DNjVnaaD.Zo6c.gygW',
                'StudyManagerFirstName', 'StudyManagerLastName', 'TESTSTUDYMANAGER', NULL, NULL, v_acl_sid_id)
        RETURNING id INTO v_randimi_user_id;

        -- user role
        INSERT INTO public.user_role (id, enum_role, user_id)
        VALUES (nextval('hibernate_sequence'), 'ROLE_STUDY_MANAGER', v_randimi_user_id);
    END
$$;

-- Insert test user manager
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
        v_randimi_user_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'TESTUSERMANAGER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'randimi@uni-muenster.de', true,
                '$2a$10$LL.72SHbLo4A6Mfo0NvKvuktHBoAmVVXr7RIK9yB/fQMhXAGy21ly', 'UserManagerFirstName',
                'UserManagerLastName', 'TESTUSERMANAGER', NULL, NULL, v_acl_sid_id)
        RETURNING id INTO v_randimi_user_id;

        -- user role
        INSERT INTO public.user_role (id, enum_role, user_id)
        VALUES (nextval('hibernate_sequence'), 'ROLE_USER_MANAGER', v_randimi_user_id);
    END
$$;

-- active test user
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'ACTIVE_TEST_USER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'active.test_user@example.com', true,
                '$2a$10$nIPVux.cCivKUUkROIecHOfUho.AIv.Msm0opOUV2s4XayK2lz332', -- password
                'Active', 'Test-User', 'ACTIVE_TEST_USER', NULL, NULL, v_acl_sid_id);
    END
$$;

-- inactive test user
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
        v_randimi_user_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'INACTIVE_TEST_USER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'inactive.test_user@example.com', false, NULL, 'Inactive', 'Test-User',
                'INACTIVE_TEST_USER', '9999-12-24 18:00:00.123', 'INVITATION_TOKEN', v_acl_sid_id)
        RETURNING id INTO v_randimi_user_id;

        -- user role
        INSERT INTO public.user_role (id, enum_role, user_id)
        VALUES (nextval('hibernate_sequence'), 'ROLE_ADMIN', v_randimi_user_id);
    END
$$;

-- obsolete test user
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'OBSOLETE_TEST_USER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'obsolete.test_user@example.com', false, NULL, 'Obsolete', 'Test-User',
                'OBSOLETE_TEST_USER', '2000-12-24 18:00:00.123', 'INVALID_INVITATION_TOKEN', v_acl_sid_id);
    END
$$;

-- Insert test disabled user
DO
$$
    DECLARE
        v_acl_sid_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'TESTDISABLEDUSER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username,
                                         invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'randimi@uni-muenster.de', false,
                '$2a$10$UmValxTcTINxXlk5J929seh/QUxOLS31l8GzTN5B0OS4zybyP.ilm', 'DisabledUserFirstName',
                'DisabledUserLastName', 'TESTDISABLEDUSER', NULL, NULL, v_acl_sid_id);
    END
$$;

-- api test user
DO
$$
    DECLARE
        v_acl_sid_id      BIGINT;
        v_randimi_user_id BIGINT;
    BEGIN
        -- acl sid
        INSERT INTO public.acl_sid (id, principal, sid)
        VALUES (nextval('hibernate_sequence'), true, 'API_TEST_USER')
        RETURNING id INTO v_acl_sid_id;

        -- randimi user
        -- password: changeme
        INSERT INTO public.randimi_user (id, e_mail, enabled, password, first_name, last_name, username, invitation_timestamp, invitation_token, sid)
        VALUES (nextval('hibernate_sequence'), 'api.test_user@example.com', true, '$2a$10$ktXRYklVAt/G5Cg/sWu0de95Chd4LAWhvenTEjQvx.SDGiL2Ls8Mq', 'Api', 'Test-User', 'API_TEST_USER', NULL, NULL, v_acl_sid_id)
        RETURNING id INTO v_randimi_user_id;

        -- user role
        INSERT INTO public.user_role (id, enum_role, user_id)
        VALUES (nextval('hibernate_sequence'), 'ROLE_API_USER', v_randimi_user_id);
    END
$$;
