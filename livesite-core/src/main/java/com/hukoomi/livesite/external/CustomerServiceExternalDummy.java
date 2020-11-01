package com.hukoomi.livesite.external;

import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
/*import org.apache.commons.io.IOUtils;
import org.springframework.util.Base64Utils;*/
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class CustomerServiceExternalDummy {
	private static final Logger logger = Logger.getLogger(CustomerServiceExternal.class);
	
	@RequestMapping(value = "/echofile", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody Document echoFile(MultipartHttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
        MultipartFile multipartFile = request.getFile("file");
        Long size = multipartFile.getSize();
        logger.info("File Size::: "+size);
        String contentType = multipartFile.getContentType();
        logger.info("File ContentType::: "+contentType);
        InputStream stream = multipartFile.getInputStream();
       // byte[] bytes = IOUtils.toByteArray(stream);
    
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("fileoriginalsize", size);
        map.put("contenttype", contentType);
        //map.put("base64", new String(Base64Utils.encode(bytes)));
        Document document = DocumentHelper.createDocument() ;
        return document;
    }

}
