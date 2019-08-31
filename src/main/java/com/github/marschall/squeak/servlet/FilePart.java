package com.github.marschall.squeak.servlet;

/**
 * An object that can be easily translated to a WAFile.
 */
public final class FilePart extends FormPart {

  private final String fileName;
  private final String contentType;
  private final byte[] contents;

  FilePart(String partName, String fileName, String contentType, byte[] contents) {
    super(partName);
    this.fileName = fileName;
    this.contentType = contentType;
    this.contents = contents;
  }

  @Override
  public boolean isFile() {
    return true;
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

  @Override
  public String toString() {
    return "FilePart(partName: " + this.getPartName() + ", fileName: " + this.fileName + ')';
  }

}