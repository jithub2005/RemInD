package at.jit.remind.web.ui.util;

import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class Navigator implements Serializable
{
	private static final long serialVersionUID = 5703337726711044874L;

	@Inject
	private transient Conversation conversation;

	public String beginConversationAndNavigateTo(String destinationPage)
	{
		beginConversation();
		return navigateTo(destinationPage);
	}

	public String navigateTo(String destinationPage)
	{
		return destinationPage + "?faces-redirect=true";
	}

	public String endConversationAndNavigateTo(String destinationPage)
	{
		endConversation();
		return navigateTo(destinationPage);
	}

	protected void beginConversation()
	{
		if (conversation.isTransient())
		{
			conversation.begin();
		}
	}

	protected void endConversation()
	{
		if (!conversation.isTransient())
		{
			conversation.end();
		}
	}
}
