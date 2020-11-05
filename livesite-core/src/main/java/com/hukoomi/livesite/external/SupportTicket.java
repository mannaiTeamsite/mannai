package com.hukoomi.livesite.external;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.apache.commons.fileupload.util.mime.MimeUtility;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;


@Controller
public class SupportTicket {
	/** Logger object to check the flow of the code. */
	private static final Logger logger = Logger.getLogger(SupportTicket.class);
	
	@RequestMapping(value = "/submitticket", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody HashMap<String, Object> submitTicket(MultipartHttpServletRequest request,
            HttpServletResponse response,@RequestParam("file") MultipartFile file) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (!file.isEmpty()) {
        MultipartFile multipartFile = request.getFile("file");
        Long size = multipartFile.getSize();
        String contentType = multipartFile.getContentType();
        InputStream stream = multipartFile.getInputStream();
        //byte[] bytes = IOUtils.toByteArray(stream);
        logger.debug("file size:"+ size);
        logger.debug("file type:"+ contentType);
        System.out.println("file size:"+ size);
        System.out.println("file type:"+ contentType);
        
        map.put("fileoriginalsize", size);
        map.put("contenttype", contentType);
        //map.put("base64", new String(Base64Utils.encode(bytes)));
        }
		map.put("qid", request.getParameter("qid"));
		
        return map;
    }
	
}
