package com.github.marschall.squeak.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
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
    return getServletContext().getRealPath(location);
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
    String dispatcherPath = getDispatcherPath();
    // TODO get encoding
    this.seasideAdaptor = this.graalContext.eval(LANGUAGE, "WAServletServerAdaptor contextPath: '" + dispatcherPath + "'");
  }

  private String getDispatcherPath() throws ServletException {
    ServletContext context = getServletContext();
    String characterEncoding = context.getRequestCharacterEncoding();
    if (characterEncoding == null) {
      // TODO log warning
    }
    String servletName = this.config.getServletName();
    ServletRegistration registration = context.getServletRegistration(servletName);
    Collection<String> mappings = registration.getMappings();
    if (mappings.isEmpty()) {
      throw new ServletException("no mapping specified for servlet: " + servletName);
    }
    if (mappings.size() > 1) {
      throw new ServletException("more than one mapping specified for servlet: " + servletName);
    }
    return context.getContextPath();
  }

  private ServletContext getServletContext() {
    return this.config.getServletContext();
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
