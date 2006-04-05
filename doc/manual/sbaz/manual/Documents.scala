package sbaz.manual

object Documents {
  abstract class AbstractText {
    def &(more: AbstractText) = SeqText(this, more)
  }

  case class SeqText(components: AbstractText*) extends AbstractText
  case class Text(text: String) extends AbstractText
  case object MDash extends AbstractText
  case class Emph(contents: AbstractText) extends AbstractText
  case class Mono(text: String) extends AbstractText
  case class Quote(contents: AbstractText) extends AbstractText
  case class BlockQuote(contents: AbstractText) extends AbstractText
  implicit def str2text(str: String) = Text(str)
  case class CodeSample(text: String) extends AbstractText


  case class DefnItem(header: String, text: AbstractText)

  abstract class Paragraph
  case class TextParagraph(text: AbstractText) extends Paragraph
  implicit def text2para(txt: AbstractText):Paragraph = TextParagraph(txt)
  implicit def str2para(str: String) = text2para(str2text(str))

  case class BulletList(items: AbstractText*) extends Paragraph
  case class NumberedList(items: AbstractText*) extends Paragraph
  case class TitledPara(title: String, text: AbstractText) extends Paragraph

  case class EmbeddedSection(section: Section) extends Paragraph
  implicit def section2Para(section: Section) = EmbeddedSection(section)

  case class Section(title: String, paragraphs: Paragraph*)

  class Document {
    var title: String = null
    var author: String = null
    var date: String = null
    var sections: List[Section] = Nil
  }
}
