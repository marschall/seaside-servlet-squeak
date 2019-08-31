package com.github.marschall.squeak.servlet;

import java.util.Objects;

/**
 * Abstract base class for multipart form fields.
 * Either {@link FilePart} for {@link FormFieldPart}.
 */
public abstract class FormPart {

  protected final String partName;

  FormPart(String partName) {
    Objects.requireNonNull(partName, "partName");
    this.partName = partName;
  }

  public String getPartName() {
    return partName;
  }

  public abstract boolean isFile();

}