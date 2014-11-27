package at.jit.remind.web.ui.controller.deployment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.swing.tree.TreeNode;

import org.jboss.solder.logging.Logger;
import org.richfaces.cdi.push.Push;
import org.richfaces.component.UITree;
import org.richfaces.event.TreeSelectionChangeEvent;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.MessageHandler;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.documentation.HtmlPrinter;
import at.jit.remind.core.documentation.PdfPrinter;
import at.jit.remind.core.documentation.PrinterException;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.InstallationDocumentModel;
import at.jit.remind.core.model.RemindModel;
import at.jit.remind.core.model.RemindModel.Iterator;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.core.model.UserInput;
import at.jit.remind.core.model.status.State;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;
import at.jit.remind.web.domain.context.reporting.service.DeploymentInformationService;
import at.jit.remind.web.domain.context.service.ConfigurationService.MissingConfigurationException;
import at.jit.remind.web.domain.context.service.PropertiesProviderService;
import at.jit.remind.web.domain.messaging.qualifier.FileUploadEnd;
import at.jit.remind.web.domain.messaging.qualifier.Identifier;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.domain.security.qualifier.NotReadOnly;

import com.google.common.collect.Iterators;

@NotReadOnly
@LoggedIn
@Named
@SessionScoped
public class TreeBean implements Serializable
{
	private static final long serialVersionUID = -7395111693184341495L;

	private static final String topicName = "remindTopic";

	@Inject
	@Push(topic = topicName, subtopic = "#{treeBean.pushIdentifier}")
	private Event<String> pushEvent;

	@Inject
	private Logger logger;

	@Inject
	private MessageHandler messageHandler;

	@Inject
	private PropertiesProviderService propertiesProvider;

	@Inject
	private DeploymentInformationService deploymentInformationHandler;

	private String identifier;

	/*
	 * Converted Schema object with Apache Velocity
	 */
	private String htmlData;

	@Inject
	@Identifier
	private Event<String> identifierEvent;

	private String fileName;
	private InstallationDocumentModel installationDocumentModel;
	private InstallationDocumentModel effectiveModel;
	private UserInput userInput;

	private boolean mayValidate;
	private boolean mayDeploy;
	private boolean deploymentActive;
	private boolean validationActive;

	private ModelNode rootNode;
	@SuppressWarnings("rawtypes")
	private Set<RemindModel> currentSelectionModels;
	private String currentSelectionName;
	private String currentSelectionData;

	private String lastState;

	@PostConstruct
	@SuppressWarnings("rawtypes")
	protected void initialize()
	{
		identifier = UUID.randomUUID().toString().replace("-", "");
		logger.info("treeBean[identifier=" + identifier + "].initialize() called");

		RemindContext.getInstance().setPropertiesProvider(propertiesProvider);
		RemindContext.getInstance().setDeploymentInformationHandler(deploymentInformationHandler);

		reset();

		identifierEvent.fire(identifier);
	}

	private void reset()
	{
		resetInputData();
		resetUserInput();
	}

	private void resetInputData()
	{
		fileName = "";
		installationDocumentModel = new InstallationDocumentModel();

		rootNode = new ModelNode();
		currentSelectionModels = new HashSet<RemindModel>();

		userInput = new UserInput();
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getPushIdentifier()
	{
		return identifier + "Push";
	}

	public String getFileName()
	{
		return fileName;
	}

	public boolean update(String xmlData)
	{
		try
		{
			installationDocumentModel.update(xmlData);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			HtmlPrinter htmlDocumentGenerator = new HtmlPrinter(baos);
			installationDocumentModel.print(htmlDocumentGenerator);
			htmlData = baos.toString();
		}
		catch (RemindModelException e)
		{
			messageHandler.addMessage(MessageLevel.ERROR, "Failed to load xml configuration data!", e.getMessage());

			return false;
		}
		catch (MissingConfigurationException e)
		{
			FeedbackContext contextContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort).build();
			messageHandler.addMessageWithFeedback(MessageLevel.ERROR, "Missing configuration " + e.getLookupKey(), "", contextContext);

			return false;
		}
		catch (PrinterException e)
		{
			messageHandler.addMessage(MessageLevel.ERROR, "Failed to generate HTML document!", e.getMessage());

			return false;
		}

		return true;
	}

	public String getData()
	{
		return installationDocumentModel.getName();
	}

	public List<ModelNode> getRootNodes()
	{
		return rootNode.getNodes();
	}

	@SuppressWarnings("rawtypes")
	public Set<RemindModel> getCurrentSelectionModels()
	{
		return currentSelectionModels;
	}

	@SuppressWarnings("rawtypes")
	public String getCurrentSelectionData()
	{
		return currentSelectionData;
	}

	public String getCurrentSelectionName()
	{
		return currentSelectionName;
	}

	public String getState()
	{
		return lastState;
	}

	private void updateTree()
	{
		currentSelectionData = "";
		currentSelectionName = "";
		currentSelectionModels.clear();

		rootNode.getNodes().clear();
		rootNode = new ModelNode();
		initTreeNode(rootNode, installationDocumentModel);
	}

	@SuppressWarnings("rawtypes")
	private void initTreeNode(ModelNode parent, RemindModel model)
	{
		if (!model.appliesFor(userInput))
		{
			return;
		}

		ModelNode modelNode = new ModelNode();
		modelNode.setModel(model);
		parent.getNodes().add(modelNode);

		Iterator iterator = model.iterator();
		while (iterator.hasNext())
		{
			initTreeNode(modelNode, (RemindModel) iterator.getNext());
		}
	}

	@SuppressWarnings("rawtypes")
	private class ModelNode implements TreeNode
	{
		private List<ModelNode> nodes = new ArrayList<ModelNode>();
		private RemindModel model;

		public RemindModel getModel()
		{
			return model;
		}

		public void setModel(RemindModel model)
		{
			this.model = model;
		}

		public TreeNode getChildAt(int childIndex)
		{
			return nodes.get(childIndex);
		}

		public int getChildCount()
		{
			return nodes.size();
		}

		public TreeNode getParent()
		{
			return null;
		}

		public int getIndex(TreeNode node)
		{
			return nodes.indexOf(node);
		}

		public boolean getAllowsChildren()
		{
			return true;
		}

		public boolean isLeaf()
		{
			return nodes.isEmpty();
		}

		public Enumeration<ModelNode> children()
		{
			return Iterators.asEnumeration(nodes.iterator());
		}

		public List<ModelNode> getNodes()
		{
			return nodes;
		}

		@Override
		public String toString()
		{
			return model.getName();
		}
	}

	public UserInput getUserInput()
	{
		return userInput;
	}

	public void setUserInput(UserInput userInput)
	{
		this.userInput = userInput;
	}

	public void applyUserInput()
	{
		logger.info("Applying user input");

		try
		{
			effectiveModel = installationDocumentModel.getResultingModel(userInput);
		}
		catch (RemindModelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		applyUserInputImpl();
	}

	public void resetUserInput()
	{
		logger.info("Resetting user input");
		userInput.setLowerTestCycleNumber(0);
		userInput.setUpperTestCycleNumber(100);
		userInput.setEnvironment(Environment.ALL.value());

		applyUserInputImpl();
	}

	private void applyUserInputImpl()
	{
		// TODO: check
		boolean fileExists = !fileName.isEmpty();
		mayValidate = fileExists && userInput.getEnvironment().equals(Environment.ALL.value());
		mayDeploy = fileExists && !userInput.getEnvironment().equals(Environment.ALL.value());

		lastState = State.Unknown.toString();

		updateTree();
	}

	public boolean isMayValidate()
	{
		return mayValidate;
	}

	public boolean isValidationActive()
	{
		return validationActive;
	}

	public void validate()
	{
		mayValidate = false;
		validationActive = true;
		logger.info("Set message handler in context");
		RemindContext.getInstance().setMessageHandler(messageHandler);

		logger.info("Validate installation document model");
		installationDocumentModel.validate();

		lastState = installationDocumentModel.getState().toString();

		mayValidate = true;
		validationActive = false;
		pushEvent.fire("Done");
	}

	public boolean isMayDeploy()
	{
		return mayDeploy;
	}

	public boolean isDeploymentActive()
	{
		return deploymentActive;
	}

	public void deploy()
	{
		mayDeploy = false;
		deploymentActive = true;
		logger.info("Set message handler in context");
		RemindContext.getInstance().setMessageHandler(messageHandler);

		logger.info("Deploying installation document model...");

		try
		{
			effectiveModel.deploy(userInput);
		}
		catch (MessageHandlerException e)
		{
			lastState = State.Error.toString();
		}
		finally
		{
			lastState = effectiveModel.getState().toString();
			mayDeploy = true;
			deploymentActive = false;
			pushEvent.fire("Done");
		}
	}

	public boolean isMayApply()
	{
		return !fileName.isEmpty();
	}

	public void handleFileUpload(@Observes @FileUploadEnd FileInfo fileInfo)
	{
		logger.info("treeBean[identifier=" + identifier + "].handleFileUpload(): " + fileInfo.getName());

		try
		{
			if (update(fileInfo.getContent()))
			{
				fileName = fileInfo.getName();
				messageHandler.addMessage("Successfully loaded file " + fileName);
			}
			else
			{
				messageHandler.addMessage("Failed to load file " + fileInfo.getName());
				reset();

				return;
			}
		}
		catch (RuntimeException e)
		{
			messageHandler.addMessage(MessageLevel.ERROR, "Failed to load file " + fileInfo.getName(), e.getMessage());
		}
		finally
		{
			messageHandler.endCurrentAction();
		}

		resetUserInput();
		pushEvent.fire("Done");
	}

	public void selectionChanged(TreeSelectionChangeEvent selectionChangeEvent)
	{
		logger.info("selectionChanged()");
		List<Object> selection = new ArrayList<Object>(selectionChangeEvent.getNewSelection());
		Object currentSelectionKey = selection.get(0);
		UITree tree = (UITree) selectionChangeEvent.getSource();

		Object storedKey = tree.getRowKey();
		tree.setRowKey(currentSelectionKey);

		try
		{
			ModelNode selectedNode = (ModelNode) tree.getRowData();
			currentSelectionData = selectedNode.getModel().getXmlData();
			currentSelectionName = selectedNode.getModel().getName();
		}
		catch (RemindModelException e)
		{
			currentSelectionData = "<InvalidData />";
			currentSelectionName = "";
		}

		tree.setRowKey(storedKey);
	}

	/**
	 * Create PDF file with presentation of deployed XML document.
	 * 
	 * @throws RemindModelException
	 */
	public void createPdfDocument() throws RemindModelException
	{
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		// We can also use "force-download" instead of "pdf"
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "inline; filename=\"PDF-DeploymentReport-" + new Date() + ".pdf\"");

		try
		{
			PdfPrinter pdfDocumentGenerator = new PdfPrinter(response.getOutputStream());
			installationDocumentModel.print(pdfDocumentGenerator);
		}
		catch (IOException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "PDF printing caused an exception!", e.getMessage());
			throw new RemindModelException(e);
		}
		catch (PrinterException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "PDF printing caused an exception!", e.getMessage());
			throw new RemindModelException(e);
		}

		faces.responseComplete();
	}

	public String getHtmlData()
	{
		return htmlData;
	}

	public void setHtmlData(String htmlData)
	{
		this.htmlData = htmlData;
	}

	public String getDeploymentPushAddress()
	{
		return getPushIdentifier() + "@" + topicName;
	}
}
