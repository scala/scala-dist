<?php
/*************************************************************************************
 * scala3.php
 * --------
 * A variant of the scala.php GeSHI file, modified to remove
 * some of the colors and to conform more closely to the
 * original xmlfilterScala highlighter
 *
 * Scala language file for GeSHi.
 *
 * CHANGES
 * -------
 * 2008/07/07
 *   -  Removed coloring,
 *      made it more similar to xmlfilterScala.
 *
 * 2007/01/03 
 *   -  Created by copying the Java highlighter
 *
 * TODO
 *
 * Add html coloring ('color:blue;'). That seems remarkably
 * difficult to do in the current implementation of GeSHI:
 * - the scripts support would only highlight stuff inside tags
 * - the regexp support does not allow the user to specify a
 * regexp that includes quotes (the QUOTEMARKS directive takes
 * precedence)
 * - the multiline comments are all colored in the same way. Which
 * would work, except that html tags then look like comments.
 * -Toni
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
	'LANG_NAME' => 'Scala (Toni)',
	'COMMENT_SINGLE' => array(1 => '//'),   /* import statements are not comments! */
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
			/* Scala keywords, part 3: standard value types */
			'unit', 'Unit', 'boolean', 'Boolean', 'int', 'Int', 'Any', 'AnyVal', 'Nothing',
			),
		4 => array(
			/* other reserved words in Scala: literals */
			/* should be styled to look similar to numbers and Strings */
			'false', 'null', 'true'
			),
		5 => array(
			/* Scala reference types */
                        'AnyRef', 'Null', 'List', 'String', 'Integer', 'Option', 'Array'
			)
		),
	'SYMBOLS' => array(
                1 => array(
                  '=>'
                ),
                2 => array(
		  ':', '*', '&', '%', '!', ';', '<', '>', '?', '_', '=',
                  '<-', '<:', '<%', '>:', '#', '@'
                )
		),
	'CASE_SENSITIVE' => array(
		GESHI_COMMENTS => true,
		/* all Scala keywords are case sensitive */
		1 => true, 2 => true, 3 => true, 4 => true, 5 => true, 6 => true ),
	'STYLES' => array(
		'KEYWORDS' => array(
			1 => 'font-weight:bold;',
			2 => 'font-weight:bold;',
			3 => '',
			4 => 'font-weight:bold;',
                        5 => ''
			),
		'SYMBOLS' => array(
			1 => 'font-weight:bold;',
			2 => ''
			),
		'COMMENTS' => array(
			1 => 'color:green;',
			'MULTI' => 'color:green;'
			),
		'ESCAPE_CHAR' => array(
			0 => 'color:violet;'
			),
		'BRACKETS' => array(
			0 => ''
			),
		'STRINGS' => array(
			0 => 'color:red;'
			),
		'NUMBERS' => array(
			0 => ''
			),
		'METHODS' => array(
			1 => '',
			2 => ''
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
