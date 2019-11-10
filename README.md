Seaside Servlet Bridge for Squeak
=================================

Run [Seaside](http://www.seaside.st) in a Servlet container using [GraalSqueak](https://github.com/hpi-swa/graalsqueak).

Usage
-----

Add the dependency to your project

```xml
<dependency>
  <groupId>com.github.marschall</groupId>
  <artifactId>seaside-servlet-squeak</artifactId>
  <version>0.2.0</version>
</dependency>
```

Add the servlet to your `web.xml`


```xml
<servlet>
  <servlet-name>SeasideGraalSqueakBridgeServlet</servlet-name>
  <servlet-class>com.github.marschall.seaside.servlet.squeak.SeasideGraalSqueakBridgeServlet</servlet-class>
  <init-param>
    <param-name>smalltalk.ImagePath</param-name>
    <param-value>/WEB-INF/squeak/graalsqueak-1.0.0-rc2-seaside.image</param-value>
  </init-param>
  <async-supported>false</async-supported>
</servlet>

<servlet-mapping>
  <servlet-name>SeasideGraalSqueakBridgeServlet</servlet-name>
  <url-pattern>/*</url-pattern>
</servlet-mapping>
```

* load GraalSqueak-Core
* load Seaside


```
Installer ensureRecentMetacello. 
Metacello new
 baseline:'Seaside3';
 repository: 'github://SeasideSt/Seaside:develop/repository';
 load
```

* load `Servlet-Seaside` from this repository
* stop `WAWebServerAdaptor` as it fails during startup on GraalSqueak

And run with GraalVM.

Make sure you explode WARs for Tomcat, this means having `unpackWARs="unpackWARs"` on the `<Host>` element of `server.xml`. See [Tomcat Web Application Deployment](https://tomcat.apache.org/tomcat-9.0-doc/deployer-howto.html).

Make sure you have GraalSqueak installed in GraalVM.

Have a look at [marschall/seaside-servlet-squeak-demo](https://github.com/marschall/seaside-servlet-squeak-demo) for a complete demo project.

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
  * Stack traces currently are not very useful.
  * Continuations are not supported.
  * Initial performance before warm up is not good compared to OpenSmalltalk VM.
  * Startup is not good compared to OpenSmalltalk VM.
  * Memory consumption and allocation rate are higher compared to OpenSmalltalk VM
* Asynchronous web request processing is not supported.

JMX
---

Included is a small framework to register Squeak objects as MBeans. To create a custom one:

* sublcass `WAMBean`
* override `#objectName`, check out the [ObjectName](https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/ObjectName.html) class comment for information about the syntax
* add methods with `<attribute>` and `<operation>` pragmas
* have a look at `WAAdminMBean` for an example

Tips & Tricks
-------------

Enable Graal compliation logging

    export CATALINA_OPTS="-Dgraal.TraceTruffleCompilation=true"

To make WAUrlDecodingFunctionalTest pass use, this is not needed in general

    export CATALINA_OPTS="-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"

If you have the Tomcat examples application installed (per default) this overrides the Seaside examples.

Warm up the compiler

    for run in {1..100}; do curl -L 'http://127.0.0.1:8080/tests/functional/WALotsaLinksFunctionalTest?_s=_eYKKM3Gl3XzVM5T&_k=JIp_6FO_QQ26nQob' > /dev/null; done

Include the session key to avoid session creation and follow redirects.

Running the image directly with GraalVM

    $JAVA_HOME/bin/graalsqueak --jvm --polyglot src/main/webapp/WEB-INF/squeak/graalsqueak-1.0.0-rc2-seaside.image

Building a native Tomcat image https://ci.apache.org/projects/tomcat/tomcat9/docs/graal.html

