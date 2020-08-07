package com.hukoomi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SitePathUtils {

	
	private final static Pattern REGEX_SITE_MATCHER = Pattern.compile(".*/sites/([^\\\\]+)/.*");
	
	public static String getSiteFromVpath(final String _vpath){
		
		if (_vpath == null){
			return null;
		}
		
		Matcher m = REGEX_SITE_MATCHER.matcher(_vpath);
		
		if (m.matches()){
			return m.group(1);
		}
		
		else return null;
		
	}
}
