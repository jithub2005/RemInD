create sequence cw_seq_rm_test;

create table crm.cw_rm_test_table(id number , description varchar2(50));


comment on column crm.cw_rm_test_table.description
  is 'This is a remind column comment'
/  

insert into crm.cw_rm_test_table values (cw_seq_rm_test.nextval, '1st test description');

commit;