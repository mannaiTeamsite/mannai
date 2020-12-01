/**
 * 
 */
package com.hukoomi.bo;

/**
 * @author vmohandass
 *
 */
public class PollsBO {
    private String action;
    private String pollId;
    private String lang;
    private String userId;
    private String userAgent;
    private String ipAddress;
    private String startDate;
    private String endDate;
    private String persona;
    private String votedOn;
    private String votedFrom;
    private String question;
    private String selectedOption;
    private String currentPollsPerPage;
    private String pastPollsPerPage;
    private String group;
    private String groupCategory;
    private String category;
    private String solrCategory;
    
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getPollId() {
        return pollId;
    }
    public void setPollId(String pollId) {
        this.pollId = pollId;
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
    public String getVotedOn() {
        return votedOn;
    }
    public void setVotedOn(String votedOn) {
        this.votedOn = votedOn;
    }
    public String getVotedFrom() {
        return votedFrom;
    }
    public void setVotedFrom(String votedFrom) {
        this.votedFrom = votedFrom;
    }
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public String getSelectedOption() {
        return selectedOption;
    }
    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
    public String getCurrentPollsPerPage() {
        return currentPollsPerPage;
    }
    public void setCurrentPollsPerPage(String currentPollsPerPage) {
        this.currentPollsPerPage = currentPollsPerPage;
    }
    public String getPastPollsPerPage() {
        return pastPollsPerPage;
    }
    public void setPastPollsPerPage(String pastPollsPerPage) {
        this.pastPollsPerPage = pastPollsPerPage;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
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
                + solrCategory + "]";
    }
}
