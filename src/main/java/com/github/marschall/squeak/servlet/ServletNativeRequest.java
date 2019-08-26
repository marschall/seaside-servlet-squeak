package com.github.marschall.squeak.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Object passed to WAServerAdaptor >> #process:
 */
public final class ServletNativeRequest {

  private final HttpServletRequest request;
  private final HttpServletResponse response;

  ServletNativeRequest(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public String getRequestMethod() {
    return this.request.getMethod();
  }

  public String getQueryStringRaw() {
    // not decoded
    StringBuilder builder = new StringBuilder();

    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int portNumber = request.getServerPort();
    String requestURI = this.request.getRequestURI();
    String queryString = this.request.getQueryString();

    builder.append(scheme).append("://");
    builder.append(serverName);
    builder.append(':').append(portNumber);
    builder.append('/').append(requestURI);
    if (queryString != null) {
      builder.append('?').append(queryString);
    }
    return requestURI + queryString;
  }
  
  public String getRequestBodyAsString() throws IOException {
    StringBuilder builder = new StringBuilder();
    char[] buffer = new char[8192];
    try (BufferedReader reader = this.request.getReader()) {
      // TODO Java 10 #transferTo
      int read = reader.read(buffer);
      while (read != -1) {
        builder.append(buffer, 0, read);
        read = reader.read(buffer);
      }
    }
    return builder.toString();
  }

  public String getSslSessionId() {
    // https://stackoverflow.com/questions/6269416/can-a-servlet-get-https-session-id
    return (String) this.request.getAttribute("javax.servlet.request.ssl_session_id");
  }

  public String getRequestVersion() {
    return this.request.getProtocol();
  }

  public HttpServletResponse getResponse() {
    return response;
  }

}
