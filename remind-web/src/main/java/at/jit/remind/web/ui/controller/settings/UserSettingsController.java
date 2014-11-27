package at.jit.remind.web.ui.controller.settings;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.solder.logging.Logger;

import at.jit.remind.core.context.CompositeKeyProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler;
import at.jit.remind.web.domain.context.model.Configuration;
import at.jit.remind.web.domain.context.model.Property;
import at.jit.remind.web.domain.context.service.PropertiesProviderService;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.domain.security.qualifier.NotReadOnly;

@NotReadOnly
@LoggedIn
@Named
@SessionScoped
public class UserSettingsController implements Serializable
{
	private static final long serialVersionUID = 7393656391050250906L;

	@Inject
	private Logger logger;

	@Inject
	private MessageHandler messageHandler;

	@Inject
	private PropertiesProviderService configurationService;

	private List<CompositeKeyProvider> compositeKeyProviders;

	private CompositeKeyProvider compositeKeyProvider;

	private Configuration configuration;

	private boolean editMode;
	private boolean enterValuesPanelExpanded;

	@PostConstruct
	protected void initialize()
	{
		logger.info("userSettingsBean.initialize() called");

		configuration = new Configuration.Builder().build();
		enterValuesPanelExpanded = false;

		compositeKeyProviders = RemindContext.getInstance().getCompositeKeyProviders();
		setType(compositeKeyProviders.get(0).getType());

		logger.info("Default composite key provider is " + compositeKeyProvider.getType());
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	public boolean isEnterValuesPanelExpanded()
	{
		return enterValuesPanelExpanded;
	}

	public void setEnterValuesPanelExpanded(boolean enterValuesPanelExpanded)
	{
		this.enterValuesPanelExpanded = enterValuesPanelExpanded;
	}

	public CompositeKeyProvider getCompositeKeyProvider()
	{
		return compositeKeyProvider;
	}

	public Collection<CompositeKeyProvider> getCompositeKeyProviders()
	{
		return compositeKeyProviders;
	}

	private CompositeKeyProvider findCompositeKeyProvider(String type)
	{
		for (CompositeKeyProvider ckp : compositeKeyProviders)
		{
			if (ckp.getType().equals(type))
			{
				return ckp;
			}
		}

		return null;
	}

	public String getType()
	{
		return compositeKeyProvider.getType();
	}

	public void setType(String type)
	{
		CompositeKeyProvider ckp = findCompositeKeyProvider(type);
		if (ckp == null)
		{
			return;
		}

		logger.info("userSettingsBean.setType(" + type + ")");

		compositeKeyProvider = ckp;
		reset();
	}

	public List<Configuration> getConfigurations()
	{
		return configurationService.getAllByType(getType());
	}

	public void createNew()
	{
		logger.info("userSettingsBean.createNew() called");

		reset();
		setEnterValuesPanelExpanded(true);
	}

	public void edit(long id)
	{
		logger.info("userSettingsBean.edit() called: id=" + id);

		reset();

		configuration = configurationService.getById(id);
		editMode = true;
		setEnterValuesPanelExpanded(true);
	}

	public void save()
	{
		logger.info("userSettingsBean.save() called");

		Map<String, String> lookupKeyMapping = new HashMap<String, String>();
		for (Property pp : configuration.getIdentifiers())
		{
			lookupKeyMapping.put(pp.getKey(), pp.getValue());
		}

		configuration.setLookupKey(compositeKeyProvider.getLookupId(lookupKeyMapping));
		if (configurationService.existsOtherWithLookupKey(configuration))
		{
			messageHandler.addMessage("Configuration with same lookup key already exists");
			FacesContext.getCurrentInstance().addMessage(
					"",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Configuration with same lookup key already exists",
							"Configuration with same lookup key already exists"));

			return;
		}

		configurationService.save(configuration);

		messageHandler.addMessage("Configuration with lookup key " + configuration.getLookupKey() + " successfully saved");
		reset();
	}

	public void delete(long id)
	{
		logger.info("userSettingsBean.delete() called");

		configuration = configurationService.getById(id);
		configurationService.delete(configuration);
		reset();
	}

	public void cancel()
	{
		logger.info("userController.cancel() called");

		if (editMode)
		{
			configurationService.update(configuration);
		}

		reset();
	}

	public void reset()
	{
		logger.info("userSettingsBean.reset() called");

		configuration = new Configuration.Builder().withType(getType()).build();
		editMode = false;
		enterValuesPanelExpanded = false;

		for (String identifier : compositeKeyProvider.getLookupKeys())
		{
			configuration.createIdentifierWithKey(identifier);
		}

		for (String contentKey : compositeKeyProvider.getContentKeys())
		{
			// //TODO
			configuration.createPropertyWithKey(contentKey);

			if (compositeKeyProvider.isContentEncrypted(contentKey))
			{
				Property p = configuration.getPropertyByKey(contentKey);
				p.setEncoded(true);
			}
		}
	}
}
