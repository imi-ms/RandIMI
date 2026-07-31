-- Create user and database. --
-- Execute this script as postgres user --

CREATE ROLE randomuser WITH PASSWORD 'changeme' LOGIN;
CREATE DATABASE randimi_db OWNER randomuser;
CREATE DATABASE randimi_test_db OWNER randomuser;
