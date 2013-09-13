package at.jit.remind.core.model;

import at.jit.remind.core.documentation.Printer;
import at.jit.remind.core.documentation.PrinterException;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;

public interface RemindModel<T, I> extends Description
{
	void update(String xmlData) throws RemindModelException;

	String getXmlData() throws RemindModelException;

	void update(T element) throws RemindModelException;

	void validate();

	void deploy(UserInput userInput) throws MessageHandlerException;

	boolean appliesFor(UserInput userInput);

	Iterator<I> iterator();

	interface Iterator<I>
	{
		boolean hasNext();

		I getNext();
	}

	void print(Printer<?> printer) throws PrinterException;
}
