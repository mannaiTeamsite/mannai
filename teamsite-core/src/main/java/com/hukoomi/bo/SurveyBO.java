package com.hukoomi.bo;

/**
 * SurveyBO is the survey business object class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class SurveyBO {
    /**
     * Survey action performed by the user
     */
    private String action;
    /**
     * Survey id of the survey
     */
    private String surveyId;
    /**
     * Language of the survey
     */
    private String lang;
    /**
     * Logged-in UserId
     */
    private String userId;
    /**
     * User Agent information
     */
    private String userAgent;
    /**
     * IPAddress of the user
     */
    private String ipAddress;
    /**
     * Start date of the survey
     */
    private String startDate;
    /**
     * End date of the survey
     */
    private String endDate;
    /**
     * Survey persona
     */
    private String persona;
    /**
     * Survey taken on
     */
    private String takenOn;
    /**
     * Survey taken from
     */
    private String takenFrom;
    /**
     * Survey total questions
     */
    private String totalQuestions;
    /**
     * Survey google re-captcha response
     */
    private String captchaResponse;
    /**
     * Survey group selcted in the homepage/dashboard component
     */
    private String group;
    /**
     * Survey title
     */
    private String title;
    /**
     * Survey description
     */
    private String description;
    /**
     * Survey question id
     */
    private Long questionId;
    /**
     * Survey question number
     */
    private int questionNo;
    /**
     * Survey group category name
     */
    private String groupCategory;
    /**
     * Survey category name
     */
    private String category;
    /**
     * Survey solr category name
     */
    private String solrCategory;
    /**
     * Service Entities
     */
    private String serviceEntities;
    /**
     * Survey Topics
     */
    private String topics;
    /**
     * Submit Type
     */
    private String submitType;

    /**
     * Getter method to get survey action
     * 
     * @return @return Returns survey action
     */
    public String getAction() {
        return action;
    }

    /**
     * Setter method to set survey action
     * 
     * @param action Survey action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Getter method to get survey id
     * 
     * @return Returns survey id
     */
    public String getSurveyId() {
        return surveyId;
    }

    /**
     * Setter method to set survey id
     * 
     * @param surveyId Survey id
     */
    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    /**
     * Getter method to get survey language
     * 
     * @return Returns survey language
     */
    public String getLang() {
        return lang;
    }

    /**
     * Setter method to set survey language
     * 
     * @param lang Survey language
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Getter method to get user id
     * 
     * @return Returns survey user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter method to set user id
     * 
     * @param userId User id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter method to get user agent
     * 
     * @return Returns user agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Setter method to set user agent
     * 
     * @param userAgent User agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Getter method to get ip address
     * 
     * @return Returns ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Setter method to set ip address
     * 
     * @param ipAddress Returns ip address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Getter method to get survey start date
     * 
     * @return Returns survey start date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Setter method to set survey start date
     * 
     * @param startDate Survey start date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter method to get survey end date
     * 
     * @return Returns survey end date
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Setter method to set survey end date
     * 
     * @param endDate Survey end date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter method to get survey persona
     * 
     * @return Returns survey persona
     */
    public String getPersona() {
        return persona;
    }

    /**
     * Setter method to set survey persona
     * 
     * @param persona Survey persona
     */
    public void setPersona(String persona) {
        this.persona = persona;
    }

    /**
     * Getter method to get survey taken on
     * 
     * @return Returns survey taken on
     */
    public String getTakenOn() {
        return takenOn;
    }

    /**
     * Setter method to set survey taken on
     * 
     * @param takenOn Survey taken on
     */
    public void setTakenOn(String takenOn) {
        this.takenOn = takenOn;
    }

    /**
     * Getter method to get survey taken from
     * 
     * @return Returns survey taken from
     */
    public String getTakenFrom() {
        return takenFrom;
    }

    /**
     * Setter method to set survey taken from
     * 
     * @param takenFrom Survey taken from
     */
    public void setTakenFrom(String takenFrom) {
        this.takenFrom = takenFrom;
    }

    /**
     * Getter method to get survey total questions
     * 
     * @return Returns survey total questions
     */
    public String getTotalQuestions() {
        return totalQuestions;
    }

    /**
     * Setter method to set survey total questions
     * 
     * @param totalQuestions Survey total questions
     */
    public void setTotalQuestions(String totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    /**
     * Getter method to get google re-captcha response
     * 
     * @return Returns Google re-captcha response
     */
    public String getCaptchaResponse() {
        return captchaResponse;
    }

    /**
     * Setter method to set google re-captcha response
     * 
     * @param captchaResponse Google re-captcha response
     */
    public void setCaptchaResponse(String captchaResponse) {
        this.captchaResponse = captchaResponse;
    }

    /**
     * Getter method to get survey group
     * 
     * @return Returns survey group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Setter method to set survey group
     * 
     * @param group Survey group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Getter method to get survey title
     * 
     * @return Returns survey title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter method to set survey title
     * 
     * @param title Survey title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter method to get survey description
     * 
     * @return Returns survey description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter method to set survey description
     * 
     * @param description Survey description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter method to get survey question id
     * 
     * @return Returns survey question id
     */
    public Long getQuestionId() {
        return questionId;
    }

    /**
     * Setter method to set survey question id
     * 
     * @param questionId Survey question id
     */
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /**
     * Getter method to get survey question number
     * 
     * @return Returns survey question number
     */
    public int getQuestionNo() {
        return questionNo;
    }

    /**
     * Setter method to set survey question number
     * 
     * @param questionNo Survey question number
     */
    public void setQuestionNo(int questionNo) {
        this.questionNo = questionNo;
    }

    /**
     * Getter method to get survey group category
     * 
     * @return Returns survey group category
     */
    public String getGroupCategory() {
        return groupCategory;
    }

    /**
     * Setter method to set survey group category
     * 
     * @param groupCategory Survey group category
     */
    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
    }

    /**
     * Getter method to get survey category
     * 
     * @return Returns survey category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Setter method to set survey category
     * 
     * @param category Survey category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Getter method to get survey solr category
     * 
     * @return Returns survey solr category
     */
    public String getSolrCategory() {
        return solrCategory;
    }

    /**
     * Setter method to set survey solr category
     * 
     * @param solrCategory Survey solr category
     */
    public void setSolrCategory(String solrCategory) {
        this.solrCategory = solrCategory;
    }

    public String getServiceEntities() {
        return serviceEntities;
    }

    public void setServiceEntities(String serviceEntities) {
        this.serviceEntities = serviceEntities;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public String getSubmitType() {
        return submitType;
    }

    public void setSubmitType(String submitType) {
        this.submitType = submitType;
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
                + ", solrCategory=" + solrCategory + ", serviceEntities="
                + serviceEntities + ", topics=" + topics + ", submitType="
                + submitType + "]";
    }
}
