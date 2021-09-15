package com.hukoomi.contactcenter.helper;

import com.hukoomi.contactcenter.controller.ReportIncidentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SSLInterceptor implements HandlerInterceptor  {

	private static final Logger logger = LoggerFactory.getLogger(ReportIncidentController.class);

	@Autowired
	private Environment env;

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String sslEnabled = env.getProperty("server.ssl.security.certificate.enable");
		if(sslEnabled!=null && sslEnabled.equals("true")) {
			logger.debug("SSL certificate trust enabled for testing & development purpose -- ");
			SSLUtilities.trustAllHostnames();
	        SSLUtilities.trustAllHttpsCertificates();
		}
		
        return true;
	}
}
