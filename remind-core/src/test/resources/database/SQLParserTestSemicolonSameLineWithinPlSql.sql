begin 
  DBMS_OUTPUT.PUT_LINE('BEGIN');
  
  select count(*) into vres from remind_tmp_table_1;

end;
/