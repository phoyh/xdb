package xdb;

import java.io.*;

/**
** Encapsulates and objectifizes a <u>U</u>niform <u>R</u>esource <u>L</u>ocator (<b>URI</b>).
*/

public class URI {
 private String uriString;
 private String fileName;
 private boolean isFile;
/** @param resource The URL or filename of the resource */
 public URI(String resource) {
  if (resource.indexOf("http://")!=-1) {
   fileName="";
   isFile=false;
   uriString=resource;
  }
  else {
   isFile=true;
   if (resource.indexOf("file:///")==-1) {
    fileName=new File(resource).getAbsolutePath();
    uriString="file:///"+fileName.replace('\\','/');
   }
   else {
    uriString=resource;
    fileName=resource.substring(new String("file:///").length());
    markAsDOS();
   }
  }
 }
/** @return true iff this is a file URI */
 public boolean isFile() {
  return isFile;
 }
/** @return the URI of the resource */
 public String getURI() {
  return uriString;
 }
/** @return the URL or absolute file path of the resource */
 public String toString() {
  if (isFile) {
   return fileName;
  }
  else {
   return uriString;
  }
 }
/** If resource is a file, its representation should be DOS-like */
 public void markAsDOS() {
  fileName=fileName.replace('/','\\');
 }
/** If resource is a file, its representation should be Unix-like */
 public void markAsUnix() {
  fileName=fileName.replace('\\','/');
 }
}