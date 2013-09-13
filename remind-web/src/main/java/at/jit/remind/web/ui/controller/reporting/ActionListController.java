package at.jit.remind.web.ui.controller.reporting;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.EqualFilter;
import at.jit.remind.web.domain.base.model.GreaterThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.LessThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.ToStringLikeFilter;
import at.jit.remind.web.domain.context.reporting.model.Action;
import at.jit.remind.web.domain.context.reporting.service.ActionService;
import at.jit.remind.web.domain.context.reporting.service.FileInfoService;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.ui.controller.base.EntityListControllerBase;
import at.jit.remind.web.ui.controller.base.EntityListDataModelBase;
import at.jit.remind.web.ui.util.Navigator;

@LoggedIn
@Named
@SessionScoped
public class ActionListController extends EntityListControllerBase<Action>
{
	private static final long serialVersionUID = -6775340147818947336L;

	private static final String sessionIdFilterName = "sessionId";
	private static final String usernameFilterName = "username";
	private static final String fileNameFilterName = "filename";
	private static final String actionTypeFilterName = "actionType";
	private static final String createdOnUpperFilterName = "createdOn.upper";
	private static final String createdOnLowerFilterName = "createdOn.lower";
	private static final String fileInfoIdFilterName = "fileInfoId";

	@Inject
	private Navigator navigator;

	@EJB
	private ActionService actionService;

	@EJB
	private FileInfoService fileInfoService;

	private ActionListDataModel dataModel;

	private EqualFilter<Action, Long> fileInfoIdFilter;

	private ToStringLikeFilter<Action> sessionIdFilter;

	private ToStringLikeFilter<Action> fileNameFilter;

	private boolean showFileInputCreationDateColumn;

	@PostConstruct
	protected void initialize()
	{
		dataModel = new ActionListDataModel();
		dataModel.setEntityService(actionService);

		fileInfoIdFilter = new EqualFilter<Action, Long>("fileInfo.id", Long.class);
		dataModel.addParameter(fileInfoIdFilterName, fileInfoIdFilter);

		fileNameFilter = new ToStringLikeFilter<Action>("fileInfo.name");
		dataModel.addParameter(fileNameFilterName, fileNameFilter);

		// pre-defined ordering on createdOn is descending
		dataModel.addParameter(createdOnLowerFilterName, new GreaterThanOrEqualFilter<Action, Date>("createdOn", Date.class));
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.addParameter(createdOnUpperFilterName, new LessThanOrEqualFilter<Action, Date>("createdOn", Date.class));
		dataModel.addParameter(actionTypeFilterName, new ToStringLikeFilter<Action>("type"));
		dataModel.addParameter(usernameFilterName, new ToStringLikeFilter<Action>("user.username"));

		sessionIdFilter = new ToStringLikeFilter<Action>(sessionIdFilterName);
		dataModel.addParameter(sessionIdFilterName, sessionIdFilter);
	}

	public void resetFilters()
	{
		dataModel.getParameter(fileInfoIdFilterName).clear();

		// pre-defined ordering on createdOn is descending
		dataModel.getParameter(createdOnLowerFilterName).clear();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.getParameter(createdOnUpperFilterName).clear();
		dataModel.getParameter(actionTypeFilterName).clear();
		dataModel.getParameter(fileNameFilterName).clear();
		dataModel.getParameter(usernameFilterName).clear();
		dataModel.getParameter(sessionIdFilterName).clear();

		showFileInputCreationDateColumn = false;
	}

	public String navigateTo(long fileInfoId)
	{
		if (fileInfoId != 0)
		{
			resetFiltersAndSetAscendingOrdering();

			fileInfoIdFilter.setValue(fileInfoId);
			fileNameFilter.setValue(fileInfoService.find(fileInfoId).getName());
			showFileInputCreationDateColumn = true;
		}

		return navigator.navigateTo("/actionList");
	}

	public String navigateToBySessionId(String sessionId)
	{
		resetFiltersAndSetAscendingOrdering();

		sessionIdFilter.setValue(sessionId);
		return navigator.navigateTo("/actionList");
	}

	private void resetFiltersAndSetAscendingOrdering()
	{
		// When user navigates to other pages with implicit filtering, date order shall be ascending
		resetFilters();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
	}

	public boolean isShowFileInputCreationDateColumn()
	{
		return showFileInputCreationDateColumn;
	}

	public void setShowFileInputCreationDateColumn(boolean showFileInputCreationDateColumn)
	{
		this.showFileInputCreationDateColumn = showFileInputCreationDateColumn;
	}

	@Override
	public ActionListDataModel getDataModel()
	{
		return dataModel;
	}

	public ActionListDataModel getModel()
	{
		return getDataModel();
	}

	private static final class ActionListDataModel extends EntityListDataModelBase<Action>
	{
		private ActionListDataModel()
		{
			super(new CriteriaQueryParameterHolder<Action>(Action.class));
		}
	}
}
