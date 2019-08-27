package com.github.marschall.squeak.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.graalvm.polyglot.HostAccess.Export;

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

  public HttpServletResponse getResponse() {
    return response;
  }

  @Export
  public String getRequestMethod() {
    return this.request.getMethod();
  }

  @Export
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

  @Export
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

  @Export
  public String getRequestAddressString() {
    return this.request.getRemoteAddr();
  }

  @Export
  public Cookie[] getCookies() {
    return this.request.getCookies();
  }

  @Export
  public Entry<String, String[]>[] getRequestHeaders() {
    List<Entry<String, String[]>> result = new ArrayList<>();
    Enumeration<String> headerNames = this.request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headers = request.getHeaders(headerName);
      result.add(new SimpleImmutableEntry<>(headerName, toArray(headers)));
    }
    return (Entry<String, String[]>[]) result.toArray();
  }

  @Export
  public Entry<String, String[]>[] getRequestFields() {
    List<Entry<String, String[]>> result = new ArrayList<>();
    Enumeration<String> parameterNames = this.request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String parameterName = parameterNames.nextElement();
      String[] parameters = request.getParameterValues(parameterName);
      result.add(new SimpleImmutableEntry<>(parameterName, parameters));
    }
    return (Entry<String, String[]>[]) result.toArray();
  }
  
  @Export
  public ServletFile[] getServletFiles() throws IOException, ServletException {
    if (isMultipartFormData()) {
      Collection<Part> parts = this.request.getParts();
      ServletFile[] files = new ServletFile[parts.size()];
      int i = 0;
      for (Part part : parts) {
        String name = part.getName();
        String fileName = part.getSubmittedFileName();
        String contentType = part.getContentType();
        byte[] contents = getContents(part);
        files[i++] = new ServletFile(name, fileName, contentType, contents);
        part.delete();
      }
      return files;
    } else {
      return new ServletFile[0];
    }
  }
  
  private static byte[] getContents(Part part) throws IOException {
    long size = part.getSize();
    if (size > Integer.MAX_VALUE) {
      throw new IOException("part too large");
    }
    byte[] contents = new byte[(int) size];
    try (InputStream inputStream = part.getInputStream()) {
      int read = 0;
      while (read < size) {
        read += inputStream.read(contents, read, (int) (size - read));
      }
    }
    return contents;
  }

  private boolean isMultipartFormData() {
    return this.request.getContentType().equals("multipart/form-data");
  }

  private static String[] toArray(Enumeration<String> enumeration) {
    List<String> result = new ArrayList<>(2);
    while (enumeration.hasMoreElements()) {
      String element =  enumeration.nextElement();
      result.add(element);
    }
    return result.toArray(new String[0]);
  }

  @Export
  public String getSslSessionId() {
    // https://stackoverflow.com/questions/6269416/can-a-servlet-get-https-session-id
    return (String) this.request.getAttribute("javax.servlet.request.ssl_session_id");
  }

  @Export
  public String getRequestVersion() {
    return this.request.getProtocol();
  }

}
