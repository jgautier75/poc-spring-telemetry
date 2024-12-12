select 'create database "DBNAME" with encoding ''UTF-8'' connection limit -1' where not exists (select from pg_database where datname='DBNAME'); \gexec
