package xdb;

/**
** The objectifizer of an XSLT document. The parameters of transformations
** can be set through the according primitives.
** Along with their
** declaration within the sheet, a default value can be assigned, too.
*/

public interface XSLT extends XML {
/** @param parameterName The parameter variable within the XSLT
** @param parameterValue The value to be set to
** @exception xdbException The sheet does not have such a parameter variable
*/
 void setParameter(String parameterName,String parameterValue) throws xdbException;
/** @param parameterName The parameter variable within the XSLT
** @return The value of the parameter value
** @exception xdbException The sheet does not have such a parameter variable
*/
 String getParameter(String parameterName) throws xdbException;
/** @param sourceDocument The XML to be transformed
** @return The resulting XML document
*/
 XML transform(XML sourceDocument) throws xdbException;
/** @param sourceDocument The XML to be transformed
** @param destinationDocument The empty XML which will hold the result. It really has to be an empty document. Conventional XML documents cannot be placed here.
** @return The resulting XML document
*/
 void transform(XML sourceDocument,XML destinationDocument) throws xdbException;
}