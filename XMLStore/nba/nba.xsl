<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:variable name="bgColor">green</xsl:variable>
 <xsl:template match="nba">
  <html>
   <body>
    <xsl:apply-templates/>
   </body>
  </html>
 </xsl:template>
 <xsl:template match="team">
  <table>
   <tr><th><i>
    <xsl:value-of select="@name"/>
   </i></th><td><u><xsl:value-of select="@shirt"/></u></td></tr>
   <xsl:apply-templates/>
  </table>
 </xsl:template>
 <xsl:template match="player">
  <tr>
   <th bgColor="{$bgColor}">
    <xsl:value-of select="@name"/>
   </th>
   <xsl:apply-templates/>
  </tr>
 </xsl:template>
 <xsl:template match="age|height">
  <td>
   <xsl:value-of select="."/>
  </td>
 </xsl:template>
</xsl:stylesheet>
