;;; -*-Emacs-Lisp-*-
;;; scala-mode.el - Major mode for editing Scala code.

;; Copyright (C) 2009-2011 Scala Dev Team at EPFL
;; Authors: See AUTHORS file
;; Keywords: scala languages oop

;;; License

;; SCALA LICENSE
;;  
;; Copyright (c) 2002-2011 EPFL, Lausanne, unless otherwise specified.
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

(defcustom scala-mode:api-url "http://www.scala-lang.org/docu/files/api/index.html"
  "URL to the online Scala documentation"
  :type 'string
  :group 'scala)

(defconst scala-mode-version "0.5.99.5")
(defconst scala-mode-svn-revision "$Revision: 21917 $")
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





(defvar scala-mode-abbrev-table nil
  "Abbrev table in use in `scala-mode' buffers.")
(define-abbrev-table 'scala-mode-abbrev-table nil)


(defvar scala-mode-syntax-table nil
  "Syntax table used in `scala-mode' buffers.")
(when (not scala-mode-syntax-table)
  (setq scala-mode-syntax-table (make-syntax-table))
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
  ;; the `n' means that comments can be nested
  (modify-syntax-entry ?\/  ". 124b" scala-mode-syntax-table)
  (modify-syntax-entry ?\*  ". 23n"   scala-mode-syntax-table)
  (modify-syntax-entry ?\n  "> b" scala-mode-syntax-table)
  (modify-syntax-entry ?\r  "> b" scala-mode-syntax-table))


;;; Mode
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;###autoload
(defun scala-mode ()
  "Major mode for editing Scala code.
When started, run `scala-mode-hook'.
\\{scala-mode-map}"
  (interactive)
  ;; set up local variables
  (kill-all-local-variables)
  (make-local-variable 'font-lock-defaults)
  (make-local-variable 'paragraph-separate)
  (make-local-variable 'paragraph-start)
  (make-local-variable 'paragraph-ignore-fill-prefix)
  (make-local-variable 'require-final-newline)
  (make-local-variable 'comment-start)
  (make-local-variable 'comment-end)
  (make-local-variable 'comment-start-skip)
  (make-local-variable 'comment-end-skip)
  (make-local-variable 'comment-column)
  ;(make-local-variable 'comment-indent-function)
  (make-local-variable 'indent-line-function)
  ;;
  (set-syntax-table scala-mode-syntax-table)
  (setq major-mode                    'scala-mode
	mode-name                     "Scala"
	local-abbrev-table            scala-mode-abbrev-table
	font-lock-defaults            '(scala-font-lock-keywords
                                       nil
                                       nil
                                       ((?\_ . "w"))
                                       nil
                                       (font-lock-syntactic-keywords . scala-font-lock-syntactic-keywords)
                                       (parse-sexp-lookup-properties . t))
	paragraph-separate            (concat "^\\s *$\\|" page-delimiter)
	paragraph-start               (concat "^\\s *$\\|" page-delimiter)
	paragraph-ignore-fill-prefix  t
	require-final-newline         t
	comment-start                 "// "
	comment-end                   ""
	comment-start-skip            "/\\*+ *\\|//+ *"
	comment-end-skip              " *\\*+/\\| *"
	comment-column                40
;	comment-indent-function       'scala-comment-indent-function
	indent-line-function          'scala-indent-line
	)

  (use-local-map scala-mode-map)
  (turn-on-font-lock)
  (scala-mode-feature-install)
  (if scala-mode-hook
      (run-hooks 'scala-mode-hook)))




