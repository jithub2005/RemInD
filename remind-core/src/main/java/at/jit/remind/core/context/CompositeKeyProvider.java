package at.jit.remind.core.context;

import java.util.List;
import java.util.Map;

public interface CompositeKeyProvider
{
	String getType();

	List<String> getLookupKeys();

	String getLookupId(Map<String, String> lookupKeyMapping);

	List<String> getContentKeys();

	boolean isContentEncrypted(String contentKey);
}
