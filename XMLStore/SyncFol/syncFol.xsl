<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:variable name="refModified">0</xsl:variable>
 <xsl:template match="fs"><fs><xsl:apply-templates/></fs></xsl:template>
 <xsl:template match="dir"><dir name="{@name}"><xsl:apply-templates/></dir></xsl:template>
 <xsl:template match="file[@lastModified &gt; $refModified]">
  <xsl:copy-of select="."/>
 </xsl:template>
</xsl:stylesheet>