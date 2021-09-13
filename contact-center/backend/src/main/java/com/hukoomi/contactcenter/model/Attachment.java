package com.hukoomi.contactcenter.model;

import java.io.Serializable;


/**
 * Common attachment class for all services.
 * 
 *
 */
public class Attachment implements Serializable {
	
	private static final long serialVersionUID = 3452036683685652832L;
	/* Name of the file when it attached */
	private String fileName;
	/* Size of the file as string in KB*/
	private String fileSize;
	/* File content */
	private volatile byte[] content;
	private String fileType;
	
	/* 
	 * Path where the file will be saved, the path is full path with file name, 
	 * where file name will be changed to be sure its unique, the fileName field
	 * will still be the same and not be effected   
	 */
	private String filePath;
	
	/**
	 * This id will be used to compare file instances (usually this id will be generated from the Backend)
	 */
	private int id;
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Default constructor
	 */
	public Attachment() {
		
	}
	
	/**
	 * Constructor initialize all <code>Attachment</code> fields
	 * 
	 * @param content	File content
	 * @param fileName	Name of the file when it attached
	 * @param filePath	Full file path including file name
	 * @param fileSize	Size of the file as string in KB
	 */
	public Attachment(byte[] content, String fileName, String filePath,
                      String fileSize) {
		super();
		this.content = content;
		this.fileName = fileName;
		this.filePath = filePath;
		this.fileSize = fileSize;
	}
	
	/**
	 * Constructor initialize all <code>Attachment</code> fields
	 * 
	 * @param id file id (Usually generated at beackend)
	 * @param content	File content
	 * @param fileName	Name of the file when it attached
	 * @param filePath	Full file path including file name
	 * @param fileSize	Size of the file as string in KB
	 */
	public Attachment(int id, byte[] content, String fileName, String filePath,
                      String fileSize) {
		super();
		this.id = id;
		this.content = content;
		this.fileName = fileName;
		this.filePath = filePath;
		this.fileSize = fileSize;
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return the fileSize
	 */
	public String getFileSize() {
		return fileSize;
	}
	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}
	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileType() {
		return fileType;
	}

	@Override
	public String toString() {
        final String TAB = "    ";

        StringBuffer retValue = new StringBuffer();
        retValue.append("Attachment ( ");
        retValue.append(super.toString());
        retValue.append(TAB);

        retValue.append("fileName = ");
        retValue.append(this.fileName);
        retValue.append(TAB);

        retValue.append("fileSize = ");
        retValue.append(this.fileSize);
        retValue.append(TAB);

        retValue.append("filePath = ");
        retValue.append(this.filePath);
        retValue.append(TAB);

        retValue.append(")");

		return retValue.toString();
	}
	
}
