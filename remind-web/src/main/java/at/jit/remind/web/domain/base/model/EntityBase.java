package at.jit.remind.web.domain.base.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@MappedSuperclass
public abstract class EntityBase implements Serializable
{
	private static final long serialVersionUID = 882676199915403098L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@Version
	private Long version;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedOn;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion(Long version)
	{
		this.version = version;
	}

	public Date getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(Date createdOn)
	{
		this.createdOn = createdOn;
	}

	public Date getModifiedOn()
	{
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn)
	{
		this.modifiedOn = modifiedOn;
	}

	@PrePersist
	public void initTimeStamps()
	{
		if (createdOn == null)
		{
			createdOn = new Date();
		}
		modifiedOn = createdOn;
	}

	@PreUpdate
	public void updateTimeStamp()
	{
		modifiedOn = new Date();
	}

	public static class NamedQueryParameterBuilder<E>
	{
		private NamedQueryParameterHolder<E> parameterHolder;

		public NamedQueryParameterBuilder(Class<E> clazz)
		{
			this.parameterHolder = new NamedQueryParameterHolder<E>(clazz);
		}

		public NamedQueryParameterBuilder<E> withQueryName(String queryName)
		{
			parameterHolder.setQueryName(queryName);
			return this;
		}

		public NamedQueryParameterBuilder<E> withParameter(String name, Object value)
		{
			parameterHolder.setParameter(name, value);
			return this;
		}

		public NamedQueryParameterHolder<E> build()
		{
			return parameterHolder;
		}
	}
}
