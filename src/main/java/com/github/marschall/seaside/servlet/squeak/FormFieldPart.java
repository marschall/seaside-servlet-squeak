package com.github.marschall.seaside.servlet.squeak;

public final class FormFieldPart extends FormPart {

  private final String value;

  FormFieldPart(String partName, String value) {
    super(partName);
    this.value = value;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "FormFieldPart(partName: " + this.getPartName() + ')';
  }

}
