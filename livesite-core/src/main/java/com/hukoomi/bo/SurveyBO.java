package com.hukoomi.bo;

public class SurveyBO {
    private String action;
    private String surveyId;
    private String lang;
    private String userId;
    private String userAgent;
    private String ipAddress;
    private String startDate;
    private String endDate;
    private String persona;
    private String takenOn;
    private String takenFrom;
    private String totalQuestions;
    private String captchaResponse;
    private String group;
    private String title;
    private String description;
    private Long questionId;
    private int questionNo;
    private String groupCategory;
    private String category;
    private String solrCategory;
    
    public String getCaptchaResponse() {
        return captchaResponse;
    }
    public void setCaptchaResponse(String captchaResponse) {
        this.captchaResponse = captchaResponse;
    }
    public String getTotalQuestions() {
        return totalQuestions;
    }
    public void setTotalQuestions(String totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getSurveyId() {
        return surveyId;
    }
    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }
    public String getLang() {
        return lang;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getPersona() {
        return persona;
    }
    public void setPersona(String persona) {
        this.persona = persona;
    }
    public String getTakenOn() {
        return takenOn;
    }
    public void setTakenOn(String takenOn) {
        this.takenOn = takenOn;
    }
    public String getTakenFrom() {
        return takenFrom;
    }
    public void setTakenFrom(String takenFrom) {
        this.takenFrom = takenFrom;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
	public Long getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	public int getQuestionNo() {
		return questionNo;
	}
	public void setQuestionNo(int questionNo) {
		this.questionNo = questionNo;
	}
	public String getGroupCategory() {
        return groupCategory;
    }
    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getSolrCategory() {
        return solrCategory;
    }
    public void setSolrCategory(String solrCategory) {
        this.solrCategory = solrCategory;
    }
    @Override
    public String toString() {
        return "SurveyBO [action=" + action + ", surveyId=" + surveyId
                + ", lang=" + lang + ", userId=" + userId + ", userAgent="
                + userAgent + ", ipAddress=" + ipAddress + ", startDate="
                + startDate + ", endDate=" + endDate + ", persona="
                + persona + ", takenOn=" + takenOn + ", takenFrom="
                + takenFrom + ", totalQuestions=" + totalQuestions
                + ", captchaResponse=" + captchaResponse + ", group="
                + group + ", title=" + title + ", description="
                + description + ", questionId=" + questionId
                + ", questionNo=" + questionNo + ", groupCategory="
                + groupCategory + ", category=" + category
                + ", solrCategory=" + solrCategory + "]";
    }
}
