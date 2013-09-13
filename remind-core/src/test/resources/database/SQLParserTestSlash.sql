BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
SCRIPT_NAME_IN => 'grants_to_sep.sql'
,SCRIPT_VERSION_IN => '$Rev$'
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
GRANT select ON crm.opt_option_system TO sep_proc
/
grant select on crm.prod_tarif_details_system to sep with grant option
/
grant select on prod_Tarif_details_man to sep with grant option
/
grant select on crm.prod_tarif_details_system to sep_proc
/
grant select on crm.prod_Tarif_details_man to sep_proc
/
BEGIN
LOGMGR.INSTALLATION_TAPI.INS(
SCRIPT_NAME_IN => 'grants_to_sep.sql'
,SCRIPT_VERSION_IN => '$Rev$'
,PROJECT_IN => 'PX Loyalty'
,SCHEMA_NAME_IN => 'CRM'
,REMARK_IN => 'Skript ENDE'
);
END;
/
COMMIT
/