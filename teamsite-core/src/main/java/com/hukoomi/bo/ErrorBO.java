package com.hukoomi.bo;

public class ErrorBO {
	 /**
     * Error id of the error
     */
    private String errorId;
    /**
     * Language of the error
     */
    private String lang;
    /**
     * last updated date of the error
     */
    private String statusCode;
    /**
     * error errorNameTechnical
     */
    private String errorNameTechnical;
    /**
     * Error title
     */
    private String title;
    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	/**
     * Error description
     */
    private String message;
    
    
	public String getErrorId() {
		return errorId;
	}
	
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getErrorNameTechnical() {
		return errorNameTechnical;
	}
	public void setErrorNameTechnical(String errorNameTechnical) {
		this.errorNameTechnical = errorNameTechnical;
	}
	@Override
	public String toString() {
		return "ErrorBO [errorId=" + errorId + ", lang=" + lang + ", statusCode=" + statusCode + ", errorNameTechnical="
				+ errorNameTechnical + ", title=" + title + ", message=" + message + "]";
	}

}
