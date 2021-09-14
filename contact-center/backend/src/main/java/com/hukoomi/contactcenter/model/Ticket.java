package com.hukoomi.contactcenter.model;

public class Ticket {
    private String qid;
    private String eid;
    private String eServiceName;
    private String ticketStatus;
    private String createdDate;
    private String success;
    private boolean fromService;
    private String ticketNumber;

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String geteServiceName() {
        return eServiceName;
    }

    public void seteServiceName(String eServiceName) {
        this.eServiceName = eServiceName;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public boolean isFromService() {
        return fromService;
    }

    public void setFromService(boolean fromService) {
        this.fromService = fromService;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
}
