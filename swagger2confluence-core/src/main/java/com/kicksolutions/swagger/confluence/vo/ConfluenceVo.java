package com.kicksolutions.swagger.confluence.vo;

/**
 * 
 * @author MSANTOSH
 *
 */
public class ConfluenceVo {

	private String userName;
	private String password;
	private String confluenceURL;
	private String parentPageID;
	private String specFile;
	private String title;
	private String version;
	private String content;
	private String spaceKey;
	private boolean isExistingPage;
	private String pageID;
	
	/**
	 * 
	 */
	public ConfluenceVo() {
		super();
	}

	/**
	 * @param userName
	 * @param password
	 * @param confluenceURL
	 * @param parentPageID
	 * @param specFile
	 * @param title
	 * @param version
	 */
	public ConfluenceVo(String userName, String password, String confluenceURL, String pageID,String parentPageID, String specFile,
			String title, String version,String content,String spaceKey,boolean isExistingPage) {
		super();
		this.userName = userName;
		this.password = password;
		this.confluenceURL = confluenceURL;
		this.parentPageID = parentPageID;
		this.specFile = specFile;
		this.title = title;
		this.version = version;
		this.content = content;
		this.spaceKey = spaceKey;
		this.isExistingPage = isExistingPage;
		this.pageID = pageID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfluenceURL() {
		return confluenceURL;
	}

	public void setConfluenceURL(String confluenceURL) {
		this.confluenceURL = confluenceURL;
	}

	public String getParentPageID() {
		return parentPageID;
	}

	public void setParentPageID(String parentPageID) {
		this.parentPageID = parentPageID;
	}

	public String getSpecFile() {
		return specFile;
	}

	public void setSpecFile(String specFile) {
		this.specFile = specFile;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSpaceKey() {
		return spaceKey;
	}

	public void setSpaceKey(String spaceKey) {
		this.spaceKey = spaceKey;
	}

	public boolean isExistingPage() {
		return isExistingPage;
	}

	public void setExistingPage(boolean isExistingPage) {
		this.isExistingPage = isExistingPage;
	}

	public String getPageID() {
		return pageID;
	}

	public void setPageID(String pageID) {
		this.pageID = pageID;
	}	
}