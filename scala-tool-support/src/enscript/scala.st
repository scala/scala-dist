/**
 * Name       : scala
 * Description: Scala programming language (scala-lang.org)
 * Author     : Stephane Micheloud <stephane.micheloud@epfl.ch>
 */

state html_tag extends Highlight
{
  />/ {
    language_print ($0);
    return;
  }
  /\"/ {
    builtin_face (false);
    string_face (true);
    language_print ($0);
    call (c_string);
    string_face (false);
    builtin_face (true);
  }
}

state scala extends HighlightEntry
{
  /* Comments. */
  /\/\*/ {
    comment_face (true);
    language_print ($0);
    call (c_comment);
    comment_face (false);
  }
  /\/\// {
    comment_face (true);
    language_print ($0);
    call (eat_one_line);
    comment_face (false);
  }

  /* String constants. */
  /\"/ {
    string_face (true);
    language_print ($0);
    call (c_string);
    string_face (false);
  }

  /* Character constants. */
  /'.'|'\\\\.'/ {
    string_face (true);
    language_print ($0);
    string_face (false);
  }

  /* XML expressions. */  /*
  /</ {
    builtin_face (true);
    language_print ($0);
    call (html_tag);
    builtin_face (false);
  } */

  /* Annotations. */
  /\b@(BeanProperty|SerialVersionUID|cloneable|deprecated\
|remote|serializable|t(hrows|ransient)|un(checked|sealed)|volatile)\b/ {
    keyword_face (true);
    language_print ($0);
    keyword_face (false);
  }

  /* Keywords.
     (build-re '(abstract case catch class def do else extends false final
     finally for if implicit import match new null object override package
     private protected requires return sealed super this throw trait true
     try type val var while with yield))
  */
  /\b(abstract|c(a(se|tch)|lass)\
|d(ef|o)|e(lse|xtends)|f(alse|inal(|ly)|or(|Some))\
|i(f|mp(licit|ort))|lazy|match|n(ew|ull)|o(bject|verride)\
|p(ackage|r(ivate|otected))|re(quires|turn)\
|s(ealed|uper)|t(h(is|row)|r(ait|ue|y)|ype)\
|va(l|r)|w(hile|ith)|yield)\b/ {
    keyword_face (true);
    language_print ($0);
    keyword_face (false);
  }
}

/*
Local variables:
mode: c
End:
*/
