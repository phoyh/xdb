package xdb;

import com.sun.xml.tree.*;
import java.io.*;

class xmlProxyImpl implements documentProxy {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("xmlProxy."+place+": "+cause);
 }

 XmlDocument doc;

 public void create(URI resource, String rootName) throws xdbException{
  if (resource.isFile()) {
   try {
    FileOutputStream fos=new FileOutputStream(resource.toString());
    fos.write(new String("<"+rootName+"></"+rootName+">").getBytes());
   }
   catch (IOException e) {trap("create","Couldn't write to resource "+resource+"\n"+e);}
  }
  else trap("createXML","The resource "+resource+" is read-only");
 }
 public URI load() throws xdbException {
  doc=new XmlDocument();
  return new URI("dummy.xml");
 }
 public void load(URI resource) throws xdbException {
  try {
   doc=XmlDocument.createXmlDocument(resource.getURI());
  }
  catch (Exception e) {trap("load","Error while parsing the resource "+resource+"\n"+e);}
 }
 public void save(URI resource) throws xdbException {
  if (resource.isFile()) {
   try {
    FileOutputStream fos=new FileOutputStream(resource.toString());
    doc.write(fos);
    fos.close();
   }
   catch (IOException e) {trap("save","Couldn't write to resource "+resource);}
  }
  else trap("save","The resource "+resource+" is read-only");
 }
 public org.w3c.dom.Document getDOM() {
  return doc;
 }
}