package xdb;

/**
** Enables access to data within standardized XML documents without
** DOM. The type of the XML document has to comply to this pseudo DTD:<br><code><p>
** &lt!ELEMENT recordset (record*)&gt<br>
** &lt!ELEMENT record (tag*)&gt<br>
** &lt!ELEMENT tag (#PCDATA)&gt<br>
** </p></code>
** To build a valid DTD, just replace tag with the valid property-tags
** which you records will use.
*/
public interface recordset {
/** @return The number of records within the recordset */
 int size();
/** @param index The zero-bound index of the record to be pointed to */
 void moveTo(int index);
/** Jumps to the first record if existent */
 void moveFirst();
/** Jumps to the next record within the recordset if existent */
 void moveNext();
/** @return The zero-bound index of the record pointed to */
 int getIndex();
/** @return True iff the record pointed to is out of bounds */
 boolean eof();
/** @return The property names of the actual record */
 String[] getPropertyNames();
/** @return The actual record's property value or an empty String if property not existent */
 String getPropertyValue(String propertyName);
}