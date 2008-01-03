<?php
/*************************************************************************************
 * scala.php
 * --------
 * Author: Geoffrey Washburn (washburn@acm.org)
 * Copyright: (c) 2008 Geoffrey Washburn (washburn@acm.org)
                  2004 Nigel McNie (http://qbnz.com/highlighter/)
 * Release Version: ???
 * Date Started: 2008/01/03
 *
 * Scala language file for GeSHi.
 *
 * CHANGES
 * -------
 * 2008/01/03 
 *   -  Created by copying the Java highlighter
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
			'if', 'match', 'while'
			),
		2 => array(
			/* Scala keywords, part 2 */
			'return', 'throw',
			'try', 'catch', 'finally',

			'abstract', 'class', 'def', 'extends', 
			'final', 'forSome', 'implicit', 'import', 
                        'lazy', 'new', 'object', 'override', 'package', 
                        'private', 'protected',
			'requires', 'sealed', 'super', 'this', 'trait', 'type', 
                        'val', 'var', 'with', 'yield'
			),
		3 => array(
			/* Scala keywords, part 3: standard value tpyes */
			'unit', 'Unit', 'boolean', 'Boolean', 'int', 'Int'
			),
		4 => array(
			/* other reserved words in Scala: literals */
			/* should be styled to look similar to numbers and Strings */
			'false', 'null', 'true'
			)
		),
	'SYMBOLS' => array(
		'(', ')', '[', ']', '{', '}', 
                '*', '&', '%', '!', ';', '<', 
                '>', '?', '_', ':', '=', '=>', 
                '<-', '<:', '<%', '>:', '#', '@'
		),
	'CASE_SENSITIVE' => array(
		GESHI_COMMENTS => true,
		/* all Scala keywords are case sensitive */
		1 => true, 2 => true, 3 => true, 4 => true
	),
	'STYLES' => array(
		'KEYWORDS' => array(
			1 => 'color: #b1b100;',
			2 => 'color: #000000; font-weight: bold;',
			3 => 'color: #993333;',
			4 => 'color: #b13366;'
			),
		'COMMENTS' => array(
			1 => 'color: #808080; font-style: italic;',
			'MULTI' => 'color: #808080; font-style: italic;'
			),
		'ESCAPE_CHAR' => array(
			0 => 'color: #000099; font-weight: bold;'
			),
		'BRACKETS' => array(
			0 => 'color: #66cc66;'
			),
		'STRINGS' => array(
			0 => 'color: #ff0000;'
			),
		'NUMBERS' => array(
			0 => 'color: #cc66cc;'
			),
		'METHODS' => array(
			1 => 'color: #006600;',
			2 => 'color: #006600;'
			),
		'SYMBOLS' => array(
			0 => 'color: #66cc66;'
			),
		'SCRIPT' => array(
			),
		'REGEXPS' => array(
			)
		),
	'URLS' => array(
		1 => '',
		2 => '',
		3 => '',
		4 => ''
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
