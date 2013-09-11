package at.jit.remind.core.documentation;

public interface Printer<T>
{
	void print(T t) throws PrinterException;
}
