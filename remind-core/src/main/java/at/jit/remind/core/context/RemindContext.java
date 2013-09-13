package at.jit.remind.core.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import at.jit.remind.core.context.messaging.MessageHandler;
import at.jit.remind.core.context.reporting.DeploymentInformationHandler;
import at.jit.remind.core.documentation.CreateHtmlDocumentation;
import at.jit.remind.core.documentation.ParseWiki;
import at.jit.remind.core.model.content.database.DatabaseTarget;
import at.jit.remind.core.model.content.scm.SubversionSource;

public final class RemindContext
{
	private static final RemindContext instance = new RemindContext();

	private static final AtomicInteger contextIdCounter = new AtomicInteger(0);

	// list is containing composite key providers in order of registration, map is used for fast lookup when
	// a specific key provider is requested by type
	private List<CompositeKeyProvider> compositeKeyProviders = new ArrayList<CompositeKeyProvider>();
	private Map<String, CompositeKeyProvider> compositeKeyProviderMap = new TreeMap<String, CompositeKeyProvider>();

	private PropertiesProvider propertiesProvider;

	private DeploymentInformationHandler deploymentInformationHandler;

	private Map<Integer, MessageHandler> messageHandlerMap = new HashMap<Integer, MessageHandler>();

	private static final ThreadLocal<Integer> uniqueContextId = new ThreadLocal<Integer>()
	{
		@Override
		protected Integer initialValue()
		{
			return contextIdCounter.getAndIncrement();
		}
	};

	private static int getContextId()
	{
		return uniqueContextId.get();
	}

	private RemindContext()
	{
		// TODO: check other possibilities for composite key provider registration
		registerCompositeKeyProvider(SubversionSource.subversionKeyProvider);
		registerCompositeKeyProvider(DatabaseTarget.connectionKeyProvider);
		registerCompositeKeyProvider(DatabaseTarget.schemaKeyProvider);

		registerCompositeKeyProvider(ParseWiki.syntaxKeyProvider);
		registerCompositeKeyProvider(CreateHtmlDocumentation.velocityKeyProvider);
	}

	public static RemindContext getInstance()
	{
		return instance;
	}

	public void registerCompositeKeyProvider(CompositeKeyProvider compositeKeyProvider)
	{
		compositeKeyProviders.add(compositeKeyProvider);
		compositeKeyProviderMap.put(compositeKeyProvider.getType(), compositeKeyProvider);
	}

	public CompositeKeyProvider getCompositeKeyProvider(String type)
	{
		return compositeKeyProviderMap.get(type);
	}

	public List<CompositeKeyProvider> getCompositeKeyProviders()
	{
		return compositeKeyProviders;
	}

	public PropertiesProvider getPropertiesProvider()
	{
		return propertiesProvider;
	}

	public void setPropertiesProvider(PropertiesProvider propertiesProvider)
	{
		this.propertiesProvider = propertiesProvider;
	}

	public DeploymentInformationHandler getDeploymentInformationHandler()
	{
		return deploymentInformationHandler;
	}

	public void setDeploymentInformationHandler(DeploymentInformationHandler deploymentInformationHandler)
	{
		this.deploymentInformationHandler = deploymentInformationHandler;
	}

	public synchronized MessageHandler getMessageHandler()
	{
		return messageHandlerMap.get(getContextId());
	}

	public synchronized void setMessageHandler(MessageHandler messageHandler)
	{
		messageHandlerMap.put(getContextId(), messageHandler);
	}
}
