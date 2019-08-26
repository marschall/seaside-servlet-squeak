package com.github.marschall.squeak.servlet;

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

  public HttpServletResponse getResponse() {
    return response;
  }

}
