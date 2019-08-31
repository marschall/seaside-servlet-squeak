Squeak Seaside Servlet Bridge
=============================

Run [Seaside](http://www.seaside.st) in a Servlet container using [GraalSqueak](https://github.com/hpi-swa/graalsqueak).


Usage
-----

Add the dependency to your project

```xml
<dependency>
  <groupId>com.github.marschall</groupId>
  <artifactId>squeak-servlet</artifactId>
  <version>0.1.0</version>
</dependency>
```

Add the servlet to your `web.xml`


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

And run with Graal VM.

Make sure you explode WARs for Tomcat, this means having `unpackWARs="unpackWARs"` on the `<Host>` element of `server.xml`. See [Tomcat Web Application Deployment](https://tomcat.apache.org/tomcat-9.0-doc/deployer-howto.html).

Make sure you have GraalSqueak installed in GraalVM.

Requirements
------------

 * GraalVM 19.0.0
 * Servlet 3.1

Limitations
-----------

* Limitations inherited from GraalSqueak
** As GraalSqueak is currently not thread safe we are limited to one concurrent request. This is especially damning in case of blocking IO like database access.
** Continuations are not supported.
** High memory consumption and allocation rate compared to OpenSmalltalk VM
* Initial performance is not good compared to GraalSqueak. At this point it is unclear how fast it would ultimately be once the optimizations stabilize.

Tips & Tricks
-------------

Enable Graal compliation logging

    export CATALINA_OPTS="-Dgraal.TraceTruffleCompilation=true"

