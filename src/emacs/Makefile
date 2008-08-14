############################################################-*-Makefile-*-####
# Makefile for compiling the major mode of Emacs
##############################################################################
# $Id$

##############################################################################
# Configuration

ROOT			 = .

SOURCE_DIR		 = $(ROOT)

##############################################################################
# Variables

# Emacs Lisp
ELISP_COMMAND		?= emacs
ELISP_OPTIONS		+= -batch -no-site-file
ELISP_OPTIONS		+= -eval '(setq byte-compile-emacs19-compatibility t)'
ELISP_OPTIONS		+= -f batch-byte-compile

ELISP_FILES		+= inferior-scala-mode
ELISP_FILES		+= scala-mode-auto
ELISP_FILES		+= scala-mode
ELISP_FILES		+= scala-electric
ELISP_SOURCES		+= $(ELISP_FILES:%=$(SOURCE_DIR)/%.el)

##############################################################################

RM			?= rm -f
TOUCH			?= touch

##############################################################################
# Commands

all: .latest-build

clean:
	$(RM) *.elc .latest-* autoloads.el

.PHONY: all
.PHONY: clean

##############################################################################
# Rules

.latest-build: $(ELISP_SOURCES)
	$(ELISP_COMMAND) $(ELISP_OPTIONS) $(ELISP_SOURCES)
	@$(TOUCH) $@

##############################################################################

autoloads: $(ELISP_SOURCES)
	emacs -batch -q --no-site-file --eval "(setq make-backup-files nil)" --eval "(setq generated-autoload-file (expand-file-name \"autoloads.el\"))" -f batch-update-autoloads `pwd`