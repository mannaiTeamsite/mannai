package com.hukoomi.bo;

import java.sql.Date;

/**
 * @author pnetiyil
 * 
 * This object is used to sync user info from Sql to Postgre
 *
 */
public class PhpListUserBO {
    
    
    /**
     * user id in phplist
     */
    private int id;   
    
    /**
     * user email in phplist
     */
    private String email;
    
    /**
     * user confirmed config in phplist
     */
    private int confirmed;
    
    /**
     * user blacklisted config in phplist
     */
    private int blacklisted;
    
    /**
     * user opeted in id in phplist
     */
    private int optedin;
    
    /**
     * user bouncecount number in phplist
     */
    private int bouncecount;
    
    /**
     * user created date in phplist
     */
    private Date entered;
    
    /**
     * user modified date in phplist
     */
    private Date modified;
    
    /**
     * user uniqid in phplist
     */
    private String uniqid;
    
    /**
     * user uuid in phplist
     */
    private String uuid;
    
    /**
     * user htmlemail id in phplist
     */
    private int htmlemail;
    
    /**
     * user subscribepage id in phplist
     */
    private int subscribepage;
    
    /**
     * user rssfrequency in phplist
     */
    private String rssfrequency;
    
    /**
     * user password in phplist
     */
    private String password;
    
    /**
     * user passwordchanged date in phplist
     */
    private Date passwordchanged;
    
    /**
     * user disabled id in phplist
     */
    private int disabled;
    
    /**
     * user extradata in phplist
     */
    private String extradata;
    
    /**
     * user foreignkey in phplist
     */
    private String foreignkey;
    
    /**
     * variables for list user - start 
     */
    
    
    /**
     * user id in phplist list table
     */
    private int userid;
    
    /**
     * list id in phplist list table
     */
    private int listid;
    
    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getListid() {
        return listid;
    }

    public void setListid(int listid) {
        this.listid = listid;
    }
    
    
    /** 
     * variables for list user - end 
     */
    
    /**
     * @return user id in phplist
     * Getter method for datasync
     */
    public int getId() {
        return id;
    }

    /**
     * Setter method for datasync
     * sets the user id in phplist
     * @param id
     *      */
    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public int getBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(int blacklisted) {
        this.blacklisted = blacklisted;
    }

    public int getOptedin() {
        return optedin;
    }

    public void setOptedin(int optedin) {
        this.optedin = optedin;
    }

    public int getBouncecount() {
        return bouncecount;
    }

    public void setBouncecount(int bouncecount) {
        this.bouncecount = bouncecount;
    }

    public Date getEntered() {
        return entered;
    }

    public void setEntered(Date entered) {
        this.entered = entered;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getUniqid() {
        return uniqid;
    }

    public void setUniqid(String uniqid) {
        this.uniqid = uniqid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getHtmlemail() {
        return htmlemail;
    }

    public void setHtmlemail(int htmlemail) {
        this.htmlemail = htmlemail;
    }

    public int getSubscribepage() {
        return subscribepage;
    }

    public void setSubscribepage(int subscribepage) {
        this.subscribepage = subscribepage;
    }

    public String getRssfrequency() {
        return rssfrequency;
    }

    public void setRssfrequency(String rssfrequency) {
        this.rssfrequency = rssfrequency;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getPasswordchanged() {
        return passwordchanged;
    }

    public void setPasswordchanged(Date passwordchanged) {
        this.passwordchanged = passwordchanged;
    }

    public int getDisabled() {
        return disabled;
    }

    public void setDisabled(int disabled) {
        this.disabled = disabled;
    }

    public String getExtradata() {
        return extradata;
    }

    public void setExtradata(String extradata) {
        this.extradata = extradata;
    }

    public String getForeignkey() {
        return foreignkey;
    }

    public void setForeignkey(String foreignkey) {
        this.foreignkey = foreignkey;
    }      

}
