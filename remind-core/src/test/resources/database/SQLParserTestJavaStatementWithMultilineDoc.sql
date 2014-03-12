CREATE OR REPLACE AND RESOLVE JAVA SOURCE NAMED MAXWELL."WSG" AS
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.StringTokenizer;
public final class WSG {
    /* use eventName and eventKey as single values
     * keys and values may contain more than one entry seperated by "|"
     * eg:
     *      keys:       "info|wert"
     *      values:     "das ist ein Testwert|42"
     * 
     */
    public static String raiseOWFEvent(String serverUrl,String sender, String correlationId, String timeout, String eventName, String eventKey, String keys, String values) throws Exception {
        String lead =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sen=\"http://system.crm.tmobile.at/services/notify/sendnotification\" xmlns:sen1=\"http://system.crm.tmobile.at/datatypes/notify/sendnotification\" xmlns:dat=\"http://messaging.ei.tmobile.net/datatypes\"> \n"+
            "   <soapenv:Header/> "+
            "   <soapenv:Body> \n"+
            "      <sen:raiseOracleWfEvent> \n"+
            "         <sen1:eiMessageContext> \n"+
            "            <dat:sender>"+sender+"</dat:sender> \n"+
            "            <dat:correlationId>"+correlationId+"</dat:correlationId> \n"+
            "         </sen1:eiMessageContext> \n"+
            "         <sen1:data> \n"+
            "            <sen1:eventName>"+eventName+"</sen1:eventName> \n"+
            "            <sen1:eventKey>"+eventKey+"</sen1:eventKey> \n";
        String trail=
            "         </sen1:data> \n"+
            "      </sen:raiseOracleWfEvent> \n"+
            "   </soapenv:Body> "+
            "</soapenv:Envelope> ";
        StringTokenizer keyTokens= new StringTokenizer(keys, "|");
        StringTokenizer valueTokens= new StringTokenizer(values, "|");
        String keyValue="";
        while (keyTokens.hasMoreElements()) {
            keyValue+=
                "            <sen1:eventData> \n"+
                "               <sen1:name>"+keyTokens.nextToken()+"</sen1:name> \n"+
                "               <sen1:value>"+valueTokens.nextToken()+"</sen1:value> \n"+
                "            </sen1:eventData> \n";
        }
        String current="1";
        String inputStr=lead+keyValue+trail;
        try {
            URL url = new URL(serverUrl);
            current="2";
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            current="3";
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "text/xml; charset=ISO-8859-1"); //UTF-8
            final byte[] inputArr = inputStr.getBytes("iso-8859-1");
            conn.setRequestProperty("Content-Length",
                    (new Integer(inputArr.length)).toString());
            conn.setRequestProperty("SOAPAction", "");
            current="4";
            conn.connect();
            current="5";
            BufferedOutputStream bOutStream = new
            BufferedOutputStream(conn.getOutputStream());
            current="6";
            bOutStream.write(inputArr);
            current="6";
            bOutStream.flush();
            current="7";
            bOutStream.close();
            current="8";
            InputStream inStream = conn.getInputStream();
            BufferedInputStream bInStream = new BufferedInputStream(inStream);
            current="9";
            int pos = 0;
            byte[] bytes = new byte[2048];
            int l;
            while (0 <= (l = bInStream.read(bytes, pos, 2048 - pos))) pos += l;
            l = conn.getContentLength();
            if (l >= 0 && l <= pos) pos = l;
            String outStr = new String(bytes, 0, pos, "iso-8859-1");
            //System.out.println("result: " + outStr + "\n\n");
            inStream.close();
            conn.disconnect();
            return "0";
        } catch(Exception ex) {
            return current+": "+ex.toString();
        }
    }
    public static String callRegisterNewSubscriber(String serverUrl,String sender, String correlationId, String timeout, String vertId) throws Exception {
        String inputStr =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<SOAP-ENV:Envelope xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAPSDK1=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAPSDK2=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAPSDK3=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:m=\"http://system.tmobile.at/services/maxwell/callnewsubscriber\" "+
            "xmlns:m0=\"http://messaging.ei.tmobile.net/datatypes\" xmlns:m1=\"http://system.tmobile.at/datatypes/maxwell/callnewsubscriber\" "+
            "xmlns:com=\"http://csm.tmobile.at/datatypes/common\" "+
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
            "  <SOAP-ENV:Header/>"+
            "  <SOAP-ENV:Body>"+
            "    <m:callNewSubscriber>"+
            "      <m1:eiMessageContext>"+
            "        <n0:target xmlns:n0=\"http://messaging.ei.tmobile.net/datatypes\">callnewsubscriber2</n0:target>"+
            "        <n0:requestId xmlns:n0=\"http://messaging.ei.tmobile.net/datatypes\">%requestId%</n0:requestId>"+
            "        <n0:timeLeft xmlns:n0=\"http://messaging.ei.tmobile.net/datatypes\">%timeout%</n0:timeLeft>"+
            "        <n0:priority xmlns:n0=\"http://messaging.ei.tmobile.net/datatypes\">4</n0:priority>"+
            "        <n0:sender xmlns:n0=\"http://messaging.ei.tmobile.net/datatypes\">%sender%</n0:sender>"+
            "        <n0:correlationId xmlns:n0=\"http://messaging.ei.tmobile.net/datatypes\">%correlationId%</n0:correlationId>"+
            "      </m1:eiMessageContext>"+
            "      <m1:data>"+
            "        <m1:csm>"+
            "          <com:messageSetId>%vertId%</com:messageSetId>"+
            "        </m1:csm>"+
            "        <m1:workflowId>%vertId%</m1:workflowId>"+
            "      </m1:data>"+
            "    </m:callNewSubscriber>"+
            "  </SOAP-ENV:Body>"+
            "</SOAP-ENV:Envelope>";
        String current="1";
        inputStr=replaceAll(inputStr,"%vertId%", vertId);
        inputStr=replaceAll(inputStr,"%vertId%", vertId);
        inputStr=replaceAll(inputStr,"%timeout%", timeout);
        inputStr=replaceAll(inputStr,"%correlationId%", correlationId);
        inputStr=replaceAll(inputStr,"%sender%", sender);
        inputStr=replaceAll(inputStr,"%requestId%", "maxwell.ablauf."+vertId);
        try {
            URL url = new URL(serverUrl);
            current="2";
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            current="3";
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "text/xml; charset=ISO-8859-1"); //UTF-8
            final byte[] inputArr = inputStr.getBytes("iso-8859-1");
            conn.setRequestProperty("Content-Length",
                    (new Integer(inputArr.length)).toString());
            conn.setRequestProperty("SOAPAction", "");
            current="4";
            conn.connect();
            current="5";
            BufferedOutputStream bOutStream = new
            BufferedOutputStream(conn.getOutputStream());
            current="6";
            bOutStream.write(inputArr);
            current="6";
            bOutStream.flush();
            current="7";
            bOutStream.close();
            current="8";
            InputStream inStream;
            BufferedInputStream bInStream;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
              current="9";
            	inStream = conn.getInputStream();
            	bInStream = new BufferedInputStream(inStream);
              int pos = 0;
              byte[] bytes = new byte[2048];
              int l;
              current="10";
              while (0 <= (l = bInStream.read(bytes, pos, 2048 - pos))) pos += l;
              l = conn.getContentLength();
              if (l >= 0 && l <= pos) pos = l;
              String outStr = new String(bytes, 0, pos, "iso-8859-1");
              inStream.close();
              conn.disconnect();
              if (outStr!=null && outStr.indexOf("Exception")!=-1) {
                  return "-1";
              } else {
                  return "0";
              }
            } else {
              current="11";
            	inStream = conn.getErrorStream();
            	InputStreamReader in = new InputStreamReader(inStream);
				      BufferedReader reader = new BufferedReader(in);
				      String lineStr;
				      String responseStr = "";
				      while ((lineStr = reader.readLine()) != null) {
    				     responseStr += lineStr;
      				}
            	return "-1: "+responseStr;
            }
        } catch(Exception ex) {
            return current+": "+ex.toString();
        }
    }
    public static String replaceAll(String input,String search,String replace) {
        StringBuffer result=new StringBuffer();
        int i=input.indexOf(search);
        result.append(input.substring(0,i));
        result.append(replace);
        result.append(input.substring(i+search.length()));
        return result.toString();
    }
}
/