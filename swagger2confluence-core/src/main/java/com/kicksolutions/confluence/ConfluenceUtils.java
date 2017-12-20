/**
 * 
 */
package com.kicksolutions.confluence;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.kicksolutions.confluence.vo.ConfluenceVo;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author MSANTOSH
 *
 */
public class ConfluenceUtils {

	private static final String EXPAND = "expand";
	private static final String ID = "id";
	private static final String SPACE_KEY = "spaceKey";
	private static final String TITLE = "title";

	/**
	 * 
	 */
	public ConfluenceUtils() {
		super();
	}

	@SuppressWarnings("unchecked")
	public boolean isPageExists(ConfluenceVo confluenceVo) {
		String authenticationString = getAuthenticationString(confluenceVo.getUserName(), confluenceVo.getPassword());
		final HttpHeaders httpHeaders = buildHttpHeaders(authenticationString);
		final HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

		final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceVo.getConfluenceURL()).path("/content")
				.queryParam(SPACE_KEY, confluenceVo.getSpaceKey()).queryParam(TITLE, confluenceVo.getTitle())
				.queryParam(EXPAND, "body.storage,version,ancestors").build().toUri();

		final ResponseEntity<String> responseEntity = new RestTemplate().exchange(targetUrl, HttpMethod.GET,
				requestEntity, String.class);

		final String jsonBody = responseEntity.getBody();

		try {
			final String id = JsonPath.read(jsonBody, "$.results[0].id");
			final Integer version = JsonPath.read(jsonBody, "$.results[0].version.number");

			final JSONArray ancestors = JsonPath.read(jsonBody, "$.results[0].ancestors");

			if (!ancestors.isEmpty()) {
				final Map<String, Object> lastAncestor = (Map<String, Object>) ancestors.get(ancestors.size() - 1);
				confluenceVo.setParentPageID((String) lastAncestor.get(ID));
			}

			confluenceVo.setPageID(id);
			confluenceVo.setVersion(String.valueOf(version));
			confluenceVo.setExistingPage(true);

			return true;

		} catch (final PathNotFoundException e) {
			confluenceVo.setExistingPage(false);
		}

		return false;
	}

	/**
	 * 
	 * @param confluenceVo
	 * @return
	 */
	public String createPage(ConfluenceVo confluenceVo) {
		final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceVo.getConfluenceURL()).path("/content")
				.build().toUri();

		final HttpHeaders httpHeaders = buildHttpHeaders(
				getAuthenticationString(confluenceVo.getUserName(), confluenceVo.getPassword()));
		final String jsonPostBody = buildPostBody(confluenceVo.getParentPageID(), confluenceVo.getTitle(),
				confluenceVo.getContent(), confluenceVo.getSpaceKey()).toJSONString();

		final HttpEntity<String> requestEntity = new HttpEntity<>(jsonPostBody, httpHeaders);
		final HttpEntity<String> responseEntity = new RestTemplate().exchange(targetUrl, HttpMethod.POST, requestEntity,
				String.class);

		final String responseJson = responseEntity.getBody();
		final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

		try {
			final JSONObject response = jsonParser.parse(responseJson, JSONObject.class);
			return (String) response.get(ID);
		} catch (net.minidev.json.parser.ParseException e) {
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * 
	 * @param confluenceVo
	 * @return
	 */
	public String updatePage(ConfluenceVo confluenceVo) {

		final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceVo.getConfluenceURL())
				.path(String.format("/content/%s", confluenceVo.getPageID())).build().toUri();

		final HttpHeaders httpHeaders = buildHttpHeaders(getAuthenticationString(confluenceVo.getUserName(), confluenceVo.getPassword()));

		final JSONObject postVersionObject = new JSONObject();
		postVersionObject.put("number", Integer.parseInt(confluenceVo.getVersion())+1);
				
		final JSONObject postBody = buildPostBody(confluenceVo.getParentPageID(), confluenceVo.getTitle(), confluenceVo.getContent(),confluenceVo.getSpaceKey());
		postBody.put(ID, confluenceVo.getPageID());
		postBody.put("version", postVersionObject);

		final HttpEntity<String> requestEntity = new HttpEntity<>(postBody.toJSONString(), httpHeaders);

		final HttpEntity<String> responseEntity = new RestTemplate().exchange(targetUrl, HttpMethod.PUT, requestEntity,String.class);

		final String responseJson = responseEntity.getBody();
		final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

		try {
			final JSONObject response = jsonParser.parse(responseJson, JSONObject.class);
			return (String) response.get(ID);
		} catch (net.minidev.json.parser.ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param ancestorId
	 * @param confluenceTitle
	 * @param content
	 * @param spacekey
	 * @return
	 */
	private JSONObject buildPostBody(final String ancestorId, final String confluenceTitle, final String content,
			String spacekey) {

		final JSONObject jsonSpaceObject = new JSONObject();
		jsonSpaceObject.put("key", spacekey);

		final JSONObject jsonStorageObject = new JSONObject();
		jsonStorageObject.put("value", content);
		jsonStorageObject.put("representation", "storage");

		final JSONObject jsonBodyObject = new JSONObject();
		jsonBodyObject.put("storage", jsonStorageObject);

		final JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "page");
		jsonObject.put(TITLE, confluenceTitle);
		jsonObject.put("space", jsonSpaceObject);
		jsonObject.put("body", jsonBodyObject);

		if (ancestorId != null) {
			final JSONObject ancestor = new JSONObject();
			ancestor.put("type", "page");
			ancestor.put(ID, ancestorId);

			final JSONArray ancestors = new JSONArray();
			ancestors.add(ancestor);

			jsonObject.put("ancestors", ancestors);
		}

		return jsonObject;
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	private String getAuthenticationString(String userName, String password) {
		return DatatypeConverter.printBase64Binary(
				new StringBuilder().append(userName).append(":").append(password).toString().getBytes());
	}

	private HttpHeaders buildHttpHeaders(final String confluenceAuthentication) {
		final HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", String.format("Basic %s", confluenceAuthentication));
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		return headers;
	}
}