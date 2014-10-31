CREATE OR REPLACE
PACKAGE modifying_date
AS
  /* Reformats date */
  FUNCTION reformat_date(pi_date VARCHAR2)
    RETURN VARCHAR2;
  END;
END modifying_date; 
/

CREATE OR REPLACE
PACKAGE body modifying_date
AS
FUNCTION reformat_date (pi_date VARCHAR2 )
  RETURN VARCHAR2
IS
  BEGIN
    -- For Testing Purpose only
    RETURN(REPLACE (pi_date,'.','/')); 
    
  END reformat_date;
END modifying_date;
/
