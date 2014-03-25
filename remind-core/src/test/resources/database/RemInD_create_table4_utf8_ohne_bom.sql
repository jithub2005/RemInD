BEGIN
  LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_create_table4.sql'
    ,SCRIPT_VERSION_IN => '2013-07-11 133.1'
    ,OBJECT_VERSION_IN => '$Rev: 4326 $'
    ,PROJECT_IN        => 'MAXIT'
    ,SCHEMA_NAME_IN    => 'REMIND'
    ,REMARK_IN         => 'Skript START'
  );
END;

/

commit
/

/*
  A remind test table
*/
--Bug1
--some comments
create table  remind_tmp_table_1  (id number, name varchar2(50))
/

alter table remind_tmp_table_1
add (create_date date,
     name2 varchar2(10),
     name3 varchar2(10));

alter table remind_tmp_table_1
add constraint pk_rtt_id primary key(id);

create index ind_rtt_name on remind_tmp_table_1(name)
/
     
/*
 -- old remind fails here
 --alter table remind_tmp_table_1 
 --add change_date varchar2(50);
 
*/

-- comments for remind_test_table;
--Bug2
comment on column remind.remind_tmp_table_1.id is 'this is the id'
/

comment on column remind.remind_tmp_table_1.name is 'names'
;

comment on column remind.remind_tmp_table_1.name2 is 'names2';

comment on column remind.remind_tmp_table_1.name3 is 'names3'

/

-- insert
insert into remind.remind_tmp_table_1(id, name, create_date) values (1,' testÄÖÜüöäß ',sysdate)
;

-- hier fehlt ein ; um Fixed Statement zu testens
insert into remind.remind_tmp_table_1(id, name, create_date) values (2,' row 2 ',sysdate)


/*
 --insert into remind.remind_tmp_table_1 values (2,'not valid');
 this is a slash in comment test /
 2 / 3
*/

commit
/

declare
res number;
begin
  select 6 / 2
  into res
  from dual;
  
  insert into remind.remind_tmp_table_1(id, name, create_date) values (res,' row 3 ',sysdate);
  
  commit;
end;
/

BEGIN
  LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_create_table4.sql'
    ,SCRIPT_VERSION_IN => '2013-07-11 133.1'
    ,OBJECT_VERSION_IN => '$Rev: 4326 $'
    ,PROJECT_IN        => 'MAXIT'
    ,SCHEMA_NAME_IN    => 'REMIND'
    ,REMARK_IN         => 'Skript START'
  );
END;

/

COMMIT;
--EXIT;