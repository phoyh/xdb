package xdb;

/**
** The driver interface of the XML database.<br>
** <br>
** <u>Terminology</u>:<table border=0 cellspacing=6><tr><td valign=top>
** <a name="client"><i>client</i> or <i>XDB client</i></a>
** </td><td valign=top>-</td><td valign=top>
** The entity <b>using</b> the XDB driver.
** </td></tr><tr><td valign=top>
** <a name="resource"><i>document resource</i></a>
** </td><td valign=top>-</td><td valign=top>
** A resource being made up by a document. Such a resource has an <a href="#uri"><b>URI</b></a>.
** </td></tr><tr><td valign=top>
** <a name="logical"><i>logical</i></a>
** </td><td valign=top>-</td><td valign=top>
** A view on something existing, which is usually more convenient by
** added features and hidden problems. It is top-down by being defined
** by the clients' needs. It is supposed that the existing thing on which
** the view is built upon, is bottom-up due to its close relation to
** the underlying technology and its constraints.
** </td></tr><tr><td valign=top>
** <a name="uri"><i>URI</i></a>
** </td><td valign=top>-</td><td valign=top>
** <u>U</u>niform <u>R</u>esource <u>I</u>dentifier (<b>URI</b>).
** The set of valid <b>URI</b>s is isomorph to the set of
** resources.
** </td></tr><tr><td valign=top>
** <a name="xdb"><i>XDB</i></a>
** </td><td valign=top>-</td><td valign=top>
** The abbreviation of <u>X</u>ML <u>d</u>ata<u>b</u>ase.
** It has to be conform to the requirement specification given.
** </td></tr><tr><td valign=top>
** <a name="processor"><i>XML/XSLT processor</i></a>
** </td><td valign=top>-</td><td valign=top>
** A processor responsible for parsing of XML/XSLT documents. In addition,
** XSL Transformations (<b>XSLT</b>) are supported. The interface of
** the processor is <b>SAX</b> or <b>DOM</b>, or this interface is provided
** by an adapter-service upon the processor.
** </td></tr></table>
** <br>

** The XML database, and hence the driver, have the following <b>requirements</b>:<ul><li>
** Version1.0
** <ul><li>
** The emphasis lies on the <i>adequate</i> functionality to be provided.
** Therefore, performance is only a minor concern. Nevertheless, one day
** a performant implementation shall be built, too, with neither the interface
** nor the behaviour having to be changed.
** </li><li>
** It shall be possible to exchange and to upgrade underlying technology
** without any changes within the <a href="#client">XDB clients</a>. They shan't bother about
** which <a href="#processor">XML/XSLT processor</a> being used and about how the access to <a href="#resource">document
** resources</a> is regulated.
** </li><li>
** There has to be a mechanism to retrieve <a href="#resource">document resources</a>, to interact
** with them by reading and writing, and to store them persistently.
** </li><li>
** It should be possible to transform an XML by using predifined XSLT. Such
** kind of transformations shall be able to take parameters.
** </li><li>
** The usage of <b>DTD</b>s in XML documents shall be supported.
** </li><li>
** Concurrent interaction with <a href="#resource">document resources</a> should be supported, at
** least on a <a href="#logical">logical</a> level.
** </li><li>
** The interface and the classes exposed to the <a href="#client">client</a>
** shall be as simple as possible. No convenience functions or classes
** have to be provided.
** </li><li>
** The context of an interaction of a group of <a href="#client">clients</a> who
** cooperate by using the <b>XDB</b> has not to be managed and be cared of
** by the <b>XDB</b> itself. The <a href="#client">clients</a> are fully
** responsible for ensuring correct interaction based on the functionality
** and restrictions of the <b>XDB</b>.
** </li><li>
** No support required for query interfaces built upon <b>RDBMS</b>/<b>OODBMS</b> syntaxes
** (f.e. SQL queries haven't to be supported).
** </li></ul></li><li>
** Version1.1
** <ul><li>
** It shall be possible to interact with any kind of document type which supports DOM
** </li></ul>
** </li></ul>
** <br>

** The resulting design decision are as follows:<ul><li>
** Version1.0
** <ul><li>
** The level of concurrence is hidden from the interface. However, it
** is possible to acquire possession of <a href="#resource">resources</a>.
** It is recommended that the <a href="#client">clients</a> are very
** cautious about the control over their <a href="#resource">resources</a>,
** so that, in case of doubt, they take possession over them.
** The granularity of possession is on <a href="#resource">resource</a> level.
** The problem of deadlocks which comes together with the allocation of
** <a href="#resource">resources</a>, has to be solved on <a href="#client">
** client</a> level (f.e. by ordering the <a href="#resource">resources</a>
** and applying a two-phase-commit protocol). Though the problem is out of scope for <b>XDB</b>,
** a mechanism is provided to ensure that the <a href="#client">client</a>
** threads are not blocked: After allocation failure the control goes back
** to the callee with an indication.
** </li><li>
** An XML document is checked against a <b>DTD</b> while being loaded,
** if the <b>DTD</b> is provided. In case of errors, the <b>DTD</b> is
** ignored.
** </li><li>
** Allow the <a href="#client">clients</a> to operate on XSLT <b>and</b> DOM
** in order to work with their XML documents in the most appropriate manner.
** Even more rigoriously, the <a href="#client">clients</a> can decide to
** work exclusively on only one of then, XSLT or DOM. For exclusive work on
** XSLT, a mechanism for data input from XML documents is provided by
** a specialized class, which transforms a standardized document into
** a data structure accessible from the <a href="#client">clients</a>.
** </li><li>
** The <b>DOM</b> level 1 interface is directly exposed to the <a href="#client">
** clients</a>. It is specified in <u>org.w3c.dom</u>. This is acceptable,
** because <b>DOM1</b> is stable and not vendor specific. Furthermore,
** the following versions of <b>DOM</b> include <b>DOM1</b> as subset, so that
** <a href="#client">client</a> maintenance is not necessairy (but perhaps
** desirable), in case of higher versions of <b>DOM</b> supported.
** </li><li>
** Primitives for load/store and read/write XML documents are provided. This
** implies that the <a href="#client">clients</a> have explicit control of
** the <a href="#resource">resources</a>.
** </li><li>
** Decomposition of the major entities of the <b>XDB</b>.
** </li><li>
** The parameters of the are not bound to transformation primitives, but to
** the XSLs themselves. In order to refer to the parameters, one can regard
** them as XSLT variables within the XSLT.
** </li></ul></li><li>
** Version1.1
** <ul><li>
** <a href="#client">Clients</a> who want to work with their own non-XML
** documents, have to implement an interface, thus building a proxy for
** their documents with the feature of a factory of DOM compliant object hierarchies.
** The DOM referred to is DOM1. One sample implementation for XML documents exists and is
** internally used as default.
** </li></ul>
** </li></ul>

** @version 1.1
** @author Phoyh
*/

public interface xmlDriver {
/** @param resource The URI of the resource to be allocated
** @return True iff the resource was successfully allocated
** @exception xdbException This kind of resource is not allocatable
*/
 boolean allocResource(URI resource) throws xdbException;
/** Frees a resource from allocation. It is not checked whether the client is the owner, so that foreign allocation and freeing is possible
** @param resource The URI of the resource to be freed
** @exception xdbException This kind of resource is not allocatable
*/
 void freeResource(URI resource) throws xdbException;
/** Loads an XML document
** @param resource The URI of the XML document resource
** @return The XML document accessing object
** @exception xdbException The XML object couldn't be created.
*/
 XML getXML(URI resource) throws xdbException;
/** Loads a document with an arbitrary type
** @param resource The URI of the XML document resource
** @param proxy The proxy of the resource
** @return The document accessing object
** @exception xdbException The XML object couldn't be created.
*/
 XML getXML(URI resource,documentProxy proxy) throws xdbException;
/** Loads an XSLT document
** @param resource The URI of the XSLT document resource
** @return The XSLT document accessing object
** @exception xdbException The XSLT object couldn't be created.
*/
 XSLT getXSLT(URI resource) throws xdbException;
/** Creates a new XML document
** @param resource The URI of the XML document resource; the resource has to be a file system resource
** @param rootName The tag name of the root node.
** @exception xdbException The resource couldn't be created.
*/
 void createXML(URI resource, String rootName) throws xdbException;
/** Creates a new document of arbitrary type
** @param resource The URI of the document resource; the resource has to be a file system resource
** @param proxy The proxy of the resource
** @param rootName The tag name of the root node.
** @exception xdbException The resource couldn't be created.
*/
 void createXML(URI resource, documentProxy proxy, String rootName) throws xdbException;
}
