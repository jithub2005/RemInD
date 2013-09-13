BEGIN
  log_mgr.installation_tapi.ins (
    'ODS.ODS.SHA256EncoderForOracle.sql',
    '1',
    NULL,
    'Rel 12.1',  -- Projekt
    'TC0',        -- Testcycle
    'ODS',             -- Schema
    NULL         -- Bemerkung
    );
END;
/ 

-- Start of DDL Script for Java Source ODS.sha256encoderfororacle
-- Generated 01-12-2011 from ODS@DODS05.WORLD

-- !!!!!!!!!Wichtig!!!!!!!!
-- bitte mit SQLPLUS ausführen
-- !!!!!!!!!Wichtig!!!!!!!!

set scan off

create or replace and compile java source named sha256encoderfororacle as
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * This is a class exclusively created for use in Oracle. This doesn't have any dependencies besides a JVM.
 *
 */
public class SHA256EncoderForOracle
{

  public static String getSHA256WithSalt( String pwd,String salt ) throws Exception
  {
    MessageDigest messageDigest = MessageDigest.getInstance( "SHA-256" );
    byte[] digest;
    try
    {
      digest = messageDigest.digest( mergePasswordAndSalt( pwd, salt, false ).getBytes( "UTF-8" ) );
    }
    catch( UnsupportedEncodingException e )
    {
      throw new IllegalStateException( "UTF-8 not supported!" );
    }

    StringBuilder sb = new StringBuilder();
    for ( byte element : digest )
    {
      sb.append( Integer.toString( ( element & 0xff ) + 0x100, 16 ).substring( 1 ) );
    }

    return sb.toString().toUpperCase();

  }

  public static String getSHA256( String pwd ) throws Exception
  {
    if ( pwd == null )
    {
      pwd = "";
    }
    MessageDigest messageDigest = MessageDigest.getInstance( "SHA-256" );
    byte[] digest;
    try
    {
      digest = messageDigest.digest( pwd.getBytes( "UTF-8" ) );
    }
    catch( UnsupportedEncodingException e )
    {
      throw new IllegalStateException( "UTF-8 not supported!" );
    }

    StringBuilder sb = new StringBuilder();
    for ( byte element : digest )
    {
      sb.append( Integer.toString( ( element & 0xff ) + 0x100, 16 ).substring( 1 ) );
    }

    return sb.toString().toUpperCase();

  }

  protected static String mergePasswordAndSalt( String password, Object salt, boolean strict )
  {
    if ( password == null )
    {
      password = "";
    }

    if ( strict && ( salt != null ) )
    {
      if ( ( salt.toString().lastIndexOf( "{" ) != -1 ) || ( salt.toString().lastIndexOf( "}" ) != -1 ) )
      {
        throw new IllegalArgumentException( "Cannot use { or } in salt.toString()" );
      }
    }

    if ( ( salt == null ) || "".equals( salt ) )
    {
      return password;
    }
    else
    {
      return password + "{" + salt.toString() + "}";
    }
  }

}
/

BEGIN
  log_mgr.installation_tapi.ins (
    'ODS.ODS.SHA256EncoderForOracle.sql',
    '1',
    NULL,
    'Rel 12.1',  -- Projekt
    'TC0',        -- Testcycle
    'ODS',             -- Schema
    NULL         -- Bemerkung
    );
END;
/ 