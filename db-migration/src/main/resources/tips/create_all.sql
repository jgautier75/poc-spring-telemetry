CREATE USER tec_poc_st_dba WITH PASSWORD 'tec_poc_st_dba';
GRANT ALL PRIVILEGES ON DATABASE "poc_st" TO tec_poc_st_dba;
ALTER ROLE tec_poc_st_dba NOCREATEDB NOCREATEROLE INHERIT LOGIN;
CREATE USER tec_poc_st_app WITH PASSWORD 'tec_poc_st_app';
ALTER ROLE tec_poc_st_app NOCREATEDB NOCREATEROLE INHERIT LOGIN;
create database "poc_st" with encoding 'UTF-8' connection limit -1;
grant all privileges on schema public to tec_poc_st_dba;
grant select, insert, update, delete on all tables in schema public to tec_poc_st_app;
grant usage, select on all sequences in schema public to tec_poc_st_app;