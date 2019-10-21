package com.github.marschall.seaside.servlet.squeak;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.graalvm.polyglot.Value;

/**
 * Object passed to {@code WAServerAdaptor >> #process:}.
 * Can easily be converted to a {@code WARequest} and from a
 * {@code WAResponse}.
 */
public final class ServletNativeRequest {

  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final Value seasideAdaptor;

  ServletNativeRequest(HttpServletRequest request, HttpServletResponse response, Value seasideAdaptor) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(response, "response");
    Objects.requireNonNull(seasideAdaptor, "seasideAdaptor");
    this.request = request;
    this.response = response;
    this.seasideAdaptor = seasideAdaptor;
  }

  // request methods

  public HttpServletRequest getRequest() {
    return this.request;
  }

  public HttpServletResponse getResponse() {
    return this.response;
  }

  public String getRequestMethod() {
    return this.request.getMethod();
  }

  public String getQueryStringRaw() {
    // not decoded
    StringBuilder builder = new StringBuilder();

    String scheme = this.request.getScheme();
    String serverName = this.request.getServerName();
    int portNumber = this.request.getServerPort();
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
    if (this.isFormUrlencoded()) {
      // if the body is URL encoded and we read it here fully
      // the parameters in the body will be missing from getParameterNames()
      // which is called in getRequestFields() after this method
      return null;
    }
    if (this.isMultipartFormData()) {
      // if the body is form data and we read it here fully
      // the parameters in the body will be missing from getServletFiles()
      return null;
    }
    if (this.getRequestMethod().equals("GET")) {
      // avoid buffer creating on GET
      // TODO generalize
      return null;
    }
    StringBuilder builder = new StringBuilder();
    // TODO read request size
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

  private boolean isFormUrlencoded() {
    return Objects.equals(this.request.getContentType(), "application/x-www-form-urlencoded");
  }

  public String getRequestAddressString() {
    return this.request.getRemoteAddr();
  }

  public List<Cookie> getCookies() {
    Cookie[] cookies = this.request.getCookies();
    if (cookies == null) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(cookies);
    }
  }

  public List<Entry<String, List<String>>> getRequestHeaders() {
    List<Entry<String, List<String>>> result = new ArrayList<>();
    Enumeration<String> headerNames = this.request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headers = this.request.getHeaders(headerName);
      result.add(new SimpleImmutableEntry<>(headerName, toList(headers)));
    }
    return result;
  }

  public List<Entry<String, List<String>>> getRequestFields() {
    if (this.request.getMethod().equals("GET")) {
      // URL parameters are returned for a GET, Seaside doesn't expect this
      return Collections.emptyList();
    }

    List<Entry<String, List<String>>> result = new ArrayList<>();
    Enumeration<String> parameterNames = this.request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String parameterName = parameterNames.nextElement();
      String[] parameters = this.request.getParameterValues(parameterName);
      List<String> parameterList;
      if (parameters.length== 0) {
        parameterList = Collections.emptyList();
      } else if (parameters.length == 1) {
        parameterList = Collections.singletonList(parameters[0]);
      } else {
        parameterList = Arrays.asList(parameters);
      }
      result.add(new SimpleImmutableEntry<>(parameterName, parameterList));
    }
    return result;
  }

  public List<FormPart> getFormParts() throws IOException, ServletException {
    if (!this.isMultipartFormData()) {
      return Collections.emptyList();
    }

    Collection<Part> parts = this.request.getParts();
    List<FormPart> formParts = new ArrayList<>(parts.size());
    for (Part part : parts) {
      String name = part.getName();
      String fileName = part.getSubmittedFileName();
      FormPart formPart;
      if (fileName != null) {
        // only if there is a file name it's a file
        // if it is a normal multi part form field
        // it is returned in getRequestFields
        String contentType = part.getContentType();
        Value contents = this.getContentsAsValue(part);
        formPart = new FilePart(name, fileName, contentType, contents);
        formParts.add(formPart);
      }
      part.delete();
    }
    return formParts;
  }

  /**
   * Create a Squeak ByteArray from a {@link Part}. Avoid creating a Java
   * byte[] and copying to a ByteArray.
   *
   * This method has to be invoked from behind the Graal context lock.
   *
   * @param part the part to convert
   * @return the Squeak ByteArray
   */
  private Value getContentsAsValue(Part part) {
    return this.seasideAdaptor.invokeMember("partAsByteArray:", part);
  }

  private boolean isMultipartFormData() {
    String contentType = this.request.getContentType();
    if (contentType == null) {
      return false;
    }
    return contentType.startsWith("multipart/form-data");
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

  public void setResponseStatus(int status, String message) {
    this.response.setStatus(status);
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

  public void setResponseContentsAsByteArray(Value contents) throws IOException {
    int arraySize = Math.toIntExact(contents.getArraySize());
    byte[] value = new byte[arraySize];
    for (int i = 0; i < arraySize; i++) {
      value[i] = (byte) contents.getArrayElement(i).asInt();
    }
    this.response.getOutputStream().write(value);
  }

}
