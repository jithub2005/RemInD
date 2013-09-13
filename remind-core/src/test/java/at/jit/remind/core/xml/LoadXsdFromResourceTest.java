package at.jit.remind.core.xml;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

public class LoadXsdFromResourceTest
{
	@Test
	public void test() throws SAXException
	{
		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Source schemaFile = new StreamSource(getClass().getResourceAsStream("/remind-v01.xsd"));
		Schema schema = factory.newSchema(schemaFile);

		Assert.assertNotNull(schema);
	}
}
