create database "poc-st" with encoding 'UTF-8' connection limit -1;
CREATE USER poc_st_dba WITH PASSWORD 'poc_st_dba';
GRANT ALL PRIVILEGES ON DATABASE "poc-st" TO poc_st_dba;
ALTER ROLE poc_st_dba NOCREATEDB NOCREATEROLE INHERIT LOGIN;
CREATE USER poc_st_app WITH PASSWORD 'poc_st_app';
ALTER ROLE poc_st_app NOCREATEDB NOCREATEROLE INHERIT LOGIN;
grant all privileges on schema public to poc_st_dba;
grant select, insert, update, delete on all tables in schema public to poc_st_app;
grant usage, select on all sequences in schema public to poc_st_app;