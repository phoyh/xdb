package xdb;

import java.io.*;

/**
** This is a full implementation of the XDB driver interface.<br>
** <br>
** For support of different underlying technologies, several packages are
** needed:<ul><li>
** <u>com.sun.xml.tree</u> (only if the default documentProxy used):
** DOM1, XML1, DTD
** </li><li>
** <u>org.w3c.dom</u>:
** DOM1
** </li><li>
** <u>com.jclark.xsl.dom</u>:
** DOM1, XSLT1
** </li></ul>
** @version 1.0
** @author Phoyh
*/

public class xmlDriverImpl implements xmlDriver {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("xmlDriver."+place+": "+cause);
 }
 private File getLockFile(URI resource) throws xdbException {
  if (!resource.isFile()) trap("ResourceLock","The resource doesn't support allocation management because it is not a file");
  return new File(resource.toString()+".lck");
 }
 static documentProxy defaultProxy() {
  return new xmlProxyImpl();
 }

 public boolean allocResource(URI resource) throws xdbException{
  File f=getLockFile(resource);
  if ((f==null) || (f.exists())) {
   return false;
  }
  else {
   try {
    FileOutputStream fos=new FileOutputStream(f);
    fos.close();
    return true;
   }
   catch (Exception e) {return false;}
  }
 }
 public void freeResource(URI resource) throws xdbException {
  File f=getLockFile(resource);
  if (f!=null) f.delete();
 }
 public XML getXML(URI resource) throws xdbException{
  return getXML(resource,xmlDriverImpl.defaultProxy());
 }
 public XML getXML(URI resource,documentProxy proxy) throws xdbException{
  return new xmlImpl(resource,proxy);
 }
 public XSLT getXSLT(URI resource) throws xdbException{
  return new xsltImpl(resource,xmlDriverImpl.defaultProxy());
 }
 public void createXML(URI resource, String rootName) throws xdbException{
  createXML(resource,xmlDriverImpl.defaultProxy(),rootName);
 }
 public void createXML(URI resource, documentProxy proxy, String rootName) throws xdbException{
  proxy.create(resource,rootName);
 }
}
