;;; -*-Emacs-Lisp-*-
;;; scala-mode-feature-templates.el - 

;; Copyright (C) 2008 Scala Dev Team at EPFL
;; Authors: See AUTHORS file
;; Keywords: scala languages oop
;; $Id$

;;; License

;; SCALA LICENSE
;;  
;; Copyright (c) 2002-2008 EPFL, Lausanne, unless otherwise specified.
;; All rights reserved.
;;  
;; This software was developed by the Programming Methods Laboratory of the
;; Swiss Federal Institute of Technology (EPFL), Lausanne, Switzerland.
;;  
;; Permission to use, copy, modify, and distribute this software in source
;; or binary form for any purpose with or without fee is hereby granted,
;; provided that the following conditions are met:
;;  
;;    1. Redistributions of source code must retain the above copyright
;;       notice, this list of conditions and the following disclaimer.
;;  
;;    2. Redistributions in binary form must reproduce the above copyright
;;       notice, this list of conditions and the following disclaimer in the
;;       documentation and/or other materials provided with the distribution.
;;  
;;    3. Neither the name of the EPFL nor the names of its contributors
;;       may be used to endorse or promote products derived from this
;;       software without specific prior written permission.
;;  
;;  
;; THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
;; ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
;; IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
;; ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
;; FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
;; DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
;; SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
;; CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
;; LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
;; OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
;; SUCH DAMAGE.

;;; Code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(provide 'scala-mode-feature-templates)

(eval-when-compile
  (require 'tempo)
  (require 'scala-mode-variables))

(setq tempo-interactive t)

;;; Helper functions

(defun scala-tmpl-helper-name (qst)
  ""
  (let
      (tmpl-name (read-string qst))
    (if (string= tmpl-name "") 
	"NONAME"
      tmpl-name)))


(defun scala-tmpl-helper-extend ()
  ""
  (let
      (tmpl-name (read-string "Extend: "))
    (progn
      (if (string= tmpl-name "") 
	  (setq tmpl-name "")
	(setq tmpl-name (concat " extends " tmpl-name)))
      tmpl-name )))


(defun scala-tmpl-helper-with () 
  ""
  (let
      ((tmpl-accum "")(tmpl-name (read-string "With: ")))
    (progn
      (while (not (string= tmpl-name ""))
	(setq tmpl-accum (concat tmpl-accum " with " tmpl-name))
	(setq tmpl-name (read-string (concat "(" tmpl-accum " ) With: "))))
      tmpl-accum)))


(defun scala-tmpl-helper-find-abstract-class-name ()
  "Helper function for finding the name of the abstract class above point"
  (save-excursion
    (let 
	((tmpl-name ""))
      (if (re-search-backward "^abstract\\([ \t]*\\)class\\([ \t]*\\)\\(\\w+\\)\\([ \t]*\\)" nil t)
	  (setq tmpl-name (match-string 3))
	(progn
	  (message "No abstract class found! Using class Object.")
	  (setq tmpl-name "Object")))
      tmpl-name)))

;;; Templates


;; application template
(tempo-define-template "scala-object-main"
		       '(> "object App {" > n >
			 > "def main(args : Array[String]) : Unit = {" > n >
			 > r n >
			 > "}" > n >
			 "}" > n >
			 )
		       "application"
		       "Insert a new object with main method"
		       'scala-mode-feature-tempo-tags)

;; simple templates for trait, object, class, abs class
(tempo-define-template "scala-trait-s"
		       '(> (p "trait name: " traitname 'noinsert) "trait " (s traitname) " {" > n > r n "}" > n > )
		       "strait"
		       "Insert a new trait (simple)"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-object-s"
		       '(> (p "object name: " objname 'noinsert) "object " (s objname) " {" > n > r n "}" > n > )
		       "sobject"
		       "Insert a new object (simple)"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-class-s"
		       '(> (p "class name: " classname 'noinsert) "class " (s classname) " {" > n > r n "}" > n > )
		       "sclass"
		       "Insert a new class (simple)"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-abs-class-s"
		       '(> (p "abstract class name: " classname 'noinsert) "abstract class " (s classname) " {" > n > r > n "}" > n > )
		       "sabsclass"
		       "Insert a new abstract class (simple)"
		       'scala-mode-feature-tempo-tags)

;; Case classes (both abstract and case)

(tempo-define-template "scala-abs-case-class-s"
		       '(> (p "abstract case class name: " classname 'noinsert) "abstract class " (s classname) > n > r > )
		       "abscaseclass"
		       "Insert a new abstract class (simple)"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-case-class-s"
		       '(> (p "case class name: " cclassname 'noinsert) "case class " (s cclassname) "(" r ") extends " (scala-tmpl-helper-find-abstract-class-name) > n > )
		       "caseclass"
		       "Insert a new case class (simple)"
		       'scala-mode-feature-tempo-tags)


;; extended templates for trait, object, class and abs class
(tempo-define-template "scala-trait-e"
		       '(> "trait " (scala-tmpl-helper-name "trait name: ") (scala-tmpl-helper-extend) (scala-tmpl-helper-with) " {" > n r > n "}" > n > )
		       "trait"
		       "Insert a new trait"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-object-e"
		       '(> "object " (scala-tmpl-helper-name "object name: ") (scala-tmpl-helper-extend) (scala-tmpl-helper-with) " {" > n > r n "}" > n > )
		       "object"
		       "Insert a new object"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-class-e"
		       '(> "class " (scala-tmpl-helper-name "class name: ") (scala-tmpl-helper-extend) (scala-tmpl-helper-with) " {" > n > r n "}" > n > )
		       "class"
		       "Insert a new class"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-abs-class-e"
		       '(> "abstract class " (scala-tmpl-helper-name "class name: ") (scala-tmpl-helper-extend) (scala-tmpl-helper-with) " {" > n > r n "}" > n > )
		       "absclass"
		       "Insert a new abstract class"
		       'scala-mode-feature-tempo-tags)


;; expressions statements

(tempo-define-template "scala-stmt-if"
		       '(> "if (" (p "if clause: ") ") " r > n > )
		       "if"
		       "Insert a simple one-line if statement"
		       'scala-mode-feature-tempo-tags)
 

(tempo-define-template "scala-stmt-else"
		       '(> "else " r > n > )
		       "else"
		       "Insert a simple one-line else statement"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-stmt-ifelse"
		       '(> "if (" (p "if clause: ") ") {" > n > r n > "} else {" > n > n > "}"> n > )
		       "ifelse"
		       "Insert a if statement with else clause"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scala-stmt-match"
		       '(> "match { " > n > r > n > "}" > n > )
		       "match"
		       "Insert a match statement"
		       'scala-mode-feature-tempo-tags)


;(tempo-define-template "scala-stmt-case"
; 		       '(> "case " (p "case class: ") "(" r ") => " > n >)
; 		       "case-statement"
; 		       "Insert a case statement"
; 		       'scala-mode-feature-tempo-tags)

(tempo-define-template "scala-stmt-case"
		       '(> "case " (p "case pattern: ") r " => " > n >)
		       "case"
		       "Insert a case statement"
		       'scala-mode-feature-tempo-tags)

(tempo-define-template "scala-stmt-case-guard"
		       '(> "case " (p "case pattern: ") r " if " (p "case guard: ") " => " > n >)
		       "gcase"
		       "Insert a case guard statement"
		       'scala-mode-feature-tempo-tags)

(tempo-define-template "scala-stmt-while"
		       '(> "while (" (p "while clause: ") ") { " > n > r > n > "}" > n > )
		       "while"
		       "Insert a while statement"
		       'scala-mode-feature-tempo-tags)

(tempo-define-template "scala-stmt-do-while"
		       '(> "do { " > n > r > n > "} while (" (p "do-while clause: ") ")" > n > )
		       "dowhile"
		       "Insert a do-while statement"
		       'scala-mode-feature-tempo-tags)

(tempo-define-template "scala-stmt-for"
		       '(> "for (" (p "for comprehension: ") ") { " > n > r > n > "}" > n > )
		       "for"
		       "Insert a while comprehension statement"
		       'scala-mode-feature-tempo-tags)

(tempo-define-template "scala-stmt-try-catch"
		       '(> "try {" > n > r n > "} catch { " > n > n > "}" > n > )
		       "trycatch"
		       "Insert a try/catch statement"
		       'scala-mode-feature-tempo-tags)

(defun scala-mode-feature-templates-install ()
  t)