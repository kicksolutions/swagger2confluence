package com.kicksolutions.swagger.confluence;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.kicksolutions.swagger.confluence.vo.ConfluenceVo;
import com.kicksolutions.swagger.plantuml.cliargs.CliArgs;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

/**
 * MSANTOSH
 *
 */
public class Swagger2Confluence {
	private static final Logger LOGGER = Logger.getLogger(Swagger2Confluence.class.getName());
	private static final String USAGE = new StringBuilder()
			.append(" Usage: com.kicksolutions.swagger.confluence.Swagger2Confluence <options> \n")
			.append(" -i <Spec File> ").append(" -a <Parent Page Id> ").append(" -u <User Name> ")
			.append(" -p <Password> ").append(" -l <Conflunce URL> ").append(" -s <Confluenec Space key>")
			.append(" -r <alternate URL>").append("-c <Clinet Kit URL>").append("-d <HTML Documentation URL>").toString();

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

		if (StringUtils.isNotEmpty(specFile) && StringUtils.isNotEmpty(parentPageID) && StringUtils.isNotEmpty(userName)
				&& StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(confluenceURL)
				&& StringUtils.isNotEmpty(confluenceSpaceKey)) {
			processSwagger2Confluence(specFile, parentPageID, userName, password, confluenceURL, 
					confluenceSpaceKey,alternateURL,clientkitURL,htmlDocumentationURL);
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
	 */
	private void processSwagger2Confluence(String specFile, String parentPageID, String userName, String password,
			String confluenceURL, String spaceKey,String alternateURL, String clientkitURL, String htmlDocumentationURL) {
		Swagger swaggerObject = new SwaggerParser().read(specFile);
		
		if(swaggerObject!=null){
						
			String version = swaggerObject.getInfo().getVersion();
			String title = swaggerObject.getInfo().getTitle();
			
			String parentTitle = new StringBuilder().append(title).toString();
			String versionTitle = new StringBuilder().append("V").append(version).append("-").append(title).toString();
			String swaggerPageContent =  StringUtils.isNotEmpty(alternateURL) ? swaggerMacroContent(alternateURL,clientkitURL,htmlDocumentationURL) : swaggerMacroContent(specFile,clientkitURL,htmlDocumentationURL);
			
			if (StringUtils.isNotEmpty(version) && StringUtils.isNotEmpty(title)) {
				// Create a Page whose name is same as Swagger Title Ex: Pet Store
				LOGGER.log(Level.INFO, "About to generate Page -->" + title);
				
				ConfluenceVo parentPageVo = createSwaggerPage(new ConfluenceVo(userName, password, confluenceURL, "",
						parentPageID, "", parentTitle, "0", parentPageContent(versionTitle), spaceKey, false));
				
				LOGGER.log(Level.INFO, "About to generate Page --> " + versionTitle);
				
				// Create Child Page Under Parent ex: V1.0.0 Pet Store
				ConfluenceVo childPageVo =  createSwaggerPage(new ConfluenceVo(userName, password, confluenceURL, "", parentPageVo.getPageID(),
						specFile, versionTitle, "0", swaggerPageContent, spaceKey, false));
				
				LOGGER.log(Level.INFO, "Done.... by generating Pages "+ parentPageVo.getPageID() +" and "+ childPageVo.getPageID());
			}
			else{
				throw new RuntimeException("Swagger Definition is missing version and title information");
			}
		}	
		else{
			throw new RuntimeException("Cannot Process Swagger Definition for the given URL");
		}
	}
	
	/**
	 * 
	 * @param versionTitle
	 * @return
	 */
	private String parentPageContent(String versionTitle) {
		return new StringBuilder().append("<table><tbody><tr><th>Latest Version</th><th>").append(versionTitle)
				.append("</th></tr></tbody></table>").toString();
	}

	/**
	 * 
	 * @param swaggerLoctaion
	 * @param htmlDocumentationURL 
	 * @param clientkitURL 
	 * @return
	 */
	private String swaggerMacroContent(String swaggerLoctaion, String clientkitURL, String htmlDocumentationURL) {
		
		StringBuilder macroString = new StringBuilder();
		macroString.append("<ac:structured-macro ac:name=\"multiexcerpt\" ac:schema-version=\"1\" ac:macro-id=\"cc925abe-8df0-4f3b-933c-3b88e4e3daec\">")
		.append("<ac:parameter ac:name=\"MultiExcerptName\">pub_api_operation</ac:parameter>")
		.append("<ac:parameter ac:name=\"atlassian-macro-output-type\">INLINE</ac:parameter>")
		.append("<ac:rich-text-body>")
		.append("<table class=\"relative-table wrapped\" style=\"width: 46.4912%;\">")
		.append("<colgroup>")
		.append("<col style=\"width: 38.3376%;\" />")
		.append("<col style=\"width: 61.408%;\" />")
		.append("</colgroup>")
		.append("<tbody><tr>")
		.append("<th>Description</th><th>Links</th>")
		.append("</tr>");
		
		if(StringUtils.isNotEmpty(clientkitURL)){
			macroString.append("<tr>")
			.append("<td colspan=\"1\">")
			.append("<strong>Clientkit</strong>")
			.append("</td>")
			.append("<td colspan=\"1\">")
			.append("<a href=\"")
			.append(clientkitURL)
			.append("\">")
			.append("here")
			.append("</a>")
			.append("</td>")
			.append("</tr>");
		}
		
		if(StringUtils.isNotEmpty(htmlDocumentationURL)){
			macroString.append("<tr>")
			.append("<td colspan=\"1\">")
			.append("<strong>Html Documentation</strong>")
			.append("</td>")
			.append("<td colspan=\"1\">")
			.append("<a href=\"")
			.append(htmlDocumentationURL)
			.append("\">")
			.append("here")
			.append("</a>")
			.append("</td>")
			.append("</tr>");
		}
		
		if(StringUtils.isNotEmpty(swaggerLoctaion)){
			macroString.append("<tr>")
			.append("<td colspan=\"1\">")
			.append("<strong>Swagger</strong>")
			.append("</td>")
			.append("<td colspan=\"1\">")
			.append("<a href=\"")
			.append(swaggerLoctaion)
			.append("\">")
			.append("here")
			.append("</a>")
			.append("</td>")
			.append("</tr>");
		}
		
		macroString.append("</tbody></table>")
		.append("<ac:structured-macro ac:name=\"open-api\" ac:schema-version=\"1\" ac:macro-id=\"86cdf71a-5e3a-4a30-833a-70548a238b4d\">")
		.append("<ac:parameter ac:name=\"validatorUrl\">None</ac:parameter>")
		.append("<ac:parameter ac:name=\"url\">")
		.append(swaggerLoctaion)
		.append("</ac:parameter></ac:structured-macro></ac:rich-text-body></ac:structured-macro>");
		
		return macroString.toString();
	}
	

	/**
	 * 
	 * @param title
	 * @return
	 */
	private ConfluenceVo createSwaggerPage(ConfluenceVo confluenceVo) {
		ConfluenceUtils confluenceUtils = new ConfluenceUtils();

		if (!confluenceUtils.isPageExists(confluenceVo)) {
			LOGGER.log(Level.INFO, "Page Doesn't Exists, so Creating Page");
			String pageID = confluenceUtils.createPage(confluenceVo);
			confluenceVo.setPageID(pageID);
		} else {
			LOGGER.log(Level.INFO, "Page Exists, so Updating Page");
			String pageID = confluenceUtils.updatePage(confluenceVo);
			confluenceVo.setPageID(pageID);
		}

		return confluenceVo;
	}
}