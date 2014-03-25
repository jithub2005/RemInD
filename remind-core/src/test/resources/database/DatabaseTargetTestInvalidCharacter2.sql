-- WHENEVER bla
/*BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_create_table4.sql',
     SCRIPT_VERSION_IN => '2013-07-11 133.1',
     OBJECT_VERSION_IN => '$Rev: 4326 $',
     PROJECT_IN        => 'MAXIT',
     SCHEMA_NAME_IN    => 'REMIND',
     REMARK_IN         => 'Skript START'
);
END;

/

commit
/
 */

select * from mp_test_table;

BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
     SCRIPT_NAME_IN    => 'RemInD_create_table4.sql',
     SCRIPT_VERSION_IN => '2013-07-11 133.1',
     OBJECT_VERSION_IN => '$Rev: 4326 $',
     PROJECT_IN        => 'MAXIT',
     SCHEMA_NAME_IN    => 'REMIND',
     REMARK_IN         => 'Skript START'
);
END;