<?php
/*************************************************************************************
 * scala.php
 * --------
 * Author: Geoffrey Washburn (washburn@acm.ogr)
 * Copyright: (c) 2008 Geoffrey Washburn
 * Copyright: (c) 2004 Nigel McNie (http://qbnz.com/highlighter/)
 * Release Version: ???
 * Date Started: 2008/01/03
 *
 * Scala language file for GeSHi.
 *
 * CHANGES
 * -------
 * 2008/01/03 
 *   -  Created by modifying the Java highlighter
 * 2008/07/06 
 *   -  More updates.
 *
 * TODO
 * -------------------------
 * * Finish
 *
 *************************************************************************************
 *
 *     This file is part of GeSHi.
 *
 *   GeSHi is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   GeSHi is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GeSHi; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 ************************************************************************************/

$language_data = array (
	'LANG_NAME' => 'Scala',
	'COMMENT_SINGLE' => array(1 => '//'),   
	'COMMENT_MULTI' => array('/*' => '*/'),
	'CASE_KEYWORDS' => GESHI_CAPS_NO_CHANGE,
	'QUOTEMARKS' => array("'", '"', '"""'),
	'ESCAPE_CHAR' => '\\',
	'KEYWORDS' => array(
		1 => array(
			/* Scala keywords, part 1: control flow */
			'case', 'default', 'do', 'else', 'for',
			'if', 'match', 'while', 'throw', 'return',
			'try', 'catch', 'finally', 'yield'
			),
		2 => array(
			/* Scala keywords, part 2 */
			'abstract', 'class', 'def', 'extends', 
			'final', 'forSome', 'implicit', 'import', 
                        'lazy', 'new', 'object', 'override', 
                        'package', 'private', 'protected',
			'sealed', 'super', 'this', 'trait', 
                        'type', 'val', 'var', 'with', 
			),
		3 => array(
			/* Scala value types, top and bottom types.  */
			'Unit', 'Char', 'Byte', 'Short', 'Int', 'Long', 
                        'Boolean', 'Float', 'Double', 'Any', 
                        'AnyVal', 'Nothing',
			),
		4 => array(
			/* Some common Scala reference types */
                        'AnyRef', 'Object', 'ScalaObject', 'Singleton', 
                        'Seq', 'Iterable', 'Null', 'List', 'String', 'Integer', 
                        'Option', 'Array'
			),
		5 => array(
			/* Other reserved words in Scala: literals */
			/* should be styled to look similar to numbers and Strings */
			'false', 'null', 'true'
			)

		),
	'SYMBOLS' => array(
		':', '*', '&', '%', '!', ';', '<', '>', '?', '_', '=', '=>', 
                '<-', '<:', '<%', '>:', '#', '@', ','
		),
	'CASE_SENSITIVE' => array(
		GESHI_COMMENTS => true,
		/* all Scala keywords are case sensitive */
		1 => true, 2 => true, 3 => true, 4 => true, 5 => true ),
	'STYLES' => array(
		'KEYWORDS' => array(
			1 => 'color: #000099;',
			2 => 'color: #009900;',
			3 => 'color: #5555cc;',
			4 => 'color: #99cc99;',
                        5 => 'color: #f78811;'
			),
		'SYMBOLS' => array(
			0 => 'color: #a00000;'
			),
		'COMMENTS' => array(
			1 => 'color: #808080; font-style: italic;',
			'MULTI' => 'color: #808080; font-style: italic;'
			),
		'ESCAPE_CHAR' => array(
			0 => 'color: #0000ff;'
			),
		'BRACKETS' => array(
			0 => 'color: #e77801;'
			),
		'STRINGS' => array(
			0 => 'color: #6666ff;'
			),
		'NUMBERS' => array(
			0 => 'color: #f78811;'
			),
		'METHODS' => array(
			1 => 'color: #006600;',
			2 => 'color: #006600;'
			),
		'SCRIPT' => array(
			),
		'REGEXPS' => array(
			)
		),
	'URLS' => array(
		1 => 'http://www.scala-lang.org/docu/files/ScalaReference.pdf',
		2 => 'http://www.scala-lang.org/docu/files/ScalaReference.pdf',
		3 => 'http://www.scala-lang.org/docu/files/api/index.html',
		4 => 'http://www.scala-lang.org/docu/files/api/index.html',
		5 => 'http://www.scala-lang.org/docu/files/ScalaReference.pdf',
		),
	'OOLANG' => true,
	'OBJECT_SPLITTERS' => array(
		1 => '.'
		),
	'REGEXPS' => array(
		),
	'STRICT_MODE_APPLIES' => GESHI_NEVER,
	'SCRIPT_DELIMITERS' => array(
		),
	'HIGHLIGHT_STRICT_BLOCK' => array(
		)
);

?>
