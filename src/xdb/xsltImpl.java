package xdb;

import org.w3c.dom.*;
import com.jclark.xsl.dom.*;
import java.io.*;
import java.util.*;

class xsltImpl extends xmlImpl implements XSLT {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("XSLT."+place+": "+cause);
 }

 private XSLTransformEngine engine;
 private Transform transformer;
 private Dictionary parameters;

 xsltImpl (URI resource,documentProxy proxy) throws xdbException {
  uri=resource;
  docProxy=proxy;
  load();
  engine=new XSLTransformEngine();
  transformer=null;
  try {
   parameters=new Hashtable();
   NodeList vars=getDOM().getDocumentElement().getElementsByTagName("xsl:variable");
   for (int i=0;i<vars.getLength();i++) {
    parameters.put(((Element)vars.item(i)).getAttribute("name"),vars.item(i).getFirstChild());
   }
  }
  catch (DOMException e) {trap("XSLT","Couldn't trace the XSLT as far as the parameters are concerned\n"+e);}
 }
 public void setParameter(String parameterName,String parameterValue) throws xdbException {
  transformer=null;
  try {
   ((Node)parameters.get(parameterName)).setNodeValue(parameterValue);
  }
  catch (Exception e) {trap("setParameter","Parameter "+parameterName+" is not defined as variable within the XSLT");}
 }
 public String getParameter(String parameterName) throws xdbException {
  try {
   return ((Node)parameters.get(parameterName)).getNodeValue();
  }
  catch (Exception e) {trap("getParameter","Parameter "+parameterName+" is not defined as variable within the XSLT");return null;}
 }
 public XML transform(XML sourceDocument) throws xdbException {
  XML x=new xmlImpl(xmlDriverImpl.defaultProxy());
  transform(sourceDocument,x);
  return x;
 }
 public void transform(XML sourceDocument,XML destDocument) throws xdbException {
  try {
   if (transformer==null) {
    transformer=engine.createTransform(getDOM());
   }
   transformer.transform(sourceDocument.getDOM(),destDocument.getDOM());
  }
  catch (TransformException e) {trap("transform","XSL Transformation not feasable\n"+e);}
 }
}