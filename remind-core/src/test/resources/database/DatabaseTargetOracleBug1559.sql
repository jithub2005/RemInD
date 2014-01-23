BEGIN
null;
END;
/
-- simulating grants and comments
grant select on mp_test_table to public;

comment on table mp_test_table is 'this is a test table. stmt2';


COMMENT ON COLUMN mp_test_table.name  IS 'this is a test column. stmt3'
/
COMMENT ON COLUMN mp_test_table.name  IS 'this is a test column. stmt4';


BEGIN
null;
END;
/
