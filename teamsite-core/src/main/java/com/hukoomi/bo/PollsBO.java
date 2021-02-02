/**
 * 
 */
package com.hukoomi.bo;

/**
 * PollsBO is the polls business object class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class PollsBO {
    /**
     * Poll action performed by the user
     */
    private String action;
    /**
     * Poll id of the poll
     */
    private String pollId;
    /**
     * Language of the poll
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
     * Start date of the poll
     */
    private String startDate;
    /**
     * End date of the poll
     */
    private String endDate;
    /**
     * Poll persona
     */
    private String persona;
    /**
     * Poll voted on
     */
    private String votedOn;
    /**
     * Poll voted from
     */
    private String votedFrom;
    /**
     * Poll question
     */
    private String question;
    /**
     * selected poll option
     */
    private String selectedOption;
    /**
     * Current polls per page
     */
    private String currentPollsPerPage;
    /**
     * Past polls per page
     */
    private String pastPollsPerPage;
    /**
     * Poll group selcted in the homepage/dashboard component
     */
    private String group;
    /**
     * Poll group category name
     */
    private String groupCategory;
    /**
     * Poll category name
     */
    private String category;
    /**
     * Poll solr category name
     */
    private String solrCategory;
    /**
     * Service Entities
     */
    private String serviceEntities;
    /**
     * Poll Topics
     */
    private String topics;

    /**
     * Getter method to get poll action
     * 
     * @return Returns poll action
     */
    public String getAction() {
        return action;
    }

    /**
     * Setter method to set poll action
     * 
     * @param action Poll action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Getter method to get poll id
     * 
     * @return Returns poll id
     */
    public String getPollId() {
        return pollId;
    }

    /**
     * Setter method to set poll id
     * 
     * @param pollId Poll id
     */
    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    /**
     * Getter method to get poll language
     * 
     * @return Returns poll language
     */
    public String getLang() {
        return lang;
    }

    /**
     * Setter method to set poll language
     * 
     * @param lang Poll language
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Getter method to get user id
     * 
     * @return Returns poll user id
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
     * Getter method to get poll start date
     * 
     * @return Returns poll start date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Setter method to set poll start date
     * 
     * @param startDate Poll start date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter method to get poll end date
     * 
     * @return Returns poll end date
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Setter method to set poll end date
     * 
     * @param endDate Poll end date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter method to get poll persona
     * 
     * @return Returns poll persona
     */
    public String getPersona() {
        return persona;
    }

    /**
     * Setter method to set poll persona
     * 
     * @param persona Poll persona
     */
    public void setPersona(String persona) {
        this.persona = persona;
    }

    /**
     * Getter method to get poll voted on
     * 
     * @return Returns poll voted on
     */
    public String getVotedOn() {
        return votedOn;
    }

    /**
     * Setter method to set poll voted on
     * 
     * @param votedOn Poll voted on
     */
    public void setVotedOn(String votedOn) {
        this.votedOn = votedOn;
    }

    /**
     * Getter method to get poll voted from
     * 
     * @return Returns poll voted from
     */
    public String getVotedFrom() {
        return votedFrom;
    }

    /**
     * Setter method to set poll voted from
     * 
     * @param votedFrom Poll voted from
     */
    public void setVotedFrom(String votedFrom) {
        this.votedFrom = votedFrom;
    }

    /**
     * Getter method to get poll question
     * 
     * @return Returns poll question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Setter method to set poll question
     * 
     * @param question Poll question
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Getter method to get selected poll option
     * 
     * @return Returns selected poll option
     */
    public String getSelectedOption() {
        return selectedOption;
    }

    /**
     * Setter method to set selected poll option
     * 
     * @param selectedOption Selected poll option
     */
    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    /**
     * Getter method to get current polls per page
     * 
     * @return Returns current polls per page
     */
    public String getCurrentPollsPerPage() {
        return currentPollsPerPage;
    }

    /**
     * Setter method to set current polls per page
     * 
     * @param currentPollsPerPage current polls per page
     */
    public void setCurrentPollsPerPage(String currentPollsPerPage) {
        this.currentPollsPerPage = currentPollsPerPage;
    }

    /**
     * Getter method to get past polls per page
     * 
     * @return Returns past polls per page
     */
    public String getPastPollsPerPage() {
        return pastPollsPerPage;
    }

    /**
     * Setter method to set past polls per page
     * 
     * @param pastPollsPerPage Past polls per page
     */
    public void setPastPollsPerPage(String pastPollsPerPage) {
        this.pastPollsPerPage = pastPollsPerPage;
    }

    /**
     * Getter method to get poll group
     * 
     * @return Returns poll group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Setter method to set poll group
     * 
     * @param group Poll group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Getter method to get poll group category
     * 
     * @return Returns poll group category
     */
    public String getGroupCategory() {
        return groupCategory;
    }

    /**
     * Setter method to set poll group category
     * 
     * @param groupCategory Poll group category
     */
    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
    }

    /**
     * Getter method to get poll category
     * 
     * @return Returns poll category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Setter method to set poll category
     * 
     * @param category Poll category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Getter method to get poll solr category
     * 
     * @return Returns poll solr category
     */
    public String getSolrCategory() {
        return solrCategory;
    }

    /**
     * Setter method to set poll solr category
     * 
     * @param solrCategory Poll solr category
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

    /**
     * Overrides the toString method to print the business objects values
     * 
     * @return Returns string of all business objects values
     */
    @Override
    public String toString() {
        return "PollsBO [action=" + action + ", pollId=" + pollId
                + ", lang=" + lang + ", userId=" + userId + ", userAgent="
                + userAgent + ", ipAddress=" + ipAddress + ", startDate="
                + startDate + ", endDate=" + endDate + ", persona="
                + persona + ", votedOn=" + votedOn + ", votedFrom="
                + votedFrom + ", question=" + question
                + ", selectedOption=" + selectedOption
                + ", currentPollsPerPage=" + currentPollsPerPage
                + ", pastPollsPerPage=" + pastPollsPerPage + ", group="
                + group + ", groupCategory=" + groupCategory
                + ", category=" + category + ", solrCategory="
                + solrCategory + ", serviceEntities=" + serviceEntities
                + ", topics=" + topics + "]";
    }
}
