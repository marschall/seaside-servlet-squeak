package com.github.marschall.squeak.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Object passed to WAServerAdaptor &gt;&gt; #process:
 */
public final class ServletNativeRequest {

  private final HttpServletRequest request;
  private final HttpServletResponse response;

  ServletNativeRequest(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  // request methods

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
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
    builder.append(requestURI);
    if (queryString != null) {
      builder.append('?').append(queryString);
    }
    return builder.toString();
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

  public String getRequestAddressString() {
    return this.request.getRemoteAddr();
  }

  public Cookie[] getCookies() {
    return this.request.getCookies();
  }

  public List<Entry<String, List<String>>> getRequestHeaders() {
    List<Entry<String, List<String>>> result = new ArrayList<>();
    Enumeration<String> headerNames = this.request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headers = request.getHeaders(headerName);
      result.add(new SimpleImmutableEntry<>(headerName, toList(headers)));
    }
    return result;
  }

  public List<Entry<String, List<String>>> getRequestFields() {
    List<Entry<String, List<String>>> result = new ArrayList<>();
    Enumeration<String> parameterNames = this.request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String parameterName = parameterNames.nextElement();
      String[] parameters = request.getParameterValues(parameterName);
      result.add(new SimpleImmutableEntry<>(parameterName, Arrays.asList(parameters)));
    }
    return result;
  }

  public List<ServletFile> getServletFiles() throws IOException, ServletException {
    if (isMultipartFormData()) {
      Collection<Part> parts = this.request.getParts();
      List<ServletFile> files = new ArrayList<>(parts.size());
      for (Part part : parts) {
        String name = part.getName();
        String fileName = part.getSubmittedFileName();
        String contentType = part.getContentType();
        byte[] contents = getContents(part);
        files.add(new ServletFile(name, fileName, contentType, contents));
        part.delete();
      }
      return files;
    } else {
      return Collections.emptyList();
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

  private static <T> List<T> toList(Enumeration<T> enumeration) {
    List<T> result = new ArrayList<>(2);
    while (enumeration.hasMoreElements()) {
      T element =  enumeration.nextElement();
      result.add(element);
    }
    return result;
  }

  public String getSslSessionId() {
    // https://stackoverflow.com/questions/6269416/can-a-servlet-get-https-session-id
    return (String) this.request.getAttribute("javax.servlet.request.ssl_session_id");
  }

  public String getRequestVersion() {
    return this.request.getProtocol();
  }

  // response methods

  public void setResponseStatus(long status /* should be int but is bug in GraalSqueak */, String message) {
    // TODO message
    // TODO GraalSqueak bug
    this.response.setStatus(Math.toIntExact(status));
  }

  public void addHeader(String key, String value) {
    this.response.addHeader(key, value);
  }

  public Cookie newCookie(String name, String value) {
    return new Cookie(name, value);
  }

  public void addCookie(Cookie cookie) {
    this.response.addCookie(cookie);
  }

  public void setResponseContentsAsString(String contents) throws IOException {
    this.response.getWriter().append(contents);
  }

  public void setResponseContentsAsByteArray(byte[] contents) throws IOException {
    this.response.getOutputStream().write(contents);
  }

}
