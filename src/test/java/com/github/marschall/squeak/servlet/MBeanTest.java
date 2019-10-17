package com.github.marschall.squeak.servlet;

import java.io.IOException;
import java.nio.file.Paths;

import javax.management.JMException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class MBeanTest {

  private static final String LANGUAGE = "smalltalk";

  public static void main(String[] args) throws JMException, IOException {
    String imagePath = Paths.get("../squeak-servlet-demo/src/main/webapp/WEB-INF/squeak/graalsqueak-1.0.0-rc2-seaside.image").toAbsolutePath().toString();
    Context graalContext = Context.newBuilder()
        .option(LANGUAGE + ".ImagePath", imagePath)
        .allowAllAccess(true)
        .build();
    System.out.println("looking up adapter");
    Value serverAdatpor = graalContext.eval(LANGUAGE,
        "WAServletServerAdaptor basicNew");
    System.out.println("registering MBean");
    serverAdatpor.invokeMember("registerMBeanWithLock:", new Object());
    System.out.println("registered MBean");
    System.in.read();
  }

}
