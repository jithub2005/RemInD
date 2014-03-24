--WHENEVER SQLERROR EXIT SQL.SQLCODE
--WHENEVER OSERROR  EXIT SQL.OSCODE

BEGIN
  LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_create_table4.sql'
    ,SCRIPT_VERSION_IN => '2013-07-11 133.1'
    ,OBJECT_VERSION_IN => '$Rev: 4321 $'
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

create table  remind_tmp_table_1  (id number, name varchar2(50))
/

alter table remind_tmp_table_1
add (create_date date);

/*
 -- old remind fails here
 --alter table remind_tmp_table_1 
 --add change_date varchar2(50);
 
*/

-- comments for remind_test_table;

comment on column remind.remind_tmp_table_1.id is 'this is the id'
/

comment on column remind.remind_tmp_table_1.name is 'names'
;
-- insert
insert into remind.remind_tmp_table_1 values (1,' &testöäüÄÜÖ ',sysdate)
;

/*
 --insert into remind.remind_tmp_table_1 values (2,'not valid');
*/

commit
/

BEGIN
  LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_create_table4.sql'
    ,SCRIPT_VERSION_IN => '2013-07-11 133.1'
    ,OBJECT_VERSION_IN => '$Rev: 4321 $'
    ,PROJECT_IN        => 'MAXIT'
    ,SCHEMA_NAME_IN    => 'REMIND'
    ,REMARK_IN         => 'Skript START'
  );
END;

/

COMMIT;
