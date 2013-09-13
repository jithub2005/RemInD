/*
CREATE TABLE installation_tapi (script_name_in VARCHAR2(150),
	script_version_in VARCHAR2(20),
	object_version_in VARCHAR2(20),
	schema_name_in VARCHAR2(30),
	remark_in VARCHAR2(150)) 
/
 */

DECLARE
    lvv_scriptname       VARCHAR2(150);     -- Name des Scripts
    lvv_script_version   VARCHAR2(20);      -- Versionsdatum + Scriptversion ( Version nur bei Scripts nicht bei Packages )
    lvv_object_version   VARCHAR2(20);      -- Bei Packages die Version
    lvv_schema_name      VARCHAR2(30);      -- muss befüllt werden (falls mehrere Schemata betroffen sind, so sind diese durch Beistrich getrennt anzuführen)
    lvv_remark           VARCHAR2(150);     -- muss mit dem Text "Skript START" oder "Skript ENDE" befüllt werden.
                                            -- Ist ein zusätzlicher erklärender Text gewünscht, so ist dieser nach den verpflichtenden Einträgen durch Doppelpunkt  und Leerzeichen getrennt einzugeben, 
                                            -- z.B.: "Skript START: Korrekturskript für falsch synchronisierte Sammelkunden".
BEGIN
    lvv_scriptname       := 'dummy.sql';
    lvv_script_version   := '2012-05-30';
    lvv_object_version   := '$Revision: 235 $';
    lvv_schema_name      := 'ALLGEMEIN';
    lvv_remark           := 'SCRIPT STARTii';
    
    insert into installation_tapi values (lvv_scriptname, lvv_script_version, lvv_object_version, lvv_schema_name, lvv_remark);
	commit;
END;
/

begin
null;
end;
/
  
DECLARE
    lvv_scriptname       VARCHAR2(150);     -- Name des Scripts
    lvv_script_version   VARCHAR2(20);      -- Versionsdatum + Scriptversion ( Version nur bei Scripts nicht bei Packages )
    lvv_object_version   VARCHAR2(20);      -- Bei Packages die Version
    lvv_schema_name      VARCHAR2(30);      -- muss befüllt werden (falls mehrere Schemata betroffen sind, so sind diese durch Beistrich getrennt anzuführen)
    lvv_remark           VARCHAR2(150);     -- muss mit dem Text "Skript START" oder "Skript ENDE" befüllt werden.
                                            -- Ist ein zusätzlicher erklärender Text gewünscht, so ist dieser nach den verpflichtenden Einträgen durch Doppelpunkt  und Leerzeichen getrennt einzugeben, 
                                            -- z.B.: "Skript START: Korrekturskript für falsch synchronisierte Sammelkunden".
BEGIN
    lvv_scriptname       := 'dummy.sql';
    lvv_script_version   := '2012-05-30';
    lvv_object_version   := '122.0';
    lvv_schema_name      := 'ALLGEMEIN';
    lvv_remark           := 'SCRIPT ENDE';
    
	insert into installation_tapi values (lvv_scriptname, lvv_script_version, lvv_object_version, lvv_schema_name, lvv_remark);   
	commit;
END;
/
