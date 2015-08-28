package uk.co.force.documenter.common;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Convenience class for managing calls to the Salesforce REST API.
 * 
 * @author tsellers
 *
 */
public class SFRestApi {
	
	/**
	 * Returns the authentication response from a SF login. 
	 * Executes the Password OAuth flow. 
	 * @param username 
	 * @param password
	 * @param client 
	 * @param instance - the SF login environment you want to authenticate to (e.g. https://login.salesforce.com)
	 * @return - The JSONObject returned from this call
	 * @throws Exception - nothing is caught here.
	 */
	public JSONObject login(String username, String password, CloseableHttpClient client, String instance, String clientId, String clientSecret) throws Exception {

		CloseableHttpResponse response = null;
		JSONObject jobj = null;

		String tokenUrl = instance + "/services/oauth2/token";

		RequestBuilder builder = RequestBuilder.create(HttpPost.METHOD_NAME);
		builder.addParameter("grant_type", "password");
		builder.addParameter("client_id", clientId);
		builder.addParameter("client_secret", clientSecret);
		builder.addParameter("username", username);
		builder.addParameter("password", password);
		builder.setUri(tokenUrl);

		response = client.execute(builder.build());

		HttpEntity entity = response.getEntity();
		jobj = new JSONObject(new JSONTokener(entity.getContent()));
		EntityUtils.consume(entity);

		return jobj;
	}
	
	/**
	 * Perform a GET request
	 * @param url - the complete URL (e.g https://na1.salesforce.com/services/v34.0/sobjects)
	 * @param client
	 * @param authResponse - the JSONObject that has been returned from login. Should contain an access token
	 * @return - The JSONObject returned from this call
	 * @throws Exception - nothing is caught here.
	 */
	public JSONObject get(String url, CloseableHttpClient client, JSONObject authResponse) throws Exception{
		
		RequestBuilder builder = RequestBuilder.create(HttpGet.METHOD_NAME);
		builder.addHeader("Authorization", "Bearer "+authResponse.getString("access_token"));
		builder.setUri(url);
		
		CloseableHttpResponse response = client.execute(builder.build());
		HttpEntity entity = response.getEntity();

		System.out.println(IOUtils.toString(entity.getContent()));
	
		JSONObject jobj = new JSONObject(new JSONTokener(entity.getContent()));
		EntityUtils.consume(entity);
		
		return jobj;
	}
	
	/**
	 * Perform a POST request
	 * @param url - the complete URL (e.g https://na1.salesforce.com/services/v34.0/sobjects)
	 * @param client
	 * @param authResponse
	 * @param bodyString
	 * @return
	 * @throws Exception - nothing is caught here.
	 */
	public JSONObject post(String url, CloseableHttpClient client, JSONObject authResponse, String bodyString) throws Exception {
		
		RequestBuilder builder = RequestBuilder.create(HttpPost.METHOD_NAME);
		builder.addHeader("Authorization", "Bearer "+authResponse.getString("access_token"));
		builder.setUri(url);
		
		HttpEntity body = new StringEntity(bodyString, "UTF-8");
		builder.setEntity(body);
		
		CloseableHttpResponse response = client.execute(builder.build());
		HttpEntity entity = response.getEntity();
		JSONObject jobj = new JSONObject(new JSONTokener(entity.getContent()));
		EntityUtils.consume(entity);
		
		return jobj;
		
	}
}
