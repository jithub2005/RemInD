package at.jit.remind.core.model;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.jit.remind.core.exception.RemindModelException;

public class XmlHelper
{
	public static Element parseDocumentElement(String xmlData) throws RemindModelException
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlData));
			Document document = db.parse(is);
			Element documentElement = document.getDocumentElement();

			return documentElement;
		}
		catch (ParserConfigurationException e)
		{
			throw new RemindModelException(e);
		}
		catch (SAXException e)
		{
			throw new RemindModelException(e);
		}
		catch (IOException e)
		{
			throw new RemindModelException(e);
		}
	}

	public static JAXBElement<?> unmarshall(Node nodeToUnmarshal, Class<?> clazz) throws RemindModelException
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			JAXBElement<?> unmarshalledObject = unmarshaller.unmarshal(nodeToUnmarshal, clazz);

			return unmarshalledObject;
		}
		catch (JAXBException e)
		{
			throw new RemindModelException(e);
		}
	}
}
