package com.github.marschall.squeak.servlet;

import java.io.IOException;

import javax.management.JMException;

import org.graalvm.polyglot.Context;

public class MBeanTest {

  private static final String LANGUAGE = "squeaksmalltalk";

  public static void main(String[] args) throws JMException, IOException {
    Context graalContext = Context.newBuilder()
        .option(LANGUAGE + ".ImagePath", "/Users/marschall/git/squeak-servlet-demo/src/main/webapp/WEB-INF/squeak/graalsqueak-0.8.4-seaside.image")
        .allowAllAccess(true)
        .build();
    System.out.println("registering MBean");
    graalContext.eval(LANGUAGE,
        "WAServletServerAdaptor registerMBean");
    System.out.println("registered MBean");
    System.in.read();
  }

}
