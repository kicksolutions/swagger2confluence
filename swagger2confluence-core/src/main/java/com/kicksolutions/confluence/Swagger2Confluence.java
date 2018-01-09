package com.kicksolutions.confluence;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kicksolutions.CliArgs;

/**
 * MSANTOSH
 *
 */
public class Swagger2Confluence {
	private static final Logger LOGGER = Logger.getLogger(Swagger2Confluence.class.getName());
	private static final String USAGE = new StringBuilder()
			.append(" Usage: ")
			.append(Swagger2Confluence.class.getName()).append(" <options> \n")
			.append(" -i <Spec File> ").append(" -a <Parent Page Id> ").append(" -u <User Name> ")
			.append(" -p <Password> ").append(" -l <Conflunce URL> ").append(" -s <Confluenec Space key>")
			.append(" -r <alternate URL>").append("-c <Clinet Kit URL>").append("-d <HTML Documentation URL>")
			.append(" -f <Prefix for Confluence Page>").toString();

	public Swagger2Confluence() {
		super();
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Swagger2Confluence swagger2Confluence = new Swagger2Confluence();
		swagger2Confluence.init(args);
	}

	/**
	 * 
	 * @param args
	 */
	private void init(String args[]) {
		CliArgs cliArgs = new CliArgs(args);
		String specFile = cliArgs.getArgumentValue("-i", "");
		String parentPageID = cliArgs.getArgumentValue("-a", "");
		String userName = cliArgs.getArgumentValue("-u", "");
		String password = cliArgs.getArgumentValue("-p", "");
		String confluenceURL = cliArgs.getArgumentValue("-l", "");
		String confluenceSpaceKey = cliArgs.getArgumentValue("-s", "");
		String alternateURL = cliArgs.getArgumentValue("-r", "");
		String clientkitURL = cliArgs.getArgumentValue("-c", "");
		String htmlDocumentationURL = cliArgs.getArgumentValue("-d", "");
		String prefixForConfluencePage = cliArgs.getArgumentValue("-f", "");
		
		if (StringUtils.isNotEmpty(specFile) && StringUtils.isNotEmpty(parentPageID) && StringUtils.isNotEmpty(userName)
				&& StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(confluenceURL)
				&& StringUtils.isNotEmpty(confluenceSpaceKey)) {
			try{
			processSwagger2Confluence(specFile, parentPageID, userName, password, confluenceURL, 
					confluenceSpaceKey,alternateURL,clientkitURL,htmlDocumentationURL,prefixForConfluencePage);
			}
			catch(Exception e){
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				throw new RuntimeException(e);
			}
		} else {
			LOGGER.severe(USAGE);
		}
	}

	/**
	 * Title: 
	 * | 
	 * |----- V1.0 Title
	 * | 
	 * |----- V1.1 Title
	 * 
	 * 
	 * @param specFile
	 * @param parentPageID
	 * @param userName
	 * @param password
	 * @param confluenceURL
	 * @param spaceKey
	 * @param htmlDocumentationURL 
	 * @param clientkitURL 
	 * @param prefixForConfluencePage
	 * @throws JsonProcessingException 
	 */
	private void processSwagger2Confluence(String specFile, String parentPageID, String userName, String password,
			String confluenceURL, String spaceKey,String alternateURL, String clientkitURL, String htmlDocumentationURL, 
			String prefixForConfluencePage) throws JsonProcessingException {
		SwaggerConfluenceUploader confluenceUploader = new SwaggerConfluenceUploader();
		confluenceUploader.processSwagger2Confluence(specFile, parentPageID, userName, password, confluenceURL, spaceKey, alternateURL, 
				clientkitURL, htmlDocumentationURL,prefixForConfluencePage,true);
	}
}