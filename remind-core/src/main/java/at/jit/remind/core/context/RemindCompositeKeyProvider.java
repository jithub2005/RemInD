package at.jit.remind.core.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemindCompositeKeyProvider implements CompositeKeyProvider
{
	private static final String Empty = "void";
	private static final String Extension = "properties";

	private String type;
	private List<String> lookupKeys = new ArrayList<String>();
	private List<String> contentKeys = new ArrayList<String>();

	private Set<String> contentToEncryptKeys = new HashSet<String>();

	public RemindCompositeKeyProvider(String type, String[] lookupKeys, String[] contentKeys)
	{
		this.type = type;

		for (int i = 0; i < lookupKeys.length; ++i)
		{
			this.lookupKeys.add(lookupKeys[i]);
		}

		for (int i = 0; i < contentKeys.length; ++i)
		{
			this.contentKeys.add(contentKeys[i]);
		}
	}

	@Override
	public String getType()
	{
		return type;
	}

	@Override
	public List<String> getLookupKeys()
	{
		return new ArrayList<String>(lookupKeys);
	}

	@Override
	public String getLookupId(Map<String, String> lookupKeyMapping)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(type).append('.');

		for (Iterator<String> iterator = lookupKeys.iterator(); iterator.hasNext(); stringBuilder.append('.'))
		{
			String key = iterator.next();
			if (lookupKeyMapping.containsKey(key))
			{
				stringBuilder.append(lookupKeyMapping.get(key));
			}
			else
			{
				stringBuilder.append(Empty);
			}
		}

		stringBuilder.append(Extension);

		return stringBuilder.toString();
	}

	@Override
	public List<String> getContentKeys()
	{
		return new ArrayList<String>(contentKeys);
	}

	public void markContentAsEncrypted(String contentKey)
	{
		if (contentKeys.contains(contentKey))
		{
			contentToEncryptKeys.add(contentKey);
		}
	}

	@Override
	public boolean isContentEncrypted(String contentKey)
	{
		return contentToEncryptKeys.contains(contentKey);
	}
}
