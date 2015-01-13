create table remind_tmp_table_2
(id number, name varchar2(50));

insert into remind_tmp_table_2 (id, name)
values(1,'wert 1');

create or replace force view remind_tmp_forced_view_1 (id, name) as select id, name from remind_tmp_table_2; 