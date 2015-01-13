create table remind_tmp_table_1
(id number, name varchar2(50));

create table remind_tmp_table_2
(id number, name varchar2(50));

MERGE INTO remind_tmp_table_2 a 
using (select id, name from remind_tmp_table_1 b) 
ON ( a.id = b.id)
when matched then
	update set a.name = b.name
when not matched then
	insert (a.id, a.name) values (b.id, b.name);
	
MERGE INTO remind_tmp_table_2 a using (select id, name from remind_tmp_table_1 b) 
ON ( a.id = b.id) when matched then update set a.name = b.name
when not matched then insert (a.id, a.name) values (b.id, b.name);
	
MERGE INTO remind_tmp_table_2 a 
using (select id, name from remind_tmp_table_1 b) 
ON ( a.id = b.id)
when matched then
	update set a.name = b.name
when not matched then
	insert (a.id, a.name) values (b.id, b.name)
/