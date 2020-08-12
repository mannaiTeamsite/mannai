package com.hukoomi.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import com.hukoomi.utils.SitePathUtils;

public class SitePathUtilsTest {

	
	@Test
	public void getSiteFromVpathTest(){
		
		//good test
		String vpath="//vm-ts-dev-001/default/main/EStudioGlobal/WORKAREA/default/sites/globalsite/default.site";
		String result = SitePathUtils.getSiteFromVpath(vpath);
		assertEquals("Site should be globalsite",result,"globalsite");
		
		//bad site test
		String bad_vpath="//vm-ts-dev-001/default/main/EStudioGlobal/WORKAREA/default/templatedata/blog/default-blog/data/test.xml";
		String bad_result = SitePathUtils.getSiteFromVpath(bad_vpath);
		assertNull("No site on this vpath",bad_result);
		
	}
}
