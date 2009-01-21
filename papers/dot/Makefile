# Need a verbfilterScala to build. You can get it, but compiling
# scala-docs/support/verbfilterScala.scala

main.pdf			: main.tex
	pdflatex main

main.tex			: main.src 
	java verbfilterScala main.src main.tex
