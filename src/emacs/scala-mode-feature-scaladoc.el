;;; -*-Emacs-Lisp-*-
;;; scala-mode-feature-scaladoc.el - minor mode for handling scaladoc comments

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

(provide 'scala-mode-feature-scaladoc)

(eval-when-compile
  (require 'tempo)
  (require 'scala-mode-variables))

(setq tempo-interactive t)

;; Customization
 
(defgroup scala-mode-feature:scaladoc nil
  "Minor mode providing scaladoc commands for scala files"
  :group 'scala)

(defcustom scala-mode-feature:scaladoc-on-per-default nil
  "*Controls whether scaladoc mode should be on per default or not."
  :type 'boolean
  :group 'scala-mode-feature:scaladoc)

;; Variables

(defvar scala-mode-feature-scaladoc-mode scala-mode-feature:scaladoc-on-per-default
  "nil disables scaladoc mode, non-nil enables.")

(defvar scala-mode-feature-scaladoc-mode-map (make-sparse-keymap)
  "Keymap for scaladoc minor mode.")


;; Mode Setup

(make-variable-buffer-local 'scala-mode-feature-scaladoc-mode)

(defun scala-mode-feature-scaladoc-mode (&optional arg)
  ""
  (interactive "P")
  (setq scala-mode-feature-scaladoc-mode
        (if (null arg)
            ;; Toggle mode
            (not scala-mode-feature-scaladoc-mode)
          ;; Enable/Disable according to arg
          (> (prefix-numeric-value arg) 0)))
  )

;; Alias for some backwards compat
(defalias 'scala-scaladoc-mode 'scala-mode-feature-scaladoc-mode)


;; Functions
(defun scala-mode-feature-scaladoc-active-p ()
  scala-mode-feature-scaladoc-mode)


;(defun scala-mode-feature-scaladoc-extract-args-list (args-string)
;  "Extracts the arguments from the given list (given as a string)."
;  (cond
;   ;; arg list is empty
;   ((string-match "\\`[ \t\n]*\\'" args-string)
;    nil)
;   ;; argument list consists of one word
;   ((string-match "\\`[ \t\n]*\\([a-zA-Z0-9_]+\\)[ \t\n]*\\'" args-string)
;    ;; ... extract this word
;    (let ((arg (substring args-string (match-beginning 1) (match-end 1))))
;      ;; if this arg is a void type return nil
;      (if (string-match (regexp-quote arg) doxymacs-void-types)
;          nil
;        ;; else return arg
;        (list arg))))
;   ;; else split the string and extact var names from args
;   (t
;    (doxymacs-extract-args-list-helper
;     (doxymacs-save-split args-string)))))
; 
; 
;(defun scala-mode-feature-scaladoc-parm-tempo-element (parms)
;  "Inserts tempo elements for the given parms in the given style."
;  (if parms
;      (let ((prompt (concat "Parameter " (car parms) ": ")))
; 	(list 'l " * @param " (car parms) " " (list 'p prompt) '> 'n
; 		(scala-mode-feature-scaladoc-parm-tempo-element (cdr parms)))
; 	
; 	)
;    nil))
 
 
(defun scala-mode-feature-scaladoc-find-next-func ()
  "Returns a list describing next function declaration, or nil if not found.
The argument list is a list of strings."
  (interactive)
  (save-excursion
    (if (re-search-forward
	 (concat
	  ;; function name
	  "def[ \t\n]+\\([a-zA-Z0-9_]+\\)[ \t\n]*"
 
	  ;; arguments
	  "\\((\\([a-zA-Z0-9_:* \t\n]*\\))\\)?"
	  ) nil t)
 
	(let* ((func (buffer-substring (match-beginning 1) (match-end 1)))
	       (args (buffer-substring (match-beginning 3) (match-end 3))))
	  (list (cons 'func func)
		(cons 'args args)));(scala-mode-feature-scaladoc-extract-args-list args))))
      nil)))


(tempo-define-template "scaladoc-multiline-comment"
		       '(> "/**" > n > 
			 > " * " r > n >
			 > " * " > n > 
			 > " */" > n >)
		       "mlc"
		       "Inserts a new multi line scaladoc comment"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scaladoc-file-comment"
		       '("/**" > n
			 " * @file      "
			 (if (buffer-file-name)
			     (file-name-nondirectory (buffer-file-name))
			   "") > n
			   " * @author    " user-full-name " <" user-email-address ">" > n
			   " * @version   0.0.0" > n
			   " * " > n
			   " * " r > n
			   " */" > n)
		       "filec"
		       "Inserts a new file scaladoc comment"
		       'scala-mode-feature-tempo-tags)


(tempo-define-template "scaladoc-function-comment"
		       '((let ((next-func (scala-mode-feature-scaladoc-find-next-func)))
			   (if next-func
			       (list
				'l
				"/** " '> 'n
				" * " 'r '> 'n
				" * " '> 'n
		       ;	  (scala-mode-feature-scaladoc-parm-tempo-element (cdr (assoc 'args next-func)))
				" * @return " '> 'n
				" */" '>)
			     (progn
			       (error "Can't find next function declaration.")
			       nil))))
		       "func"
		       "Inserts a new function scaladoc comment"
		       'scala-mode-feature-tempo-tags)



		       
;; Install function
(defun scala-mode-feature-scaladoc-install ()
  (or (assoc 'scala-mode-feature-scaladoc-mode minor-mode-alist)
      (setq minor-mode-alist
	    (cons '(scala-mode-feature-scaladoc-mode " doc") minor-mode-alist)))
  
  (or (assoc 'scala-mode-feature-scaladoc-mode minor-mode-map-alist)
      (setq minor-mode-map-alist
	    (cons (cons 'scala-mode-feature-scaladoc-mode scala-mode-feature-scaladoc-mode-map)
		  minor-mode-map-alist)))

  (define-key scala-mode-feature-scaladoc-mode-map "\C-cdf"  'tempo-template-scaladoc-function-comment)
  (define-key scala-mode-feature-scaladoc-mode-map "\C-cdi"  'tempo-template-scaladoc-file-comment)
  (define-key scala-mode-feature-scaladoc-mode-map "\C-cdm"  'tempo-template-scaladoc-multiline-comment)
  
  t)