package at.jit.remind.web.domain.context.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import at.jit.remind.web.domain.base.model.EntityBase;

@Entity
@Table(name = "CONFIGURATION", uniqueConstraints = {@UniqueConstraint(columnNames = {"CON_LOOKUP_KEY"})})
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "CON_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "CON_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "CON_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "CON_MODIFIED_ON"))})
@NamedQueries({@NamedQuery(name = "Configuration.findByLookupKey", query = "select c from Configuration c where c.lookupKey = :lookupKey"),
		@NamedQuery(name = "Configuration.existsForLookupKey", query = "select count(c.id) from Configuration c where c.lookupKey = :lookupKey"),
		@NamedQuery(name = "Configuration.findByType", query = "select c from Configuration c where c.type = :type")})
public class Configuration extends EntityBase
{
	private static final long serialVersionUID = 5272067524838031293L;

	public static final String findByLookupKeyQuery = Configuration.class.getSimpleName() + ".findByLookupKey";
	public static final String existsForLookupKeyQuery = Configuration.class.getSimpleName() + ".existsForLookupKey";
	public static final String findByTypeQuery = Configuration.class.getSimpleName() + ".findByType";

	public static final String lookupKeyParameter = "lookupKey";
	public static final String findByTypeParameter = "type";

	@Nonnull
	@Column(name = "CON_LOOKUP_KEY", unique = true)
	private String lookupKey;

	@Nonnull
	@Column(name = "CON_TYPE")
	private String type;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CON_2_PRO_IDE", joinColumns = @JoinColumn(name = "C2P_CON_ID"), inverseJoinColumns = @JoinColumn(name = "C2P_PRO_ID"))
	private List<Property> identifiers;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "CON_2_PRO", joinColumns = @JoinColumn(name = "C2P_CON_ID"), inverseJoinColumns = @JoinColumn(name = "C2P_PRO_ID"))
	private List<Property> properties;

	protected Configuration()
	{
		identifiers = new ArrayList<Property>();
		properties = new ArrayList<Property>();
	}

	public String getLookupKey()
	{
		return lookupKey;
	}

	public void setLookupKey(String lookupKey)
	{
		this.lookupKey = lookupKey;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public List<Property> getIdentifiers()
	{
		return identifiers;
	}

	public List<Property> getProperties()
	{
		return properties;
	}

	@Transient
	public void clear()
	{
		for (Property property : properties)
		{
			property.setValue("");
		}

		setLookupKey("");
	}

	@Transient
	public boolean createIdentifierWithKey(String key)
	{
		// TODO check duplicates
		Property property = new Property();
		property.setKey(key);
		identifiers.add(property);

		return true;
	}

	@Transient
	public Property getIdentifierByKey(String key)
	{
		return getPropertyByKeyWithin(key, identifiers);
	}

	@Transient
	public boolean createPropertyWithKey(String key)
	{
		// TODO check duplicates
		Property property = new Property();
		property.setKey(key);
		properties.add(property);

		return true;
	}

	@Transient
	public Property getPropertyByKey(String key)
	{
		return getPropertyByKeyWithin(key, properties);
	}

	private Property getPropertyByKeyWithin(String key, List<Property> propertiesToSearchWithin)
	{
		for (Property property : propertiesToSearchWithin)
		{
			if (property.getKey().equals(key))
			{
				return property;
			}
		}

		return new Property();
	}

	public static class Builder
	{
		private Configuration configuration;

		public Builder()
		{
			configuration = new Configuration();
		}

		public Builder withType(String type)
		{
			configuration.setType(type);

			return this;
		}

		public Builder withLookupKey(String lookupKey)
		{
			configuration.setLookupKey(lookupKey);

			return this;
		}

		public Builder withProperty(String key, String value)
		{
			Property property = new Property();
			property.setKey(key);
			property.setValue(value);

			configuration.getProperties().add(property);

			return this;
		}

		public Configuration build()
		{
			return configuration;
		}
	}
}
