package at.jit.remind.core.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MarshalNestedElementTest
{
	@Test
	public void testWithDosumentInformation() throws JAXBException, IOException, ParserConfigurationException, SAXException
	{
		DocumentInformation documentInformation = new DocumentInformation();
		documentInformation.setTitle("Test");
		documentInformation.setRelease("R12.1");
		documentInformation.setTestCycle("Test");
		documentInformation.setTarget("tmp");
		documentInformation.setVersion("1");

		JAXBContext context = JAXBContext.newInstance(DocumentInformation.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		File tempFile = File.createTempFile("JUnit", getClass().getSimpleName());
		tempFile.deleteOnExit();

		marshaller.marshal(new JAXBElement<DocumentInformation>(QName.valueOf(DocumentInformation.class.getSimpleName().toLowerCase()),
				DocumentInformation.class, documentInformation), tempFile);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(tempFile);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<DocumentInformation> unmarshalledObject = unmarshaller.unmarshal(document.getFirstChild(), DocumentInformation.class);

		DocumentInformation unmarshalledDocumentInformation = unmarshalledObject.getValue();
		Assert.assertEquals(documentInformation.getTitle(), unmarshalledDocumentInformation.getTitle());
		Assert.assertEquals(documentInformation.getRelease(), unmarshalledDocumentInformation.getRelease());
		Assert.assertEquals(documentInformation.getTestCycle(), unmarshalledDocumentInformation.getTestCycle());
		Assert.assertEquals(documentInformation.getTarget(), unmarshalledDocumentInformation.getTarget());
		Assert.assertEquals(documentInformation.getVersion(), unmarshalledDocumentInformation.getVersion());
	}
}
