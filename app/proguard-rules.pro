# POI and Log4j rules to handle MethodHandle issues on API < 26
-dontwarn org.apache.poi.poifs.nio.CleanerUtil
-dontwarn org.apache.logging.log4j.util.ServiceLoaderUtil
-dontwarn java.lang.invoke.MethodHandle
-dontwarn java.lang.invoke.MethodHandles$Lookup

# Keep POI and Log4j
-keep class org.apache.poi.** { *; }
-keep class org.apache.logging.log4j.** { *; }

# General POI / XMLBeans dependencies
-dontwarn javax.xml.stream.**
-dontwarn com.sun.msv.**
-dontwarn org.relaxng.datatype.**
-dontwarn org.apache.xmlbeans.**

# Ignore missing AWT/Swing/OSGi/BouncyCastle classes which POI tries to use but are not needed for simple XLSX
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.swing.**
-dontwarn javax.xml.crypto.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.pdfbox.**
-dontwarn de.rototor.pdfbox.**
-dontwarn org.bouncycastle.**
-dontwarn org.ietf.jgss.**
-dontwarn org.osgi.framework.**
-dontwarn org.w3c.dom.svg.**
-dontwarn org.w3c.dom.events.**
-dontwarn org.w3c.dom.traversal.**
-dontwarn org.apache.jcp.xml.dsig.internal.dom.**
-dontwarn org.apache.xml.security.**
