package xdb;

/** <b>XDB</b> is able to operate with every XML-like resource, as far as it comes with
** an implementation of this interface. An implementation has to come along with a fabric
** functionality for constructing DOM1 conformant objects.<br>
** It is guaranteed that the document is first loaded to the proxy before the other
** primitives are called.
*/

public interface documentProxy {
/** Creates an empty document
** @param resource The URI of the document
** @param rootNode The label of the root node of the document
*/
 void create(URI resource,String rootNode) throws xdbException;
/** Inits the proxy with an empty document
** @return The default URI for this kind document
*/
 URI load() throws xdbException;
/** Inits the proxy with a specified resource */
 void load(URI resource) throws xdbException;
/** Commits the work done with the proxy and stores the document persistently at the specified location */
 void save(URI resource) throws xdbException;
/** Gets the corresponding DOM1 representation of the document */
 org.w3c.dom.Document getDOM();
}