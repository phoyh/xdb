package xdb;

import org.w3c.dom.*;
import java.util.*;

class recordsetImpl implements recordset {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("Recordset."+place+": "+cause);
 }

 public static final String ROOTNAME="recordset";
 public static final String ELEMENTNAME="record";
 Dictionary[] records;
 int iteratorPos=0;

 recordsetImpl(Document doc) throws xdbException {
  Element recset=doc.getDocumentElement();
  if (!recset.getTagName().equalsIgnoreCase(recordsetImpl.ROOTNAME)) trap("recordset","Root is not DTD conform");
  try {
   NodeList recs=recset.getElementsByTagName(recordsetImpl.ELEMENTNAME);
   records=new Hashtable[recs.getLength()];
   for (int i=0;i<recs.getLength();i++) {
    records[i]=new Hashtable();
    for (Node el=recs.item(i).getFirstChild();(el!=null);el=el.getNextSibling()) {
     if (el.getNodeType()==Node.ELEMENT_NODE) {
      records[i].put(el.getNodeName(),el.getFirstChild().getNodeValue());
     }
    }
   }
  }
  catch (DOMException e) {trap("recordset","Set cannot be built");}
 }
 public int size() {
  return records.length;
 }
 public void moveTo(int index) {
  iteratorPos=index;
 }
 public void moveFirst() {
  iteratorPos=0;
 }
 public void moveNext() {
  iteratorPos++;
 }
 public int getIndex() {
  return iteratorPos;
 }
 public boolean eof() {
  return (iteratorPos<0 || iteratorPos>=size());
 }
 public String[] getPropertyNames() {
  if (!eof()) {
   String[] props=new String[records[iteratorPos].size()];
   Enumeration e=records[iteratorPos].keys();
   for (int i=0;e.hasMoreElements();i++) {
    props[i]=(String)e.nextElement();
   }
   return props;
  }
  return new String[0];
 }
 public String getPropertyValue(String propertyName) {
  if (!eof()) {
   Object o=records[iteratorPos].get(propertyName);
   if (o!=null) return (String)o;
  }
  return new String("");
 }
}