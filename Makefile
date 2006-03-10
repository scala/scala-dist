############################################################-*-Makefile-*-####
# Scala Documentation
##############################################################################
# $Id: $

##############################################################################
# Variables

# project
BUILDDIR	+= build
DISTDIR		+= dists
LIBDIR		+= lib
SOURCEDIR	+= src

# tools
CP				?= cp
GZIP			?= gzip
LATEXMK			?= latexmk
LATEXMK_FLAGS	+= -g -ps -pdf
MKDIR			?= mkdir
RM				?= rm -f
TOUCH			?= touch

##############################################################################
# Commands

all: build

build: .latest-reference
build: .latest-spec

spec: .latest-spec

dist: clean
dist: build
dist: .latest-dist

clean:
	@$(RM) .latest-* -r $(BUILDDIR)

clean.all: clean
	@$(RM) -r $(DISTDIR)

.PHONY: all
.PHONY: build
.PHONY: clean
.PHONY: clean.all
.PHONY: dist

##############################################################################
# Rules

.latest-reference:
	@[ -d "$(BUILDDIR)" ] || $(MKDIR) -p $(BUILDDIR)
	lib=`pwd`/$(LIBDIR); \
	src=`pwd`/$(SOURCEDIR)/reference; \
	cd $(BUILDDIR); \
	env TEXINPUTS=$$lib:$$src: BIBINPUTS=$$src: \
	$(LATEXMK) $(LATEXMK_FLAGS) $$src/Changes; \
	env TEXINPUTS=$$lib:$$src: BIBINPUTS=$$src: \
	$(LATEXMK) $(LATEXMK_FLAGS) $$src/ScalaRationale; \
	env TEXINPUTS=$$lib:$$src: BIBINPUTS=$$src: \
	$(LATEXMK) $(LATEXMK_FLAGS) $$src/ScalaByExample; \
	env TEXINPUTS=$$lib:$$src: BIBINPUTS=$$src: \
	$(LATEXMK) $(LATEXMK_FLAGS) $$src/ProgrammingInScala
	$(TOUCH) $@

.latest-spec:
	@[ -d "$(BUILDDIR)" ] || $(MKDIR) -p $(BUILDDIR)
	lib=`pwd`/$(LIBDIR); \
	src=`pwd`/$(SOURCEDIR)/reference; \
	cd $(BUILDDIR); \
	env TEXINPUTS=$$lib:$$src: BIBINPUTS=$$src: \
	$(LATEXMK) $(LATEXMK_FLAGS) $$src/ScalaReference
	$(TOUCH) $@

.latest-dist:
	@[ -d "$(DISTDIR)" ] || $(MKDIR) -p $(DISTDIR)
	$(CP) $(BUILDDIR)/*.pdf $(DISTDIR)
	@for f in `ls $(BUILDDIR)/*.ps`; do \
	  $(GZIP) -9 $$f; \
	done
	$(CP) $(BUILDDIR)/*.ps.gz $(DISTDIR)
	@$(TOUCH) $@

##############################################################################
