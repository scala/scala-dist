;;; -*-Emacs-Lisp-*-
;;; scala-mode-indent.el -

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

(provide 'scala-mode-indent)

(defcustom scala-mode-indent:step 2
  "Indentation step."
  :type 'integer
  :group 'scala)

(defcustom scala-mode-indent:dot-indent t
  "Non-nil means indent trailing lines with . prefix."
  :type 'boolean
  :group 'scala)

(defun scala-parse-partial-sexp ()
  (parse-partial-sexp (point-min) (point)))

(defun scala-in-comment-p ()
  "Return t iff the point is inside a comment."
  ;; The two branches of the "if" below do not have the same behaviour
  ;; when the point is on the comment beginning/ending character(s).
  (or (scala-in-multi-line-comment-p)
      (scala-in-single-line-comment-p)))

(defun scala-in-single-line-comment-p ()
  "Return t iff the point is inside a single line comment."
  (let
      (begin
       end
       subst
       match)
    (save-excursion
      (setq end (point))
      (beginning-of-line)
      (setq begin (point))
      (setq subst (buffer-substring begin end))
      (setq match (string-match "//" subst))
      (if match t nil))))

(defun scala-in-multi-line-comment-p ()
  "Return t iff the point is inside a multi line comment."
  (if font-lock-mode
      (and (not (scala-in-single-line-comment-p))
	   (eq (get-text-property (point) 'face) 'font-lock-comment-face))
    nil))


(defun scala-in-string-p ()
  "Return t iff the point is inside a string."
  (if font-lock-mode
      (eq (get-text-property (point) 'face) 'font-lock-string-face)
    (let ((limit (point)))
      (beginning-of-line)
      (loop while (search-forward-regexp "\\(^\\|[^\\\\]\\)\"" limit 'move)
            count (not (scala-in-comment-p)) into quotes
            finally return (oddp quotes)))))


(defun scala-in-same-level (position)
  "Return t if current point is at same level of POSITION."
  (let ((up-pos (lambda (x) ; return up-list position or 'top
                  (save-excursion
                    (goto-char x)
                    (condition-case ex
                        (progn (backward-up-list)
                               (point))
                      ('error
                       ;; already top level
                       'top))))))
    (equal (funcall up-pos (point))
           (funcall up-pos position))))

(defun scala-indentation ()
  "Return the suggested indentation for the current line."
  (save-excursion
    (beginning-of-line)
    (or (and (scala-in-comment-p)
             (not (= (char-after) ?\/))
             (scala-comment-indentation))
        (scala-indentation-from-following)
        (scala-indentation-from-preceding)
        (scala-indentation-from-block)
        0)))

(defun scala-comment-indentation ()
  ;; Return suggested indentation inside of a comment.
  (forward-line -1)
  (beginning-of-line)
  (skip-syntax-forward " ")
  (if (looking-at "/\\*")
      (1+ (current-column))
    (current-column)))

(defun scala-case-p ()
  (let ((case-p (looking-at scala-case-re)))
    (forward-word)
    (scala-forward-ignorable)
    (and case-p
         (not (looking-at scala-class-re)))))

(defun scala-case-block-p ()
  (save-excursion
    (forward-comment (buffer-size))
    (scala-case-p)))

(defun scala-case-line-p ()
  (save-excursion
    (beginning-of-line)
    (scala-forward-ignorable)
    (scala-case-p)))

(defun scala-lambda-p () 
  ;; Returns t if the block loocks like a lambda block.
  ;; Any block with a => at a line end is considered lambda.
  (save-excursion
    (scala-backward-ignorable)
    ;; only '{' or '(' can start a lambda
    (and (or (= (preceding-char) ?\{)
             (= (preceding-char) ?\())
         ;; jump over sexp untill we see '=>' or something
         ;; that does not belong. TODO: the real pattern
         ;; that we would like to skip over is id: Type
         (progn 
           (scala-forward-ignorable)
           (ignore-errors
            (while (not (or (looking-at scala-double-arrow-re)
                            (looking-at scala-case-re)
                            (scala-looking-at-empty-line)
                            (= (char-after) ?\;)
                            (= (char-after) ?\,)))
              (forward-sexp)
              (scala-forward-ignorable)
              ))
            t)
         ;; see if we arrived at '=>'
         (looking-at scala-double-arrow-re)
         ;; check that we are at line end ('{' and '(' tolerated))
         (progn
           (goto-char (match-end 0))
           (scala-forward-ignorable (line-end-position))
           (when (= (char-syntax (char-after)) ?\()
             (forward-char)
             (scala-forward-ignorable (line-end-position))
             (skip-syntax-forward " "))
           (eolp)))))

(defun scala-at-start-of-expression ()
  ;; return true if we are very sure that we are at the start of expression
  (save-excursion
    (scala-backward-ignorable)
    (let ((cb (char-before)))
      (or (= (char-syntax cb) ?\()
          (= cb ?\=)
          (= cb ?\;)
          (looking-back "=>" (- (point) 2))
          (scala-looking-backward-at-empty-line)
          ))))

(defun scala-expression-start ()
  ;; try to find the line on which an expression or definition starts
  (scala-backward-ignorable)
  (while (not (or (bobp)
                  (scala-looking-backward-at-empty-line)
                  (scala-at-start-of-expression)))
    (backward-sexp)
    (scala-backward-ignorable))
  (scala-forward-ignorable))

(defun scala-block-indentation (&optional case-or-eob)
  ;; expect to be just after {([
  (scala-backward-ignorable)
  (let ((block-start-eol (line-end-position))
        (block-after-spc (scala-point-after (forward-comment (buffer-size)))))
    (if (or (> block-after-spc block-start-eol) ;; simple block open {
            (scala-lambda-p)) ;; => on opening line
        (let ((step (* (if (and (scala-case-block-p) (not case-or-eob)) 2 1)
                       scala-mode-indent:step)))
          (backward-char)
          (scala-backward-ignorable)
          (when (= (char-before) ?\=)
            (backward-char))
          (scala-expression-start)
          (if (scala-lambda-p) ;; nested lambda block
              (scala-block-indentation case-or-eob)
            (+ (current-indentation) step)))
      (progn ;; properly indent mulitline args in a template                                    
        (skip-syntax-forward " ")
        (current-column)))))

(defun scala-indentation-from-following ()
  ;; Return suggested indentation based on the following part of the
  ;; current expression. Return nil if indentation cannot be guessed.
  (save-excursion
    (scala-forward-ignorable (line-end-position))
    (cond
     ((eobp) nil)
     ;; curry
     ((and (= (char-after) ?\() 
           (save-excursion (scala-backward-ignorable) (= (char-before) ?\))))
      (backward-list)
      (current-column))
     ;; end of block
     ((= (char-syntax (char-after)) ?\))
      (let ((parse-sexp-ignore-comments t))
        (goto-char (1+ (scan-sexps (1+ (point)) -1))))
      (- (scala-block-indentation t) scala-mode-indent:step))
     ;; don't do any of the other stuff if the previous line was
     ;; just a closing brackets
     ((scala-after-brackets-line-p) nil)
     ;; indent lines that start with . as in 
     ;; foo
     ;;   .bar 
     ((and scala-mode-indent:dot-indent
           (eq (char-after) ?\.))
      (scala-backward-ident)
      (beginning-of-line)
      (scala-forward-ignorable (line-end-position))
      (if (= (char-syntax (char-after)) ?\.)
          (scala-indentation-from-following)
        (+ (current-indentation) scala-mode-indent:step)))
     ;; align 'else', 'yield', 'extends', 'with', '=>' with start of expression
     ((looking-at scala-expr-middle-re)
      (let* ((matching-kw (cdr (assoc (match-string-no-properties 0)
                                      scala-expr-starter)))
             (found-pos (scala-search-backward-sexp matching-kw)))
        (if found-pos
            (progn
              (scala-move-if (backward-word 1)
                             (looking-at scala-else-if-re))
              (current-column))))))))


(defun scala-indentation-from-preceding ()
  ;; Return suggested indentation based on the preceding part of the
  ;; current expression, but not if it's separated by one or more empty line. 
  ;; Return nil if indentation cannot be guessed.
  (save-excursion
    (let ((am-case (scala-case-line-p)))
      (scala-backward-ignorable)
      (when (not (bobp))
	(cond
         ;; '='
	 ((looking-back scala-declr-expr-start-re (- (point) 2))
          (let ((pos (point)))
            (scala-forward-ignorable)
            (if (= (char-syntax (char-after)) ?\()
                nil
              (goto-char (1- pos))
              (scala-expression-start)
              (+ (current-indentation) scala-mode-indent:step))))
         ;; 'yield', 'else'
         ((scala-looking-at-backward scala-value-expr-cont-re)
          (+ (current-indentation) scala-mode-indent:step))
	 ;; 'if', 'else if'
	 ((eq (char-before) ?\))
	  (backward-sexp)
	  (scala-backward-ignorable)
	  (cond ((scala-looking-at-backward scala-else-if-re)
		 (+ (current-indentation) scala-mode-indent:step))
		((scala-looking-at-backward scala-if-re)
		 (backward-sexp)
		 (+ (current-column) scala-mode-indent:step)))))))))

(defun scala-indentation-from-block ()
  ;; Return suggested indentation based on the current block.
  (save-excursion
    (let* ((am-case (scala-case-line-p))
	   (state (scala-parse-partial-sexp))
           (block-start (nth 1 state)))
      (if (not block-start)
          0
        (goto-char (1+ block-start))
        (scala-block-indentation am-case)))))

(defun scala-indent-line-to (column)
  "Indent current line to COLUMN and perhaps move point.
The point is moved iff it is currently in the indentation, in which
case it is brought to the end of that indentation. Otherwise it does
not move."
  (if (<= (current-column) (current-indentation))
      (indent-line-to column)
    (save-excursion (indent-line-to column))))

(defun scala-indent-line ()
  "Indent current line as smartly as possible.
When called repeatedly, indent each time one stop further on the right."
  (interactive)
  (if (or (and (eq last-command this-command) (not (eq last-command 'scala-newline)))
          (eq last-command 'scala-undent-line))
      (scala-indent-line-to (+ (current-indentation) scala-mode-indent:step))
    (let
	((indentation (scala-indentation)))
      (scala-indent-line-to indentation))))

(defun scala-undent-line ()
  "Indent line to previous tab stop."
  (interactive)
  (scala-indent-line-to (max 0 (- (current-indentation) scala-mode-indent:step))))

(defun scala-electric-brace ()
  "Insert a brace, and if alone on a non-comment line, reindent."
  (interactive)
  (let ((on-empty-line-p (save-excursion
                           (beginning-of-line)
                           (looking-at scala-empty-line-re))))
    ;; Calling self-insert-command will blink to the matching open-brace
    ;; (if blink-matching-paren is enabled); we first indent, then
    ;; call self-insert-command, so that the close-brace is correctly
    ;; positioned during the blink.
    (when on-empty-line-p
      (insert "}")
      (scala-indent-line)
      (delete-backward-char 1))
    (call-interactively 'self-insert-command)))


(defun scala-newline ()
  (interactive)
  (if (scala-in-multi-line-comment-p)
      (progn
	(newline-and-indent)
	(insert "* "))
    (newline)))
