package sbaz.manual
import Documents._

object EmitLatex {
  val out = Console

  def escape(text: String) = {
    (text.replaceAll("\\{","\\\\{")
         .replaceAll("}","\\\\}")
         .replaceAll("_","\\\\_"))
  }

  val seccmds = List(
    "\\section",
    "\\subsection",
    "\\subsubsection")
    

  def emitSection(section: Section, depth: int): Unit = {
    def emitText(text: AbstractText): Unit =
      text match {
	case seq:SeqText => seq.components.foreach(emitText)

	case Text(text) => out.print(escape(text))

	case MDash => out.print("---")

	case Emph(text) => {
	  out.print("\\emph{")
	  emitText(text)
	  out.print("}")
	}
	
	case Mono(text) => {
	  out.print("\\texttt{")
	  out.print(escape(text))
	  out.print("}")
	}


	case Quote(text) => {
	  out.print("``")
	  emitText(text)
	  out.print("''")
	}

        case BlockQuote(text) => {
	  out.println("\\begin{quote}")
	  emitText(text)
	  out.println("\\end{quote}")
	}

	case CodeSample(code) => {
          out.println("\\begin{quote}")
	  out.println("\\begin{verbatim}")
	  out.print(code)
	  out.println("\\end{verbatim}")
          out.println("\\end{quote}")
	}
      }

    def emitParagraph(para: Paragraph) =
      para match {
	case TextParagraph(text) => {
	  emitText(text)
          out.println
	  out.println
	}
	
	case lst:BulletList => {
	  out.println("\\begin{itemize}")
	  for(val item <- lst.items) {
	    out.print("\\item ")
	    emitText(item)
	    out.println
	  }
	  out.println("\\end{itemize}")
	}

	case lst:NumberedList => {
	  out.println("\\begin{enumerate}")
	  for(val item <- lst.items) {
	    out.print("\\item ")
	    emitText(item)
	    out.println
	  }
	  out.println("\\end{enumerate}")
	}

	case TitledPara(title, text) => {
	  out.println
	  out.print("\\paragraph*{" + escape(title) + "} ")
	  emitText(text)
	  out.println
	  out.println
	}


	case EmbeddedSection(sect) => emitSection(sect, depth+1)
      }

    
    out.print(seccmds(depth))
    out.println("{" + section.title + "}")
    section.paragraphs.foreach(emitParagraph)
  }

  def emitDocument(document: Document) = {
    out.println("\\documentclass{article}")
    out.println("\\usepackage{mathpazo}")
    out.println("\\usepackage[scaled]{helvet}")
    out.println("\\topmargin=0in")
    out.println("\\oddsidemargin=0in")
    out.println("\\evensidemargin=0in")
    out.println("\\textwidth=6in")
    out.println("\\textheight=8.5in")


    out.println("\\begin{document}")
    
    if(document.title != null)
      out.println("\\title{" + document.title + "}")

    if(document.author != null)
      out.println("\\author{" + document.author + "}")

    if(document.date != null)
      out.println("\\date{" + document.date + "}")

    out.println("\\maketitle")

    document.sections.foreach(s => emitSection(s, 0))

    out.println("\\end{document}")
  }
  
  def main(args: Array[String]) = {
    emitDocument(Manual.wholeThing)
  }
}
