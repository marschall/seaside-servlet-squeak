package com.github.marschall.seaside.servlet.squeak;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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

/**
 * Exposes a Squeak Object in a {@link Value} as a {@link DynamicMBean}.
 */
public final class SqueakObjectMBean implements DynamicMBean {

  private final Value squeakObject;

  /**
   * Lock through which all access to Squeak objects happens because GraalSqueak is not thread safe.
   */
  private final Object lock;
  private final MBeanInfo mBeanInfo;
  private final Map<String, MBeanAttributeInfo> attributes;
  private final Map<String, MBeanOperationInfo> operations;

  /**
   * Constructs a new {@link SqueakObjectMBean}.
   *
   * @param squeakObject the Squeak object, not {@code null}
   * @param mBeanInfo the MBeanInfo describing {@code squeakObject}, not {@code null}
   * @param lock the lock to guard access to the Graal context
   */
  public SqueakObjectMBean(Value squeakObject, MBeanInfo mBeanInfo, Value lock) {
    Objects.requireNonNull(squeakObject, "squeakObject");
    Objects.requireNonNull(mBeanInfo, "mBeanInfo");
    Objects.requireNonNull(lock, "lock");
    this.squeakObject = squeakObject;
    this.mBeanInfo = mBeanInfo;
    this.lock = lock.asHostObject();
    this.attributes = toFeatureInfoMap(mBeanInfo.getAttributes());
    this.operations = toFeatureInfoMap(mBeanInfo.getOperations());
  }

  private static <I extends MBeanFeatureInfo> Map<String, I> toFeatureInfoMap(I[] mBeanAttributeInfos) {
    return Arrays.stream(mBeanAttributeInfos)
        .collect(toMap(MBeanFeatureInfo::getName, identity()));
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    return this.mBeanInfo;
  }

  @Override
  public Object getAttribute(String attributeName)
          throws AttributeNotFoundException, MBeanException, ReflectionException {

    synchronized (this.lock) {
      return this.doGetAttributeValue(attributeName);
    }
  }

  @Override
  public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    synchronized (this.lock) {
      this.doSetAttribute(attribute);
    }
  }

  @Override
  public AttributeList getAttributes(String[] attributeNames) {
    synchronized (this.lock) {
      return this.getGetAttributes(attributeNames);
    }
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    synchronized (this.lock) {
      return this.doSetAttributes(attributes);
    }
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException {

    synchronized (this.lock) {
      return this.doInvoke(actionName, params);
    }
  }

  private Object doGetAttributeValue(String attributeName)
          throws AttributeNotFoundException, MBeanException, ReflectionException {

    MBeanAttributeInfo attributeInfo = this.attributes.get(attributeName);
    if (attributeInfo == null) {
      throw new AttributeNotFoundException();
    }
    Value attributeValue;
    try {
      attributeValue = this.squeakObject.invokeMember(attributeName);
    } catch (PolyglotException e) {
      throw new MBeanException(e, "could not get attribute: " + attributeName);
    } catch (UnsupportedOperationException e) {
      throw new ReflectionException(e, "could not get attribute: " + attributeName);
    }
    return getHostValue(attributeInfo.getType(), attributeValue);
  }

  private void doSetAttribute(Attribute attribute)
          throws AttributeNotFoundException, MBeanException, ReflectionException {

    String attributeName = attribute.getName();
    MBeanAttributeInfo attributeInfo = this.attributes.get(attributeName);
    if (attributeInfo == null) {
      throw new AttributeNotFoundException("attribute: " + attributeName + " not found");
    }
    String selector = attributeName + ':';
    try {
      this.squeakObject.invokeMember(selector, attribute.getValue());
    } catch (PolyglotException e) {
      throw new MBeanException(e, "could not set attribute: " + attributeName);
    } catch (UnsupportedOperationException e) {
      throw new ReflectionException(e, "could not set attribute: " + attributeName);
    }
  }

  private AttributeList getGetAttributes(String[] attributeNames) {
    AttributeList result = new AttributeList(attributeNames.length);
    for (String attributeName : attributeNames) {
      try {
        Object attributeValue = this.doGetAttributeValue(attributeName);
        result.add(new Attribute(attributeName, attributeValue));
      } catch (AttributeNotFoundException | MBeanException | ReflectionException e) {
        // apparently spec says it's fine to ignore
        // TODO file RFE for public JMRuntimeException constructor
      }
    }
    return result;
  }

  private AttributeList doSetAttributes(AttributeList attributes) {
    AttributeList result = new AttributeList(attributes.size());
    for (Object each : attributes) {
      Attribute attribute = (Attribute) each;
      try {
        this.setAttribute(attribute);
        String attributeName = attribute.getName();
        result.add(new Attribute(attributeName, this.doGetAttributeValue(attributeName)));
      } catch (AttributeNotFoundException | InvalidAttributeValueException  | MBeanException | ReflectionException e) {
        // apparently spec says it's fine to ignore
      }
    }
    return result;
  }

  private Object doInvoke(String actionName, Object[] params) throws ReflectionException, MBeanException {
    MBeanOperationInfo operationInfo = this.operations.get(actionName);
    if (operationInfo == null) {
      throw new ReflectionException(new IllegalArgumentException("action: " + actionName + " does not exist"), "can not invoke: " + actionName);
    }
    try {
      Value result = this.squeakObject.invokeMember(actionName, params);
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



  private static Object getHostValue(String type, Value value) throws ReflectionException {
    switch (type) {
      case "java.lang.String":
        return value.asString();
      case "int":
        return value.asInt();
      case "boolean":
        return value.asBoolean();
      case "void":
        return null;
      default:
        throw new ReflectionException(new IllegalArgumentException("unknown type: " + type));
    }
  }

}
