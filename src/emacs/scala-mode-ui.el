;;; -*-Emacs-Lisp-*-
;;; scala-mode-ui.el - Menu entries and keyboard shortcuts for scala mode

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

(provide 'scala-mode-ui)

(require 'easymenu)

(eval-when-compile
  (require 'scala-mode-inf))

(defcustom scala-mode-ui:prefix-key "\C-c"
  "Key prefix for scala mode."
  :group 'scala)

(defmacro scala-mode-ui:key (key)
  "Simple macro for appending 'scala-mode-prefix-key' to key commands"
  `(kbd ,(concat scala-mode-ui:prefix-key " " key)))

;;; Helper functions

(defun scala-mode-ui:interpreter-running-p ()
  "True iff a Scala interpreter is currently running in a buffer."
  ;; The following makes sure that we do not autoload
  ;; scala-mode-inf just to check if the interpreter is running.
  (and (fboundp 'scala-mode-inf)
       (let ((ism-def (symbol-function 'scala-mode-inf)))
         (not (and (consp ism-def) (eq (car ism-def) 'autoload))))
       (scala-interpreter-running-p-1)))

;;; Menubar

(scala-mode-lib:define-keys scala-mode-menu-bar-map

  ([scala] (cons "Scala" (make-sparse-keymap "ScalaMode")))

  ([scala version]        '(menu-item "Version"              (lambda () (interactive) (message "Using scala mode version %s (%s)" scala-mode-version scala-mode-svn-revision)) ))
  ([scala report-bug]     '(menu-item "Report bug"           scala-mode:report-bug))
  ([scala customize]      '(menu-item "Customize"            (lambda () (interactive) (customize-group 'scala))))
  ([scala browse-api]     '(menu-item "Browse Scala API"     scala-mode:browse-api))
  ([scala browse-website] '(menu-item "Browse Scala Website" scala-mode:browse-web-site))

  ([scala sep0]           '("---"))

  ([scala feature] (cons "Features" (make-sparse-keymap "Features")))

  ([scala feature comp]	    '(menu-item "Complete word"		        scala-mode-feature-tags-complete))
  ([scala feature load]	    '(menu-item "Load TAGS file"		scala-mode-feature-tags-load))
  ([scala feature create]   '(menu-item "Create TAGS file"		scala-mode-feature-tags-create))

  ([scala feature sep1]     '("---"))

  ([scala feature speedbar] '(menu-item "Speedbar Focus"		speedbar-get-focus))

  ([scala feature sep0]     '("---"))

  ([scala feature scaladoc] '(menu-item "Toggle Scaladoc Mode" scala-scaladoc-mode
					:button (:toggle . (scala-mode-feature-scaladoc-active-p))
					:help "Toggle on/off the Scaladoc mode for Scala files"))

  ([scala feature electric] '(menu-item "Toggle Scala Electric Mode" scala-electric-mode
					:button (:toggle . (scala-mode-feature-electric-active-p))
					:help "Toggle on/off the electric insert mode for Scala files"))

  ([scala sep1]           '("---"))

  ([scala eval-buf]       '(menu-item "Evaluate buffer"          scala-eval-buffer           :enable (scala-mode-ui:interpreter-running-p)                  ))
  ([scala eval-reg]       '(menu-item "Evaluate region"          scala-eval-region           :enable (and (scala-mode-ui:interpreter-running-p) mark-active)))
  ([scala switch-interp]  '(menu-item "Switch to interpreter"    scala-switch-to-interpreter :enable (scala-mode-ui:interpreter-running-p)                  ))
  ([scala load-file]      '(menu-item "Load file in interpreter" scala-load-file             :enable (scala-mode-ui:interpreter-running-p)                  ))
  ([scala quit-interp]    '(menu-item "Quit interpreter"         scala-quit-interpreter      :enable (scala-mode-ui:interpreter-running-p)                  ))
  ([scala run-interp]     '(menu-item "Run interpreter..."       scala-run-scala             :enable (not (scala-mode-ui:interpreter-running-p))            ))

  ([scala sep2]           '("---"))

  ([scala exp] (cons "Expressions" (make-sparse-keymap "Expressions")))

  ([scala exp try]     '(menu-item "try/catch statement"       tempo-template-scala-stmt-try-catch))
  ([scala exp caseg]   '(menu-item "case-guard statement"      tempo-template-scala-stmt-case-guard))
  ([scala exp case]    '(menu-item "case statement"            tempo-template-scala-stmt-case))
  ([scala exp match]   '(menu-item "match statement"           tempo-template-scala-stmt-match))
  ([scala exp for]     '(menu-item "for statement"             tempo-template-scala-stmt-for))
  ([scala exp dowhile] '(menu-item "do-while statement"        tempo-template-scala-stmt-do-while))
  ([scala exp while]   '(menu-item "while statement"           tempo-template-scala-stmt-while))
  ([scala exp ifelse]  '(menu-item "if-else statement"         tempo-template-scala-stmt-ifelse))
  ([scala exp else]    '(menu-item "else statement (one line)" tempo-template-scala-stmt-else))
  ([scala exp if]      '(menu-item "if statement (one line)"   tempo-template-scala-stmt-if))
  
  ([scala coa] (cons "Classes and Objects" (make-sparse-keymap "ClassAndObject")))

  ([scala coa absC]   '(menu-item "Abstract Class"          tempo-template-scala-abs-class-e))
  ([scala coa Cls]    '(menu-item "Class"                   tempo-template-scala-class-e))
  ([scala coa Trt]    '(menu-item "Trait"                   tempo-template-scala-trait-e))
  ([scala coa Obj]    '(menu-item "Object"                  tempo-template-scala-object-e))
  ([scala coa sep0]   '("---"))
  ([scala coa caseC]  '(menu-item "Case Class"              tempo-template-scala-case-class-s))
  ([scala coa absCC]  '(menu-item "Abstract Case Class"     tempo-template-scala-abs-case-class-s))
  ([scala coa sep1]   '("---"))
  ([scala coa SabsC]  '(menu-item "Abstract Class (simple)" tempo-template-scala-abs-class-s))
  ([scala coa SCls]   '(menu-item "Class (simple)"          tempo-template-scala-class-s))
  ([scala coa STrt]   '(menu-item "Trait (simple)"          tempo-template-scala-trait-s))
  ([scala coa SObj]   '(menu-item "Object (simple)"         tempo-template-scala-object-s))
  ([scala coa sep2]   '("---"))
  ([scala coa SApp]   '(menu-item "Application" tempo-template-scala-object-main))

)


;;; Shortcuts

(defvar scala-mode-map
  (let ((map (make-keymap)))
    map))

(scala-mode-lib:define-keys scala-mode-map

   ;; Attach Menubar
   ([menu-bar] scala-mode-menu-bar-map)

   ;; Attach keyboard Shortcuts
   ([tab]                      'scala-indent-line)
   ([(control tab)]            'scala-undent-line)
   ([backspace]                'backward-delete-char-untabify)
   		                
   ("\r"                       'scala-newline)

   ([f1]                       'speedbar-get-focus)
			        
   ([(control c)(control l)]   'scala-load-file)
   ([(control c)(control r)]   'scala-eval-region)
   ([(control c)(control b)]   'scala-eval-buffer)
			        
   ([(control c)(control c)]   'comment-region)

   ("}"                        'scala-electric-brace)

   ((scala-mode-ui:key "f")    'tempo-complete-tag)

   ((scala-mode-ui:key "t o")  'tempo-template-scala-object-s)
   ((scala-mode-ui:key "t t")  'tempo-template-scala-trait-s)
   ((scala-mode-ui:key "t c")  'tempo-template-scala-class-s)
   ((scala-mode-ui:key "t a")  'tempo-template-scala-abs-class-s)   
   
   ((scala-mode-ui:key "a a")  'tempo-template-scala-abs-case-class-s)
   ((scala-mode-ui:key "a c")  'tempo-template-scala-case-class-s)   

   ((scala-mode-ui:key "t T")  'tempo-template-scala-trait-e)
   ((scala-mode-ui:key "t C")  'tempo-template-scala-class-e)
   ((scala-mode-ui:key "t A")  'tempo-template-scala-abs-class-e)
   ((scala-mode-ui:key "t O")  'tempo-template-scala-object-e)
   ((scala-mode-ui:key "t S")  'tempo-template-scala-case-class-e)

   ((scala-mode-ui:key "t m")  'tempo-template-scala-object-main)

   ((scala-mode-ui:key "s i")  'tempo-template-scala-stmt-if)
   ((scala-mode-ui:key "s e")  'tempo-template-scala-stmt-else)
   ((scala-mode-ui:key "s I")  'tempo-template-scala-stmt-ifelse)
   ((scala-mode-ui:key "s m")  'tempo-template-scala-stmt-match)
   ((scala-mode-ui:key "s c")  'tempo-template-scala-stmt-case)
   ((scala-mode-ui:key "s C")  'tempo-template-scala-stmt-case-guard)
   ((scala-mode-ui:key "s w")  'tempo-template-scala-stmt-while)
   ((scala-mode-ui:key "s W")  'tempo-template-scala-stmt-do-while)
   ((scala-mode-ui:key "s f")  'tempo-template-scala-stmt-for)
   ((scala-mode-ui:key "s t")  'tempo-template-scala-stmt-try-catch)

   ((scala-mode-ui:key "g n")  'scala-mode-feature-tags-create)
   ((scala-mode-ui:key "g l")  'scala-mode-feature-tags-load)
   ((scala-mode-ui:key "g c")  'scala-mode-feature-tags-complete)
   )





