/*
 * Copyright (c) 2006-10 LAMP/EPFL
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**  A text formatter for scala programs. To run it, use either 
 *   one of the following commands
 *   
 *
 *   java verbfilterScala <in.src> <in.tex>
 *   scala verbfilterScala <in.src> <in.tex>
 *  
 * This would translate the file `in.src' into `in.tex'. 
 * The program highlighs reserved words and line comments.
 * It understands the following formatting commands:
 *  
 *   \=    tab set
 *   \>    advance to next set tab
 *   $     toggle math mode
 *   \R    red color, translates to \color{red}
 *   \B    blue color, translates to \color{blue}
 *   \S    black color, translates to \color{black}
 *   \G    greenn color, translates to \color{green}
 *   \\    a single backslash `\'
 *   \$    a dollar sign `$'
 *  
 *  The source file `in.src' needs to contain definitions of the following 
 *  LaTeX commands:
 *
 *    \vem            Highlight keyword
 *    \prog{#1}       argument is inline program code
 *    \linecomment    Highlight line comment
 *
 *  It also needs to define the following environment:
 *
 *    program         for multi-line program code.
 *   
 *  The program environment needs to refine LaTeX's tabbing environment, so that
 *  \= and \> are defined.
 *  
 *  Here are examples of these LaTeX definitions:
 *
 * \newcommand{\prog}[1]     {\darkbluetext{\sf #1}}
 * \newcommand{\vem}         {\color{violet}}
 * \newcommand{\linecomment} {\color{darkgreen}}
 * \newenvironment{program}  {\begin{quote} \small \darkblue \sf \begin{tabbing}}
 *                           {\end{tabbing} \end{quote}}
 * \definecolor{darkblue}{rgb}{0, 0, 0.4}
 * \definecolor{darkgreen}{rgb}{0, 0.3, 0.0}
 */
object verbfilterScala {

  /** The number of hard whitespace characters for each leading blank in a line.
   *  settable by option -iD where 0<=D<=9. Default = 2
   */
  var leadingWhiteSpaceString: String = "~"

  /** A switch to control whether programs are extracted. Settable by option -x
   *  Default = false
   */
  var extractProgs = false

  /** Increment for tabulator characters 
   */
  val TABINC = 8

  /** Output commands, to be defined as latex commands in source file
   */
  val beginVerbString = "\\prog{"  
  val endVerbString = "}"
  val beginProgramString = "\\begin{program}"
  val endProgramString = "\\end{program}\\noindent"
  val reservedString  = "\\vem "
  val lineCommentString = "\\linecomment"
  val systemOutputString = "\\systemoutput"

  /** Strings significant for input 
   */
  val beginVerbatim = "\\begin{verbatim}"
  val endVerbatim = "\\end{verbatim}"
  val verb = "\\verb"

  /** Reserved words, sorted alphabetically 
   */
  val reserved = Array(
    "abstract", "case", "catch", "class", "def", 
    "do", "else", "extends", "false", "final", "finally",
    "for", "if", "implicit", "import", "match", "mixin",
    "new", "null", "object", "override", "package", 
    "private", "protected", "requires", "return", "sealed", 
    "super", "this", "throw", "trait", "true", 
    "try", "type", "val", "var", "while", 
    "with", "yield")

  /** line counters */
  var lineCount = 0
  var verbLine = 0

  /** output toggles */
  var writeProg = false
  var hidden = false

  /** The current program output file */
  var programOut: java.io.OutputStream = null
  var programOutIsOpen = false
  var progCount = 0

  /** The current template buffer */
  var template: Array[Byte] = null

  /** The template insertion position */
  var templatePos: Int = -1  

  val CR: Char = 0x0D

  def closeProgramOut() {
    if (programOutIsOpen) {
      if (extractProgs && templatePos >= 0) {
        var i = templatePos
        while (i < template.length) {
          programOut.write(template(i))
          i = i + 1
        }
      }
      templatePos = -1
      programOut.close()
      programOutIsOpen = false
      System.err.println("]")
    }
  }

  def openProgramOut(name: String, i: Int) {
    if (extractProgs) {
      closeProgramOut()
      programOut = new java.io.BufferedOutputStream(new java.io.FileOutputStream(name))
      programOutIsOpen = true
      System.err.print("[writing "+name)
    }
  }

  def zeroFilled(width: Int, n: Int): String = 
    if (width == 0) "" else zeroFilled(width - 1, n / 10) + (n % 10)
  
  def newName() = {
    progCount = progCount + 1
    "test/"+zeroFilled(4, progCount)+".scala"
  }

  def openProgramIn(name: String) {
    if (extractProgs) {
      template = readFile(name)
      var i = 0
      while (!((template(i) == '.') && (template(i+1) == '.') && (template(i+2) == '.'))) {
        programOut.write(template(i))
        i = i + 1
      }
      templatePos = i + 3
    }
  }

  /** the main process method */
  def process(buf: Array[Byte], out: java.io.OutputStream) {

    def puts(s: String) { 
      if (!hidden) for (c <- s) out.write(c) 
    }
    def putc(c: Byte) { 
      if (!hidden) out.write(c) 
    }

    def startsWith(offset: Int, s: String): Boolean = {
      var i = 0
      while (i < s.length && offset+i < buf.length && buf(offset+i) == s.charAt(i)) i = i + 1
      i == s.length
    }
    
    def startsWithSome(offset: Int, ss: List[String]): Boolean = 
      ss exists { s => startsWith(offset, s) }
    
    def compare(offset: Int, key: String): Int = {
      var i = offset
      var j = 0
      val l = key.length
      while (i < buf.length && j < l) {
        val bch = buf(i).toChar
        val kch = key.charAt(j)
        if (bch < kch) return -1
        else if (bch > kch) return 1
        i = i + 1
        j = j + 1
      }
      if (j < l) -1
      else if (i < buf.length &&
               ('A' <= buf(i) && buf(i) <= 'Z' ||
                'a' <= buf(i) && buf(i) <= 'z' ||
                '0' <= buf(i) && buf(i) <= '9' ||
                buf(i) == '_')) 1
      else 0
    }

    def keyIndex(i: Int): Int = {
      var lo = 0
      var hi = reserved.length - 1
      while (lo <= hi) {
        val mid = (hi + lo) / 2
        val diff = compare(i, reserved(mid))
        if (diff < 0) hi = mid - 1
        else if (diff > 0) lo = mid + 1
        else return mid
      }
      -1
    }

    def processVerbatim(i: Int, end: String): Int = {

      val END = end.charAt(0)
      var systemOutput = false

      def next(i: Int): Int = {
        if (writeProg && !systemOutput) programOut.write(buf(i))
        i + 1
      }

      def nextN(i: Int, n: Int): Int = 
        if (n == 0) i else nextN(next(i), n - 1)

      def processProgramOut(sbuf: StringBuffer, i: Int): Int =
        if (buf(i) == ']') { openProgramOut(sbuf.toString, i); i+1 }
        else { sbuf append buf(i).toChar; processProgramOut(sbuf, i+1) }

      def processProgramIn(sbuf: StringBuffer, i: Int): Int =
        if (buf(i) == ']') { openProgramIn(sbuf.toString); i+1 }
        else { sbuf append buf(i).toChar; processProgramIn(sbuf, i+1) }

      def processVerbatimOptions(i: Int): Int = 
        if (end.length == 1) i
        else if (buf(i) == '.' && buf(i+1) == '.' && buf(i+2) == '.') i+3
        else if (buf(i) == '[') {
          if (buf(i+1) == ']') { writeProg = false; i+2 }
          else processProgramOut(new StringBuffer, i+1)
        } else { 
          if (writeProg) openProgramOut(newName(), i)
          i 
        }

      def skipLeadingBlanks(i: Int): Int = 
        if (buf(i) == ' ' || buf(i) == CR) skipLeadingBlanks(i+1) 
        else if (buf(i) == '\n') { lineCount = lineCount + 1; i+1 }
        else i

      def processLineComment(i: Int): Int =
        if (buf(i) == '\n' || startsWith(i, end)) i
        else { putc(buf(i)); processLineComment(next(i)) }

      def processMath(i: Int): Int = {
        putc(buf(i))
        if (buf(i) == '$') i+1 else processMath(i+1)
      }

      def processOutputLine(i: Int): Int = {
        hidden = true
        val j = processLineComment(i)
        hidden = false
        j
      }

      def processLeadingWhitespace(i: Int): Int = {
        def loop(i: Int, col: Int): Int = {
          if (buf(i) == ' ') {
            puts(leadingWhiteSpaceString)
            loop(next(i), col+1)
          } else if (buf(i) == '\t') {
            var c = col
            do {
              puts(leadingWhiteSpaceString); c = c+1
            } while (c % TABINC != 0)
            loop(next(i), c)
          } else {
            i
          }
        }
        if (buf(i) == '%') {
          if (buf(i+1) == '[') {
            skipLeadingBlanks(processProgramIn(new StringBuffer, i+2))
          } else {
            processOutputLine(i+1)
          }
        } else if (buf(i) == '!') {
          puts(systemOutputString)
          systemOutput = true
          loop(i+1, 0)
        } else {
          loop(i, 0)
        }
      }

      def processOtherNewline(i: Int): Int =
        if (buf(i) == '\n') {
          puts("[0.5em]")
          lineCount = lineCount+1
          next(i)
        } else i

      def processCode(pre: String, i: Int): Int = {
        puts(pre)
        buf(i) match {
          case END if startsWith(i, end) =>
            i + end.length
          case '\n' => 
            systemOutput = false
            lineCount = lineCount+1
            if (startsWith(i+1, end)) processCode("\n", next(i))
            else {
              puts("\\\\\n")
              processCode("", 
                processLeadingWhitespace(processOtherNewline(next(i))))
            }
          case ' ' => 
            processCode("~", next(i))
          case '^' => 
            processCode("\\^{}$", next(i))
          case '&' => 
            processCode("\\&", next(i))
          case '%' => 
            processCode("$\\%$", next(i))
          case '_' => 
            processCode("\\_", next(i))
          case '~' => 
            processCode("\\~{}", next(i))
          case '{' => 
            processCode("\\{", next(i))
          case '}' => 
            processCode("\\}", next(i))
/*
          case '*' => 
            processCode("$*$", next(i))
          case '[' => 
            processCode("$[$", next(i))
          case ']' => 
            processCode("$]$", next(i))
          case '(' => 
            processCode("$($", next(i))
          case ')' => 
            processCode("$)$", next(i))
          case ':' => 
            if (i > 0 && Character.isJavaIdentifierPart(buf(i-1).toChar))
              puts("\\,")
            processCode("{\\rm :}", next(i))
          case '<' => 
            if (buf(i+1) == '=') processCode("$\\leq$", next(next(i)))
            else if (buf(i+1) == '-') processCode("$\\leftarrow$", next(next(i)))
            else if (buf(i+1) == '<') processCode("$<\\!$", next(i))
            else processCode("$<$", next(i))
          case '>' => 
            if (buf(i+1) == '=') processCode("$\\geq$", next(next(i)))
            else if (buf(i+1) == '>') processCode("$>\\!$", next(i))
            else processCode("$>$", next(i))
          case '=' => 
            if (buf(i+1) == '=') processCode("$==$", next(next(i)))
            else if (buf(i+1) == '>') processCode("$\\Rightarrow$", next(next(i)))
            else processCode("=", next(i))
          case '/' =>
            if (buf(i+1) == '/') { 
              puts(lineCommentString+"//"); 
              processCode("", processLineComment(next(next(i)))) 
            } else processCode("/", next(i))
          case '-' => 
            if (buf(i+1) == '>') processCode("$\\rightarrow$", next(next(i)))
            else processCode("$-$", next(i))
          case '+' => 
            processCode("$+$", next(i))
          case '|' => 
            processCode("$\\,|$", next(i))
          case '#' => 
            processCode("\\#", next(i))
*/
          case '\\' => 
            if (buf(i+1) == '=') processCode("\\=", i+2)
            else if (buf(i+1) == '>') processCode("\\>", i+2)
            else if (buf(i+1) == '$') processCode("\\$", next(i+1))
            else if (buf(i+1) == 'R') processCode("\\color{red}", i+2)
            else if (buf(i+1) == 'S') processCode("\\color{black}", i+2)
            else if (buf(i+1) == 'B') processCode("\\color{blue}", i+2)
            else if (buf(i+1) == 'G') processCode("\\color{green}", i+2)
            else if (buf(i+1) == 'g') processCode("\\color{grey}", i+2)
            else processCode("$\\backslash$", next(i))
          case '$' => 
            puts("$")
            processCode("", processMath(i+1))
          case _ =>
            if (i == 0 || !Character.isJavaIdentifierPart(buf(i-1).toChar)) {
              val k = keyIndex(i)
              val rs = if (systemOutput) "" else reservedString
              if (k >= 0) processCode("{"+rs+reserved(k)+"}", nextN(i, reserved(k).length))
              else processCode(buf(i).toChar.toString, next(i))
            } else processCode(buf(i).toChar.toString, next(i))
        }
      }
      verbLine = lineCount
      writeProg = extractProgs && end.length > 1
      val j = processCode("", processLeadingWhitespace(skipLeadingBlanks(processVerbatimOptions(i))))
      writeProg = false
      j
    }

    def processTeXComment(i: Int): Int = 
      if (i == buf.length || buf(i) == '\n') i
      else { putc(buf(i)); processTeXComment(i+1) }
    
    def processTex(i: Int): Int = {
      if (i == buf.length) {
        i
      } else if (buf(i) == '%') {
        processTex(processTeXComment(i))
      } else if (startsWith(i, beginVerbatim)) {
	puts(beginProgramString)
        val j = processVerbatim(i + beginVerbatim.length, endVerbatim)
        puts(endProgramString)
        processTex(j)
      } else if (startsWith(i, "@@")) {
	puts(beginProgramString)
        val j = processVerbatim(i + 2, "@@")
        puts(endProgramString)
        processTex(j)
      } else if (startsWith(i, verb)) {
	puts(beginVerbString)
        val j = i + verb.length
        val end = new String(Array(buf(j).toChar))
        val k = processVerbatim(j+1, end)
        puts(endVerbString)
        processTex(k)
      } else if (startsWith(i, "\\@")) {
	puts(beginVerbString)
        val k = processVerbatim(i + 2, "@")
        puts(endVerbString)
        processTex(k)
      } else {
        if (buf(i) == '\n') lineCount = lineCount+1
        putc(buf(i))
        processTex(i+1)
      }
    }

    puts("""%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
           |% DO NOT EDIT.  Automatically generated file! %
           |%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
           |""".stripMargin)
    lineCount = 1
    processTex(0)
  }

  def readFile(name: String): Array[Byte] = {
    val in = new java.io.FileInputStream(new java.io.File(name))
    val buf = new Array[Byte](in.available())
    in.read(buf, 0, buf.length)
    in.close()
    buf
  }

  def main(args: Array[String]) {
    def processOptions(i: Int): Int =
      if (i < args.length && args(i) == "-x") { 
        extractProgs = true; 
        processOptions(i + 1)
      } else if (i < args.length && (args(i) startsWith "-i") && (args(i).length == 3) && args(i)(2).isDigit) {
        leadingWhiteSpaceString = "~~~~~~~~~~".substring(0, args(i).substring(2).toInt)
        processOptions(i + 1)
      } else {
        i
      }
    val start = processOptions(0)
    if (start + 2 != args.length) {
      val classname = new Error().getStackTrace()(0).getClassName().replace('$', ' ')
      System.err.println(
        "Usage: java " + classname + "[-x -i(1-9)] <source-file> <destination-file>\n"+
        "Options:   -x       extract program files\n"+
        "           -iD      replace leading blanks by D hard spaces `~' where 0 <= D <= 9")
        System.exit(1)
    }
    System.out.println("[verbfilterScala "+args(start)+" "+args(start+1)+"]")
    val buf = readFile(args(start))
    val out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(args(start+1)))
    try {
      process(buf, out)
    } catch {
      case ex: Throwable =>
        System.err.println ("\n **** error at line " + verbLine)
        throw ex
    }
    out.close()
    closeProgramOut()
  }
}

