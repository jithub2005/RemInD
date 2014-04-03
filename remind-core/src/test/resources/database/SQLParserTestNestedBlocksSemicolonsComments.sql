create or replace package body remind_package
as

   procedure pr_del_entries
   as
   begin
    DBMS_OUTPUT.PUT_LINE('BEGIN')
    ;
     delete from remind_tmp_table_1
     -- soetwas kommt leider in manchen skripts vor
     -- 
     ;
     commit
     ;
     
   end;
end;
/