import java.io.*;

import org.w3c.dom.*;
import xdb.*;
import xdb.addIns.*;

class SynchronizeFolders {
 public static final String XMLSTORE="../XMLStore/";
 public static final String SYNCFOL=XMLSTORE+"syncFol/";
 private static String getNow() {
  String ret="0";
  try{
   File f=new File("dummy.tmp");
   new FileOutputStream(f).close();
   ret=""+f.lastModified();
   f.delete();
  }
  catch (Exception e) {System.out.println(e);}
  return ret;
 }
 private static String updateLastSync (String sourceDir,String destDir) {
  String ret="0";
  try {
   xmlDriver driver=new xmlDriverImpl();
   XML x=driver.getXML(new URI(SynchronizeFolders.SYNCFOL+"syncFol.xml"));
   Element e=null;
   for (Element it=(Element)x.getDOM().getDocumentElement().getFirstChild();(it!=null && e==null);it=(Element)it.getNextSibling()) {
    if (it.getAttribute("sourceDir").equalsIgnoreCase(sourceDir) && it.getAttribute("destDir").equalsIgnoreCase(destDir)) e=it;
   }
   if (e==null) {
    e=x.getDOM().createElement("entry");
    e.setAttribute("sourceDir",sourceDir);
    e.setAttribute("destDir",destDir);
    e.setAttribute("lastSync","0");
    x.getDOM().getDocumentElement().appendChild(e);
   }
   ret=e.getAttribute("lastSync");
   e.setAttribute("lastSync",SynchronizeFolders.getNow());
   x.save();
  }
  catch (Exception e) {System.out.println(e);}
  return ret;
 }
 public static void main(String[] args) throws Exception {
  if (args.length!=2) {
   System.out.println("Syntax: syncFol <sourceDir> <destDir>");
   System.exit(0);
  }
  String sourceDir=args[0];
  String destDir=args[1];
  String lastSync=SynchronizeFolders.updateLastSync(sourceDir,destDir);
  xmlDriver driver=new xmlDriverImpl();
  documentProxy fsProxy=new fsProxyImpl(false,"tmp");
  XML sourceX=driver.getXML(new URI(sourceDir),fsProxy);
  XSLT changeRecon=driver.getXSLT(new URI(SynchronizeFolders.SYNCFOL+"syncFol.xsl"));
  changeRecon.setParameter("refModified",lastSync);
  XML log=changeRecon.transform(sourceX);
  log.setURI(new URI(SynchronizeFolders.SYNCFOL+"syncFol.log"));
  log.save();
  XSLT identity=driver.getXSLT(new URI(SynchronizeFolders.SYNCFOL+"copyAll.xsl"));
  XML destX=driver.getXML(new URI(destDir),fsProxy);
  identity.transform(log,destX);
  destX.save();
 }
}
