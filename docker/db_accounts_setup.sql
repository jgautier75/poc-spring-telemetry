create database "poc_st" with encoding 'UTF-8' connection limit -1;

CREATE USER tec_poc_st_dba WITH PASSWORD 'tec_poc_st_dba';
GRANT ALL PRIVILEGES ON DATABASE "poc_st" TO tec_poc_st_dba;
ALTER ROLE tec_poc_st_dba NOSUPERUSER NOCREATEDB CREATEROLE INHERIT LOGIN;

create user tec_poc_st_app with PASSWORD 'tec_poc_st_app';
ALTER ROLE tec_poc_st_app NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;

grant all privileges on schema public to tec_poc_st_dba;
