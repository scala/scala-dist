package sbaz.manual
import Documents._

object EmitHtml {
  val out = Console

  def escape(text: String) = {
    text.replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
  }

  def emitSection(section: Section, depth: int): Unit = {
    def emitText(text: AbstractText): Unit =
      text match {
	case seq:SeqText => seq.components.foreach(emitText)

	case Text(text) => out.print(escape(text))

	case MDash => out.print("&#8212;")

	case Emph(text) => {
	  out.print("<em>")
	  emitText(text)
	  out.print("</em>")
	}
	
	case Mono(text) => {
	  out.print("<code>")
	  out.print(escape(text))
	  out.print("</code>")
	}


	case Quote(text) => {
	  out.print("\"")
	  emitText(text)
	  out.print("\"")
	}

        case BlockQuote(text) => {
	  out.println("<blockquote>")
	  emitText(text)
	  out.println("</blockquote>")
	}

	case CodeSample(code) => {
	  out.print("<blockquote><pre>")
	  out.print(escape(code))
	  out.println("</pre></blockquote>")
	}
      }

    def emitParagraph(para: Paragraph) =
      para match {
	case TextParagraph(text) => {
	  out.print("<p>")
	  emitText(text)
	}
	
	case lst:BulletList => {
	  out.println("<ul>")
	  for(val item <- lst.items) {
	    out.print("<li>")
	    emitText(item)
	  }
	  out.println("</ul>")
	}

	case lst:NumberedList => {
	  out.println("<ol>")
	  for(val item <- lst.items) {
	    out.print("<li>")
	    emitText(item)
	  }
	  out.println("</ol>")
	}

	case TitledPara(title, text) => {
	  out.println("<p><strong>" + escape(title) + "</strong>")
	  emitText(text)
	}


	case EmbeddedSection(sect) => emitSection(sect, depth+1)
      }

    out.println("<h" + depth + ">" + section.title +
                "</h" + depth + ">")
    section.paragraphs.foreach(emitParagraph)
  }

  def emitDocument(document: Document) = {
    out.println("<html>")


    out.println("<head>")
    if(document.title != null)
      out.println("<title>" + document.title + "</title>")
    out.println("</head>")


    out.println("<body>")
    
    if(document.title != null)
      out.println("<h1 align=\"center\">" +
                  document.title +
                  "</h1>")

    if(document.author != null)
      out.println("<p align=\"center\">" + document.author + "</p>")

    if(document.date != null)
      out.println("<p align=\"center\">" + document.date + "</p>")


    document.sections.foreach(s => emitSection(s, 1))

    out.println("</body></h1>")
  }
  
  def main(args: Array[String]) = {
    emitDocument(Manual.wholeThing)
  }
}
