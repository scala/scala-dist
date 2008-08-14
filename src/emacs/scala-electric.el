;;; -*-Emacs-Lisp-*-
;;; scala-electric.el - electric editing commands for scala files
;;; $Id$

;;; Modified by Anders Bach Nielsen <andersbach.nielsen at epfl dot ch> to fit into the scala mode
;;; Copyright (C) 2008 by Hemant Kumar (gethemant at gmail to com)
;;; Based on ruby-electric by Dee Zsombor <dee dot zsombor at gmail dot com>.


;;; Variables

(defvar scala-electric-matching-delimeter-alist
  '((?\[ . ?\])
    (?\( . ?\))
    (?\' . ?\')
    (?\` . ?\`)
    (?\" . ?\")))

;;; Customization
 
(defgroup scala-electric nil
  "Minor mode providing electric editing commands for scala files"
  :group 'scala)


(defcustom scala-electric-expand-delimiters-list '(all)
  "*List of contexts where matching delimiter should be
inserted. The word 'all' will do all insertions."
  :type '(set :extra-offset 8
              (const :tag "Everything" all )
              (const :tag "Curly brace" ?\{ )
              (const :tag "Square brace" ?\[ )
              (const :tag "Round brace" ?\( )
              (const :tag "Quote" ?\' )
              (const :tag "Double quote" ?\" )
              (const :tag "Back quote" ?\` )
              (const :tag "Vertical bar" ?\| ))
  :group 'scala-electric)


(defcustom scala-electric-newline-before-closing-bracket nil
  "*Controls whether a newline should be inserted before the
closing bracket or not."
  :type 'boolean 
  :group 'scala-electric)

;;; Mode setup

(defvar scala-electric-mode t
  "nil disables scala electric mode, non-nil enables.")

(make-variable-buffer-local 'scala-electric-mode)

(defun scala-electric-mode (&optional arg)
  ""
  (interactive "P")
  (setq scala-electric-mode
        (if (null arg)
            ;; Toggle mode
            (not scala-electric-mode)
          ;; Enable/Disable according to arg
          (> (prefix-numeric-value arg) 0)))
  )

(defvar scala-electric-mode-map (make-sparse-keymap)
  "Keymap for scala electric minor mode.")

(define-key scala-electric-mode-map "{" 'scala-electric-curlies)
(define-key scala-electric-mode-map "(" 'scala-electric-matching-char)
(define-key scala-electric-mode-map "[" 'scala-electric-matching-char)

;;;###autoload
(or (assoc 'scala-electric-mode minor-mode-alist)
    (setq minor-mode-alist
	  (cons '(scala-electric-mode " electric") minor-mode-alist)))

(or (assoc 'scala-electric-mode minor-mode-map-alist)
    (setq minor-mode-map-alist
	  (cons (cons 'scala-electric-mode scala-electric-mode-map)
		minor-mode-map-alist)))

;; Functions

(defun scala-electric-code-at-point-p()
  (and scala-electric-mode
       (let* ((properties (text-properties-at (point))))
         (and (null (memq 'font-lock-string-face properties))
              (null (memq 'font-lock-comment-face properties))))))

(defun scala-electric-string-at-point-p()
  (and scala-electric-mode
       (consp (memq 'font-lock-string-face (text-properties-at (point))))))

(defun scala-electric-is-last-command-char-expandable-punct-p()
  (or (memq 'all scala-electric-expand-delimiters-list)
      (memq last-command-char scala-electric-expand-delimiters-list)))

(defun scala-electric-curlies(arg)
  (interactive "P")
  (self-insert-command (prefix-numeric-value arg))
  (if (scala-electric-is-last-command-char-expandable-punct-p)
      (cond ((scala-electric-code-at-point-p)
             (insert " ")
             (save-excursion
               (if scala-electric-newline-before-closing-bracket
                   (newline))
               (insert "}")))
            ((scala-electric-string-at-point-p)
             (save-excursion
               (backward-char 1)
               (when (char-equal ?\# (preceding-char))
                 (forward-char 1)
                 (insert "}")))))))

(defun scala-electric-matching-char(arg)
  (interactive "P")
  (self-insert-command (prefix-numeric-value arg))
  (and (scala-electric-is-last-command-char-expandable-punct-p)
       (scala-electric-code-at-point-p)
       (save-excursion
         (insert (cdr (assoc last-command-char
                             scala-electric-matching-delimeter-alist))))))

(provide 'scala-electric)
