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
    <param-name>smalltalk.ImagePath</param-name>
    <param-value>/WEB-INF/squeak/graalsqueak-1.0.0-rc2-seaside.image</param-value>
  </init-param>
  <async-supported>false</async-supported>
</servlet>

<servlet-mapping>
  <servlet-name>GraalSqueakBridgeServlet</servlet-name>
  <url-pattern>/*</url-pattern>
</servlet-mapping>
```

* load `Servlet-Seaside` from this repository
* stop `WAWebServerAdaptor` as it fails during startup on GraalSqueak

And run with Graal VM.

Make sure you explode WARs for Tomcat, this means having `unpackWARs="unpackWARs"` on the `<Host>` element of `server.xml`. See [Tomcat Web Application Deployment](https://tomcat.apache.org/tomcat-9.0-doc/deployer-howto.html).

Make sure you have GraalSqueak installed in GraalVM.

Have a look at [marschall/squeak-servlet-demo](https://github.com/marschall/squeak-servlet-demo) for a complete demo project.

Requirements
------------

 * GraalVM 19.2.0.1
 * GraalSqueak
  * `gu install -u https://github.com/hpi-swa/graalsqueak/releases/download/1.0.0-rc2/graalsqueak-component-1.0.0-rc2-for-GraalVM-19.2.0.1.jar`
 * Servlet 3.1, but Servlet 4.0 is recommended

Limitations
-----------

* Limitations inherited from GraalSqueak
  * As GraalSqueak is currently not thread safe we are limited to one concurrent request. This is especially damning in case of blocking IO like database access.
  * Continuations are not supported.
  * High memory consumption and allocation rate compared to OpenSmalltalk VM
  * stack traces currently are not very useful
* Initial performance is not good compared to OpenSmalltalk VM.
* Asynchronous web request processing is not supported.

Tips & Tricks
-------------

Enable Graal compliation logging

    export CATALINA_OPTS="-Dgraal.TraceTruffleCompilation=true"

Warm up the compiler

    for run in {1..10}; do curl -L 'http://127.0.0.1:8080/tests/functional/WALotsaLinksFunctionalTest?_s=_eYKKM3Gl3XzVM5T&_k=JIp_6FO_QQ26nQob' > /dev/null; done

Include the session key to avoid session creation and follow redirects.

Running the image directly with GraalVM

    $JAVA_HOME/bin/graalsqueak --jvm --polyglot src/main/webapp/WEB-INF/squeak/graalsqueak-1.0.0-rc2-seaside.image

