
/* To compile this document, run "scala path-to-this-file".  The
 * HTML will be emitted on stdout.
 */

val title = "Writing Scala Compiler Plugins"
val author = "Lex Spoon"
val date = "June 26, 2008"

import xml._

val chapIntro =
  <chapter>
    <title>Introduction</title>

    <p>This tutorial briefly walks you through writing a plugin for
    the Scala compiler.  It does not go into depth, but just shows
    you the very basics needed to write a plugin and hook it into
    the Scala compiler.  For the details on how to make your plugin
    accomplish some task, you must consult other documentation.  At
    the time of writing, your best option is to look at other plugins
    and to study existing phases within the compiler source code.</p>

    <p>The command lines given in this tutorial assume you are using a
    Unixy Bourne shell and that you have the <tt>SCALA_HOME</tt>
    environment variable pointing to the root of your Scala
    installation.</p>

  </chapter>

  val chapWhenToWrite = <chapter>
    <title>When to write a plugin</title>

    <p>A compiler plugin lets you modify the behavior of the compiler
    itself without needing to change the main Scala distribution.  You
    should not actually need to modify the Scala compiler very
    frequently, because Scala's light, flexible syntax will frequently
    allow you to provide a better solution using a clever library.
    There are some times, though, where a compiler modification is
    the best choice even for Scala.  Here are a few examples:</p>

    <ul>

      <li>You might want to add additional compile-time checks,
      as with Gilad Bracha's pluggable types point of view.</li>

      <li>You might want to add compile-time optimizations for a
      heavily used library.</li>

      <li>You might want to rewrite Scala syntax into an entirely
      different, custom meaning.  Beware of this kind of plugin,
      however, because any code relying on the plugin will be
      unusable when the plugin is not available.</li>

    </ul>

    <p>Compiler plugins lets you make and distribute a compiler
    modification without needing to change the main Scala
    distribution.  You can instead write a compiler plugin that embodies
    your compiler modification, and then anyone you distribute the plugin
    to will be able to access your modification.  These modifications
    fall into two large categories with the current plugin support:</p>

    <ul>
 
      <li>You can add a phase to the compiler, thus adding extra checks
      or extra tree rewrites that apply after type checking has finished.</li>

      <li>You can tell the compiler type-checking information about an
      annotation that is intended to be applied to types.</li>

    </ul>

    <p>This tutorial walks through the most basic aspects of
    writing compiler plugins.  For more information, you will
    need to read the source of other people's compiler plugins
    and of the compiler itself.</p>
  </chapter>


val chapPlugin =
  <chapter>
    <title>A simple plugin, beginning to end</title>

    <p>A plugin is a kind of compiler component that lives in a
    separate jar file from the main compiler.  The compiler can
    then load that plugin and gain extra functionality.</p>

    <p>This section walks through writing a simple plugin.  Suppose
    you want to write a plugin that detects division by zero in
    obvious cases.  For example, suppose someone compiles a silly
    program like this:</p>

    <code>{
"""object Test {
  val five = 5
  val amount = five / 0
  def main(args: Array[String]) {
    println(amount)
  }
}
"""}</code>

    <p>Your plugin could generate an error like this:</p>
<code>{
"""Test.scala:3: error: definitely division by zero
  val amount = five / 0
                    ^
one error found
"""}</code>

    <p>There are several steps to making the plugin.  First you need
    to write and compile the source of the plugin itself.  Here is
    the source code for it:</p>
    <code>{
"""package localhost
import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

class DivByZero(val global: Global) extends Plugin {
  import global._

  val name = "divbyzero"
  val description = "checks for division by zero"
  val components = List[PluginComponent](Component)
  
  private object Component extends PluginComponent {
    val global = DivByZero.this.global
    val runsAfter = "refchecks"
    val phaseName = DivByZero.this.name
    def newPhase(prev: Phase) = new DivByZeroPhase(prev)    
  }
  
  private class DivByZeroPhase(prev: Phase) extends Phase(prev) {
    def name = DivByZero.this.name
    def run {
      for (unit <- currentRun.units;
           tree @ Apply(Select(rcvr, nme.DIV), List(Literal(Constant(0)))) <- unit.body;
           if rcvr.tpe <:< definitions.IntClass.tpe) 
      {
        unit.error(tree.pos, "definitely division by zero")
      }
    }
  }
}
"""}</code>

    <p>There is a lot going on even with this simple plugin.  Here
    are a few aspects of note.</p>

    <ul>

      <li>The plugin is described by a top-level class that inherits
      from <tt>Plugin</tt>, takes a <tt>Global</tt> as a constructor
      parameter, and exports that parameter as a <tt>val</tt>
      named <tt>global</tt>.</li>

      <li>The plugin must define one or more component objects that
      inherits from <tt>PluginComponent</tt>.  In this case the sole
      component is the nested <tt>Component</tt> object.  The components
      of a plugin are listed in the <tt>components</tt> field.</li>

      <li>Each component must define <tt>newPhase</tt> method that
      creates the component's sole compiler phase.  That phase will be
      inserted just after the specified compiler phase, in this
      case <tt>refchecks</tt>.</li>

      <li>Each phase must define a method <tt>run</tt> that does
      whatever you desire.  Usually the phase will iterate through
      the compilation units of the current run and examine the
      trees within each unit.</li>

    </ul>
 
    <p>That's the plugin itself.  The next thing you need to do is
    write a plugin descriptor for it.  A plugin descriptor is a small
    XML file giving the name and the entry point for the plugin.  In
    this case it should look as follows:</p>

    <code>{
"""<plugin>
  <name>divbyzero</name>
  <classname>localhost.DivByZero</classname>
</plugin>"""}</code>

    <p>The name of the plugin should match what is specified in your
    <tt>Plugin</tt> subclass, and the <tt>classname</tt> of the plugin
    is the name of the <tt>Plugin</tt> subclass.  All other information
    about your plugin is in the <tt>Plugin</tt> subclass.</p>

    <p>Put this XML in a file named <tt>scalac-plugin.xml</tt>
    and then create a jar with that file plus your compiled code:</p>

    <code>{"""
mkdir classes
fsc -d classes ExPlugin.scala
cp scalac-plugin.xml classes
(cd classes; jar cf ../divbyzero.jar .)
"""}</code>

   <p>Now you can use your plugin with <tt>scalac</tt> by adding
   the <tt>-Xplugin:</tt> option:</p>

<code>{
"""$ scalac -Xplugin:divbyzero.jar Test.scala
Test.scala:3: error: definitely division by zero
  val amount = five / 0
                    ^
one error found
$ 
"""}</code>

    <p>When you are happy with how the plugin behaves, you can install
    it by putting it in the
    directory <tt>misc/scala-devel/plugins</tt> within your Scala
    installation.  You can install your plugin with the following
    commands:</p>

<code>{
"""$ mkdir -p $SCALA_HOME/misc/scala-devel/plugins
$ cp divbyzero.jar $SCALA_HOME/misc/scala-devel/plugins
$ 
"""}</code>

    <p>Now the plugin will be loaded by default:</p>
<code>{
"""$ scalac Test.scala
Test.scala:3: error: definitely division by zero
  val amount = five / 0
                    ^
one error found
$ """}</code>

  </chapter>

  val chapOptions = <chapter>
    <title>Useful Compiler Options</title>

    <p>The previous section walked you through the basics of
    writing, using, and installing a compiler plugin.  There are
    several compiler options related to plugins that you should know
    about.</p>

    <ul>
    
      <li><tt>-Xshow-phases</tt><dash/>show a list of all
      compiler phases, including ones that come from plugins.</li>

      <li><tt>-Xplugin-list</tt><dash/>show a list of all
      loaded plugins.</li>

      <li><tt>-Xplugin-disable:</tt><dash/>disable a plugin.  Whenever the
      compiler encounters a plugin descriptor for the named plugin, it
      will skip over it and not even load the associated <tt>Plugin</tt>
      subclass.</li>
				   
      <li><tt>-Xplugin-require:</tt><dash/>require that a plugin is loaded
      or else abort.  This is mostly useful in build scripts.</li>

      <li><tt>-Xpluginsdir</tt><dash/>specify the directory the compiler
      will scan to load plugins.  Again, this is mostly useful for
      build scripts.</li>
      
    </ul>

    <p>The following options are not specific to writing plugins, but
    are frequently used by plugin writers:</p>

    <ul>
    
      <li><tt>-Xprint:</tt><dash/>print out the compiler trees
      immediately after the specified phase runs.</li>

      <li><tt>-Ybrowse:</tt><dash/>like <tt>-Xprint:</tt>, but instead
      of printing the trees, opens a Swing-based GUI for browsing
      the trees.</li>

    </ul>

  </chapter>

  val chapArgumentProcessing =  <chapter>
    <title>Adding your own options</title>
    
    <p>A compiler plugin can provide command-line options to the
    user.  All such option start with <tt>-P:</tt> followed
    by the name of the plugin.  For example, <tt>-P:foo:bar</tt>
    will pass option <tt>bar</tt> to plugin <tt>foo</tt>.</p>

    <p>To add options to your own plugin, you must do two things.
    First, add a <tt>processOptions</tt> method to your <tt>Plugin</tt>
    subclass with the following type signature:</p>

    <code>
{"""override def processOptions(
    options: List[String],
    error: String => Unit)
"""}</code>


    <p>The compiler will invoke this method with all options the
    users specifies for your plugin.  For convenience, the common
    prefix of <tt>-P:</tt> followed by your plugin name will already
    be stripped from all of the options passed in.</p>

    <p>The second thing you should do is add a help message for your
    plugins options.  All you need to do is override the <tt>val</tt>
    named <tt>optionsHelp</tt>.  The string you specify will be printed
    out as part of the compiler's <tt>-help</tt> output.  By convention,
    each option is printed on one line.  The option itself is printed
    starting in column 3, and the description of the option is printed
    starting in column 31.  Type <tt>scalac -help</tt> to make sure
    you got the help string looking right.</p>

    <p>Here is a complete plugin that has an option.  This plugin has
    no behavior other than to print out its option.</p>

    <code>{
"""package localhost
import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

class Silly(val global: Global) extends Plugin {
  import global._

  val name = "silly"
  val description = "goose"
  val components = List[PluginComponent](Component)
  
  var level = 1000000
  
  override def processOptions(options: List[String], error: String => Unit) {
    for (option <- options) {
      if (option.startsWith("level:")) {
        level = option.substring("level:".length).toInt
      } else {
        error("Option not understood: "+option)
      }
    }
  }
  
  override val optionsHelp: Option[String] = Some(
    "  -P:silly:level:n             set the silliness to level n")
  
  private object Component extends PluginComponent {
    val global = Silly.this.global
    val runsAfter = "refchecks"
    val phaseName = Silly.this.name
    def newPhase(prev: Phase) = new SillyPhase(prev)    
  }
  
  private class SillyPhase(prev: Phase) extends Phase(prev) {
    def name = Silly.this.name
    def run {
      println("Silliness level: " + level)
    }
  }
}
"""}</code>

  </chapter>


val wholeDocument =
  <document>
    {chapIntro}
    {chapWhenToWrite}
    {chapPlugin}
    {chapOptions}
    {chapArgumentProcessing}
  </document>


object GenHTML {
  def error(node: Node) = {
    throw new Exception(node.toString)
  }

  def escapeText(text: String): String = {
    val escapes = (Map.empty[Char,String]
      + ('<' -> "lt")
      + ('>' -> "gt")
      + ('&' -> "amp"))

    val buf = new StringBuffer

    for (c <- text)
      escapes.get(c) match {
        case None => buf append c
        case Some(esc) => buf append ("&"+esc+";")
      }

    buf.toString
  }

  def emit(node: Node) {
    node match {
      case _:Text => print(escapeText(node.text))

      case node:Elem =>
        node.label match {
  	  case "code" => print("<pre>"); print(escapeText(node.text)); println("</pre>")

          case "dash" => print("&mdash;")

          case "p"   => print("<p>"); emitAll(node.child); println("</p>")

          case "title" => // skip it

	  case "tt" =>
            print("<code>")
            emitAll(node.child)
            print("</code>")

          case "ul" =>
            print("<ul>")
            for (item <- node \ "li") {
              print("<li>")
              emitAll(item.child)
              println()
            }
            println("</ul>")

          case _ => error(node)
        }

      case _ => error(node)
    }
  }      

  def emitAll(nodes: Seq[Node]) = nodes.foreach(emit)

  def emitChapter(chap: Node) = {
    val title = (chap \ "title").text
    println("<h2>" + title + "</h2>")
    emitAll(chap.child)
  }

  def emitDocument(doc: Node) {
    println("<html><head>")
    println("<title>" + title + "</title>")
    println("<body>")
    println("<h1>" + title + "</h1>")
    println("<strong> Author: " + author + "</strong><br>")
    println("<strong> Date: " + date + "</strong><br>")

    for (chap <- doc \ "chapter")
      emitChapter(chap)
  }
}

GenHTML.emitDocument(wholeDocument)


//  LocalWords:  scala stdout TODO val Plugins xml chapIntro plugin plugins
//  LocalWords:  Unixy chapWhenToWrite Scala's Gilad Bracha's chapPlugin misc
//  LocalWords:  PluginComponent newPhase refchecks classname scalac Xplugin
//  LocalWords:  chapOptions Xshow Xpluginsdir Xprint Ybrowse foo optionsHelp
//  LocalWords:  chapArgumentProcessing processOptions wholeDocument GenHTML
//  LocalWords:  def toString escapeText lt gt buf StringBuffer esc Elem tt
//  LocalWords:  println emitAll ul Seq foreach emitChapter 
