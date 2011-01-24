<xsl:stylesheet version="1.0" 	xmlns:odm="http://www.cdisc.org/ns/odm/v1.3"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance"
                xmlns:def="http://www.cdisc.org/ns/def/v1.0" xmlns:xlink="http://www.w3c.org/1999/xlink"
                xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
	 exclude-result-prefixes="xlink"
	 xmlns:exsl="http://exslt.org/common">
	 
	 
	 <!-- standard copy template -->	 




  <xsl:template  name="copyTemplate" match="node()|@*">
  		
		<xsl:copy>
		<xsl:apply-templates select="@*|*|text()"/>
		</xsl:copy>
	</xsl:template>


<!-- template for changing any datatype to 'text',datatype needs to be sent as a parameter. -->



<xsl:template priority="3"  match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@DataType='partialDate']">


<!--<xsl:call-template name="toText">-->
<!--<xsl:with-param name="toDataType" select="'text'"></xsl:with-param>-->
<!--</xsl:call-template>-->
<xsl:attribute name="DataType">
                        
                        <xsl:value-of select="'text'"></xsl:value-of>
                </xsl:attribute>
              
</xsl:template>


     
<!-- Namespace uri needs to be changed to cdisc 1.2 -->		
 <xsl:template name="namespaceTo1.2" priority="1" match="//*[namespace-uri()='http://www.cdisc.org/ns/odm/v1.3' or namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' ] ">
        <xsl:element name="{local-name()}" namespace="http://www.cdisc.org/ns/odm/v1.2" >
            <xsl:apply-templates select="@*|*|text()" />
        </xsl:element>
    </xsl:template>
    
   
    
 <xsl:template priority="2" match="@ODMVersion">
      <xsl:attribute name="ODMVersion">1.2</xsl:attribute>
   </xsl:template>


		
		
	
</xsl:stylesheet>