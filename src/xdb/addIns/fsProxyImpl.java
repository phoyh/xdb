package xdb.addIns;

import xdb.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;

/** A file system directory structure is represented as
** a logical XML-like document. The file system DTD of such
** kind of logical documents is (fs.dtd):<code><p>
** &lt!ENTITY % fsEntry "(dir|file)"&gt<br>
** &lt!ENTITY % CDATASection "#PCDATA"&gt<br>
** &lt!ENTITY % SOURCE %CDATASection;&gt<br>
** &lt!ELEMENT fs (%fsEntry;)*&gt<br>
** &lt!ELEMENT dir (%fsEntry;)*&gt<br>
** &lt!ATTLIST dir<br>
** &nbsp;&nbsp; name CDATA #REQUIRED<br>
** &gt<br>
** &lt!ELEMENT file %SOURCE;&gt<br>
** &lt!ATTLIST file<br>
** &nbsp;&nbsp; name CDATA #REQUIRED<br>
** &nbsp;&nbsp; suffix CDATA #IMPLIED<br>
** &nbsp;&nbsp; size CDATA #IMPLIED<br>
** &nbsp;&nbsp; lastModified CDATA #IMPLIED<br>
** &nbsp;&nbsp; contentLink CDATA #IMPLIED<br>
** &gt<br>
** &lt!ELEMENT link
** </p></code><br>
** Assignment and retrieval of file contents are feasable either through
** the contentLink attribute or through the CDATA-Section child. The contentLink
** is a URI to the resource. If both, the contentLink attribute and the
** CDATA-Section, are set, priority is given to the contentLink and its
** content.<br>
** For operation on nodes which haven't been assigned a fixed place within
** the file structure, a temporary directory is needed. It shall be signalled
** during initialization, which directory is used.<br>
** No effects of DOM manipulation are visible to the outside until explicitely
** committing with the save primitive. Nevertheless, the heavyweight file contents
** are left within the file system. After committing, the DOM objects have to be
** abandoned.<br>
** The implementation tries to throw as few exceptions as possible. If
** someone wants to edit outside of the scope of the fs.dtd, his efforts
** are therefore ignored.<br>
** The following list shows, which part of the DOM implementation for a
** file system comes with non trivial decision about its behaviour. It is
** listed why certain parts are not trivial and how the design decisions
** for these parts are:<ul><li>
** <b>CDATASection.splitText: </b>New text node is without parent, old text node
** hasn't its content changed.
** </li><li>
** <b>Attr: </b>No entity references or text node children are supported,
** because both are dummy.
** </li></ul>
*/

public class fsProxyImpl implements documentProxy {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("fsProxy."+place+": "+cause);
 }

 private File root;
 private File temp;
 private DOMManager manager;
 private boolean isFileContentShown;

/** @param isFileContentShown true iff file nodes should contain a CDATA-Section as the file content*/
 public fsProxyImpl (boolean isFileContentShown, String tempDir) {
  temp=new File(tempDir);
  this.isFileContentShown=isFileContentShown;
 }
/** Inits the resource as root. rootNode is dummy */
 public void create(URI resource,String rootNode) throws xdbException {
  if (resource.isFile()) {
   new File(resource.toString()).mkdirs();
   load(resource);
  }
  else trap("create","Resource is not a file system resource");
 }
 public URI load() throws xdbException {
  root=new File("./");
  URI rootURI=new URI(root.getAbsolutePath());
  load(rootURI);
  return rootURI;
 }
 public void load(URI resource) throws xdbException {
  if (resource.isFile()) {
   root=new File(resource.toString());
   if (root.exists()) {
    manager=new DOMManager(temp,isFileContentShown);
    manager.build(root);
   }
   else {
    trap("load","Resource "+resource+" inexistent");
   }
  }
  else trap("load","Resource is not a file system resource");
 }
/** Commits the work done through the DOM interfaces. */
 public void save(URI resource) throws xdbException {
  manager.commit(new File(resource.toString()));
 }
 public Document getDOM() {return manager.getDOM();}

///////////// THE DOM IMPLEMENTATION ////////////////////

///Helpers///

class DOMManager {
 private void trap(String place,String cause) throws xdbException {
  throw new xdbException("fsManager."+place+": "+cause);
 }
 private void notify(String place, String message) {
  System.out.println("fsManager."+place+": "+message);
 }
 File root,temp;
 fsDocument doc;
 boolean fileContentShown;
 DOMManager (File temp,boolean fileContentShown) {
  this.temp=temp;
  this.fileContentShown=fileContentShown;
 }
 void build (File root) throws xdbException{
  this.root=root;
  ResourceManager.setTemp(temp);
  Stack se=new Stack();
  Stack sp=new Stack();
  doc=new fsDocument(fileContentShown);
  se.push(doc.getDocumentElement());
  sp.push(root);
  while (!se.empty()) {
   try {
    fsElement elem=(fsElement)se.pop();
    File fullPath=(File)sp.pop();
    elem.setAttribute("name",fullPath.getName());
    String[] fileNames=fullPath.list();
    for (int i=0;i<fileNames.length;i++) {
     File kid=new File(fullPath,fileNames[i]);
     if (kid.isFile()) {
      fsCDATASection cd=new fsCDATASection(ResourceManager.getRM(kid),doc);
      fsFileElement fe=new fsFileElement(cd,doc);
      fe.setAttribute("name",fileNames[i]);
      elem.appendChild(fe);
     }
     else {
      fsDirElement de=(fsDirElement)doc.createElement("dir");
      de.setAttribute("name",fileNames[i]);
      elem.appendChild(de);
      se.push(de);
      sp.push(kid);
     }
    }
   }
   catch (Exception e) {trap("build","Error while initializing "+root.getAbsolutePath()+"\n"+e);}
  }  
 }
 void commit(File destRoot) throws xdbException{
  Stack es=new Stack();
  Stack ps=new Stack();
  es.push(doc.getDocumentElement());
  ps.push(destRoot);
  while (!es.empty()) {
   try {
    fsNode elem=(fsNode)es.pop();
    File fullPath=(File)ps.pop();
    if (elem.getNodeName().equals("file")) {
     ((fsFileElement)elem).getRM().commitTo(fullPath);
    }
    else {
     fullPath.mkdirs();
     for (Node subElem=elem.getFirstChild();(subElem!=null);subElem=subElem.getNextSibling()) {
      if (subElem.getNodeType()==Node.ELEMENT_NODE) {
       es.push(subElem);
       ps.push(new File(fullPath,((fsElement)subElem).getAttribute("name")));
      }
     }
    }
   }
   catch (DOMException e) {trap("fsCommit","Error while committing\n"+e);}
  }
 }
 fsDocument getDOM() {return doc;}
}

/// The DOMs ///

abstract class fsNode implements Node {
 private fsNode parentNode=null;
 protected fsDocument owner=null;
 void setParentNode(fsNode a) {parentNode=a;}
 public String getNodeName() {return null;}
 public String getNodeValue() {return null;}
 public void setNodeValue(String dummy) {}
 public short getNodeType() {return -1;}
 public Node getParentNode() {return parentNode;}
 public NodeList getChildNodes() {return null;}
 public Node getFirstChild() {return null;}
 public Node getLastChild() {return null;}
 public Node getPreviousSibling() {return null;}
 public Node getNextSibling() {return null;}
 public NamedNodeMap getAttributes() {return null;}
 public Document getOwnerDocument() {return owner;}
 public Node insertBefore(Node dummy1, Node dummy2) {return null;}
 public Node replaceChild(Node dummy1, Node dummy2) {return null;}
 public Node removeChild(Node dummy) {return null;}
 public Node appendChild(Node dummy) {return null;}
 public boolean hasChildNodes() {return false;}
 public Node cloneNode(boolean dummy) {return null;}
}

class fsDocument extends fsNode implements Document {
 private fsRootElement docElem;
 private boolean fileContentShown;
 boolean isFileContentShown() {return fileContentShown;}
 fsDocument(boolean isFileContentOpt) {
  docElem=new fsRootElement(this);
  docElem.setParentNode(this);
  fileContentShown=isFileContentOpt;
 }
 class docChildrenNodeList extends fsNode implements NodeList {
  public Node item(int index) {
   switch (index) {
    case 0: return docElem;
    case 1: return getDoctype();
    default: return null;
   }
  }
  public int getLength() {return 2;}
 }
 class docElementsNodeList extends fsNode implements NodeList {
  private NodeList underTheRoot;
  private fsElement rootElem;
  private boolean rootContained;
  private int magicNumber;
  docElementsNodeList (fsElement docElem,String spec) {
   rootElem=docElem;
   underTheRoot=rootElem.getElementsByTagName(spec);
   rootContained=(spec.equals("*") || spec.equals("fs"));
   magicNumber=rootContained?1:0;
  }
  public Node item(int index) {
   if (index==0 && rootContained) {return rootElem;}
   else {return underTheRoot.item(index-magicNumber);}
  }
  public int getLength() {
   return underTheRoot.getLength()+magicNumber;
  }
 }
 public String getNodeName() {return "#document";}
 public short getNodeType() {return Node.DOCUMENT_NODE;}
 public NodeList getChildNodes() {return new docChildrenNodeList();}
 public Node getFirstChild() {return docElem;}
 public Node getLastChild() {return docElem;}
 public Document getOwnerDocument() {return null;}
 public Node insertBefore(Node newRoot, Node dummy2) {return appendChild(newRoot);}
 public Node replaceChild(Node newRoot, Node dummy2) {return appendChild(newRoot);}
 public Node appendChild(Node newRoot) {
  if (newRoot.getNodeName().equals("fs")) {
   docElem=(fsRootElement)newRoot;
  }
  return newRoot;
 }
 public boolean hasChildNodes() {return true;}
 public DocumentType getDoctype() {return new fsDocumentType(this);}
 public DOMImplementation getImplementation() {return new fsDOMImplementation(this);}
 public Element getDocumentElement() {return docElem;}
 public Element createElement (String tagName) {
  if (tagName.equals("file")) {return new fsFileElement(this);}
  else {
   if (tagName.equals("fs")) {return new fsRootElement(this);}
   else {return new fsDirElement(this);}
  }
 }
 public DocumentFragment createDocumentFragment () {return (fsDocumentFragment)docElem;}
 public Text createTextNode(String data) {return createCDATASection(data);}
 public Comment createComment(String data) {return new fsComment(this);}
 public CDATASection createCDATASection (String data) {
  fsCDATASection cd=new fsCDATASection(ResourceManager.getRM(null),this);
  cd.setData(data);
  return cd;
 }
 public ProcessingInstruction createProcessingInstruction (String target,String data) {return new fsProcessingInstruction(this);}
 public Attr createAttribute (String name) {return new fsAttr(this,name);}
 public EntityReference createEntityReference (String name) {return new fsEntityReference(this);}
 public NodeList getElementsByTagName (String tagName) {return new docElementsNodeList(docElem,tagName);}
}

class fsCDATASection extends fsNode implements CDATASection, Text {
 ResourceManager rm;
 ResourceManager getRM() {return rm;}
 fsCDATASection (ResourceManager mana,fsDocument ownerDocument) {
  rm=mana;
  owner=ownerDocument;
 }
 public String getNodeName() {return "#cdata-section";}
 public String getNodeValue() {return getData();}
 public void setNodeValue(String data) {setData(data);}
 public short getNodeType() {return Node.CDATA_SECTION_NODE;}
 public Node cloneNode(boolean dummy) {
  return new fsCDATASection(ResourceManager.getRM(rm.getReadHandle()),owner);
 }
 public String getData() {return substringData(0,getLength());}
 public void setData(String data) {
  try {
   FileOutputStream fso=new FileOutputStream(rm.getWriteHandle());
   fso.write(data.getBytes());
   fso.close();
  }
  catch (Exception e) {}
 }
 public int getLength() {return (int)rm.getReadHandle().length();}
 public String substringData(int offset,int count) {
  String ret=new String("");
  try {
   FileInputStream fis=new FileInputStream(rm.getReadHandle());
   fis.skip(offset);
   byte[] buffer=new byte[count];
   fis.read(buffer);
   ret=new String(buffer);
  }
  catch (Exception e) {}
  return ret;
 }
 public void appendData(String data) {insertData(getLength(),data);}
 public void insertData(int offset,String data) {replaceData(offset,0,data);}
 public void deleteData(int offset,int count) {replaceData(offset,count,"");}
 public void replaceData(int offset, int count, String data) {
  setData(substringData(0,offset)+data+substringData(offset+count,getLength()-offset-count));
 }
 public Text splitText(int offset) {
  return owner.createTextNode(substringData(offset,getLength()-offset));
 }
}

abstract class fsElement extends fsNode implements Element {
 abstract public String getTagName();
 public String getAttribute(String name) {
  Attr a=getAttributeNode(name);
  return (a==null) ? "" : a.getValue();
 }
 public void setAttribute(String name,String value) {
  Attr a=owner.createAttribute(name);
  a.setValue(value);
  setAttributeNode(a);
 }
 public void removeAttribute(String name) {
  Attr a=getAttributeNode(name);
  if (a!=null) removeAttributeNode(a);
 }
 abstract public Attr getAttributeNode(String name);
 abstract public Attr setAttributeNode(Attr newAttr);
 public Attr removeAttributeNode(Attr oldAttr) {return oldAttr;}
 abstract public NodeList getElementsByTagName(String name);
 public void normalize() {}
 public String getNodeName() {return getTagName();}
 public short getNodeType() {return Node.ELEMENT_NODE;}
 public Node getPreviousSibling() {return ((fsDirElement)getParentNode()).getMyPred(this);}
 public Node getNextSibling() {return ((fsDirElement)getParentNode()).getMySucc(this);}
}

class fsFileElement extends fsElement {
 private String fileName;
 private fsCDATASection child;
 private fileAttrNamedNodeMap attributes;
 private String contentLink;
 fsFileElement(fsCDATASection cd,fsDocument ownerDocument) {
  owner=ownerDocument;
  child=cd;
  child.setParentNode(this);
  attributes=new fileAttrNamedNodeMap(owner,this);
  fileName="myFile.tmp";
  contentLink=owner.isFileContentShown()?"":new URI(cd.getRM().getReadHandle().getAbsolutePath()).getURI();
 }
 fsFileElement (fsDocument ownerDocument) {
  this(new fsCDATASection(ResourceManager.getRM(null),ownerDocument),ownerDocument);
  contentLink="";
 }
 fsCDATASection getCDATASection() {return child;}
 String getFileName() {return fileName;}
 void setFileName(String name) {fileName=name;}
 boolean hasContentLink() {return (contentLink.length()!=0);}
 void setContentLink(String newLink) {contentLink=newLink;}
 String getContentLink() {return contentLink;}
 ResourceManager getRM() {
  if (hasContentLink()) {
   return ResourceManager.getRM(new File(new URI(contentLink).toString()));
  }
  else {return child.getRM();}
 }
 class cdataNodeList extends fsNode implements NodeList {
  fsFileElement master;
  cdataNodeList(fsFileElement m) {master=m;}
  public Node item(int index) {
   return (index==0 && master.hasChildNodes()) ? master.getCDATASection() : null;
  }
  public int getLength() {return 0;}
 }
 class fileAttrBackend implements fsAttrBackend {
  fsFileElement master;
  fileAttrBackend(fsFileElement fe) {master=fe;}
  public String getValue(String propName) {
   try {
    if (propName.equals("name")) return master.getFileName();
    if (propName.equals("suffix")) {
     String fileName=master.getFileName();
     return fileName.substring(fileName.lastIndexOf('.')+1);
    }
    if (propName.equals("size")) return ""+master.getRM().getReadHandle().length();
    if (propName.equals("lastModified")) return ""+master.getRM().getReadHandle().lastModified();
    if (propName.equals("contentLink")) return master.getContentLink();
   }
   catch (Exception e) {}
   return "";
  }
  public String setValue(String propName,String propValue) {
   if (propName.equals("name")) master.setFileName(propValue);
   if (propName.equals("suffix")) {
    String fileName=master.getFileName();
    int index=fileName.lastIndexOf('.');
    if (index>-1) master.setFileName(fileName.substring(0,index+1)+propValue);
   }
   if (propName.equals("contentLink")) master.setContentLink(propValue);
   return propValue;
  }
 }
 class fileAttrNamedNodeMap extends fsNode implements NamedNodeMap {
  fsFileElement master;
  fsAttr name,suffix,size,lastModified,contentLink;
  fileAttrNamedNodeMap(fsDocument ownerDoc,fsFileElement master) {
   owner=ownerDoc;
   this.master=master;
   name=(fsAttr)owner.createAttribute("name");
   suffix=(fsAttr)owner.createAttribute("suffix");
   size=(fsAttr)owner.createAttribute("size");
   lastModified=(fsAttr)owner.createAttribute("lastModified");
   contentLink=(fsAttr)owner.createAttribute("contentLink");
   fsAttrBackend b=new fileAttrBackend(master);
   name.setBackend(b);
   suffix.setBackend(b);
   size.setBackend(b);
   lastModified.setBackend(b);
   contentLink.setBackend(b);
  }
  public Node getNamedItem(String name) {
   if (name.equals(this.name.getName())) return this.name;
   if (name.equals(suffix.getName())) return suffix;
   if (name.equals(size.getName())) return size;
   if (name.equals(lastModified.getName())) return lastModified;
   if (master.hasContentLink() && name.equals(contentLink.getName())) return contentLink;
   return null;
  }
  public Node setNamedItem(Node arg) {
   if (arg!=null && arg.getNodeType()==Node.ATTRIBUTE_NODE) {
    Attr a=(Attr)arg;
    Attr b=(Attr)getNamedItem(a.getName());
    if (a.getName().equals(contentLink.getName())) b=contentLink;
    if (b!=null) b.setValue(a.getValue());
   }
   return arg;
  }
  public Node removeNamedItem(String name) {return null;}
  public Node item(int index) {
   switch (index) {
    case 0: return name;
    case 1: return suffix;
    case 2: return size;
    case 3: return lastModified;
    case 4: return master.hasContentLink()?contentLink:null;
    default: return null;
   }
  }
  public int getLength() {return master.hasContentLink()?5:4;}
 }
 public String getTagName() {return "file";}
 public Attr getAttributeNode(String name) {return (Attr)attributes.getNamedItem(name);}
 public Attr setAttributeNode(Attr newAttr) {return (Attr)attributes.setNamedItem(newAttr);}
 public NodeList getElementsByTagName(String name) {return new dummyNodeList();}
 public NodeList getChildNodes() {return new cdataNodeList(this);}
 public Node getFirstChild() {return hasChildNodes()?child:null;}
 public Node getLastChild() {return getFirstChild();}
 public NamedNodeMap getAttributes() {return attributes;}
 public Node insertBefore(Node newNode, Node refNode) {return appendChild(newNode);}
 public Node replaceChild(Node oldNode, Node newNode) {return appendChild(newNode);}
 public Node removeChild(Node oldNode) {return oldNode;}
 public Node appendChild(Node newNode) {
  if (newNode.getNodeType()==Node.CDATA_SECTION_NODE) {
   child.setParentNode(null);
   child=(fsCDATASection)newNode;
   child.setParentNode(this);
  }
  return child;
 }
 public boolean hasChildNodes() {return owner.isFileContentShown();}
 public Node cloneNode(boolean dummy) {
  fsFileElement copiedFE=new fsFileElement((fsCDATASection)child.cloneNode(false),owner);
  copiedFE.setAttribute("name",fileName);
  return copiedFE;
 }
}  

interface DynamicNodeList extends NodeList {
 void insert(Node newChild, Node refChild);
 void remove(Node oldChild);
 int getIndex(Node oldChild);
}

class fsDirElement extends fsElement {
 String dirName;
 dirAttrNamedNodeMap attributes;
 childrenNodeList children;
 protected fsDirElement(fsDocument ownerDocument,String dirName) {
  owner=ownerDocument;
  attributes=new dirAttrNamedNodeMap(owner,this);
  this.dirName=dirName;
  children=new childrenNodeList(this);
 }
 fsDirElement (fsDocument ownerDocument) {this(ownerDocument,"myDir");}
 String getDirName() {return dirName;}
 void setDirName(String name) {dirName=name;}
 Node getMyPred(fsElement el) {
  int index=children.getIndex(el);
  if (index>0) return children.item(index-1);
  return null;
 }
 Node getMySucc(fsElement el) {
  int index=children.getIndex(el);
  if (index>-1 && index<children.getLength()-1) return children.item(index+1);
  return null;
 }
 class dirAttrBackend implements fsAttrBackend {
  fsDirElement master;
  dirAttrBackend (fsDirElement master) {this.master=master;}
  public String getValue(String propName) {
   if (propName.equals("name")) return master.getDirName();
   return "";
  }
  public String setValue(String propName,String propValue) {
   if (propName.equals("name")) master.setDirName(propValue);
   return propValue;
  }
 }
 class dirAttrNamedNodeMap extends fsNode implements NamedNodeMap {
  fsAttr name;
  fsDirElement master;
  dirAttrNamedNodeMap (fsDocument ownerDoc,fsDirElement master) {
   this.master=master;
   owner=ownerDoc;
   name=(fsAttr)owner.createAttribute("name");
   name.setBackend(new dirAttrBackend(master));
  }
  public Node getNamedItem(String name) {
   if (name.equals(this.name.getName())) return this.name;
   return null;
  }
  public Node setNamedItem(Node arg) {
   if (arg!=null && arg.getNodeType()==Node.ATTRIBUTE_NODE) {
    Attr a=(Attr)arg;
    Attr b=(Attr)getNamedItem(a.getName());
    if (b!=null) b.setValue(a.getValue());
   }
   return arg;
  }
  public Node removeNamedItem(String name) {return null;}
  public Node item(int index) {return (index==0)?name:null;}
  public int getLength() {return 1;}
 }
 class childrenNodeList extends fsNode implements DynamicNodeList {
  fsDirElement master;
  Vector children;
  childrenNodeList(fsDirElement master) {
   this.master=master;
   children=new Vector();
  }
  public Node item(int index) {
   if (index>-1 && index<children.size()) return (Node)children.elementAt(index);
   return null;
  }
  public int getLength() {return children.size();}
  public void insert(Node newChild, Node refChild) {
   if (newChild!=null) {
    Node newChildOldParent=newChild.getParentNode();
    if (newChildOldParent!=null && newChildOldParent.getNodeType()==ELEMENT_NODE) {
     ((Element)newChildOldParent).removeChild(newChild);
    }
    int index=getIndex(refChild);
    if (index==-1) {children.addElement(newChild);}
    else {children.insertElementAt(newChild,index);}
    ((fsNode)newChild).setParentNode(master);
   }
  }
  public void remove(Node oldChild) {
   if (oldChild!=null) {
    children.removeElement(oldChild);
    ((fsNode)oldChild).setParentNode(null);
   }
  }
  public int getIndex(Node oldChild) {
   if (oldChild!=null) return children.indexOf(oldChild);
   return -1;
  }
 }
 class descendantsNodeList extends fsNode implements NodeList {
  childrenNodeList children;
  String match,tag;
  descendantsNodeList (childrenNodeList c,String match) {
   children=c;
   this.match=match;
   if (match.equals("*")) {tag="dir|file";}
   else {tag=match;}
  }
  public Node item(int index) {
   if (index>-1) {
    for (int i=0;i<children.getLength();i++) {
     Node child=children.item(i);
     if (child.getNodeType()==Node.ELEMENT_NODE) {
      if (tag.indexOf(child.getNodeName())>-1) {
       if (index==0) return child;
       index--;
      }
      NodeList childElems=((Element)child).getElementsByTagName(match);
      int childElemsLen=childElems.getLength();
      if (index<childElemsLen) return childElems.item(index);
      index-=childElemsLen;
     }
    }
   }
   return null;
  }
  public int getLength() {
   int counter=0;
   for (int i=0;i<children.getLength();i++) {
    Node child=children.item(i);
    if (child.getNodeType()==Node.ELEMENT_NODE) {
     if (tag.indexOf(child.getNodeName())>-1) {
      counter++;
     }
     counter+=((Element)child).getElementsByTagName(match).getLength();
    }
   }
   return counter;
  }
 }
 public String getTagName() {return "dir";}
 public Attr getAttributeNode(String name) {return (Attr)attributes.getNamedItem(name);}
 public Attr setAttributeNode(Attr newAttr) {return (Attr)attributes.setNamedItem(newAttr);}
 public NodeList getElementsByTagName(String name) {return new descendantsNodeList(children,name);}
 public NodeList getChildNodes() {return children;}
 public Node getFirstChild() {return children.item(0);}
 public Node getLastChild() {return children.item(children.getLength()-1);}
 public NamedNodeMap getAttributes() {return attributes;}
 public Node insertBefore(Node newNode, Node refNode) {
  if (newNode!=null && newNode.getNodeType()!=Node.CDATA_SECTION_NODE) {
   if (newNode.getParentNode()!=null) newNode.getParentNode().removeChild(newNode);
   children.insert(newNode,refNode);
  }
  return newNode;
 }
 public Node replaceChild(Node oldNode, Node newNode) {
  insertBefore(newNode,oldNode);
  children.remove(oldNode);
  return newNode;
 }
 public Node removeChild(Node oldNode) {
  children.remove(oldNode);
  ((fsNode)oldNode).setParentNode(null);
  return oldNode;
 }
 public Node appendChild(Node newNode) {
  insertBefore(newNode,null);
  return newNode;
 }
 public boolean hasChildNodes() {return children.getLength()>0;}
 public Node cloneNode(boolean deep) {
  Element de=owner.createElement("dir");
  de.setAttribute("name",dirName);
  if (deep) {
   for (Node siby=getFirstChild();(siby!=null);siby=siby.getNextSibling()) {
    de.appendChild(siby.cloneNode(true));
   }
  }
  return de;
 }
}

class fsRootElement extends fsDirElement {
 fsRootElement(fsDocument ownerDocument) {super(ownerDocument,"myRoot");}
 public String getTagName() {return "fs";}
 public Attr getAttributeNode(String name) {return null;}
 public Attr setAttributeNode(Attr newAttr) {return null;}
 public Attr removeAttributeNode(Attr oldAttr) {return null;}
 public Node getPreviousSibling() {return null;}
 public Node getNextSibling() {return owner.getDoctype();}
 public NamedNodeMap getAttributes() {return new dummyNamedNodeMap();}
}

interface fsAttrBackend {
 String getValue(String propName);
 String setValue(String propName,String propValue);
}

class fsAttr extends fsNode implements Attr {
 fsAttrBackend backend;
 String name,value;
 fsAttr (fsDocument ownerDoc,String name) {
  owner=ownerDoc;
  this.name=name;
  value="";
  backend=null;
 }
 void setBackend(fsAttrBackend b) {
  backend=b;
  setValue(value);
 }
 public String getNodeName() {return getName();}
 public String getNodeValue() {return getValue();}
 public void setNodeValue(String val) {setValue(val);}
 public short getNodeType() {return Node.ATTRIBUTE_NODE;}
 public Node cloneNode(boolean dummy) {
  Attr a=owner.createAttribute(getName());
  a.setValue(getValue());
  return a;
 }
 public String getName() {return name;}
 public boolean getSpecified() {return true;}
 public String getValue() {
  if (backend!=null) value=backend.getValue(name);
  return value;
 }
 public void setValue(String v) {
  if (backend!=null) v=backend.setValue(name,v);
  value=v;
 }
}

/// The dummy DOMs ///

abstract class fsCharacterData extends fsNode implements CharacterData {
 public String getData() {return new String("");}
 public void setData(String dummy) {}
 public int getLength() {return 0;}
 public String substringData(int dummy1, int dummy2) {return getData();}
 public void appendData(String dummy) {}
 public void insertData(int dummy1, String dummy2) {}
 public void deleteData(int dummy1, int dummy2) {}
 public void replaceData(int dummy1, int dummy2, String dummy3) {}
}

class fsProcessingInstruction extends fsNode implements ProcessingInstruction {
 fsProcessingInstruction(fsDocument ownerDoc) {
  owner=ownerDoc;
  setParentNode(owner);
 }
 public short getNodeType() {return Node.PROCESSING_INSTRUCTION_NODE;}
 public String getTarget() {return new String("");}
 public String getData() {return getTarget();}
 public void setData(String dummy) {}
 public Node cloneNode(boolean dummy) {return new fsProcessingInstruction(owner);}
}

class fsEntityReference extends fsNode implements EntityReference {
 fsEntityReference(fsDocument ownerDoc) {owner=ownerDoc;}
 public short getNodeType() {return Node.ENTITY_REFERENCE_NODE;}
 public Node cloneNode(boolean dummy) {return new fsEntityReference(owner);}
}

class fsDocumentType extends fsNode implements DocumentType {
 fsDocumentType (fsDocument ownerDocument) {
  owner=ownerDocument;
  setParentNode(owner);
 }
 public String getNodeName() {return getName();}
 public short getNodeType() {return Node.DOCUMENT_TYPE_NODE;}
 public String getName() {return new String("fs");}
 public Node getPreviousSibling() {return owner.getDocumentElement();}
 public NamedNodeMap getEntities() {return new dummyNamedNodeMap();}
 public NamedNodeMap getNotations() {return new dummyNamedNodeMap();}
}

class fsComment extends fsCharacterData implements Comment {
 fsComment(fsDocument ownerDoc) {owner=ownerDoc;}
 public String getNodeName() {return new String("#comment");}
 public String getNodeValue() {return new String("");}
 public short getNodeType() {return Node.COMMENT_NODE;}
 public Node cloneNode(boolean dummy) {return new fsComment(owner);}
}

class fsDocumentFragment extends fsRootElement implements DocumentFragment {
 fsDocumentFragment(fsDocument ownerDoc) {super(ownerDoc);}
}

class fsDOMImplementation extends fsNode implements DOMImplementation {
 fsDOMImplementation(fsDocument ownerDoc) {owner=ownerDoc;}
 public boolean hasFeature(String feature,String version) {
  return (feature.equals("XML") && version.equals("1.0"));
 }
}

class dummyNamedNodeMap extends fsNode implements NamedNodeMap {
 public Node getNamedItem(String name) {return null;}
 public Node setNamedItem(Node arg) {return null;}
 public Node removeNamedItem(String name) {return null;}
 public Node item(int index) {return null;}
 public int getLength() {return 0;}
}

class dummyNodeList extends fsNode implements NodeList {
 public Node item(int index) {return null;}
 public int getLength() {return 0;}
}

////end////// THE DOM IMPLEMENTATION ////////end/////////
}

class ResourceManager {
 private File pointer;
 private File temp;
 private boolean isTemp=false;
 private String tempName;
 public static final int BUFFERSIZE=8192;
 private void copy(File source,File outDump) {
  if (source!=null) {
   try {
    int fileLength=(int)source.length();
    byte[] buffer=new byte[BUFFERSIZE];
    FileInputStream fis=new FileInputStream(source);
    FileOutputStream fos=new FileOutputStream(outDump);
    for (int i=0;i<fileLength/BUFFERSIZE;i++) {
     fis.read(buffer);
     fos.write(buffer);
    }
    fis.read(buffer,0,fileLength%BUFFERSIZE);
    fos.write(buffer,0,fileLength%BUFFERSIZE);
    fis.close();
    fos.close();
   }
   catch (Exception e) {}
  }
 }
 ResourceManager(File temp,File myFile,String tempName) {
  pointer=myFile;
  this.temp=temp;
  this.tempName=tempName;
  if (pointer==null) getWriteHandle();
 }
 public File getReadHandle() {return pointer;}
 public File getWriteHandle() {
  if (!isTemp) {
   isTemp=true;
   File newPointer=new File(temp,tempName);
   copy(pointer,newPointer);
   pointer=newPointer;
   try {new FileOutputStream(pointer).close();}
   catch (IOException e) {}
  }
  return pointer;
 }
 public void commitTo(File outDump) {
  outDump.delete();
  if (isTemp) {
   pointer.renameTo(outDump);
  }
  else {
   copy(pointer,outDump);
  }
 }
 private static int index=0;
 private static File defTemp;
 static void setTemp(File temp) {defTemp=temp;}
 static ResourceManager getRM(File myFile) {
  return new ResourceManager(defTemp,myFile,"FS"+(index++));
 }
}
