package com.hukoomi.contactcenter.services;

import com.hp.schemas.sm._7.*;
import com.hp.schemas.sm._7.ObjectFactory;
import com.hp.schemas.sm._7.common.*;
import com.hukoomi.contactcenter.adapter.ReportIncidentAdapter;
import com.hukoomi.contactcenter.helper.Constants;
import com.hukoomi.contactcenter.model.Ticket;
import com.hukoomi.contactcenter.model.ReportIncident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReportIncidentService {

    private static final Logger logger = LoggerFactory.getLogger(ReportIncidentService.class);

    @Autowired
    ReportIncidentAdapter reportIncidentAdapter;

    public ReportIncident createReportIncident(ReportIncident reportIncident) throws Exception {
        CreateContactCenterResponse createContactCenterResponse = reportIncidentAdapter.createReportIncident(prepareCreateContactCenterRequest(reportIncident));

        ContactCenterKeysType centerKeysType = createContactCenterResponse.getModel().getKeys();
        StringType ticketNumber = centerKeysType.getCallID().getValue();

        logger.debug("Service Message : " + createContactCenterResponse.getMessage());
        logger.info("Service Status : " + createContactCenterResponse.getStatus());
        logger.debug("Service Model : " + createContactCenterResponse.getModel());
        logger.info("Ticket Number : " + ticketNumber);

        if(StatusType.SUCCESS.equals(createContactCenterResponse.getStatus())) {
            reportIncident.setSuccess(Constants.SUCCESS);
            reportIncident.setTicketNumber(ticketNumber.getValue());
        } else {
            reportIncident.setSuccess(Constants.FALSE);
            MessagesType messagesType = createContactCenterResponse.getMessages();
            ArrayList<String> msgList = new ArrayList<String>();

            if(messagesType!=null && messagesType.getMessage() != null && messagesType.getMessage().size() > 0){
                for (int index = 0; index < messagesType.getMessage().size(); index++) {
                    MessageType messageType = messagesType.getMessage().get(index);
                    logger.debug("Soap Messages:"+messageType.getValue());
                    msgList.add(messageType.getValue());
                }
            }
            reportIncident.setValidationMessage(msgList);
        }
        logger.debug("Create contact center submit ticket ending");
        return reportIncident;
    }

    private CreateContactCenterRequest prepareCreateContactCenterRequest(ReportIncident reportIncident) {

        ContactCenterInstanceType centerInstanceType = new ContactCenterInstanceType();

        ObjectFactory objectFactory = new ObjectFactory();

        // contact type
        StringType contactIdType = new StringType();
        contactIdType.setValue(reportIncident.getIdType());
        centerInstanceType.setContactIdType(objectFactory.createContactCenterInstanceTypeContactFullName(contactIdType));

        // contact full name
        StringType contactFullName = new StringType();
        contactFullName.setValue(reportIncident.getFullName());
        centerInstanceType.setContactFullName(objectFactory.createContactCenterInstanceTypeContactFullName(contactFullName));

        // passport
        StringType passport = new StringType();
        passport.setValue(reportIncident.getPassport());
        centerInstanceType.setContactFullName(objectFactory.createContactCenterInstanceTypeContactFullName(passport));

        // qid
        LongType qid = new LongType();
        if(reportIncident.getQid() != null){
            qid.setValue(reportIncident.getQid());
        }
        centerInstanceType.setContactQID(objectFactory.createContactCenterInstanceTypeContactQID(qid));

        // eid
        LongType eid = new LongType();
        if(reportIncident.getEid() != null){
            eid.setValue(reportIncident.getEid());
        }
        centerInstanceType.setContactEID(objectFactory.createContactCenterInstanceTypeContactEID(eid));

        // mobile
        StringType mobile = new StringType();
        if(reportIncident.getPhoneNo()!=null){
            logger.debug("Phone number - " + reportIncident.getPhoneNo());
            mobile.setValue(Integer.toString(reportIncident.getPhoneNo()));
        }
        centerInstanceType.setContactMobilePhone(objectFactory.createContactCenterInstanceTypeContactMobilePhone(mobile));

        // email
        StringType email = new StringType();
        logger.debug("Email - "+reportIncident.getEmailId());
        email.setValue(reportIncident.getEmailId());
        centerInstanceType.setContactEmail(objectFactory.createContactCenterInstanceTypeContactEmail(email));

        // company name
        StringType companyName = new StringType();
        companyName.setValue(reportIncident.getCompanyName());
        centerInstanceType.setContactCompanyName(objectFactory.createContactCenterInstanceTypeContactCompanyName(companyName));

        // title
        StringType title = new StringType();
        logger.debug("Subject - " + reportIncident.getSubject());
        if(reportIncident.getSubject()==null){
            reportIncident.setSubject("");
        }
        title.setValue(reportIncident.getSubject());
        centerInstanceType.setTitle(objectFactory.createContactCenterInstanceTypeTitle(title));

        // eService name
        ContactCenterInstanceType.Description description = new ContactCenterInstanceType.Description();
        StringType eServiceName = new StringType();
        eServiceName.setValue(Constants.E_SERVICE + reportIncident.geteServiceName());
        description.getDescription().add(eServiceName);

        // service name
        StringType serviceName = new StringType();
        serviceName.setValue(Constants.SERVICE + reportIncident.getServiceName());
        description.getDescription().add(serviceName);

        // nationality
        StringType nationality = new StringType();
        nationality.setValue(Constants.NATIONALITY + reportIncident.getNationality());
        description.getDescription().add(nationality);

        // comments
        StringType comments = new StringType();
        comments.setValue(reportIncident.getComments());
        description.getDescription().add(comments);
        centerInstanceType.setDescription(description);

        // origin
        StringType origin = new StringType();
        origin.setValue(Constants.ORIGIN);
        centerInstanceType.setCallOrigin(objectFactory.createContactCenterInstanceTypeCallOrigin(origin));

        // external
        BooleanType external = new BooleanType();
        external.setValue(Constants.EXTERNAL);
        centerInstanceType.setExternal(objectFactory.createContactCenterInstanceTypeExternal(external));

        // category
        StringType category = new StringType();
        category.setValue(Constants.CATEGORY);
        centerInstanceType.setCategory(objectFactory.createContactCenterInstanceTypeCategory(category));

        // service category
        StringType serviceCategory = new StringType();
        serviceCategory.setValue(Constants.SERVICE_CATEGORY);
        centerInstanceType.setServiceCategory(objectFactory.createContactCenterInstanceTypeServiceCategory(serviceCategory));

        // attachments
        if(reportIncident.getAttachment()!=null && reportIncident.getAttachment().getContent()!=null)
        {
            AttachmentType attachmentType=new AttachmentType();
            attachmentType.setValue(reportIncident.getAttachment().getContent());
            //logger.debug("reportIncident.getAttachment().getContent() "+reportIncident.getAttachment().getContent());
            //logger.debug("reportIncident.getAttachment().getContent() "+reportIncident.getAttachment().getContent().toString());
            attachmentType.setAction(Constants.ADD);
            attachmentType.setHref(reportIncident.getAttachment().getFileName());
            attachmentType.setAttachmentType(reportIncident.getAttachment().getFileType());
            logger.debug("reportIncident.getAttachment().getFileType() "+reportIncident.getAttachment().getFileType());
            attachmentType.setContentType(reportIncident.getAttachment().getFileType());
            attachmentType.setName(reportIncident.getAttachment().getFileName());
            attachmentType.setType(reportIncident.getAttachment().getFileType());
            attachmentType.setLen(reportIncident.getAttachment().getContent().length);
            logger.debug("reportIncident.getAttachment().getContent().length "+reportIncident.getAttachment().getContent().length);
            AttachmentsType attachmentsType = new AttachmentsType();
            attachmentsType.getAttachment().add(attachmentType);
            centerInstanceType.setAttachments(objectFactory.createContactCenterInstanceTypeAttachments(attachmentsType));
        }

        CreateContactCenterRequest createContactCenterRequest =new  CreateContactCenterRequest();
        ContactCenterModelType model = new ContactCenterModelType();
        model.setInstance(centerInstanceType);
        model.setKeys(new ContactCenterKeysType());
        createContactCenterRequest.setAttachmentData(Boolean.TRUE);
        createContactCenterRequest.setAttachmentInfo(Boolean.TRUE);
        createContactCenterRequest.setModel(model);
        createContactCenterRequest.setIgnoreEmptyElements(Boolean.FALSE);

        logger.debug("Create contact center request ending --"+createContactCenterRequest);
        return createContactCenterRequest;
    }

    public Ticket retrieveTicketInfo(String ticketNo) throws Exception {
        RetrieveContactCenterResponse retrieveContactCenterResponse = reportIncidentAdapter.retrieveContactCenter(prepareRetrieveContactCenterRequest(ticketNo));
        Ticket ticket = new Ticket();

        logger.debug("Response status - " + retrieveContactCenterResponse.getStatus());
        logger.debug("Response message - " + retrieveContactCenterResponse.getMessage());

        if(retrieveContactCenterResponse.getModel() != null && retrieveContactCenterResponse.getModel().getInstance() != null) {

            ContactCenterInstanceType centerInstanceType = retrieveContactCenterResponse.getModel().getInstance();
            if (centerInstanceType.getContactEID() != null) {
                ticket.setEid(String.valueOf(centerInstanceType.getContactEID().getValue().getValue()));
            }
            if (centerInstanceType.getContactQID() != null) {
                ticket.setQid(String.valueOf(centerInstanceType.getContactQID().getValue().getValue()));
            }

            if (centerInstanceType.getDescription() != null && centerInstanceType.getDescription().getDescription() != null) {
                List<StringType> descriptions = centerInstanceType.getDescription().getDescription();
                for (StringType description : descriptions) {
                    if (description != null && description.getValue() != null &&
                            description.getValue().contains(Constants.E_SERVICE)) {
                        ticket.seteServiceName(description.getValue().substring(description.getValue().indexOf(Constants.E_SERVICE) + Constants.E_SERVICE.length()));
                        logger.debug("EServiceName - " + ticket.geteServiceName());
                    }
                }
            }

            if (centerInstanceType.getOpenTime() != null && centerInstanceType.getOpenTime().getValue() != null) {
            	logger.debug("dateTimeType HPSM- " + centerInstanceType.getOpenTime().getValue().getValue());
            	logger.debug("dateTimeType HPSM- " + centerInstanceType.getOpenTime().getValue());
            	XMLGregorianCalendar dateTimeType = centerInstanceType.getOpenTime().getValue().getValue();
                logger.debug("dateTimeType - " + dateTimeType);
				/*
				 * Date date = dateTimeType.toGregorianCalendar().getTime();
				 * logger.debug("date - " + date); ticket.setCreatedDate(new
				 * SimpleDateFormat(Constants.DATETIME_FORMATTER).format(date));
				 */
                ticket.setCreatedDate(dateTimeType.toString());
            }
            JAXBElement<StringType> status = retrieveContactCenterResponse.getModel().getInstance().getStatus();
            if (status != null) {
                ticket.setTicketStatus(status.getValue().getValue());
            }

            ticket.setFromService(true);
            ticket.setTicketNumber(ticketNo);
            if (retrieveContactCenterResponse.getStatus() != null && retrieveContactCenterResponse.getStatus().equals(StatusType.SUCCESS)) {
                ticket.setSuccess(Constants.SUCCESS.toUpperCase());
            } else {
                ticket.setSuccess(Constants.FALSE.toUpperCase());
            }

            
            logger.debug("Service Name:" + ticket.geteServiceName());
            logger.debug("Created Date:" + ticket.getCreatedDate());
            logger.debug("Ticket Status:" + ticket.getTicketStatus());
        }
        return ticket;
    }

    private RetrieveContactCenterRequest prepareRetrieveContactCenterRequest(String ticketNo) {

        logger.debug("Creating contact center instance - ");
        ContactCenterInstanceType contactCenterInstanceType = new ContactCenterInstanceType();
        StringType ticket = new StringType();
        ticket.setValue(ticketNo);
        contactCenterInstanceType.setCallID(ticket);

        logger.debug("Creating contact center model - ");
        ContactCenterModelType model = new ContactCenterModelType();
        model.setInstance(contactCenterInstanceType);
        model.setKeys(new ContactCenterKeysType());

        logger.debug("Creating retrieve contact center request - ");
        RetrieveContactCenterRequest retrieveContactCenterRequest = new RetrieveContactCenterRequest();
        retrieveContactCenterRequest.setModel(model);

        return retrieveContactCenterRequest;
    }
}
