package com.kicksolutions.confluence;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kicksolutions.confluence.vo.ConfluenceVo;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Yaml;

/**
 * MSANTOSH
 *
 */
public class SwaggerConfluenceUploader {
	private static final Logger LOGGER = Logger.getLogger(SwaggerConfluenceUploader.class.getName());

	public SwaggerConfluenceUploader() {
		super();
	}

	/**
	 * Title: | |----- V1.0 Title | |----- V1.1 Title
	 * 
	 * @param specFile
	 * @param parentPageID
	 * @param userName
	 * @param password
	 * @param confluenceURL
	 * @param spaceKey
	 * @param alternateURL
	 * @param clientkitURL
	 * @param htmlDocumentationURL
	 * @param prefixForConfluencePage
	 * @param isHierarchy
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String processSwagger2Confluence(String specFile, String parentPageID, String userName, String password,
			String confluenceURL, String spaceKey, String alternateURL, String clientkitURL,
			String htmlDocumentationURL, String prefixForConfluencePage, boolean isHierarchy) throws JsonProcessingException {
		Swagger swaggerObject = new SwaggerParser().read(specFile);

		if (swaggerObject != null) {
			
			String swaggerContent = Yaml.mapper().writeValueAsString(swaggerObject);
			
			String version = swaggerObject.getInfo().getVersion();
			String title = swaggerObject.getInfo().getTitle();

			if (StringUtils.isNotEmpty(version) && StringUtils.isNotEmpty(title)) {
				String parentTitle = StringUtils.isEmpty(prefixForConfluencePage)
						? new StringBuilder().append(title).toString()
						: new StringBuilder(prefixForConfluencePage).append(" - ").append(title).toString();
				String versionTitle = new StringBuilder().append("V").append(version).append(" - ").append(parentTitle)
						.toString();
				
				if (isHierarchy) {
					
					return generatePagesinHierarchyMode(specFile, parentPageID, userName, password, confluenceURL,
							spaceKey, alternateURL, clientkitURL, htmlDocumentationURL, parentTitle, versionTitle,swaggerContent);
				}else{
					return generatePagesInNonHierarchyMode(specFile, parentPageID, userName, password, confluenceURL,
							spaceKey, alternateURL, clientkitURL, htmlDocumentationURL, parentTitle, versionTitle,swaggerContent);
				}
			} else {
				throw new RuntimeException("Swagger Definition is missing version and title information");
			}
		} else {
			throw new RuntimeException("Cannot Process Swagger Definition for the given URL");
		}
	}

	/**
	 * 
	 * @param specFile
	 * @param parentPageID
	 * @param userName
	 * @param password
	 * @param confluenceURL
	 * @param spaceKey
	 * @param alternateURL
	 * @param clientkitURL
	 * @param htmlDocumentationURL
	 * @param parentTitle
	 * @param versionTitle
	 * @return
	 */
	private String generatePagesInNonHierarchyMode(String specFile, String parentPageID, String userName,
			String password, String confluenceURL, String spaceKey, String alternateURL, String clientkitURL,
			String htmlDocumentationURL, String parentTitle, String versionTitle,String swaggerContent) {
		LOGGER.log(Level.INFO, "Hierarchy Mode is Set to False!!!");
		
		LOGGER.log(Level.INFO, "About to generate Page --> " + parentTitle);
		
		String swaggerPageContent = StringUtils.isNotEmpty(alternateURL)
				? swaggerMacroContent(alternateURL, clientkitURL, htmlDocumentationURL,swaggerContent)
				: swaggerMacroContent(specFile, clientkitURL, htmlDocumentationURL,swaggerContent);
		
		ConfluenceVo childPageVo = createSwaggerPage(
				new ConfluenceVo(userName, password, confluenceURL, "", parentPageID, specFile,
						versionTitle, "0", swaggerPageContent, spaceKey, false));
		
		LOGGER.log(Level.INFO, "Done.... by generating Page " + childPageVo.getPageID());

		return childPageVo.getPageID();
	}

	/**
	 * 
	 * @param specFile
	 * @param parentPageID
	 * @param userName
	 * @param password
	 * @param confluenceURL
	 * @param spaceKey
	 * @param alternateURL
	 * @param clientkitURL
	 * @param htmlDocumentationURL
	 * @param parentTitle
	 * @param versionTitle
	 * @return
	 */
	private String generatePagesinHierarchyMode(String specFile, String parentPageID, String userName, String password,
			String confluenceURL, String spaceKey, String alternateURL, String clientkitURL,
			String htmlDocumentationURL, String parentTitle, String versionTitle,String swaggerContent) {
		LOGGER.log(Level.INFO, "Hierarchy Mode is Set to True!!!");
		
		String swaggerPageContent = StringUtils.isNotEmpty(alternateURL)
				? swaggerMacroContent(alternateURL, clientkitURL, htmlDocumentationURL,swaggerContent)
				: swaggerMacroContent(specFile, clientkitURL, htmlDocumentationURL,swaggerContent);

		// Create a Page whose name is same as Swagger Title Ex: Pet
		// Store
		LOGGER.log(Level.INFO, "About to generate Page -->" + parentTitle);

		ConfluenceVo parentPageVo = createSwaggerPage(new ConfluenceVo(userName, password, confluenceURL,
				"", parentPageID, "", parentTitle, "0", parentPageContent(versionTitle), spaceKey, false));

		LOGGER.log(Level.INFO, "About to generate Page --> " + versionTitle);

		// Create Child Page Under Parent ex: V1.0.0 Pet Store
		ConfluenceVo childPageVo = createSwaggerPage(
				new ConfluenceVo(userName, password, confluenceURL, "", parentPageVo.getPageID(), specFile,
						versionTitle, "0", swaggerPageContent, spaceKey, false));

		LOGGER.log(Level.INFO, "Done.... by generating Pages " + parentPageVo.getPageID() + " and "
				+ childPageVo.getPageID());

		return childPageVo.getPageID();
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
	private String swaggerMacroContent(String swaggerLoctaion, String clientkitURL, String htmlDocumentationURL,String swaggerContent) {

		StringBuilder macroString = new StringBuilder();
		macroString
				.append("<ac:structured-macro ac:name=\"multiexcerpt\" ac:schema-version=\"1\" ac:macro-id=\"cc925abe-8df0-4f3b-933c-3b88e4e3daec\">")
				.append("<ac:parameter ac:name=\"MultiExcerptName\">pub_api_operation</ac:parameter>")
				.append("<ac:parameter ac:name=\"atlassian-macro-output-type\">INLINE</ac:parameter>")
				.append("<ac:rich-text-body>")
				.append("<table class=\"relative-table wrapped\" style=\"width: 46.4912%;\">").append("<colgroup>")
				.append("<col style=\"width: 38.3376%;\" />").append("<col style=\"width: 61.408%;\" />")
				.append("</colgroup>").append("<tbody><tr>").append("<th>Description</th><th>Links</th>")
				.append("</tr>");

		if (StringUtils.isNotEmpty(clientkitURL)) {
			macroString.append("<tr>").append("<td colspan=\"1\">").append("<strong>Java Client</strong>")
					.append("</td>").append("<td colspan=\"1\">").append("<a href=\"").append(clientkitURL)
					.append("\">").append("here").append("</a>").append("</td>").append("</tr>");
		}

		if (StringUtils.isNotEmpty(htmlDocumentationURL)) {
			macroString.append("<tr>").append("<td colspan=\"1\">").append("<strong>Html Documentation</strong>")
					.append("</td>").append("<td colspan=\"1\">").append("<a href=\"").append(htmlDocumentationURL)
					.append("\">").append("here").append("</a>").append("</td>").append("</tr>");
		}

		if (StringUtils.isNotEmpty(swaggerLoctaion)) {
			macroString.append("<tr>").append("<td colspan=\"1\">").append("<strong>Swagger</strong>").append("</td>")
					.append("<td colspan=\"1\">").append("<a href=\"").append(swaggerLoctaion).append("\">")
					.append("here").append("</a>").append("</td>").append("</tr>");
		}

		macroString.append("</tbody></table>")
				.append("<ac:structured-macro ac:name=\"open-api\" ac:schema-version=\"1\" ac:macro-id=\"86cdf71a-5e3a-4a30-833a-70548a238b4d\">")
				.append("<ac:parameter ac:name=\"validatorUrl\">None</ac:parameter>")
				.append("<ac:plain-text-body><![CDATA[").append(swaggerContent)
				.append("]]></ac:plain-text-body></ac:structured-macro></ac:rich-text-body></ac:structured-macro>");

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