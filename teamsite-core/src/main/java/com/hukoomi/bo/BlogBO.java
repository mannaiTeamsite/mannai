package com.hukoomi.bo;

public class BlogBO {

    /**
     * Blog id of the Blog
     */
    private String blogId;
    /**
     * Language of the blog
     */
    private String lang;
    /**
     * last updated date of the blog
     */
    private String updatedDate;
    /**
     * blog persona
     */
    private String persona;
    /**
     * Survey google re-captcha response
     */
    private String captchaResponse;
    /**
     * Blog title
     */
    private String title;
    /**
     * Blog description
     */
    private String description;


    /**
     * Getter method to get survey id
     *
     * @return Returns survey id
     */
    public String getBlogId() {
        return blogId;
    }

    /**
     * Setter method to set survey id
     *
     * @param surveyId Survey id
     */
    public void setBlogId(String blogId) {
        this.blogId = blogId;
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
     * Getter method to get survey end date
     *
     * @return Returns survey end date
     */
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Setter method to set survey end date
     *
     * @param endDate Survey end date
     */
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
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
     * Overrides the toString method to print the business objects values
     *
     * @return Returns string of all business objects values
     */
    @Override
    public String toString() {
        return "BlogBO [ blogId=" + blogId
                + ", lang=" + lang + ",  endDate=" + updatedDate + ", persona="
                + persona + ",  title=" + title + ", description="
                + description +  "]";
    }
}
