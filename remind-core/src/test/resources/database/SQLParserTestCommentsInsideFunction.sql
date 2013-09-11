BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
SCRIPT_NAME_IN => 'grants_to_sep.sql'
,SCRIPT_VERSION_IN => '$Rev$'
/* / */
,PROJECT_IN => 'PX Loyalty'
,SCHEMA_NAME_IN => 'CRM'
,REMARK_IN => 'Skript ANFANG'
);
END;
/
COMMIT
/

GRANT select ON crm.opt_option_system TO sep WITH GRANT OPTION
/

BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
SCRIPT_NAME_IN => 'grants_to_sep.sql'
,SCRIPT_VERSION_IN => '$Rev$'
--/
,PROJECT_IN => 'PX Loyalty'
,SCHEMA_NAME_IN => 'CRM'
,REMARK_IN => 'Skript ENDE'
);
END;
/
COMMIT
/

GRANT select ON crm.opt_option_system TO sep WITH GRANT OPTION
/

BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
SCRIPT_NAME_IN => 'grants_to_sep.sql'
,SCRIPT_VERSION_IN => '$Rev$'
/*
 /
 */
,PROJECT_IN => 'PX Loyalty'
,SCHEMA_NAME_IN => 'CRM'
,REMARK_IN => 'Skript ENDE'
);
END;
/
COMMIT
/