package org.akaza.openclinica.service.openrosa;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.pform.OpenRosaXmlGenerator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenRosaXmlGeneratorTest
{
	static CoreResources mockedCoreResources;
	static ResourceLoader resourceLoader = new DefaultResourceLoader();
	
	@BeforeClass
	public static void setUp()
	{
		try
		{
			resourceLoader = new DefaultResourceLoader();
			mockedCoreResources = mock(CoreResources.class);
			when(mockedCoreResources.getURL("openRosaXFormMapping.xml"))
				.thenReturn(resourceLoader.getResource("classpath:properties/openRosaXFormMapping.xml").getURL());
		} catch (Exception e)
		{
		    fail(e.getMessage());	
		}
	}
	
	@Test
	public void testFormXmlToJava()
	{
		String xform = null;
		try
		{
			OpenRosaXmlGenerator generator = new OpenRosaXmlGenerator(mockedCoreResources);
			xform = generator.buildForm("crf_version_oid");
		} catch (Exception e)
		{
			fail(e.getMessage());
		}
		assertNotNull(xform);
		assertFalse(xform.equals(""));
	}
	
}
