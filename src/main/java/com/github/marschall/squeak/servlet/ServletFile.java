package com.github.marschall.squeak.servlet;

/**
 * An object that can be easily translated to a WAFile.
 */
public final class ServletFile {

  private final String partName;
  private final String fileName;
  private final String contentType;
  private final byte[] contents;

  ServletFile(String partName, String fileName, String contentType, byte[] contents) {
    this.partName = partName;
    this.fileName = fileName;
    this.contentType = contentType;
    this.contents = contents;
  }

  public String getPartName() {
    return partName;
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public byte[] getContents() {
    return contents;
  }

}