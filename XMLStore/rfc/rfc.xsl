<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:variable name="bgColor">#000080</xsl:variable>
 <xsl:variable name="fgColor">#8888FF</xsl:variable>
 <xsl:template name="reference">
  <a>
   <xsl:attribute name="href">
    #<xsl:value-of select="@tagType"/>-<xsl:value-of select="@name"/>
   </xsl:attribute>
   <b><xsl:value-of select="@tagType"/>:</b> <xsl:value-of select="summary"/>
  </a>
 </xsl:template>
 <xsl:template name="related">
  <xsl:param name="tagType"/>
  <xsl:param name="name"/>
  <xsl:param name="relationship"/>
  <xsl:for-each select="/*/*/*[@tagType=$tagType and @name=$name]">
   <i><xsl:value-of select="$relationship"/> </i>
   <xsl:call-template name="reference"/>
   <br/>
  </xsl:for-each>
 </xsl:template>
 <xsl:template match="rfc">
  <html>
   <head>
    <title>RFC to <xsl:value-of select="@to"/></title>
   </head>
   <body bgColor="{$bgColor}" text="{$fgColor}" link="{$fgColor}" vlink="{$fgColor}" alink="{$fgColor}">
    <center><table border="0"><tr>
     <th align="right">Submitted from:</th>
     <td><xsl:value-of select="@author"/></td>
    </tr><tr>
     <th align="right">Submitted to:</th>
     <td><xsl:value-of select="@to"/></td>
    </tr><tr>
     <th align="right">Date of submission:</th>
     <td><xsl:value-of select="@date"/></td>
    </tr></table></center>
    <hr/>
    <h2 align="center"><u>Table of content</u></h2>
    <ul>
     <xsl:for-each select="*">
      <li>
       <b><xsl:value-of select="@tagType"/></b>
       <ul>
        <xsl:for-each select="*">
         <li>
          <xsl:call-template name="reference"/>
         </li>
        </xsl:for-each>
       </ul>
      </li>
     </xsl:for-each>
    </ul>
    <hr/><br/>
    <xsl:for-each select="/*/*/*">
     <xsl:call-template name="item"/>
    </xsl:for-each>
   </body>
  </html>
 </xsl:template>
 <xsl:template name="item">
  <table border="0">
  <tr>
   <td><table><tr>
    <a>
     <xsl:attribute name="name">
      <xsl:value-of select="@tagType"/>-<xsl:value-of select="@name"/>
     </xsl:attribute>
     <th>
      <xsl:attribute name="bgColor">
       <xsl:if test="@importance='LOW'">green</xsl:if>
       <xsl:if test="@importance='MEDIUM'">#60600</xsl:if>
       <xsl:if test="@importance='HIGH'">#A00000</xsl:if>
      </xsl:attribute>
      <font color="#CCCCC"><xsl:value-of select="@importance"/></font>
     </th>
     <th>
      <xsl:value-of select="@tagType"/>: <xsl:value-of select="summary"/>
     </th>
    </a>
   </tr></table></td>
  </tr>
  <tr><td>
   <xsl:value-of select="description"/>
  </td></tr>
  <tr><td>
   <xsl:for-each select="related/*">
    <xsl:call-template name="related">
     <xsl:with-param name="tagType"><xsl:value-of select="@tagType"/></xsl:with-param>
     <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
     <xsl:with-param name="relationship">refers to</xsl:with-param>
    </xsl:call-template>
   </xsl:for-each>
  </td></tr><tr><td>
   <xsl:variable name="ownTagType"><xsl:value-of select="@tagType"/></xsl:variable>
   <xsl:variable name="ownName"><xsl:value-of select="@name"/></xsl:variable>
   <xsl:for-each select="//related/*[@tagType=$ownTagType and @name=$ownName]">
    <xsl:call-template name="related">
     <xsl:with-param name="tagType"><xsl:value-of select="../../@tagType"/></xsl:with-param>
     <xsl:with-param name="name"><xsl:value-of select="../../@name"/></xsl:with-param>
     <xsl:with-param name="relationship">is refered by</xsl:with-param>
    </xsl:call-template>
   </xsl:for-each>
  </td></tr>
  </table><br/><br/>
 </xsl:template>
</xsl:stylesheet>
