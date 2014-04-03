create or replace procedure pr_remind_gen_entry
    as
    	begin
    	DBMS_OUTPUT.PUT_LINE('BEGIN');

    	insert into remind_tmp_table_1 values (5,' test',sysdate)
    	;

    end;
/
