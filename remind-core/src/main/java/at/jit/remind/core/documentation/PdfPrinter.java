package at.jit.remind.core.documentation;

import java.io.OutputStream;
import java.util.List;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Description;
import at.jit.remind.core.xml.DistributionItem;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.core.xml.FormattedDescription;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;
import at.jit.remind.core.xml.Schema;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * PDF document generator
 * 
 */
public class PdfPrinter implements Printer<InstallationDocument>
{
	// Fonts for PDF document creation
	private static final String textFont = "Arial,Verdana,sans-serif";
	private static final Font font = FontFactory.getFont(textFont, 12);
	private static final Font fontBold = FontFactory.getFont(textFont, 12, Font.BOLD);
	private static final Font bogFontBold = FontFactory.getFont(textFont, 16, Font.BOLD);
	private static final Font tableFont = FontFactory.getFont(textFont, 9);
	private static final Font tableFontBold = FontFactory.getFont(textFont, 9, Font.BOLD);

	private OutputStream os;

	public PdfPrinter(OutputStream os)
	{
		this.os = os;
	}

	@Override
	public void print(InstallationDocument model) throws PrinterException
	{
		Document document = new Document();
		document.setPageSize(PageSize.A4.rotate());

		Schema schema = new Schema();
		schema.setInstallationDocument(model);

		try
		{
			PdfWriter.getInstance(document, os);
			document.open();

			addMetaData(document);
			addContent(document, schema.getInstallationDocument());

			document.close();
		}
		catch (DocumentException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Error occurred during the PDF document creation!", e.getMessage());
			throw new PrinterException(e);
		}
	}

	/**
	 * Generates new PDF document as a presentation of Schema object from XML file.
	 * 
	 * @param schema
	 *            Schema, unmarshalled XML data.
	 * @param response
	 *            HttpServletResponse, is used for document download.
	 * @throws RemindModelException
	 */

	/**
	 * Add metadata to the PDF which can be viewed in Adobe Reader
	 * 
	 * @param document
	 *            PDF document
	 */
	private void addMetaData(Document document)
	{
		document.addTitle("Installation Document PDF");
		document.addSubject("Presents deployed XML file");
		document.addKeywords("RemInD");
		document.addAuthor("J-IT IT-Dientsleistungs GesmbH");
		document.addCreator("J-IT IT-Dientsleistungs GesmbH");
	}

	/**
	 * Add content to PDF document and create a page.
	 * 
	 * @param document
	 *            PDF document
	 * @param installationDocument
	 *            InstallationDocument from XML file
	 * @throws DocumentException
	 */
	private void addContent(Document document, InstallationDocument installationDocument) throws DocumentException
	{
		Paragraph paragraph = new Paragraph();

		paragraph.add(new Paragraph(installationDocument.getDocumentInformation().getTitle() + " (Version "
				+ installationDocument.getInstallerInformation().getVersion() + ")", bogFontBold));
		addEmptyLine(paragraph, 1);

		paragraph.add(new Paragraph(installationDocument.getDocumentInformation().getTarget(), fontBold));
		addEmptyLine(paragraph, 1);

		paragraph.add(new Paragraph(installationDocument.getDocumentInformation().getRelease(), fontBold));
		addEmptyLine(paragraph, 1);

		paragraph.add(new Paragraph(installationDocument.getDocumentInformation().getTestCycle(), fontBold));
		addEmptyLine(paragraph, 1);

		paragraph.add(new Paragraph("Verteiler: ", fontBold));

		// Create table of distribution items
		createTableOfDistributionItems(paragraph, installationDocument.getDistributionItem());
		addEmptyLine(paragraph, 1);

		// Because we are reading description from List, we need to iterate over it.
		for (Description descriptionOfContent : installationDocument.getDescriptionOfContent())
		{
			paragraph.add(new Paragraph(descriptionOfContent.getCaption(), fontBold));
			addEmptyLine(paragraph, 1);

			paragraph.add(new Paragraph(descriptionOfContent.getText(), font));
			addEmptyLine(paragraph, 1);
		}

		for (Description descriptionOfInstallationOrder : installationDocument.getDescriptionOfInstallationOrder())
		{
			paragraph.add(new Paragraph(descriptionOfInstallationOrder.getCaption(), fontBold));
			addEmptyLine(paragraph, 1);

			paragraph.add(new Paragraph(descriptionOfInstallationOrder.getText(), font));
			addEmptyLine(paragraph, 1);
		}

		for (Description descriptionOfPrecondition : installationDocument.getDescriptionOfPrecondition())
		{
			paragraph.add(new Paragraph(descriptionOfPrecondition.getCaption(), fontBold));
			addEmptyLine(paragraph, 1);

			paragraph.add(new Paragraph(descriptionOfPrecondition.getText(), font));
			addEmptyLine(paragraph, 1);
		}

		paragraph.add(new Paragraph("Installation Block: ", fontBold));
		addEmptyLine(paragraph, 1);

		for (InstallationBlock installationBlock : installationDocument.getInstallationBlock())
		{
			paragraph.add(new Paragraph("Statusbeschreibung: ", fontBold));
			addEmptyLine(paragraph, 1);

			paragraph.add(new Paragraph("Phase: ", fontBold));
			addEmptyLine(paragraph, 1);

			paragraph.add(new Paragraph(installationBlock.getPhaseDescription().getPhase().name(), font));
			addEmptyLine(paragraph, 1);

			paragraph.add(new Paragraph("Beschreibung: ", fontBold));
			addEmptyLine(paragraph, 1);

			for (FormattedDescription formattedDescription : installationBlock.getPhaseDescription().getFormattedDescription())
			{
				paragraph.add(new Paragraph(formattedDescription.getDescription().getCaption(), font));
				addEmptyLine(paragraph, 1);

				paragraph.add(new Paragraph(formattedDescription.getDescription().getText(), font));
				addEmptyLine(paragraph, 1);
			}

			paragraph.add(new Paragraph("Change List: ", fontBold));
			addEmptyLine(paragraph, 1);

			createTableOfChanges(paragraph, installationBlock.getChange());
		}

		document.add(paragraph);
		// Add new page
		document.newPage();
	}

	/**
	 * Add empty line to PDF document.
	 * 
	 * @param paragraph
	 * @param number
	 */
	private static void addEmptyLine(Paragraph paragraph, int number)
	{
		for (int i = 0; i < number; i++)
		{
			paragraph.add(new Paragraph(" "));
		}
	}

	/**
	 * Create table with distribution items.
	 * 
	 * @param paragraph
	 * @param distributionItems
	 */
	private void createTableOfDistributionItems(Paragraph paragraph, List<DistributionItem> distributionItems)
	{
		PdfPTable table = new PdfPTable(3);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setWidthPercentage(50f);

		PdfPCell c1 = new PdfPCell(new Phrase("Name", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Abteilung", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Phone", tableFontBold));
		table.addCell(c1);
		table.setHeaderRows(1);

		for (DistributionItem distributionItem : distributionItems)
		{
			table.addCell(new PdfPCell(new Phrase(distributionItem.getName(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(distributionItem.getDepartment(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(distributionItem.getPhone(), tableFont)));
		}

		paragraph.add(table);

	}

	/**
	 * Create table with changes from InstallationBlock
	 * 
	 * @param paragraph
	 * @param changeItems
	 * @throws DocumentException
	 */
	private void createTableOfChanges(Paragraph paragraph, List<Change> changeItems) throws DocumentException
	{
		PdfPTable table = new PdfPTable(10);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setWidthPercentage(100f);

		// Set width for every single column.
		table.setWidths(new int[]{110, 170, 90, 55, 60, 40, 30, 15, 15, 30});

		PdfPCell c1 = new PdfPCell(new Phrase("SVN Repository Url", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Pfad", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Target", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Beschreibung", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Developer", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Einmalig", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Pause", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("TC", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("QM", tableFontBold));
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("PROD", tableFontBold));
		table.addCell(c1);
		table.setHeaderRows(1);

		for (Change change : changeItems)
		{
			table.addCell(new PdfPCell(new Phrase(change.getSource().getSourceCodeManagement().getSubversion().getRepositoryUrl(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(change.getSource().getSourceCodeManagement().getSubversion().getPath(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(change.getTarget().getDatabase().getSchema() + "." + change.getTarget().getDatabase().getSID(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(change.getDescription(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(change.getDeveloper(), tableFont)));
			table.addCell(new PdfPCell(new Phrase(String.valueOf(change.isOnlyOnce()), tableFont)));
			table.addCell(new PdfPCell(new Phrase(String.valueOf(change.isPause()), tableFont)));
			table.addCell(new PdfPCell(new Phrase(change.getTestCycleNumber().toString(), tableFont)));

			// FIXME: This could be done in more elegant way.
			for (Environment environment : change.getEnvironment())
			{
				if (environment.name().equals(Environment.QM.toString()))
				{
					table.addCell(new PdfPCell(new Phrase("X", tableFont)));
				}
				else
				{
					table.addCell("");
				}
			}

			// FIXME: This could be done in more elegant way.
			for (Environment environment : change.getEnvironment())
			{
				if (environment.name().equals(Environment.PRODUCTION.toString()))
				{
					table.addCell(new PdfPCell(new Phrase("X", tableFont)));
				}
				else
				{
					table.addCell("");
				}
			}
		}

		paragraph.add(table);
	}

}
