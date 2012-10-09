;;; -*-Emacs-Lisp-*-
;;; scala-mode-navigation.el - 

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

(provide 'scala-mode-navigation)

(require 'scala-mode-constants)

(defun scala-when-looking-at* (regexp &optional thunk)
  (let ((saved-match-data (match-data)))
    (if (looking-at regexp)
        (progn (goto-char (match-end 0))
               (set-match-data saved-match-data)
               (or (not thunk) (funcall thunk)))
      (set-match-data saved-match-data)
      nil)))

(defmacro scala-when-looking-at (regexp &rest body)
  (if body
      `(scala-when-looking-at* ,regexp (lambda () ,@body))
    `(scala-when-looking-at* ,regexp)))

(defun scala-looking-at-empty-line ()
  ;; return t if from the point forward is just spaces
  ;; and next line is empty
  (save-excursion
   (or (bolp)
       (skip-syntax-forward " >" (1+ (line-end-position))))
   (looking-at scala-empty-line-re)))

(defun scala-looking-backward-at-empty-line ()
  ;; return t if from the point backward is just spaces
  ;; and previous line is empty
  (save-excursion
    (skip-syntax-backward " ")
    (and (bolp)
         (forward-line -1)
         (looking-at scala-empty-line-re))))

(defun scala-forward-ignorable (&optional limit)
  ;; forward over spaces and comments, but not over empty lines
  (if limit
      (save-restriction
        (narrow-to-region (point) limit)
        (scala-forward-ignorable))
    (while (and (not (scala-looking-at-empty-line))
                (forward-comment 1)))))

(defun scala-backward-ignorable ()
  (interactive)
  ;; backward over spaces and comments, but not over empty lines
  (while (and (not (scala-looking-backward-at-empty-line))
              (forward-comment -1))))

(defun scala-after-brackets-line-p ()
  (save-excursion
    (scala-backward-ignorable)
    (let ((limit (point)))
      (back-to-indentation)
      (save-restriction
        (narrow-to-region (point) limit)
        (looking-at "\\s)+$")))))

(defun scala-at-line-end ()
  ;; we are at end of line if after the point is
  ;; only ignorable (comment or whitespace)
  (save-excursion
    (or (eolp)
        (let ((line (line-number-at-pos)))
          (scala-forward-ignorable)
          (or (> (line-number-at-pos) line)
              (eolp))))))

(defun scala-looking-at-backward (re)
  (save-excursion
    (when (= 0 (skip-syntax-backward "w_")) (backward-char))
    (looking-at re)))

(defun scala-search-backward-sexp (re)
  ;; searches backward for a sexp that begins with the regular
  ;; expression. Skips over sexps. Does not continue over
  ;; end of expression (empty line or ';')
  ;; returns new point or nil if not found
  (let ((found-pos (catch 'found
                     (save-excursion
                       (while (not (bobp))
                         (scala-backward-ignorable)
                         (if (or (scala-looking-backward-at-empty-line)
                                 (eq (char-before) ?\;)
                                 (eq (char-syntax (char-before)) ?\())
                             (throw 'found nil))
                         (backward-sexp)
                         (if (looking-at re)
                             (throw 'found (point))))))))
    (if found-pos
        (goto-char found-pos))
    found-pos))

(defun scala-find-in-limit (re limit)
  ;; returns the point where re was found in limit or nil
  (save-excursion
    (search-forward-regexp re limit t)))

(defmacro scala-point-after (&rest body)
  `(save-excursion
     ,@body
     (point)))

(defmacro scala-move-if (&rest body)
  (let ((pt-sym (make-symbol "point"))
	(res-sym (make-symbol "result")))
    `(let ((,pt-sym (point))
	   (,res-sym ,(cons 'progn body)))
       (unless ,res-sym (goto-char ,pt-sym))
       ,res-sym)))

(defun scala-forward-ident ()
  ;; Move forward over an identifier.
  (scala-forward-ignorable)
  (if (looking-at scala-ident-re)
      (goto-char (match-end 0))
    (forward-char))
  t)

(defun scala-backward-ident ()
  ;; Move backward over an identifier.
  (scala-backward-ignorable)
  (if (scala-looking-at-backward scala-ident-re)
      (goto-char (match-beginning 0))
    (backward-char))
  t)

(defun scala-forward-qual-ident ()
  ;; Move forward over a qualifier identifier.
  (scala-forward-ignorable)
  (if (looking-at scala-qual-ident-re)
      (goto-char (match-end 0))
    (forward-char))
  t)

(defun scala-backward-qual-ident ()
  ;; Move backward over a qualifier identifier.
  (scala-backward-ignorable)
  (if (scala-looking-at-backward scala-qual-ident-re)
      (goto-char (match-beginning 0))
    (backward-char))
  t)

(defun scala-forward-simple-type ()
  ;; Move forward over a simple type (as defined by the grammar).
  ;; Works only when point is at the beginning of a simple type
  ;; (modulo initial spaces/comments).
  (cond ((eobp) nil)
        ((= (char-after) ?\()
         ;; Parenthesized type
         (forward-sexp)
         t)
        (t
         ;; Type designator
         (scala-forward-qual-ident)
         (scala-forward-ignorable)
         (cond ((eobp) nil)
               ((= (char-after) ?\[)
                ;; Type arguments
                (forward-sexp))
               ((= (char-after) ?\#)
                ;; Type selection
                (forward-char)
                (scala-forward-ident)))
         t)))

(defun scala-forward-type1 ()
  ;; Move forward over a type1 (as defined by the grammar).
  ;; Works only when point is at the beginning of a type (modulo
  ;; initial spaces/comments).
  (scala-forward-ignorable)
  (scala-when-looking-at "\\<class\\>"
                         (forward-word 1) (scala-forward-ignorable))
  (scala-forward-simple-type)
  (while (scala-when-looking-at "\\s *\\<with\\>\\s *")
    (if (and (not (eobp)) (= (char-after) ?\{))
        (forward-sexp)                       ;skip refinement
      (scala-forward-simple-type)))
  t)

(defun scala-forward-type ()
  ;; Move forward over a type.
  (cond ((eobp) nil)
        ((= (char-after) ?\()
         ;; Function type (several arguments)
         (forward-sexp)
         (scala-when-looking-at "\\s *=>\\s *" (scala-forward-type))
         t)
        (t
         ;; Type1 or function type with one argument
         (scala-forward-type1)
         (scala-when-looking-at "\\s *=>\\s *" (scala-forward-type))
         t)))

(defun scala-forward-type-param ()
  ;; Move over a type parameter
  ;; variance
  (scala-when-looking-at "\\s *[-+]\\s *")
  (scala-forward-ident)
  ;; bounds
  (while (scala-when-looking-at "\\s *[<>][:%]\\s *")
    (scala-forward-type))
  t)

(defun scala-forward-literal ()
  ;; Move forward over an integer, float, character or string literal.
  (scala-forward-ignorable)
  (scala-when-looking-at scala-literal-re)
  t)
