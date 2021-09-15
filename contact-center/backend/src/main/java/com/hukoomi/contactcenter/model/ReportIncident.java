package com.hukoomi.contactcenter.model;

import java.util.ArrayList;

public class ReportIncident {
    private String idType;
    private Long qid;
    private Long eid;
    private String passport;
    private String nationality;
    private String fullName;
    private String companyName;
    private Integer phoneNo;
    private String emailId;
    private int eServices;
    private int services;
    private String eServiceName;
    private String serviceName;
    private String subject;
    private String comments;
    private String comments2;
    private String verification;
    private String recaptchaChallengeField;
    private String recaptchaResponseField;
    private Attachment attachment;
    private String success;
    private String serviceMessage;
    private String ticketNumber;
    private String createdDate;
    private String ticketStatus;
    private String appNoValue;
    private ArrayList<String> validationMessage;
    private String errorMessage;
    private String user;
    private boolean fromService;

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public Long getQid() {
        return qid;
    }

    public void setQid(Long qid) {
        this.qid = qid;
    }

    public Long getEid() {
        return eid;
    }

    public void setEid(Long eid) {
        this.eid = eid;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(Integer phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public int geteServices() {
        return eServices;
    }

    public void seteServices(int eServices) {
        this.eServices = eServices;
    }

    public int getServices() {
        return services;
    }

    public void setServices(int services) {
        this.services = services;
    }

    public String geteServiceName() {
        return eServiceName;
    }

    public void seteServiceName(String eServiceName) {
        this.eServiceName = eServiceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComments2() {
        return comments2;
    }

    public void setComments2(String comments2) {
        this.comments2 = comments2;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getRecaptchaChallengeField() {
        return recaptchaChallengeField;
    }

    public void setRecaptchaChallengeField(String recaptchaChallengeField) {
        this.recaptchaChallengeField = recaptchaChallengeField;
    }

    public String getRecaptchaResponseField() {
        return recaptchaResponseField;
    }

    public void setRecaptchaResponseField(String recaptchaResponseField) {
        this.recaptchaResponseField = recaptchaResponseField;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getServiceMessage() {
        return serviceMessage;
    }

    public void setServiceMessage(String serviceMessage) {
        this.serviceMessage = serviceMessage;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getAppNoValue() {
        return appNoValue;
    }

    public void setAppNoValue(String appNoValue) {
        this.appNoValue = appNoValue;
    }

    public ArrayList<String> getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(ArrayList<String> validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isFromService() {
        return fromService;
    }

    public void setFromService(boolean fromService) {
        this.fromService = fromService;
    }
}
