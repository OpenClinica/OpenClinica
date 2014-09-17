<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:my="my:my">
    <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <my:delNs>
        <ns>http://www.openclinica.org/ns/openclinica_odm/v1.3</ns>
    </my:delNs>

    <xsl:variable name="vdelNS"
                  select="document('')/*/my:delNs/*"/>

    <xsl:template match="*">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
            <xsl:copy-of select="namespace::*[not(.=$vdelNS)]"/>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template priority="10" match=
            "*[namespace-uri()=document('')/*/my:delNs/*]">
    </xsl:template>

    <xsl:template match=
            "@*[namespace-uri()=document('')/*/my:delNs/*]">
    </xsl:template>
</xsl:stylesheet>
