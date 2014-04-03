begin 
  DBMS_OUTPUT.PUT_LINE('BEGIN');
  select count(*)
  into vres 
  from remind_tmp_table_1;

  insert into remind_tmp_table_1 values (vres + 100,' test',sysdate)
  ;
   
  commit;
   
end;
/