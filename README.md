[XML](https://en.wikipedia.org/wiki/XML) database management system ([DBMS](https://en.wikipedia.org/wiki/Database)) - short **XDB** - with built-in support for
* Schema validation ([DTD](https://en.wikipedia.org/wiki/Document_type_definition))
* Transformations ([XSLT](https://en.wikipedia.org/wiki/XSLT))
* Logical XML documents on which DTD & XSLT can be transparently applied
    * *[File System Adapter](src/xdb/addIns/fsProxyImpl.java):* Treats the subtree of a [file system](https://en.wikipedia.org/wiki/File_system) directory as a logical document

Be sure to use [Java 1.3](https://en.wikipedia.org/wiki/Java_version_history#J2SE_1.3) as `org.w3c.dom` is included in later Java versions, clashing with the included [jar](https://en.wikipedia.org/wiki/JAR_(file_format)) for version *1.3*. Also, no querying support ([XPath](https://en.wikipedia.org/wiki/XPath) or similar) is provided as this is not the focus area. Only [URI](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier)-based access to documents is provided. For reading/writing the content of XML documents without XSLT, standard [DOM](https://en.wikipedia.org/wiki/Document_Object_Model) means need to be used.

Example consumers:
* [RequestForComments](src/RequestForComments.java): Simple consumer making use of built-in XSLT capabilities. Comes with rather complex [DTD](XMLStore/rfc/RFC.dtd).
* [SynchronizeFolders](src/SynchronizeFolders.java): Makes full use of the file system adapter in order to articulate file system sync operation as XSLT `<xsl:copy-of select="."/>`, even [depending on last change date](XMLStore/SyncFol/syncFol.xsl). Also, sets up [to-do list](XMLStore/SyncFol/syncFol.xml) based on DOM primitives.

There are also further [example data](XMLStore/nba/) of XML and XSLT.
