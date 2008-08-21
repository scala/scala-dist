;;; -*-Emacs-Lisp-*-
;;; scala-mode.el - Major mode for editing Scala code.

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

(provide 'scala-mode)

(require 'cl)
(require 'tempo)

(require 'scala-mode-constants)
(require 'scala-mode-variables)
(require 'scala-mode-lib)
(require 'scala-mode-navigation)
(require 'scala-mode-indent)
(require 'scala-mode-fontlock)
(require 'scala-mode-ui)
(require 'scala-mode-feature)

;;; Customization and Variables
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defgroup scala nil
  "Mode for editing Scala code."
  :group 'languages)

(defcustom scala-mode:api-url "http://www.scala-lang.org/docu/files/api/"
  "URL to the online Scala documentation"
  :type 'string
  :group 'scala)

(defconst scala-mode-version "0.5.99.2")
(defconst scala-mode-svn-revision "$Revision$")
(defconst scala-bug-e-mail "scala@listes.epfl.ch")
(defconst scala-web-url "http://scala-lang.org/")


;;; Helper functions/macroes
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defun scala-mode:browse-web-site ()
  "Browse the Scala home-page"
  (interactive)
  (require 'browse-url)
  (browse-url scala-web-url))


(defun scala-mode:browse-api ()
  "Browse the Scala API"
  (interactive)
  (require 'browse-url)
  (browse-url scala-mode:api-url))


(defun scala-mode:report-bug ()
  "Report a bug to the author of the Scala mode via e-mail.
The package used to edit and send the e-mail is the one selected
through `mail-user-agent'."
  (interactive)
  (require 'reporter)
  (let ((reporter-prompt-for-summary-p t))
    (reporter-submit-bug-report
     scala-bug-e-mail
     (concat "Emacs Scala mode v" scala-mode-version)
     '(scala-indent-step))))



;;; Mode
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;###autoload
(define-derived-mode scala-mode fundamental-mode "Scala"
  "Major mode for editing Scala code.
When started, run `scala-mode-hook'.
\\{scala-mode-map}"
  ;; Font lock
  (make-local-variable 'font-lock-defaults)
  (setq font-lock-defaults
        `(scala-font-lock-keywords
          nil
          nil
          ((?\_ . "w"))
          nil
          (font-lock-syntactic-keywords . ,scala-font-lock-syntactic-keywords)
          (parse-sexp-lookup-properties . t)))
 
  ;; Paragraph separation
  (make-local-variable 'paragraph-start)
  (setq paragraph-start (concat "^\\s *$\\|" page-delimiter))
  (make-local-variable 'paragraph-separate)
  (setq paragraph-separate paragraph-start)
  (make-local-variable 'paragraph-ignore-fill-prefix)
  (setq paragraph-ignore-fill-prefix t)
 
  ;; Comment handling
  (make-local-variable 'comment-start)
  (setq comment-start "// ")
  (make-local-variable 'comment-end)
  (setq comment-end "")
  (make-local-variable 'comment-multi-line)
  (setq comment-multi-line nil)
  (make-local-variable 'comment-start-skip)
  (setq comment-start-skip "/\\*+ *\\|//+ *")
  (make-local-variable 'comment-end-skip)
  (setq comment-end-skip " *\\*+/\\| *")
 
  ;; Misc
  (make-local-variable 'indent-line-function)
  (setq indent-line-function #'scala-indent-line)
  (make-local-variable 'require-final-newline)
  (setq require-final-newline t)

   ;; Features
  (scala-mode-feature-install)
 
  ;; Tempo Templetes
  (tempo-use-tag-list 'scala-mode-feature-tempo-tags)
)


;; Syntax tables
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; strings and character literals
(modify-syntax-entry ?\" "\"" scala-mode-syntax-table)
(modify-syntax-entry ?\\ "\\" scala-mode-syntax-table)

;; different kinds of "parenthesis"
(modify-syntax-entry ?\( "()" scala-mode-syntax-table)
(modify-syntax-entry ?\[ "(]" scala-mode-syntax-table)
(modify-syntax-entry ?\{ "(}" scala-mode-syntax-table)
(modify-syntax-entry ?\) ")(" scala-mode-syntax-table)
(modify-syntax-entry ?\] ")[" scala-mode-syntax-table)
(modify-syntax-entry ?\} "){" scala-mode-syntax-table)

;; special characters
(modify-syntax-entry ?\_ "_" scala-mode-syntax-table)

(dolist (char scala-all-special-chars)
  (modify-syntax-entry char "." scala-mode-syntax-table))
(modify-syntax-entry ?\. "." scala-mode-syntax-table)

;; comments
(modify-syntax-entry ?\/  ". 124b" scala-mode-syntax-table)
(modify-syntax-entry ?\*  ". 23"   scala-mode-syntax-table)
(modify-syntax-entry ?\n "> b" scala-mode-syntax-table)
(modify-syntax-entry ?\r "> b" scala-mode-syntax-table)

; run hooks
(run-hooks 'scala-mode-hook)



