package at.jit.remind.core.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PropertiesFromResourceProviderTest
{
	private static final String LevelA = "LevelA";
	private static final String LevelB = "LevelB";

	private static final String ValueA = "valueA";
	private static final String ValueB = "valueB";

	private CompositeKeyProvider ckp;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
	}

	@Before
	public void setUp() throws Exception
	{
		ckp = new RemindCompositeKeyProvider(getClass().getSimpleName(), new String[]{LevelA, LevelB}, new String[]{ValueA, ValueB});
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testKeyProvider()
	{
		List<String> lookupKeys = ckp.getLookupKeys();
		Assert.assertTrue("lookupKeys match", lookupKeys.contains(LevelA) && lookupKeys.contains(LevelB));

		List<String> contentKeys = ckp.getContentKeys();
		Assert.assertTrue("contentKeys match", contentKeys.contains(ValueA) && contentKeys.contains(ValueB));
	}

	@Test
	public void testWithExistingResource()
	{
		Map<String, String> valuesToMap = new HashMap<String, String>();
		valuesToMap.put(LevelA, ValueA);
		valuesToMap.put(LevelB, ValueB);

		String compositeKey = ckp.getLookupId(valuesToMap);
		Properties properties = RemindContext.getInstance().getPropertiesProvider().getProperties(compositeKey);

		Assert.assertEquals("Value for key1 of file has correct value", properties.getProperty("key1"), "value1");
		Assert.assertEquals("Value for key2 of file has correct value", properties.getProperty("key2"), "value2");
		Assert.assertEquals("Value for key3 of file has correct value", properties.getProperty("key3"), "");
		Assert.assertEquals("Value for key4 is null", properties.getProperty("key4", "default"), "default");
	}

	@Test
	public void testWithNotExistingResource()
	{
		Properties properties = RemindContext.getInstance().getPropertiesProvider().getProperties("not existing resource");

		Assert.assertTrue("Size of properties must be 0", properties.size() == 0);
	}
}
