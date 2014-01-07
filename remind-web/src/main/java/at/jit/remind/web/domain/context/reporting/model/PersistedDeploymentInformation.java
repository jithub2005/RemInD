package at.jit.remind.web.domain.context.reporting.model;

import java.math.BigInteger;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import at.jit.remind.web.domain.base.model.EntityBase;

@Entity
@Table(name = "DEPLOYMENT_INFORMATION")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "DPI_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "DPI_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "DPI_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "DPI_MODIFIED_ON"))})
@NamedQueries({
		@NamedQuery(name = "PersistedDeploymentInformation.findSourceAndTarget", query = "select d from PersistedDeploymentInformation d where d.sourceInfo = :sourceInfo and d.targetInfo = :targetInfo order by d.createdOn desc"),
		@NamedQuery(name = "PersistedDeploymentInformation.findDeploymentByAction", query = "select d from PersistedDeploymentInformation d where d.actionId = :actionId"),
		@NamedQuery(name = "PersistedDeploymentInformation.selectAll", query = "select d from PersistedDeploymentInformation d order by d.release desc, d.environment desc, d.target asc, d.createdOn desc"),
		@NamedQuery(name = "PersistedDeploymentInformation.findByFileInfoId", query = "select d from PersistedDeploymentInformation d where d.actionId in (select a.id from Action a where a.fileInfo.id = :fileInfoId)")})
public class PersistedDeploymentInformation extends EntityBase
{
	private static final long serialVersionUID = -5299878883610113126L;

	public static final String FindSourceAndTarget = PersistedDeploymentInformation.class.getSimpleName() + ".findSourceAndTarget";
	public static final String FindDeploymentByAction = PersistedDeploymentInformation.class.getSimpleName() + ".findDeploymentByAction";
	public static final String SelectAll = PersistedDeploymentInformation.class.getSimpleName() + ".selectAll";
	public static final String FindByFileInfoId = PersistedDeploymentInformation.class.getSimpleName() + ".findByFileInfoId";
	public static final String SourceInfo = "sourceInfo";
	public static final String TargetInfo = "targetInfo";
	public static final String FileInfoId = "fileInfoId";

	@Column(name = "DPI_DEPLOYMENT_VERSION")
	private String deploymentVersion;

	@Column(name = "DPI_TITLE")
	private String title;

	@Column(name = "DPI_TARGET")
	private String target;

	@Column(name = "DPI_RELEASE")
	private String release;

	@Column(name = "DPI_TESTCYCLE")
	private String testCycle; // this is the testCycle set in documentInformation part of xml

	@Column(name = "DPI_ENVIRONMENT")
	private String environment;

	@Column(name = "DPI_TESTCYCLENUMBER", precision = 0)
	private BigInteger testCycleNumber; // this is the testCycle set in change part of xml

	@Column(name = "DPI_SOURCEINFO", length = 1024)
	private String sourceInfo;

	@Column(name = "DPI_TARGETINFO", length = 1024)
	private String targetInfo;

	@Column(name = "DPI_DEVELOPER")
	private String developer;

	@Column(name = "D2A_ACT_ID")
	private Long actionId;

	@Column(name = "DPI_STATUS")
	private String status;

	@Column(name = "DPI_STATUS_DETAILS", length = 1024)
	private String statusDetails;

	public String getDeploymentVersion()
	{
		return deploymentVersion;
	}

	public void setDeploymentVersion(String deploymentVersion)
	{
		this.deploymentVersion = deploymentVersion;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String getRelease()
	{
		return release;
	}

	public void setRelease(String release)
	{
		this.release = release;
	}

	public String getTestCycle()
	{
		return testCycle;
	}

	public void setTestCycle(String testCycle)
	{
		this.testCycle = testCycle;
	}

	public String getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(String environment)
	{
		this.environment = environment;
	}

	public BigInteger getTestCycleNumber()
	{
		return testCycleNumber;
	}

	public void setTestCycleNumber(BigInteger testCycleNumber)
	{
		this.testCycleNumber = testCycleNumber;
	}

	public String getSourceInfo()
	{
		return sourceInfo;
	}

	public void setSourceInfo(String sourceInfo)
	{
		this.sourceInfo = sourceInfo;
	}

	public String getTargetInfo()
	{
		return targetInfo;
	}

	public void setTargetInfo(String targetInfo)
	{
		this.targetInfo = targetInfo;
	}

	public String getDeveloper()
	{
		return developer;
	}

	public void setDeveloper(String developer)
	{
		this.developer = developer;
	}

	public Long getActionId()
	{
		return actionId;
	}

	public void setActionId(Long actionId)
	{
		this.actionId = actionId;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getStatusDetails()
	{
		return statusDetails;
	}

	public void setStatusDetails(String statusDetails)
	{
		this.statusDetails = statusDetails;
	}
}
