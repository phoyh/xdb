package xdb;

/**
** The objectifizer of an XML or XML-like document. It also acts as facade/proxy
** of the XML/DOM/XPath/XSLT implementations.
*/

public interface XML {
/** @param resource The desired URI of the XML document */
 void setURI(URI resource);
/** @return The URI of the XML document */
 URI getURI();
/** @return A recordset representation of the XML documentation. Eventual future changes to the XML document leave the recordset unaffected.
** @exception xdbException XML document not conform to the recordset DTD
*/
 recordset getRecordset() throws xdbException;
/** Reads an attribute or element content from the document.
** @param xPointer The path of the property. No wildcards and recursing is allowed however.
** @return The property's value
** @exception xdbException Path pointed to not found
*/
 String load(String xPointer) throws xdbException;
/** Writes an attribute or element content to the document.
** @param xPointer The path of the property. No wildcards and recursing is allowed however.
** @param value The new property's value
*/
 void store(String xPointer,String value);
/** @return The document representation as specified in DOM1.0
*/
 org.w3c.dom.Document getDOM();
/** Persistently stores the XML document at its URI (which has to be a file-URI)
** @exception xdbException Storing not successful
*/
 void save() throws xdbException;
/** Retrieves and loads the XML document at its URI
** @exception xdbException Retrieving or parsing not successful; the object still represents the prior successfully loaded document.
*/
 void load() throws xdbException;
}