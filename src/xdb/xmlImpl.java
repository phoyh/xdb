package xdb;

import org.w3c.dom.*;
import java.io.*;

class xmlImpl implements XML {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("XML."+place+": "+cause);
 }
 private Node getXPointerNode(String xPointer) throws xdbException{
  try {
   String dummy=xPointer;
   if (dummy.charAt(0)=='/' && dummy.length()>1) dummy=dummy.substring(1);
   if (dummy.charAt(dummy.length()-1)=='/') dummy=dummy.substring(0,dummy.length()-1);
   Document root=getDOM();
   Node pos=root.getDocumentElement();
   while (dummy.length()>0) {
    if (dummy.charAt(0)=='@') {
     Attr a=((Element)pos).getAttributeNode(dummy.substring(1));
     if (a==null) {
      a=root.createAttribute(dummy.substring(1));
      ((Element)pos).setAttributeNode(a);
     }
     pos=a;
     dummy="";
    }
    else {
     int nextSlash=dummy.indexOf('/');
     String item;
     if (nextSlash==-1) {
      item=dummy;
      dummy="";
     }
     else {
      item=dummy.substring(0,nextSlash);
      dummy=dummy.substring(nextSlash+1);
     }
     Node e=pos.getFirstChild();
     for (;(e!=null && !e.getNodeName().equals(item));e=e.getNextSibling()) {
     }
     if (e==null) {
      e=root.createElement(item);
      pos.appendChild(e);
     }
     pos=e;
    }
   }
   if (pos.getNodeType()==Node.ELEMENT_NODE) {
    Node t=pos.getLastChild();
    if (t==null || t.getNodeType()!=Node.TEXT_NODE) {
     t=root.createTextNode("");
     pos.appendChild(t);
    }
    pos=t;
   }
   return pos;
  }
  catch (Exception e) {trap("load","xPointer not valid");}
  return null;
 }

 protected URI uri;
 protected documentProxy docProxy;

 xmlImpl () {}
 xmlImpl (documentProxy proxy) throws xdbException {
  docProxy=proxy;
  uri=proxy.load();
 }
 xmlImpl (URI resource, documentProxy proxy) throws xdbException {
  uri=resource;
  docProxy=proxy;
  load();
 }
 public void setURI(URI resource) {
  uri=resource;
 }
 public URI getURI() {
  return uri;
 }
 public recordset getRecordset() throws xdbException {
  return new recordsetImpl(getDOM());
 }
 public String load(String xPointer) throws xdbException {
  return getXPointerNode(xPointer).getNodeValue();
 }
 public void store(String xPointer,String value) {
  try {getXPointerNode(xPointer).setNodeValue(value);}
  catch (Exception e) {}
 }
 public Document getDOM() {
  return docProxy.getDOM();
 }
 public void save() throws xdbException {
  docProxy.save(uri);
 }
 public void load() throws xdbException {
  docProxy.load(uri);
 }
}