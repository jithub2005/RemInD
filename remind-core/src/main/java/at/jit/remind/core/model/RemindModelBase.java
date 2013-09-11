package at.jit.remind.core.model;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.jit.remind.core.context.messaging.MessageHandler.Range;
import at.jit.remind.core.context.reporting.DeploymentInformation;
import at.jit.remind.core.documentation.Printer;
import at.jit.remind.core.documentation.PrinterException;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.status.State;
import at.jit.remind.core.xml.Schema;

public abstract class RemindModelBase<T, I> implements RemindModel<T, I>
{
	private static final String schemaTagName = getTagName(Schema.class);

	@SuppressWarnings("rawtypes")
	private RemindModelBase parent;

	private T element;
	private Class<T> clazz;

	private String tagName;

	private State state = State.Unknown;

	private Range range;

	private String name;
	private String details;

	private DeploymentInformation deploymentInformation = new DeploymentInformation();

	protected RemindModelBase(Class<T> clazz)
	{
		this.clazz = clazz;
		tagName = getTagName(clazz);
	}

	@SuppressWarnings("rawtypes")
	protected RemindModelBase getParent()
	{
		return parent;
	}

	@SuppressWarnings("rawtypes")
	protected void setParent(RemindModelBase parent)
	{
		this.parent = parent;
	}

	public String getTagName()
	{
		return tagName;
	}

	@Override
	public void print(Printer printer) throws PrinterException
	{
		printer.print(getElement());
	}

	@Override
	public void update(String xmlData) throws RemindModelException
	{
		update(unmarshall(xmlData));
	}

	protected T unmarshall(String xmlData) throws RemindModelException
	{
		Element documentElement = XmlHelper.parseDocumentElement(xmlData);
		Node nodeToUnmarshal = documentElement;

		if (schemaTagName.equals(documentElement.getLocalName()))
		{
			NodeList nodeList = documentElement.getElementsByTagName(tagName);

			if (nodeList.getLength() != 1)
			{
				throw new RemindModelException("RemindModelBase: no or ambiguous content received");
			}

			nodeToUnmarshal = nodeList.item(0);
		}

		if (!tagName.equals(nodeToUnmarshal.getLocalName()))
		{
			throw new RemindModelException("RemindModelBase: wrong content received (expected: " + tagName + " received: " + nodeToUnmarshal.getLocalName()
					+ ")");
		}

		@SuppressWarnings("unchecked")
		JAXBElement<T> unmarshalledObject = (JAXBElement<T>) XmlHelper.unmarshall(nodeToUnmarshal, clazz);

		return unmarshalledObject.getValue();
	}

	@Override
	public String getXmlData() throws RemindModelException
	{
		return getXmlData(getElement());
	}

	protected String getXmlData(T element) throws RemindModelException
	{
		try
		{
			StringWriter writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(clazz);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(new JAXBElement<T>(QName.valueOf(getTagName(clazz)), clazz, element), writer);

			return writer.toString();
		}
		catch (JAXBException e)
		{
			throw new RemindModelException(e);
		}
	}

	protected void removeAllChilds()
	{
	}

	protected abstract void validate(UserInput userInput) throws MessageHandlerException;

	@Override
	public boolean appliesFor(UserInput userInput)
	{
		return true;
	}

	protected T getElement()
	{
		return element;
	}

	protected void setElement(T element)
	{
		this.element = element;
	}

	public State getState()
	{
		return state;
	}

	protected void setState(State state)
	{
		this.state = state;
	}

	protected abstract State determineState();

	protected abstract void resetState();

	protected Range getRange()
	{
		return range;
	}

	protected void setRange(Range range)
	{
		this.range = range;
	}

	@Override
	public String getName()
	{
		return name;
	}

	protected void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getDetails()
	{
		return details;
	}

	protected void setDetails(String details)
	{
		this.details = details;
	}

	protected DeploymentInformation getDeploymentInformation()
	{
		return deploymentInformation;
	}

	protected abstract void fill(DeploymentInformation deploymentInformation);

	public static String getTagName(Class<?> clazz)
	{
		return StringUtils.uncapitalize(clazz.getSimpleName());
	}
}
