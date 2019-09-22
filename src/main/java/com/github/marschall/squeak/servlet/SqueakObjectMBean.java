package com.github.marschall.squeak.servlet;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

public final class SqueakObjectMBean implements DynamicMBean {
  
  // TODO lock
  
  private final Value value;
  private final MBeanInfo mBeanInfo;
  private final Map<String, MBeanAttributeInfo> attributes;
  private final Map<String, MBeanOperationInfo> operations;

  public SqueakObjectMBean(Value value, MBeanInfo mBeanInfo) {
    this.value = value;
    this.mBeanInfo = mBeanInfo;
    this.attributes = toFeatureInfoMap(mBeanInfo.getAttributes());
    this.operations = toFeatureInfoMap(mBeanInfo.getOperations());
  }

  private static <I extends MBeanFeatureInfo> Map<String, I> toFeatureInfoMap(I[] mBeanAttributeInfos) {
    return Arrays.stream(mBeanAttributeInfos)
        .collect(toMap(MBeanFeatureInfo::getName, identity()));
  }

  @Override
  public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
    MBeanAttributeInfo attributeInfo = this.attributes.get(attributeName);
    if (attributeInfo == null) {
      throw new AttributeNotFoundException();
    }
    Value attributeValue;
    try {
      attributeValue = this.value.invokeMember(attributeName);
    } catch (PolyglotException e) {
      throw new MBeanException(e, "could not get attribute: " + attributeName);
    } catch (UnsupportedOperationException e) {
      throw new ReflectionException(e, "could not get attribute: " + attributeName);
    }
    return getHostValue(attributeInfo.getType(), attributeValue);
  }
  
  private static Object getHostValue(String type, Value value) throws ReflectionException {
    switch (type) {
      case "java.lang.String":
        return value.asString();
      case "int":
        return value.asInt();
      case "boolean":
        return value.asBoolean();
      default:
        throw new ReflectionException(new IllegalArgumentException("unknown type: " + type));
    }
  }

  @Override
  public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    String attributeName = attribute.getName();
    MBeanAttributeInfo attributeInfo = this.attributes.get(attributeName);
    if (attributeInfo == null) {
      throw new AttributeNotFoundException("attribute: " + attributeName + " not found");
    }
    String selector = attributeName + ':';
    try {
      this.value.invokeMember(selector, attribute.getValue());
    } catch (PolyglotException e) {
      throw new MBeanException(e, "could not set attribute: " + attributeName);
    } catch (UnsupportedOperationException e) {
      throw new ReflectionException(e, "could not set attribute: " + attributeName);
    }
  }

  @Override
  public AttributeList getAttributes(String[] attributeNames) {
    AttributeList result = new AttributeList(attributeNames.length);
    for (String attributeName : attributeNames) {
      try {
        Object attribute = getAttribute(attributeName);
        result.add(attribute);
      } catch (AttributeNotFoundException | MBeanException | ReflectionException e) {
        // apparently spec says it's fine to ignore
        // TODO file RFE for public JMRuntimeException constructor
      }
    }
    return result;
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    AttributeList result = new AttributeList(attributes.size());
    for (Object each : attributes) {
      Attribute attribute = (Attribute) each;
      try {
        this.setAttribute(attribute);
        String attributeName = attribute.getName();
        result.add(new Attribute(attributeName, this.getAttribute(attributeName)));
      } catch (AttributeNotFoundException | InvalidAttributeValueException  | MBeanException | ReflectionException e) {
        // apparently spec says it's fine to ignore
      }
    }
    return result;
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException {
    MBeanOperationInfo operationInfo = this.operations.get(actionName);
    if (operationInfo == null) {
      throw new ReflectionException(new IllegalArgumentException("action: " + actionName + " does not exist"), "can not invoke: " + actionName);
    }
    try {
      Value result = this.value.invokeMember(actionName, params);
      if (operationInfo.getImpact() == MBeanOperationInfo.INFO) {
        return null;
      }
      if (result.isNull()) {
        return null;
      }
      return getHostValue(operationInfo.getReturnType(), result);
    } catch (PolyglotException e) {
      throw new MBeanException(e, "could not invoke: " + actionName);
    } catch (UnsupportedOperationException e) {
      throw new ReflectionException(e, "could not invoke: " + actionName);
    }
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    return this.mBeanInfo;
  }

}
