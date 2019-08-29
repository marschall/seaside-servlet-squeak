package com.github.marschall.squeak.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * Loads a Squeak image and dispatches all requests to Seaside in the image.
 */
public class GraalSqueakBridgeServlet implements Servlet {

  private static final String IMAGE_LOCATION_PARAMETER = "squeak.image.location";

  private static final String LANGUAGE = "squeaksmalltalk";

  private volatile ServletConfig config;

  private volatile Context graalContext;

  private volatile Value seasideAdaptor;
  
  private final Object imageLock = new Object();

  @Override
  public void init(ServletConfig config) throws ServletException {
    this.config = config;
    this.loadSqueakImage();
  }

  @Override
  public ServletConfig getServletConfig() {
    return this.config;
  }

  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    dispatchToSeaside((HttpServletRequest) req, (HttpServletResponse) res);
  }

  @Override
  public String getServletInfo() {
    return "Graal Squeak Bridge Servlet";
  }

  @Override
  public void destroy() {
    this.stopSqueakImage();
    this.config = null;
  }

  // Squeak methods
  
  private String getImageLocation() throws ServletException {
    String location = this.config.getInitParameter(IMAGE_LOCATION_PARAMETER);
    if (location == null) {
      throw new ServletException("init parameter: \"" + IMAGE_LOCATION_PARAMETER + "\" missing");
    }
    return this.config.getServletContext().getRealPath(location);
  }

  protected void loadSqueakImage() throws ServletException {
    this.graalContext = Context.newBuilder(LANGUAGE)
        .option(LANGUAGE + ".ImagePath", this.getImageLocation())
        .allowAllAccess(true)
//        .allowNativeAccess(true)
//        .allowEnvironmentAccess(EnvironmentAccess.INHERIT)
//        .allowHostAccess(HostAccess.ALL) // Map.Entry methods are not annotated
//        .allowIO(true)
        .build();
    String contextPath = this.config.getServletContext().getContextPath();
    this.seasideAdaptor = this.graalContext.eval(LANGUAGE, "WAServletServerAdaptor instance");
  }

  protected void dispatchToSeaside(HttpServletRequest request, HttpServletResponse response) {
    ServletNativeRequest nativeRequest = new ServletNativeRequest(request, response);
    // GraalSqueak is not thread safe so we have to lock here
    // even though this is forbidden in Java EE
    synchronized (this.imageLock) {
      this.seasideAdaptor.invokeMember("process:", nativeRequest);
    }
  }

  protected void stopSqueakImage() {
    this.graalContext.close();
    this.graalContext = null;
  }

}
