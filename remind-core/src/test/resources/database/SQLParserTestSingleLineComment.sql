BEGIN
  LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_grants_table.sql'
    ,SCRIPT_VERSION_IN => '2013-07-11 133.1'
    ,OBJECT_VERSION_IN => '$Rev: 3624 $'
    ,PROJECT_IN        => 'MAXIT'
    ,SCHEMA_NAME_IN    => 'REMIND'
    ,REMARK_IN         => 'Skript START'
  );
END;
/
-- simulating grants and comments
grant select on remind_tmp_table to public;

comment on table remind_tmp_table is 'test table for remind';
