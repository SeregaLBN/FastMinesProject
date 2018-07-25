<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='2.0'
   xmlns:svg='http://www.w3.org/2000/svg'
   xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
   xmlns:xs='http://www.w3.org/2001/XMLSchema'
   xmlns:fn='http://www.w3.org/2005/xpath-functions'
   xmlns:android='http://schemas.android.com/apk/res/android'
   xmlns:aapt='http://schemas.android.com/aapt'
>
   <!-- xmlns='http://www.w3.org/1999/xhtml' -->

   <xsl:output method='xml' version='1.0' encoding='UTF-8' omit-xml-declaration='yes' indent='yes'/>

   <xsl:template match='/'>
      <xsl:element name='vector'>
<!--
         <xsl:attribute name='xmlns:android' namespace='http://www.w3.org/1999/xhtml' >http://schemas.android.com/apk/res/android</xsl:attribute>
         <xsl:attribute name='xmlns:aapt'    namespace='http://www.w3.org/1999/xhtml' >http://schemas.android.com/aapt</xsl:attribute>
-->
         <xsl:attribute name='android:width'          ><xsl:value-of select='//@width'  />dp</xsl:attribute>
         <xsl:attribute name='android:height'         ><xsl:value-of select='//@height' />dp</xsl:attribute>
         <xsl:attribute name='android:viewportWidth'  ><xsl:value-of select='//@width'  /></xsl:attribute>
         <xsl:attribute name='android:viewportHeight' ><xsl:value-of select='//@height' /></xsl:attribute>

         <xsl:for-each select="//svg:path">
            <xsl:call-template name='pathHandler'>
               <xsl:with-param name='pathNode' select='.' />
            </xsl:call-template>
         </xsl:for-each>

      </xsl:element>
   </xsl:template>

   <xsl:template name="pathHandler">
      <xsl:param name='pathNode'/>

      <xsl:element name='path'>

         <xsl:attribute name='android:pathData'><xsl:value-of select='$pathNode/@d' /></xsl:attribute>

         <!--   url(#ownRays20)   =>   ownRays20   -->
         <xsl:variable name='linearGradientId' select='substring(./@fill, 6, string-length(./@fill)-6)' />
         <xsl:variable name='linearGradientNode' select='//svg:linearGradient[@id = $linearGradientId]' />
         <xsl:variable name='stopFirstNode' select='$linearGradientNode/svg:stop[@offset="0%"]' />
         <xsl:variable name='stopLastNode'  select='$linearGradientNode/svg:stop[@offset="100%"]' />

         <xsl:element name='aapt:attr'>
            <xsl:attribute name='name'>android:fillColor</xsl:attribute>
            <xsl:element name='gradient'>
               <xsl:attribute name='android:startX'><xsl:value-of select='$linearGradientNode/@x1' /></xsl:attribute>
               <xsl:attribute name='android:startY'><xsl:value-of select='$linearGradientNode/@y1' /></xsl:attribute>
               <xsl:attribute name='android:endX'  ><xsl:value-of select='$linearGradientNode/@x2' /></xsl:attribute>
               <xsl:attribute name='android:endY'  ><xsl:value-of select='$linearGradientNode/@y2' /></xsl:attribute>
               <xsl:attribute name='android:type'>linear</xsl:attribute>
               <xsl:element name='item'>
                  <xsl:attribute name='android:color'><xsl:value-of select='$stopFirstNode/@stop-color' /></xsl:attribute>
                  <xsl:attribute name='android:offset'>0.0</xsl:attribute>
               </xsl:element>
               <xsl:element name='item'>
                  <xsl:choose>
                     <xsl:when test='$stopLastNode/@stop-opacity'>
                        <xsl:attribute name='android:color'>#00<xsl:value-of select='substring($stopLastNode/@stop-color,2)' /></xsl:attribute>
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:attribute name='android:color'><xsl:value-of select='$stopLastNode/@stop-color' /></xsl:attribute>
                     </xsl:otherwise>
                  </xsl:choose>
                  <xsl:attribute name='android:offset'>1.0</xsl:attribute>
               </xsl:element>
            </xsl:element>
         </xsl:element>
      </xsl:element>
   </xsl:template>

</xsl:stylesheet>
<!--
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"
   android:width="108dp"
   android:height="108dp"
   android:viewportWidth="108"
   android:viewportHeight="108">
    ...
   <path android:pathData="M16.1943 16.1943 L42.9391 27.2677 L38.3614 38.3614 Z">
      <aapt:attr name="android:fillColor">
         <gradient android:startX="42.9391" android:startY="27.2677" android:endX="35.1041" android:endY="35.1027" android:type="linear">
            <item android:color="#FF00BF" android:offset="0.0"></item>
            <item android:color="#001F00FF" android:offset="1.0"></item>
         </gradient>
      </aapt:attr>
   </path>
</vector>
-->