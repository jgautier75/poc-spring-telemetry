create database "poc_st" with encoding 'UTF-8' connection limit -1;

CREATE USER poc_st_dba WITH PASSWORD 'poc_st_dba';
GRANT ALL PRIVILEGES ON DATABASE "poc_st" TO poc_st_dba;
ALTER ROLE poc_st_dba NOSUPERUSER NOCREATEDB CREATEROLE INHERIT LOGIN;

create user poc_st_app with PASSWORD 'poc_st_app';
ALTER ROLE poc_st_app NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;

grant all privileges on schema public to poc_st_dba;
