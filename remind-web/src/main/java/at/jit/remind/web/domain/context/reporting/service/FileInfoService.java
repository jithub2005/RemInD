package at.jit.remind.web.domain.context.reporting.service;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import at.jit.remind.web.domain.base.service.EntityServiceBase;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;

@Stateless
public class FileInfoService extends EntityServiceBase<FileInfo>
{
	private static final long serialVersionUID = 5168368160720434066L;

	@PostConstruct
	@Override
	protected void initialize()
	{
		super.initialize();
		setEntityClazz(FileInfo.class);
	}
}
