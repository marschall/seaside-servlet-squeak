Squeak Seaside Servlet Bridge
=============================

Run Seaside in a Servlet container using [GraalSqueak](https://github.com/hpi-swa/graalsqueak).


Usage
-----


```xml
  <servlet>
    <servlet-name>GraalSqueakBridgeServlet</servlet-name>
    <servlet-class>com.github.marschall.squeak.servlet.GraalSqueakBridgeServlet</servlet-class>
    <init-param>
      <param-name>squeak.image.location</param-name>
      <param-value>/WEB-INF/squeak/graalsqueak-0.8.4-seaside.image</param-value>
    </init-param>
    <async-supported>false</async-supported>
  </servlet>

  <servlet-mapping>
    <servlet-name>GraalSqueakBridgeServlet</servlet-name>
    <url-pattern>/*</url-pattern>
  </servlet-mapping>
```


Make sure you explode WARs for Tomcat this means having `unpackWARs="unpackWARs"` on the `<Host>` element of `server.xml`. See [Tomcat Web Application Deployment](https://tomcat.apache.org/tomcat-9.0-doc/deployer-howto.html).

Limitations
-----------

As GraalSqueak is currently not thread safe this is limited to one thread at a time. This is especially damning in case of blocking IO like database access.

