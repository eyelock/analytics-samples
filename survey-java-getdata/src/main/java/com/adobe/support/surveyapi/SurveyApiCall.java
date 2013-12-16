package com.adobe.support.surveyapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurveyApiCall implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(SurveyApiCall.class);
	
	private static String ARGS_PREFIX = " *** ";
	
	private String url;
	private String user;
	private String secret;
	private String method;
	private SurveyCallArgs callArgs;
	
	private boolean isValid = true;

	public SurveyApiCall(PropertiesHelper propertiesHelper) {
		super();
		this.url = propertiesHelper.getSurveyApiUrl();
		this.user = propertiesHelper.getSurveyApiUser();
		this.secret = propertiesHelper.getSurveyApiSecret();
		this.method = propertiesHelper.getSurveyApiMethod();
		
		this.callArgs = new SurveyCallArgs();
		this.callArgs.setReportSuiteId(propertiesHelper.getReportSuiteId());
		this.callArgs.setSurveyId(propertiesHelper.getSurveyId());
		this.callArgs.setStartDate(propertiesHelper.getStartDate());
		this.callArgs.setEndDate(propertiesHelper.getEndDate());
		
		logger.info(ARGS_PREFIX + "URL: {}", this.url);
		logger.info(ARGS_PREFIX + "User: {}", this.user);
		logger.info(ARGS_PREFIX + "Secret: [Nae chance]");
		logger.info(ARGS_PREFIX + "Method: {}", this.method);
	}
	
	private void validateArgs() {
		isValid = true;
		
		if (url == null || url.length() ==0) {
			logger.error("the url parameter is required");
			isValid = false;
		}
		
		if (user == null || user.length() ==0) {
			logger.error("the user parameter is required");
			isValid = false;
		}
		
		if (secret == null || secret.length() ==0) {
			logger.error("the secret parameter is required");
			isValid = false;
		}
		
		if (method == null || method.length() ==0) {
			logger.error("the method parameter is required");
			isValid = false;
		}
			
		if (callArgs.getReportSuiteId() == null || callArgs.getReportSuiteId().length() == 0) {
			logger.error("the reportsuite parameter is required");
			isValid = false;
		}
		
		if (callArgs.getSurveyId() == null || callArgs.getSurveyId().length() == 0) {
			logger.error("the surveyid parameter is required");
			isValid = false;
		}
	}
	
	@Override
	public void run() {
		validateArgs();
		
		if (!isValid) {
			logger.error("INVALID ARGS: See error messages before");
		} else {
			try {
				getData();
			} catch (IOException e) {
				logger.error("An Exception occurred running the test: {}", e.getMessage());
			}
		}
	}
	
	public void getData() throws IOException {
		String response = callMethod(this);
		
		logger.info(ARGS_PREFIX + "RSID: {}", this.callArgs.getReportSuiteId());
		logger.info(ARGS_PREFIX + "Survey Id: {}", this.callArgs.getSurveyId());
		logger.info(ARGS_PREFIX + "Start Date: {}", this.callArgs.getStartDate());
		logger.info(ARGS_PREFIX + "End Date: {}", this.callArgs.getEndDate());
		
		if (response == null) {
			logger.error("the HTTP call returned nothing, something is definitely amiss");
			return;
		} 
		
		JSONArray jsonArray = JSONArray.fromObject(response);	
		@SuppressWarnings("unchecked")
		Collection<JSONObject> items = JSONArray.toCollection(jsonArray);
		
		logger.info("Length: {}", items.size());

		if (logger.isDebugEnabled()) {
			int count = 0;
			for (Object object : items) {
				count++;
				JSONObject jsonObject = JSONObject.fromObject(object);
				logger.debug(count + ": " + jsonObject.toString());
			}
		} else {
			logger.info("Set to debug level to see output of response");
		}
	}
	
	
	public String getUrl() {
		return url;
	}


	public String getUser() {
		return user;
	}


	public String getSecret() {
		return secret;
	}


	public String getMethod() {
		return method;
	}
	

	public SurveyCallArgs getCallArgs() {
		return callArgs;
	}
	
	public static String callMethod(SurveyApiCall call) throws IOException {
		String response = null;
		
		URL url = new URL(call.getUrl() + "?method=" + call.getMethod());
		URLConnection connection = url.openConnection();
		connection.addRequestProperty("X-WSSE", getHeader(call.getUser(), call.getSecret()));
		
		connection.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		logger.debug("call args: {}", call.getCallArgs().toJson());
		wr.write(call.getCallArgs().toJson());
		wr.flush();
		
		InputStream in = connection.getInputStream();
		
		
		HttpURLConnection sslConnection = (HttpURLConnection) connection;
		int statusCode = sslConnection.getResponseCode();
		
		if (statusCode != HttpsURLConnection.HTTP_OK) {
			logger.error("error calling url: {} - {}", statusCode, sslConnection.getResponseMessage());
		} else {
			BufferedReader res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			StringBuffer sBuffer = new StringBuffer();
			String inputLine;
			while ((inputLine = res.readLine()) != null)
				sBuffer.append(inputLine);
			
			res.close();
			
			response = sBuffer.toString();
		}
		
		return response;
	}
	
	private static String getHeader(String username, String password) throws UnsupportedEncodingException {
		byte[] nonceB = generateNonce();
		String nonce = base64Encode(nonceB);
		String created = generateTimestamp();
		String password64 = getBase64Digest(nonceB, created.getBytes("UTF-8"), password.getBytes("UTF-8"));
		StringBuffer header = new StringBuffer("UsernameToken Username=\"");
		header.append(username);
		header.append("\", ");
		header.append("PasswordDigest=\"");
		header.append(password64.trim());
		header.append("\", ");
		header.append("Nonce=\"");
		header.append(nonce.trim());
		header.append("\", ");
		header.append("Created=\"");
		header.append(created);
		header.append("\"");
		return header.toString();
	}
	
	private static byte[] generateNonce() {
	    String nonce = Long.toString(new Date().getTime());
	    return nonce.getBytes();
	}
	
	private static String generateTimestamp() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return dateFormatter.format(new Date());
	}

	private static synchronized String getBase64Digest(byte[] nonce, byte[] created, byte[] password) {
	  try {
	    MessageDigest messageDigester = MessageDigest.getInstance("SHA-1");
	    // SHA-1 ( nonce + created + password )
	    messageDigester.reset();
	    messageDigester.update(nonce);
	    messageDigester.update(created);
	    messageDigester.update(password);
	    return base64Encode(messageDigester.digest());
	  } catch (java.security.NoSuchAlgorithmException e) {
	    throw new RuntimeException(e);
	  }
	}
	
	private static String base64Encode(byte[] bytes) {
		 return Base64Coder.encodeLines(bytes);
	}
}


class SurveyCallArgs {	
	private String rsid;
	private String survey_id;
	private String start;
	private String end;
	
	public SurveyCallArgs() {
		super();
	}

	public String getReportSuiteId() {
		return rsid;
	}
	public void setReportSuiteId(String reportSuiteId) {
		this.rsid = reportSuiteId;
	}
	public String getSurveyId() {
		return survey_id;
	}
	public void setSurveyId(String surveyId) {
		this.survey_id = surveyId;
	}
	public String getStartDate() {
		return start;
	}
	public void setStartDate(String startDate) {
		this.start = startDate;
	}
	public String getEndDate() {
		return end;
	}
	public void setEndDate(String endDate) {
		this.end = endDate;
	}
	
	public String toJson() {	
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (end != null && end.length() > 0) 
			map.put("end", getEndDate());
		
		if (rsid != null && rsid.length() > 0) 
			map.put("rsid", getReportSuiteId());
		
		if (start != null && start.length() > 0) 
			map.put("start", getStartDate());
		
		if (survey_id != null && survey_id.length() > 0) 
			map.put("survey_id", getSurveyId());
		
		return JSONObject.fromObject(map).toString();
	}
}

