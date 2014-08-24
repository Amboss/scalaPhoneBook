# --- First database schema

# --- !Ups
set ignorecase true;
create table person (
  id    bigint not null,
  name  varchar(255) not null,
  phone varchar(255) not null,
  constraint pk_person primary key (id))
;
create sequence person_seq start with 1000;

# --- !Downs
set referential_integriry false;
drop table if exists person;
set referential_integriry true;
drop sequence if exists person_seq;