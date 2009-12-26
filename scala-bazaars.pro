#
# This ProGuard configuration file illustrates how to process applications.
# Usage:
#     java -jar proguard.jar @applications.pro
#

-printseeds
-dontobfuscate
-dontoptimize

# Must support Java5
-target 5

# Specify the input jars, output jars, and library jars.
-injars build/sbaz.jar
-injars /home/matlikj/workspace/scala/dists/latest/lib/scala-library.jar(!META-INF/MANIFEST.MF)
-outjars build/scala-bazaars.jar

-libraryjars /usr/lib/jvm/java-6-sun/jre/lib/rt.jar
-libraryjars lib/servlet-api.jar
-libraryjars lib/jetty-6.1.19.jar
-libraryjars lib/jetty-util-6.1.19.jar

#-dontskipnonpubliclibraryclasses
-keepclasseswithmembers public class sbaz.clui.CommandLine {
    public static void main(java.lang.String[]);
}

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# The case object creates $Empty$ but not $Empty
-dontwarn scala.collection.immutable.RedBlack$Empty

# Entry point
#-keep public class sbaz.clui.CommandLine

# Dynamically loaded
-keep public class org.xml.sax.EntityResolver

