package com.hukoomi.contactcenter.controller;

import com.hukoomi.contactcenter.model.ApplicationException;
import com.hukoomi.contactcenter.model.ReportIncident;
import com.hukoomi.contactcenter.services.ReportIncidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.Properties;

import static com.hukoomi.contactcenter.helper.Constants.*;

@RestController
@RequestMapping("ticket")
public class ReportIncidentController {

    private static final Logger logger = LoggerFactory.getLogger(ReportIncidentController.class);

    private final Properties errorProperties;

    @Autowired
    ReportIncidentService reportIncidentService;

    @Autowired
    public ReportIncidentController() {
        errorProperties = new Properties();
        try {
            ClassLoader classLoader = ReportIncidentController.class.getClassLoader();
            errorProperties.load(classLoader.getResourceAsStream("error.properties"));
        } catch (IOException e) {
            logger.error("Can't load error properties -- ",e);
        }
    }

    @GetMapping(path = "/{ticketNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTicket(@PathVariable String ticketNo) {
        logger.info("Get ticket info -- " + ticketNo);

        Object response;
        try {
            response = reportIncidentService.retrieveTicketInfo(ticketNo);
            logger.debug("Got ticket info -- " + ticketNo);
        } catch (WebServiceException e) {
            logger.error("Can't contact web service --", e);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApplicationException(ERROR001, HttpStatus.INTERNAL_SERVER_ERROR.value(), errorProperties.getProperty(ERROR001)));
        } catch (Exception e) {
            logger.error("Error occurred while create report incident info --", e);
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApplicationException(ERROR002, HttpStatus.BAD_REQUEST.value(), errorProperties.getProperty(ERROR002)));
        }

        return response;
    }

    @PostMapping()
    public Object createTicket(@RequestBody ReportIncident reportIncident) {
        logger.info("Report incident creating -- ");

        Object response;
        try {
            response = reportIncidentService.createReportIncident(reportIncident);
        } catch (WebServiceException e) {
            logger.error("Can't contact web service --", e);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApplicationException(ERROR001, HttpStatus.INTERNAL_SERVER_ERROR.value(), errorProperties.getProperty(ERROR001)));
        } catch (Exception e) {
            logger.error("Error occurred while get ticket info --", e);
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApplicationException(ERROR003, HttpStatus.BAD_REQUEST.value(), errorProperties.getProperty(ERROR003)));
        }

        return response;
    }
}
