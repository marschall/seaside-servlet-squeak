package com.github.marschall.seaside.servlet.squeak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class SeasideGraalSqueakBridgeServlet implements Servlet {

  /**
   * Java servlet specification specifies ISO-8859-1 as default.
   */
  private static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

  private static final String LANGUAGE = "smalltalk";

  private static final String IMAGE_LOCATION_PARAMETER = LANGUAGE + ".ImagePath";

  private volatile ServletConfig config;

  private volatile Context graalContext;

  private volatile Value seasideAdaptor;

  /**
   * Instance of WAMBean, {@code nil} if already registered.
   */
  private volatile Value waMBean;

  /**
   * Lock through which all access to Squeak objects happens because GraalSqueak is not thread safe.
   */
  private final Object imageLock = new Object();

  @Override
  public void init(ServletConfig config) throws ServletException {
    this.config = config;
    this.loadSqueakImage();
    this.registerServerAdaptor();
    this.registerMBean();
  }

  @Override
  public ServletConfig getServletConfig() {
    return this.config;
  }

  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    this.dispatchToSeaside((HttpServletRequest) req, (HttpServletResponse) res);
  }

  @Override
  public String getServletInfo() {
    return "Graal Squeak Bridge Servlet";
  }

  @Override
  public void destroy() {
    this.unregisterMBean();
    this.stopSqueakImage();
    this.config = null;
  }

  // Squeak methods

  private String getImageLocation() throws ServletException {
    String location = this.config.getInitParameter(IMAGE_LOCATION_PARAMETER);
    if (location == null) {
      throw new ServletException("init parameter: \"" + IMAGE_LOCATION_PARAMETER + "\" missing");
    }
    String imageLocation = this.getServletContext().getRealPath(location);
    if (!Files.exists(Paths.get(imageLocation))) {
      throw new ServletException("image path: " + imageLocation + " does not exist");
    }
    return imageLocation;
  }

  private void loadSqueakImage() throws ServletException {
    this.graalContext = Context.newBuilder()
        .option(IMAGE_LOCATION_PARAMETER, this.getImageLocation())
        .allowAllAccess(true)
        //.allowPolyglotAccess(PolyglotAccess.ALL)
        //.allowNativeAccess(true)
        //.allowEnvironmentAccess(EnvironmentAccess.INHERIT)
        //.allowHostAccess(HostAccess.ALL) // Map.Entry methods are not annotated
        //.allowIO(true)
        .build();
  }

  private void registerMBean() {
    synchronized (this.imageLock) {
      this.waMBean = this.seasideAdaptor.invokeMember("registerMBeanWithLock:", this.imageLock);
    }
  }

  private void registerServerAdaptor() throws ServletException {
    String dispatcherPath = this.getDispatcherPath();
    String encoding = this.getCharacterEncoding();
    this.seasideAdaptor = this.graalContext.eval(LANGUAGE,
        "WAServletServerAdaptor path: '" + dispatcherPath + "' encoding: '" + encoding + "'");
  }

  private String getDispatcherPath() throws ServletException {
    ServletContext context = this.getServletContext();
    String servletMapping = this.getServletMapping();
    return context.getContextPath() + servletMapping;
  }

  private String getCharacterEncoding() {
    if (this.supportsGetCharacterEncoding()) {
      return this.getCharacterEncodingFromContext();
    } else {
      ServletContext context = this.getServletContext();
      context.log("reading character encoding from context not supported, guessing. use servlet 4.0 if possible");
      return DEFAULT_CHARACTER_ENCODING;
    }
  }

  private boolean supportsGetCharacterEncoding() {
    ServletContext context = this.getServletContext();
    return context.getMajorVersion() >= 4;
  }

  private String getCharacterEncodingFromContext() {
    ServletContext context = this.getServletContext();
    String requestCharacterEncoding = context.getRequestCharacterEncoding();
    String responseCharacterEncoding = context.getResponseCharacterEncoding();
    if (requestCharacterEncoding == null && responseCharacterEncoding == null) {
      // while ISO-8859-1 is what the spec says some servers have other default
      // so we have to guess here
      context.log("no request or response encoding set, falling back to " + DEFAULT_CHARACTER_ENCODING);
      return DEFAULT_CHARACTER_ENCODING;
    }
    if (requestCharacterEncoding != null && responseCharacterEncoding != null && !requestCharacterEncoding.equals(responseCharacterEncoding)) {
      context.log("inconsistent request and response character encodings " + requestCharacterEncoding + " vs. " + responseCharacterEncoding
          + " falling back to: " + requestCharacterEncoding);
      return requestCharacterEncoding;
    }
    if (requestCharacterEncoding != null) {
      return requestCharacterEncoding;
    }
    return responseCharacterEncoding;
  }

  private String getServletMapping() throws ServletException {
    ServletContext context = this.getServletContext();
    String servletName = this.config.getServletName();
    ServletRegistration registration = context.getServletRegistration(servletName);
    Collection<String> mappings = registration.getMappings();
    if (mappings.isEmpty()) {
      context.log("no mapping specified for servlet: " + servletName);
      return "";
    }
    if (mappings.size() > 1) {
      throw new ServletException("more than one mapping specified for servlet: " + servletName);
    }
    String mapping = mappings.iterator().next();
    if (mapping.endsWith("*")) {
      return mapping.substring(0, mapping.length() - 1);
    }
    if (mapping.startsWith("*")) {
      context.log("prefix mapping mapping '" + mapping + "' not supported for servlet: " + servletName);
    }
    return mapping;
  }

  private ServletContext getServletContext() {
    return this.config.getServletContext();
  }

  private void dispatchToSeaside(HttpServletRequest request, HttpServletResponse response) {
    ServletNativeRequest nativeRequest = new ServletNativeRequest(request, response, this.seasideAdaptor);
    // GraalSqueak is not thread safe so we have to lock here
    // even though this is forbidden in Java EE
    synchronized (this.imageLock) {
      this.seasideAdaptor.invokeMember("process:", nativeRequest);
    }
  }

  private void unregisterMBean() {
    synchronized (this.imageLock) {
      if (this.waMBean != null && !this.waMBean.isNull()) {
        this.waMBean.invokeMember("unregister");
      }
      this.waMBean = null;
    }
  }

  private void stopSqueakImage() {
    synchronized (this.imageLock) {
      this.graalContext.close();
    }
    this.graalContext = null;
    this.waMBean = null;
    this.seasideAdaptor = null;
  }

}
