package com.hukoomi.bo;

/**
 * DashboardSettingsBO is the dashboard settings business object class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class DashboardSettingsBO {
    
    /**
     * Dashboard settings action performed by the user
     */
    private String action;
    /**
     * Logged-in UserId
     */
    private String userId;
    /**
     * User role
     */
    private String userRole;
    /**
     * Persona for the logged-in user
     */
    private String persona;
    
    /**
     * Getter method to get action
     * 
     * @return Returns action
     */
    public String getAction() {
        return action;
    }
    /**
     * Setter method to set action
     * 
     * @param action action for the dashboard settings
     */
    public void setAction(String action) {
        this.action = action;
    }
    /**
     * Getter method to get user id
     * 
     * @return Returns user id
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Setter method to set user id
     * 
     * @param userId user id of the logged-in user
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /**
     * Getter method to get user role
     * 
     * @return Returns user role
     */
    public String getUserRole() {
        return userRole;
    }
    /**
     * Setter method to set user role
     * 
     * @param userRole role of the logged-in user
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    /**
     * Getter method to get persona
     * 
     * @return Returns persona
     */
    public String getPersona() {
        return persona;
    }
    /**
     * Setter method to set persona
     * 
     * @param persona persona of the user
     */
    public void setPersona(String persona) {
        this.persona = persona;
    }
    
    /**
     * Overrides the toString method to print the business objects values
     * 
     * @return Returns string of all business objects values
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DashboardSettingsBO [action=").append(action)
                .append(", userId=").append(userId).append(", userRole=")
                .append(userRole).append(", persona=").append(persona)
                .append("]");
        return builder.toString();
    }

}
