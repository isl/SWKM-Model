/* 
 *  COPYRIGHT (c) 2008-2009 by Institute of Computer Science, 
 *  Foundation for Research and Technology - Hellas
 *  Contact: 
 *      POBox 1385, Heraklio Crete, GR-700 13 GREECE
 *      Tel:+30-2810-391632
 *      Fax: +30-2810-391638
 *      E-mail: isl@ics.forth.gr
 *      http://www.ics.forth.gr/isl
 *
 *   Authors  :  Dimitris Andreou, Nelly Vouzoukidou.
 *
 *   This file is part of SWKM model APIs (see also http://athena.ics.forth.gr:9090/SWKM/).
 *
 *    SWKM model APIs is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *   SWKM model APIs is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with SWKM model APIs.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   SWKM has been partially supported by EU project KP-Lab (IP IST-27490) kp-lab.org
 */


/**
Copyright Aduna (http://www.aduna-software.com/)  2001-2007
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer. Redistributions in binary form must
reproduce the above copyright notice, this list of conditions and the following
disclaimer in the documentation and/or other materials provided with the
distribution.
Neither the name of the copyright holder nor the names of its contributors may
be used to endorse or promote products derived from this software without
specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE
*/

/**
 * Adapted code from:
 * http://sesame.cvs.sourceforge.net/viewvc/sesame/openrdf/src/org/openrdf/util/xml/XmlDatatypeUtil.java?view=markup
 */
package gr.forth.ics.swkm.model2;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import static gr.forth.ics.swkm.model2.vocabulary.XmlSchema.*;

/**
 * Provides methods for handling the standard XML Schema datatypes.
 */
class XmlDatatypeUtil {
	static boolean isValidValue(String value, Uri datatype) {
		boolean result = true;

		if (datatype.equals(DECIMAL)) {
			result = isValidDecimal(value);
		}
		else if (datatype.equals(INTEGER)) {
			result = isValidInteger(value);
		}
		else if (datatype.equals(NEGATIVE_INTEGER)) {
			result = isValidNegativeInteger(value);
		}
		else if (datatype.equals(NON_POSITIVE_INTEGER)) {
			result = isValidNonPositiveInteger(value);
		}
		else if (datatype.equals(NON_NEGATIVE_INTEGER)) {
			result = isValidNonNegativeInteger(value);
		}
		else if (datatype.equals(POSITIVE_INTEGER)) {
			result = isValidPositiveInteger(value);
		}
		else if (datatype.equals(LONG)) {
			result = isValidLong(value);
		}
		else if (datatype.equals(INT)) {
			result = isValidInt(value);
		}
		else if (datatype.equals(SHORT)) {
			result = isValidShort(value);
		}
		else if (datatype.equals(BYTE)) {
			result = isValidByte(value);
		}
		else if (datatype.equals(UNSIGNED_LONG)) {
			result = isValidUnsignedLong(value);
		}
		else if (datatype.equals(UNSIGNED_INT)) {
			result = isValidUnsignedInt(value);
		}
		else if (datatype.equals(UNSIGNED_SHORT)) {
			result = isValidUnsignedShort(value);
		}
		else if (datatype.equals(UNSIGNED_BYTE)) {
			result = isValidUnsignedByte(value);
		}
		else if (datatype.equals(FLOAT)) {
			result = isValidFloat(value);
		}
		else if (datatype.equals(DOUBLE)) {
			result = isValidDouble(value);
		}
		else if (datatype.equals(BOOLEAN)) {
			result = isValidBoolean(value);
		}
		else if (datatype.equals(DATETIME)) {
			result = isValidDateTime(value);
		}

		return result;
	}

	static boolean isValidDecimal(String value) {
		try {
			normalizeDecimal(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

    static boolean isValidInteger(String value) {
		try {
			normalizeInteger(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidNegativeInteger(String value) {
		try {
			normalizeNegativeInteger(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidNonPositiveInteger(String value) {
		try {
			normalizeNonPositiveInteger(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidNonNegativeInteger(String value) {
		try {
			normalizeNonNegativeInteger(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidPositiveInteger(String value) {
		try {
			normalizePositiveInteger(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidLong(String value) {
		try {
			normalizeLong(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidInt(String value) {
		try {
			normalizeInt(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidShort(String value) {
		try {
			normalizeShort(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidByte(String value) {
		try {
			normalizeByte(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidUnsignedLong(String value) {
		try {
			normalizeUnsignedLong(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidUnsignedInt(String value) {
		try {
			normalizeUnsignedInt(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidUnsignedShort(String value) {
		try {
			normalizeUnsignedShort(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidUnsignedByte(String value) {
		try {
			normalizeUnsignedByte(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidFloat(String value) {
		try {
			normalizeFloat(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidDouble(String value) {
		try {
			normalizeDouble(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidBoolean(String value) {
		try {
			normalizeBoolean(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isValidDateTime(String value) {
		try {
			DateTime dt = new DateTime(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

/*--------------------+
| Value normalization |
+--------------------*/

	/**
	 * Normalizes the supplied value according to the normalization
	 * rules for the supplied datatype.
	 *
	 * @param value The value to normalize.
	 * @param datatype The value's datatype.
	 * @return The normalized value if there are any (supported)
	 * normalization rules for the supplied datatype, or the original
	 * supplied value otherwise.
	 * @exception IllegalArgumentException If the supplied value is
	 * illegal considering the supplied datatype.
	 **/
	static String normalize(String value, Uri datatype) {
		String result = value;

		if (datatype.equals(DECIMAL)) {
			result = normalizeDecimal(value);
		}
		else if (datatype.equals(INTEGER)) {
			result = normalizeInteger(value);
		}
		else if (datatype.equals(NEGATIVE_INTEGER)) {
			result = normalizeNegativeInteger(value);
		}
		else if (datatype.equals(NON_POSITIVE_INTEGER)) {
			result = normalizeNonPositiveInteger(value);
		}
		else if (datatype.equals(NON_NEGATIVE_INTEGER)) {
			result = normalizeNonNegativeInteger(value);
		}
		else if (datatype.equals(POSITIVE_INTEGER)) {
			result = normalizePositiveInteger(value);
		}
		else if (datatype.equals(LONG)) {
			result = normalizeLong(value);
		}
		else if (datatype.equals(INT)) {
			result = normalizeInt(value);
		}
		else if (datatype.equals(SHORT)) {
			result = normalizeShort(value);
		}
		else if (datatype.equals(BYTE)) {
			result = normalizeByte(value);
		}
		else if (datatype.equals(UNSIGNED_LONG)) {
			result = normalizeUnsignedLong(value);
		}
		else if (datatype.equals(UNSIGNED_INT)) {
			result = normalizeUnsignedInt(value);
		}
		else if (datatype.equals(UNSIGNED_SHORT)) {
			result = normalizeUnsignedShort(value);
		}
		else if (datatype.equals(UNSIGNED_BYTE)) {
			result = normalizeUnsignedByte(value);
		}
		else if (datatype.equals(FLOAT)) {
			result = normalizeFloat(value);
		}
		else if (datatype.equals(DOUBLE)) {
			result = normalizeDouble(value);
		}
		else if (datatype.equals(BOOLEAN)) {
			result = normalizeBoolean(value);
		}
		else if (datatype.equals(DATETIME)) {
			result = normalizeDateTime(value);
		}

		return result;
	}

	/**
	 * Normalizes a boolean value to its canonical representation.
	 * More specifically, the values <tt>1</tt> and <tt>0</tt> will be
	 * normalized to the canonical values <tt>true</tt> and
	 * <tt>false</tt>, respectively. Supplied canonical values will
	 * remain as is.
	 *
	 * @param value The boolean value to normalize.
	 * @return The normalized value.
	 * @exception IllegalArgumentException If the supplied value is
	 * not a legal boolean.
	 **/
	static String normalizeBoolean(String value) {
		value = collapseWhiteSpace(value);

		if (value.equals("1")) {
			return "true";
		}
		else if (value.equals("0")) {
			return "false";
		}
		else if (value.equals("true") || value.equals("false")) {
			return value;
		}
		else {
            throw new IllegalArgumentException("Not a legal boolean value: " + value);
		}
	}

	/**
	 * Normalizes a decimal to its canonical representation.
	 * For example: <tt>120</tt> becomes <tt>120.0</tt>,
	 * <tt>+.3</tt> becomes <tt>0.3</tt>,
	 * <tt>00012.45000</tt> becomes <tt>12.45</tt> and
	 * <tt>-.0</tt> becomes <tt>0.0</tt>.
	 *
	 * @param decimal The decimal to normalize.
	 * @return The canonical representation of <tt>decimal</tt>.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal decimal.
	 **/
	static String normalizeDecimal(String decimal) {
		decimal = collapseWhiteSpace(decimal);

		String errMsg = "Not a legal decimal: " + decimal;

		int decLength = decimal.length();
		StringBuffer result = new StringBuffer(decLength + 2);

		if (decLength == 0) {
            throw new IllegalArgumentException(errMsg);
		}

		boolean isZeroPointZero = true;

		// process any sign info
		int idx = 0;
		if (decimal.charAt(idx) == '-') {
			result.append('-');
			idx++;
		}
		else if (decimal.charAt(idx) == '+') {
			idx++;
		}

		if (idx == decLength) {
            throw new IllegalArgumentException(errMsg);
		}

		// skip any leading zeros
		while (idx < decLength && decimal.charAt(idx) == '0') {
			idx++;
		}

		// Process digits before the dot
		if (idx == decLength) {
			// decimal consists of zeros only
			result.append('0');
		}
		else if (idx < decLength && decimal.charAt(idx) == '.') {
			// no non-zero digit before the dot
			result.append('0');
		}
		else {
			isZeroPointZero = false;

			// Copy any digits before the dot
			while (idx < decLength) {
				char c = decimal.charAt(idx);
				if (c == '.') {
					break;
				}
				if (!_isDigit(c)) {
                    throw new IllegalArgumentException(errMsg);
				}
				result.append(c);
				idx++;
			}
		}

		result.append('.');

		// Process digits after the dot
		if (idx == decLength) {
			// No dot was found in the decimal
			result.append('0');
		}
		else {
			idx++;

			// search last non-zero digit
			int lastIdx = decLength - 1;
			while (lastIdx >= 0 && decimal.charAt(lastIdx) == '0') {
				lastIdx--;
			}

			if (idx > lastIdx) {
				// No non-zero digits found
				result.append('0');
			}
			else {
				isZeroPointZero = false;

				while (idx <= lastIdx) {
					char c = decimal.charAt(idx);
					if (!_isDigit(c)) {
                        throw new IllegalArgumentException(errMsg);
					}
					result.append(c);
					idx++;
				}
			}
		}

		if (isZeroPointZero) {
			// Make sure we don't return "-0.0"
			return "0.0";
		}
		else {
			return result.toString();
		}
	}

	/**
	 * Normalizes an integer to its canonical representation.
	 * For example: <tt>+120</tt> becomes <tt>120</tt> and
	 * <tt>00012</tt> becomes <tt>12</tt>.
	 *
	 * @param value The value to normalize.
	 * @return The canonical representation of <tt>value</tt>.
	 * @throws IllegalArgumentException If the supplied value
	 * is not a legal integer.
	 **/
	static String normalizeInteger(String value) {
		return _normalizeIntegerValue(value, null, null);
	}

	/**
	 * Normalizes an xsd:negativeInteger.
	 **/
	static String normalizeNegativeInteger(String value) {
		return _normalizeIntegerValue(value, null, "-1");
	}

	/**
	 * Normalizes an xsd:nonPositiveInteger.
	 **/
	static String normalizeNonPositiveInteger(String value) {
		return _normalizeIntegerValue(value, null, "0");
	}

	/**
	 * Normalizes an xsd:nonNegativeInteger.
	 **/
	static String normalizeNonNegativeInteger(String value) {
		return _normalizeIntegerValue(value, "0", null);
	}

	/**
	 * Normalizes an xsd:positiveInteger.
	 **/
	static String normalizePositiveInteger(String value) {
		return _normalizeIntegerValue(value, "1", null);
	}

	/**
	 * Normalizes an xsd:long.
	 **/
	static String normalizeLong(String value) {
		return _normalizeIntegerValue(value, "-9223372036854775808", "9223372036854775807");
	}

	/**
	 * Normalizes an xsd:int.
	 **/
	static String normalizeInt(String value) {
		return _normalizeIntegerValue(value, "-2147483648", "2147483647");
	}

	/**
	 * Normalizes an xsd:short.
	 **/
	static String normalizeShort(String value) {
		return _normalizeIntegerValue(value, "-32768", "32767");
	}

	/**
	 * Normalizes an xsd:byte.
	 **/
	static String normalizeByte(String value) {
		return _normalizeIntegerValue(value, "-128", "127");
	}

	/**
	 * Normalizes an xsd:unsignedLong.
	 **/
	static String normalizeUnsignedLong(String value) {
		return _normalizeIntegerValue(value, "0", "18446744073709551615");
	}

	/**
	 * Normalizes an xsd:unsignedInt.
	 **/
	static String normalizeUnsignedInt(String value) {
		return _normalizeIntegerValue(value, "0", "4294967295");
	}

	/**
	 * Normalizes an xsd:unsignedShort.
	 **/
	static String normalizeUnsignedShort(String value) {
		return _normalizeIntegerValue(value, "0", "65535");
	}

	/**
	 * Normalizes an xsd:unsignedByte.
	 **/
	static String normalizeUnsignedByte(String value) {
		return _normalizeIntegerValue(value, "0", "255");
	}

	/**
	 * Normalizes an integer to its canonical representation and
	 * checks that the value is in the range [minValue, maxValue].
	 **/
	private static String _normalizeIntegerValue(String integer, String minValue, String maxValue) {
		integer = collapseWhiteSpace(integer);

		String errMsg = "Not a legal integer: " + integer;

		int intLength = integer.length();

		if (intLength == 0) {
            throw new IllegalArgumentException(errMsg);
		}

		int idx = 0;

		// process any sign info
		boolean isNegative = false;
		if (integer.charAt(idx) == '-') {
			isNegative = true;
			idx++;
		}
		else if (integer.charAt(idx) == '+') {
			idx++;
		}

		if (idx == intLength) {
            throw new IllegalArgumentException(errMsg);
		}

		if (integer.charAt(idx) == '0' && idx < intLength - 1) {
			// integer starts with a zero followed by more characters,
			// skip any leading zeros
			idx++;
			while (idx < intLength - 1 && integer.charAt(idx) == '0') {
				idx++;
			}
		}

		String norm = integer.substring(idx);

		// Check that all characters in 'norm' are digits
		for (int i = 0; i < norm.length(); i++) {
			if (!_isDigit(norm.charAt(i))) {
                throw new IllegalArgumentException(errMsg);
			}
		}

		if (isNegative && norm.charAt(0) != '0') {
			norm = "-" + norm;
		}

		// Check lower and upper bounds, if applicable
		if (minValue != null) {
			if (compareCanonicalIntegers(norm, minValue) < 0) {
                throw new IllegalArgumentException(errMsg);
			}
		}
		if (maxValue != null) {
			if (compareCanonicalIntegers(norm, maxValue) > 0) {
                throw new IllegalArgumentException(errMsg);
			}
		}

		return norm;
	}

	/**
	 * Normalizes a float to its canonical representation.
	 *
	 * @param value The value to normalize.
	 * @return The canonical representation of <tt>value</tt>.
	 * @throws IllegalArgumentException If the supplied value
	 * is not a legal float.
	 **/
	static String normalizeFloat(String value) {
		return _normalizeFPNumber(value, "-16777215.0", "16777215.0", "-149", "104");
	}

	/**
	 * Normalizes a double to its canonical representation.
	 *
	 * @param value The value to normalize.
	 * @return The canonical representation of <tt>value</tt>.
	 * @throws IllegalArgumentException If the supplied value
	 * is not a legal double.
	 **/
	static String normalizeDouble(String value) {
		return _normalizeFPNumber(value, "-9007199254740991.0", "9007199254740991.0", "-1075", "970");
	}

	/**
	 * Normalizes a floating point number to its canonical
	 * representation.
	 *
	 * @param value The value to normalize.
	 * @return The canonical representation of <tt>value</tt>.
	 * @throws IllegalArgumentException If the supplied value
	 * is not a legal floating point number.
	 **/
	static String normalizeFPNumber(String value) {
		return _normalizeFPNumber(value, null, null, null, null);
	}


	/**
	 * Normalizes a floating point number to its canonical
	 * representation.
	 *
	 * @param value The value to normalize.
	 * @param minMantissa A normalized decimal indicating the lowest
	 * value that the mantissa may have.
	 * @param maxMantissa A normalized decimal indicating the highest
	 * value that the mantissa may have.
	 * @param minExponent A normalized integer indicating the lowest
	 * value that the exponent may have.
	 * @param maxExponent A normalized integer indicating the highest
	 * value that the exponent may have.
	 * @return The canonical representation of <tt>value</tt>.
	 * @throws IllegalArgumentException If the supplied value
	 * is not a legal floating point number.
	 **/
	private static String _normalizeFPNumber(
		String value,
		String minMantissa, String maxMantissa,
		String minExponent, String maxExponent)
	{
		value = collapseWhiteSpace(value);

		// handle special values
		if (value.equals("INF") || value.equals("-INF") || value.equals("NaN")) {
			return value;
		}

		// Search for the exponent character E or e
		int eIdx = value.indexOf('E');
		if (eIdx == -1) {
			// try lower case
			eIdx = value.indexOf('e');
		}

		// Extract mantissa and exponent
		String mantissa, exponent;
		if (eIdx == -1) {
			mantissa = normalizeDecimal(value);
			exponent = "0";
		}
		else {
			mantissa = normalizeDecimal(value.substring(0, eIdx));
			exponent = normalizeInteger(value.substring(eIdx + 1));
		}

		// Check lower and upper bounds, if applicable
		if (minMantissa != null) {
			if (compareCanonicalDecimals(mantissa, minMantissa) < 0) {
                throw new IllegalArgumentException("Mantissa smaller than minimum value (" + minMantissa + ")");
			}
		}
		if (maxMantissa != null) {
			if (compareCanonicalDecimals(mantissa, maxMantissa) > 0) {
                throw new IllegalArgumentException("Mantissa larger than maximum value (" + minMantissa + ")");
			}
		}
		if (minExponent != null) {
			if (compareCanonicalIntegers(exponent, minExponent) < 0) {
                throw new IllegalArgumentException("Exponent smaller than minimum value (" + minExponent + ")");
			}
		}
		if (maxExponent != null) {
			if (compareCanonicalIntegers(exponent, maxExponent) > 0) {
                throw new IllegalArgumentException("Exponent larger than maximum value (" + maxExponent + ")");
			}
		}

		// Normalize mantissa to one non-zero digit before the dot
		int shift = 0;

		int dotIdx = mantissa.indexOf('.');
		int digitCount = dotIdx;
		if (mantissa.charAt(0) == '-') {
			digitCount--;
		}

		if (digitCount > 1) {
			// more than one digit before the dot, e.g 123.45, -10.0 or 100.0
			StringBuffer buf = new StringBuffer(mantissa.length());
			int firstDigitIdx = 0;
			if (mantissa.charAt(0) == '-') {
				buf.append('-');
				firstDigitIdx = 1;
			}
			buf.append(mantissa.charAt(firstDigitIdx));
			buf.append('.');
			buf.append(mantissa.substring(firstDigitIdx + 1, dotIdx));
			buf.append(mantissa.substring(dotIdx + 1));

			mantissa = buf.toString();

			// Check if the mantissa has excessive trailing zeros.
			// For example, 100.0 will be normalize to 1.000 and
			// -10.0 to -1.00.
			int nonZeroIdx = mantissa.length() - 1;
			while (nonZeroIdx >= 3 && mantissa.charAt(nonZeroIdx) == '0') {
				nonZeroIdx--;
			}

			if (nonZeroIdx < 3 && mantissa.charAt(0) == '-') {
				nonZeroIdx++;
			}

			if (nonZeroIdx < mantissa.length() - 1) {
				mantissa = mantissa.substring(0, nonZeroIdx + 1);
			}

			shift = 1 - digitCount;
		}
		else if (mantissa.startsWith("0.") || mantissa.startsWith("-0.")) {
			// Example mantissas: 0.0, -0.1, 0.00345 and 0.09
			// search first non-zero digit
			int nonZeroIdx = 2;
			while (nonZeroIdx < mantissa.length() && mantissa.charAt(nonZeroIdx) == '0') {
				nonZeroIdx++;
			}

			// 0.0 does not need any normalization:
			if (nonZeroIdx < mantissa.length()) {
				StringBuffer buf = new StringBuffer(mantissa.length());
				buf.append(mantissa.charAt(nonZeroIdx));
				buf.append('.');
				if (nonZeroIdx == mantissa.length() - 1) {
					// There was only one non-zero digit, e.g. as in 0.09
					buf.append('0');
				}
				else {
					buf.append(mantissa.substring(nonZeroIdx + 1));
				}

				mantissa = buf.toString();
				shift = nonZeroIdx - 1;
			}
		}

		if (shift != 0) {
			try {
				int exp = Integer.parseInt(exponent);
				exponent = String.valueOf(exp - shift);
			}
			catch (NumberFormatException e) {
				throw new RuntimeException("NumberFormatException: " + e.getMessage());
			}
		}

		return mantissa + "E" + exponent;
	}

	/**
	 * Normalizes an xsd:dateTime.
	 *
	 * @param value The value to normalize.
	 * @return The normalized value.
	 * @throws IllegalArgumentException If the supplied value is not a legal
	 * xsd:dateTime value.
	 */
	static String normalizeDateTime(String value) {
		DateTime dt = new DateTime(value);
		dt.normalize();
		return dt.toString();
	}

	/**
	 * Replaces all contiguous sequences of #x9 (tab), #xA (line feed) and #xD
	 * (carriage return) with a single #x20 (space) character, and removes any
	 * leading and trailing whitespace characters, as specified for whiteSpace
	 * facet <tt>collapse</tt>.
	 **/
	static String collapseWhiteSpace(String s) {
		// Note: the following code is optimized for the case where no white
		// space collapsing is necessary, which (hopefully) is often the case.
		StringTokenizer st = new StringTokenizer(s, "\t\r\n ");

		if (!st.hasMoreTokens()) {
			// Empty string or string containing white space only
			return "";
		}
		else {
			String firstToken = st.nextToken();

			if (!st.hasMoreTokens()) {
				// Single token only, no need to create a StringBuffer
				return firstToken;
			}
			else {
				StringBuffer buf = new StringBuffer(s.length());
				buf.append(firstToken);

				while (st.hasMoreTokens()) {
					buf.append(' ').append(st.nextToken());
				}

				return buf.toString();
			}
		}
	}

	static int compare(String value1, String value2, Uri datatype) {
		if (datatype.equals(DECIMAL)) {
			return compareDecimals(value1, value2);
		}
		else if (datatype.equals(INTEGER)) {
			return compareIntegers(value1, value2);
		}
		else if (datatype.equals(NEGATIVE_INTEGER)) {
			return compareNegativeIntegers(value1, value2);
		}
		else if (datatype.equals(NON_POSITIVE_INTEGER)) {
			return compareNonPositiveIntegers(value1, value2);
		}
		else if (datatype.equals(NON_NEGATIVE_INTEGER)) {
			return compareNonNegativeIntegers(value1, value2);
		}
		else if (datatype.equals(POSITIVE_INTEGER)) {
			return comparePositiveIntegers(value1, value2);
		}
		else if (datatype.equals(LONG)) {
			return compareLongs(value1, value2);
		}
		else if (datatype.equals(INT)) {
			return compareInts(value1, value2);
		}
		else if (datatype.equals(SHORT)) {
			return compareShorts(value1, value2);
		}
		else if (datatype.equals(BYTE)) {
			return compareBytes(value1, value2);
		}
		else if (datatype.equals(UNSIGNED_LONG)) {
			return compareUnsignedLongs(value1, value2);
		}
		else if (datatype.equals(UNSIGNED_INT)) {
			return compareUnsignedInts(value1, value2);
		}
		else if (datatype.equals(UNSIGNED_SHORT)) {
			return compareUnsignedShorts(value1, value2);
		}
		else if (datatype.equals(UNSIGNED_BYTE)) {
			return compareUnsignedBytes(value1, value2);
		}
		else if (datatype.equals(FLOAT)) {
			return compareFloats(value1, value2);
		}
		else if (datatype.equals(DOUBLE)) {
			return compareDoubles(value1, value2);
		}
		else if (datatype.equals(DATETIME)) {
			return compareDateTime(value1, value2);
		}
		else {
            throw new IllegalArgumentException("Data type: " + datatype + " not ordered");
		}
	}

	/**
	 * Compares two decimals to eachother.
	 *
	 * @return A negative number if <tt>dec1</tt> is smaller than
	 * <tt>dec2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>dec1</tt> is larger than <tt>dec2</tt>.
	 * @throws IllegalArgumentException If one of the supplied strings is
	 * not a legal decimal.
	 **/
	static int compareDecimals(String dec1, String dec2) {
		dec1 = normalizeDecimal(dec1);
		dec2 = normalizeDecimal(dec2);

		return compareCanonicalDecimals(dec1, dec2);
	}


	/**
	 * Compares two canonical decimals to eachother.
	 *
	 * @return A negative number if <tt>dec1</tt> is smaller than
	 * <tt>dec2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>dec1</tt> is larger than <tt>dec2</tt>. The
	 * result is undefined when one or both of the arguments is not
	 * a canonical decimal.
	 * @throws IllegalArgumentException If one of the supplied strings is
	 * not a legal decimal.
	 **/
	static int compareCanonicalDecimals(String dec1, String dec2) {
		if (dec1.equals(dec2)) {
			return 0;
		}

		// Check signs
		if (dec1.charAt(0) == '-' && dec2.charAt(0) != '-') {
			// dec1 is negative, dec2 is not
			return -1;
		}
		if (dec2.charAt(0) == '-' && dec1.charAt(0) != '-') {
			// dec2 is negative, dec1 is not
			return 1;
		}

		int dotIdx1 = dec1.indexOf('.');
		int dotIdx2 = dec2.indexOf('.');

		// The decimal with the most digits before the dot is the largest
		int result = dotIdx1 - dotIdx2;

		if (result == 0) {
			// equal number of digits before the dot, compare them
			for (int i = 0; result == 0 && i < dotIdx1; i++) {
				result = dec1.charAt(i) - dec2.charAt(i);
			}

			// Continue comparing digits after the dot if necessary
			int dec1Length = dec1.length();
			int dec2Length = dec2.length();
			int lastIdx = dec1Length <= dec2Length ? dec1Length : dec2Length;

			for (int i = dotIdx1 + 1; result == 0 && i < lastIdx; i++) {
				result = dec1.charAt(i) - dec2.charAt(i);
			}

			// Still equal? The decimal with the most digits is the largest
			if (result == 0) {
				result = dec1Length - dec2Length;
			}
		}

		if (dec1.charAt(0) == '-') {
			// reverse result for negative values
			result = -result;
		}

		return result;
	}

	/**
	 * Compares two integers to eachother.
	 *
	 * @return A negative number if <tt>int1</tt> is smaller than
	 * <tt>int2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>int1</tt> is larger than <tt>int2</tt>.
	 * @throws IllegalArgumentException If one of the supplied strings is
	 * not a legal integer.
	 **/
	static int compareIntegers(String int1, String int2) {
		int1 = normalizeInteger(int1);
		int2 = normalizeInteger(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	/**
	 * Compares two canonical integers to eachother.
	 *
	 * @return A negative number if <tt>int1</tt> is smaller than
	 * <tt>int2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>int1</tt> is larger than <tt>int2</tt>. The
	 * result is undefined when one or both of the arguments is not
	 * a canonical integer.
	 * @throws IllegalArgumentException If one of the supplied strings is
	 * not a legal integer.
	 **/
	static int compareCanonicalIntegers(String int1, String int2) {
		if (int1.equals(int2)) {
			return 0;
		}

		// Check signs
		if (int1.charAt(0) == '-' && int2.charAt(0) != '-') {
			// int1 is negative, int2 is not
			return -1;
		}
		if (int2.charAt(0) == '-' && int1.charAt(0) != '-') {
			// int2 is negative, int1 is not
			return 1;
		}

		// The integer with the most digits is the largest
		int result = int1.length() - int2.length();

		if (result == 0) {
			// equal number of digits, compare them
			for (int i = 0; result == 0 && i < int1.length(); i++) {
				result = int1.charAt(i) - int2.charAt(i);
			}
		}

		if (int1.charAt(0) == '-') {
			// reverse result for negative values
			result = -result;
		}

		return result;
	}

	static int compareNegativeIntegers(String int1, String int2) {
		int1 = normalizeNegativeInteger(int1);
		int2 = normalizeNegativeInteger(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareNonPositiveIntegers(String int1, String int2) {
		int1 = normalizeNonPositiveInteger(int1);
		int2 = normalizeNonPositiveInteger(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareNonNegativeIntegers(String int1, String int2) {
		int1 = normalizeNonNegativeInteger(int1);
		int2 = normalizeNonNegativeInteger(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int comparePositiveIntegers(String int1, String int2) {
		int1 = normalizePositiveInteger(int1);
		int2 = normalizePositiveInteger(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareLongs(String int1, String int2) {
		int1 = normalizeLong(int1);
		int2 = normalizeLong(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareInts(String int1, String int2) {
		int1 = normalizeInt(int1);
		int2 = normalizeInt(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareShorts(String int1, String int2) {
		int1 = normalizeShort(int1);
		int2 = normalizeShort(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareBytes(String int1, String int2) {
		int1 = normalizeByte(int1);
		int2 = normalizeByte(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareUnsignedLongs(String int1, String int2) {
		int1 = normalizeUnsignedLong(int1);
		int2 = normalizeUnsignedLong(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareUnsignedInts(String int1, String int2) {
		int1 = normalizeUnsignedInt(int1);
		int2 = normalizeUnsignedInt(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareUnsignedShorts(String int1, String int2) {
		int1 = normalizeUnsignedShort(int1);
		int2 = normalizeUnsignedShort(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	static int compareUnsignedBytes(String int1, String int2) {
		int1 = normalizeUnsignedByte(int1);
		int2 = normalizeUnsignedByte(int2);

		return compareCanonicalIntegers(int1, int2);
	}

	/**
	 * Compares two floats to eachother.
	 *
	 * @return A negative number if <tt>float1</tt> is smaller than
	 * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal float or if <tt>NaN</tt> is compared to a float
	 * other than <tt>NaN</tt>.
	 **/
	static int compareFloats(String float1, String float2) {
		float1 = normalizeFloat(float1);
		float2 = normalizeFloat(float2);

		return compareCanonicalFloats(float1, float2);
	}

	/**
	 * Compares two canonical floats to eachother.
	 *
	 * @return A negative number if <tt>float1</tt> is smaller than
	 * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>. The
	 * result is undefined when one or both of the arguments is not
	 * a canonical float.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal float or if <tt>NaN</tt> is compared to a
	 * float other than <tt>NaN</tt>.
	 **/
	static int compareCanonicalFloats(String float1, String float2) {
		return compareCanonicalFPNumbers(float1, float2);
	}

	/**
	 * Compares two doubles to eachother.
	 *
	 * @return A negative number if <tt>double1</tt> is smaller than
	 * <tt>double2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>double1</tt> is larger than <tt>double2</tt>.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal double or if <tt>NaN</tt> is compared to a
	 * double other than <tt>NaN</tt>.
	 **/
	static int compareDoubles(String double1, String double2) {
		double1 = normalizeDouble(double1);
		double2 = normalizeDouble(double2);

		return compareCanonicalDoubles(double1, double2);
	}

	/**
	 * Compares two canonical doubles to eachother.
	 *
	 * @return A negative number if <tt>double1</tt> is smaller than
	 * <tt>double2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>double1</tt> is larger than <tt>double2</tt>. The
	 * result is undefined when one or both of the arguments is not
	 * a canonical double.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal double or if <tt>NaN</tt> is compared to a
	 * double other than <tt>NaN</tt>.
	 **/
	static int compareCanonicalDoubles(String double1, String double2) {
		return compareCanonicalFPNumbers(double1, double2);
	}

	/**
	 * Compares two floating point numbers to eachother.
	 *
	 * @return A negative number if <tt>float1</tt> is smaller than
	 * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal floating point number or if <tt>NaN</tt> is
	 * compared to a floating point number other than <tt>NaN</tt>.
	 **/
	static int compareFPNumbers(String fp1, String fp2) {
		fp1 = normalizeFPNumber(fp1);
		fp2 = normalizeFPNumber(fp2);

		return compareCanonicalFPNumbers(fp1, fp2);
	}

	/**
	 * Compares two canonical floating point numbers to eachother.
	 *
	 * @return A negative number if <tt>float1</tt> is smaller than
	 * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
	 * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>. The
	 * result is undefined when one or both of the arguments is not
	 * a canonical floating point number.
	 * @throws IllegalArgumentException If one of the supplied strings
	 * is not a legal floating point number or if <tt>NaN</tt> is
	 * compared to a floating point number other than <tt>NaN</tt>.
	 **/
	static int compareCanonicalFPNumbers(String float1, String float2) {
		// Handle special case NaN
		if (float1.equals("NaN") || float2.equals("NaN")) {
			if (float1.equals(float2)) {
				// NaN is equal to itself
				return 0;
			}
			else {
                throw new IllegalArgumentException("NaN cannot be compared to other floats");
			}
		}

		// Handle special case INF
		if (float1.equals("INF")) {
			return (float2.equals("INF")) ? 0 : 1;
		}
		else if (float2.equals("INF")) {
			return -1;
		}

		// Handle special case -INF
		if (float1.equals("-INF")) {
			return (float2.equals("-INF")) ? 0 : -1;
		}
		else if (float2.equals("-INF")) {
			return 1;
		}

		// Check signs
		if (float1.charAt(0) == '-' && float2.charAt(0) != '-') {
			// float1 is negative, float2 is not
			return -1;
		}
		if (float2.charAt(0) == '-' && float1.charAt(0) != '-') {
			// float2 is negative, float1 is not
			return 1;
		}

		int eIdx1 = float1.indexOf('E');
		String mantissa1 = float1.substring(0, eIdx1);
		String exponent1 = float1.substring(eIdx1 + 1);

		int eIdx2 = float2.indexOf('E');
		String mantissa2 = float2.substring(0, eIdx2);
		String exponent2 = float2.substring(eIdx2 + 1);

		// Compare exponents
		int result = compareCanonicalIntegers(exponent1, exponent2);

		if (result != 0 && float1.charAt(0) == '-') {
			// reverse result for negative values
			result = -result;
		}

		if (result == 0) {
			// Equal exponents, compare mantissas
			result = compareCanonicalDecimals(mantissa1, mantissa2);
		}

		return result;
	}

	/**
	 * Compares two dateTime objects. <b>Important:</b> The comparison only
	 * works if both values have, or both values don't have specified a valid
	 * value for the timezone.
	 *
	 * @param value1 An xsd:dateTime value.
	 * @param value2 An xsd:dateTime value.
	 * @return <tt>-1</tt> if <tt>value1</tt> is before <tt>value2</tt> (i.e. if
	 * the dateTime object represented by value1 is before the dateTime object
	 * represented by value2), <tt>0</tt> if both are equal and <tt>1</tt> if
	 * <tt>value2</tt> is before <tt>value1</tt><br>.
	 */
	static int compareDateTime(String value1, String value2) {
		DateTime dateTime1 = new DateTime(value1);
		DateTime dateTime2 = new DateTime(value2);

		dateTime1.normalize();
		dateTime2.normalize();

		return dateTime1.compareTo(dateTime2);
	}

	/**
	 * Checks whether the supplied character is a digit.
	 */
	private static final boolean _isDigit(char c) {
		return c >= '0' && c <= '9';
    }

    private static class DateTime implements Cloneable, Comparable {

        /** The raw dateTime string that was used to initialize this object. */
        private String _dateTimeString;

        /** Flag indicating whether the year is positive or negative. */
        private boolean _isNegativeYear;

        /** year part of the dateTime object as String */
        private String _year;

        /** month part of the dateTime object as String */
        private String _months;

        /** day part of the dateTime object as String */
        private String _days;

        /** hour part of the dateTime object as String */
        private String _hours;

        /** minutes part of the dateTime object as String */
        private String _minutes;

        /** seconds part of the dateTime object as String */
        private String _seconds;

        /** fractional seconds part of the dateTime object as String */
        private String _fractionalSeconds;

        /** Flag indicating whether the timezone, if any, is positive or negative. */
        private boolean _isNegativeTimezone;

        /** hours part of the optional timezone as String */
        private String _hoursTimezone;

        /** minutes part of the optional timezone as String */
        private String _minutesTimezone;

        /** year part of the dateTime object as int */
        private int _iYear;

        /** month part of the dateTime object as int */
        private int _iMonths;

        /** day part of the dateTime object as int */
        private int _iDays;

        /** hour part of the dateTime object as int */
        private int _iHours;

        /** minute part of the dateTime object as int */
        private int _iMinutes;

        /** second part of the dateTime object as int */
        private int _iSeconds;

        /** fractional seconds part of the dateTime object as int */
        private double _iFractionalSeconds;

        /** hours part of the optional timezone as int */
        private int _iHoursTimezone;

        /** minutes part of the optional timezone as int */
        private int _iMinutesTimezone;

        /** Flag indicating whether the values have been normalized. */
        private boolean _isNormalized = false;

        /**
         * Creates a new DateTime object for the supplied xsd:dateTime string value.
         *
         * @param dateTimeString An xsd:dateTime value, for example
         * <tt>1999-05-31T13:20:00-05:00</tt>.
         */
        DateTime(String dateTimeString) {
            _dateTimeString = dateTimeString;
            _parseDateTimeString();
            _setNumericFields();
            _validateFieldValues();
        }

        private void _parseDateTimeString() {
            if (_dateTimeString.length() < 19) {
                throw new IllegalArgumentException(
                        "String value too short to be a valid xsd:dateTime value: " + _dateTimeString);
            }

            String errMsg = "Invalid xsd:dateTime value: " + _dateTimeString;

            StringTokenizer st = new StringTokenizer(_dateTimeString, "+-:.TZ", true);
            try {
                _year = st.nextToken();
                _isNegativeYear = _year.equals("-");
                if (_isNegativeYear) {
                    _year = st.nextToken();
                }
                _verifyTokenValue(st.nextToken(), "-", errMsg);
                _months = st.nextToken();
                _verifyTokenValue(st.nextToken(), "-", errMsg);
                _days = st.nextToken();
                _verifyTokenValue(st.nextToken(), "T", errMsg);
                _hours = st.nextToken();
                _verifyTokenValue(st.nextToken(), ":", errMsg);
                _minutes = st.nextToken();
                _verifyTokenValue(st.nextToken(), ":", errMsg);
                _seconds = st.nextToken();

                String token = st.hasMoreTokens() ? st.nextToken() : null;

                if (".".equals(token)) {
                    _fractionalSeconds = st.nextToken();
                    token = st.hasMoreTokens() ? st.nextToken() : null;
                }

                if ("+".equals(token) || "-".equals(token)) {
                    _isNegativeTimezone = "-".equals(token);
                    _hoursTimezone = st.nextToken();
                    _verifyTokenValue(st.nextToken(), ":", errMsg);
                    _minutesTimezone = st.nextToken();
                }
                else if ("Z".equals(token)) {
                    _isNegativeTimezone = false;
                    _hoursTimezone = _minutesTimezone = "00";
                }

                if (st.hasMoreTokens()) {
                    throw new IllegalArgumentException(errMsg);
                }
            }
            catch (NoSuchElementException e) {
                throw new IllegalArgumentException(errMsg);
            }
        }

        private void _verifyTokenValue(String token, String expected, String errMsg) {
            if (!token.equals(expected)) {
                throw new IllegalArgumentException(errMsg);
            }
        }

        private void _setNumericFields() {
            try {
                // FIXME: the following statement fails when the year is
                // outside the range of integers (comment by Arjohn)
                _iYear = Integer.parseInt(_year);
                _iMonths = Integer.parseInt(_months);
                _iDays = Integer.parseInt(_days);
                _iHours = Integer.parseInt(_hours);
                _iMinutes = Integer.parseInt(_minutes);
                _iSeconds = Integer.parseInt(_seconds);

                if (_fractionalSeconds != null) {
                    // FIXME: the following statement fails when the fractional
                    // seconds are outside the range of doubles (comment by Arjohn)
                    _iFractionalSeconds = Double.parseDouble("0." + _fractionalSeconds);
                }
                if (_hoursTimezone != null) {
                    _iHoursTimezone = Integer.parseInt(_hoursTimezone);
                }
                if (_minutesTimezone != null) {
                    _iMinutesTimezone = Integer.parseInt(_minutesTimezone);
                }
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("All fields must be numbers: " + _dateTimeString);
            }
        }

        private void _validateFieldValues() {
            if (_year.length() < 4) {
                throw new IllegalArgumentException("Year field requires at least 4 digits: " + _dateTimeString);
            }
            if (_months.length() != 2) {
                throw new IllegalArgumentException("Month field must be two digits: " + _dateTimeString);
            }
            if (_days.length() != 2) {
                throw new IllegalArgumentException("Days field must be two digits: " + _dateTimeString);
            }
            if (_hours.length() != 2) {
                throw new IllegalArgumentException("Hours field must be two digits: " + _dateTimeString);
            }
            if (_minutes.length() != 2) {
                throw new IllegalArgumentException("Minutes field must be two digits: " + _dateTimeString);
            }
            if (_seconds.length() != 2) {
                throw new IllegalArgumentException("Seconds field must be two digits: " + _dateTimeString);
            }
            if (_hoursTimezone != null) {
                if (_hoursTimezone.length() != 2) {
                    throw new IllegalArgumentException("Timezone-hours field must be two digits: " + _dateTimeString);
                }
                if (_minutesTimezone.length() != 2) {
                    throw new IllegalArgumentException("Timezone-minutes field must be two digits: " + _dateTimeString);
                }
            }

            if (_year.length() > 4 && _year.charAt(0) == '0') {
                throw new IllegalArgumentException("Leading zeros in years with more than 4 digits are prohibited: " + _dateTimeString);
            }
            if (_iYear == 0) {
                throw new IllegalArgumentException("0000 is not a valid year: " + _dateTimeString);
            }
            if (_iHours > 24) {
                throw new IllegalArgumentException("Invalid hour value: " + _dateTimeString);
            }
            if (_iMinutes > 59) {
                throw new IllegalArgumentException("Invalid minute value: " + _dateTimeString);
            }
            if (_iSeconds > 59) {
                throw new IllegalArgumentException("Invalid second value: " + _dateTimeString);
            }
            if (_iHours == 24 && (_iMinutes != 0 || _iSeconds != 0)) {
                throw new IllegalArgumentException("Invalid time: " + _dateTimeString);
            }
            if (_iHoursTimezone > 14 ||
                _iMinutesTimezone > 59 ||
                _iHoursTimezone == 14 && _iMinutesTimezone != 0)
            {
                throw new IllegalArgumentException("Invalid timezone: " + _dateTimeString);
            }
        }

        /**
         * Checks whether this object has already been normalized.
         */
        boolean isNormalized() {
            return _isNormalized;
        }

        /**
         * Normalizes this dateTime object.
         */
        void normalize() {
            if (_isNormalized) {
                // Values already normalized
                return;
            }

            if (_iHours == 24 ||
                _hoursTimezone != null && (_iHoursTimezone != 0 || _iMinutesTimezone != 0))
            {
                // Normalize the timezone to Coordinated Universal Time (UTC)

                // Insert values into a GregorianCalendar object.
                // Note: GregorianCalendar uses 0-based months
                Calendar cal = new GregorianCalendar(_iYear, _iMonths - 1, _iDays, _iHours, _iMinutes, _iSeconds);
                if (_isNegativeYear) {
                    cal.set(Calendar.ERA, GregorianCalendar.BC);
                }

                // Add/subtract the timezone
                if (_isNegativeTimezone) {
                    cal.add(Calendar.HOUR_OF_DAY, _iHoursTimezone);
                    cal.add(Calendar.MINUTE, _iMinutesTimezone);
                }
                else {
                    cal.add(Calendar.HOUR_OF_DAY, -_iHoursTimezone);
                    cal.add(Calendar.MINUTE, -_iMinutesTimezone);
                }

                // Get the updated fields
                if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
                    _isNegativeYear = true;
                }
                _iYear = cal.get(Calendar.YEAR);
                _iMonths = cal.get(Calendar.MONTH) + 1;
                _iDays = cal.get(Calendar.DAY_OF_MONTH);
                _iHours = cal.get(Calendar.HOUR_OF_DAY);
                _iMinutes = cal.get(Calendar.MINUTE);
                _iSeconds = cal.get(Calendar.SECOND);

                _year = _int2string(_iYear, 4);
                _months = _int2string(_iMonths, 2);
                _days = _int2string(_iDays, 2);
                _hours = _int2string(_iHours, 2);
                _minutes = _int2string(_iMinutes, 2);
                _seconds = _int2string(_iSeconds, 2);

                if (_hoursTimezone != null) {
                    _iHoursTimezone = _iMinutesTimezone = 0;
                    _hoursTimezone = _minutesTimezone = "00";
                    _isNegativeTimezone = false;
                }
            }

            if (_fractionalSeconds != null) {
                // Remove any trailing zeros
                int zeroCount = 0;
                for (int i = _fractionalSeconds.length() - 1; i >= 0; i--) {
                    if (_fractionalSeconds.charAt(i) == '0') {
                        zeroCount++;
                    }
                    else {
                        break;
                    }
                }

                if (zeroCount == _fractionalSeconds.length()) {
                    _fractionalSeconds = null;
                }
                else if (zeroCount > 0) {
                    _fractionalSeconds = _fractionalSeconds.substring(0, _fractionalSeconds.length() - zeroCount);
                }
            }

            _isNormalized = true;
        }

        /**
         * Converts an integer to a string, enforcing the resulting string to have
         * at least <tt>minDigits</tt> digits by prepending zeros if it has less
         * than that amount of digits.
         */
        private String _int2string(int iValue, int minDigits) {
            String result = String.valueOf(iValue);

            int zeroCount = minDigits - result.length();
            if (zeroCount > 0) {
                StringBuffer buf = new StringBuffer(minDigits);
                for (int i = 0; i < zeroCount; i++) {
                    buf.append('0');
                }
                buf.append(result);

                result = buf.toString();
            }

            return result;
        }

        /**
         * Returns the xsd:dateTime string-representation of this object.
         *
         * @return An xsd:dateTime value, e.g. <tt>1999-05-31T13:20:00-05:00</tt>.
         */
        @Override
        public String toString() {
            StringBuffer result = new StringBuffer(32);

            if (_isNegativeYear) {
                result.append('-');
            }
            result.append(_year);
            result.append('-');
            result.append(_months);
            result.append('-');
            result.append(_days);
            result.append('T');
            result.append(_hours);
            result.append(':');
            result.append(_minutes);
            result.append(':');
            result.append(_seconds);

            if (_fractionalSeconds != null) {
                result.append('.');
                result.append(_fractionalSeconds);
            }

            if (_hoursTimezone != null) {
                if (_iHoursTimezone == 0 && _iMinutesTimezone == 0) {
                    result.append("Z");
                }
                else {
                    if (_isNegativeTimezone) {
                        result.append('-');
                    }
                    else {
                        result.append('+');
                    }
                    result.append(_hoursTimezone);
                    result.append(':');
                    result.append(_minutesTimezone);
                }
            }

            return result.toString();
        }

        /**
         * Compares this DateTime object to another DateTime object.
         *
         * @throws ClassCastException If <tt>other</tt> is not a DateTime object.
         */
        public int compareTo(Object other) {
            DateTime thisDT = this;
            DateTime otherDT = (DateTime)other;

            if (thisDT._hoursTimezone != null &&
                (thisDT._iHoursTimezone != 0 || thisDT._iMinutesTimezone != 0))
            {
                // Create a normalized copy of this DateTime object
                thisDT = (DateTime)thisDT.clone();
                thisDT.normalize();
            }

            if (otherDT._hoursTimezone != null &&
                (otherDT._iHoursTimezone != 0 || otherDT._iMinutesTimezone != 0))
            {
                // Create a normalized copy of this DateTime object
                otherDT = (DateTime)otherDT.clone();
                otherDT.normalize();
            }

            if (thisDT._isNegativeYear && !otherDT._isNegativeYear) {
                return -1;
            }
            else if (!thisDT._isNegativeYear && otherDT._isNegativeYear) {
                return 1;
            }

            int result = 0;
            if (thisDT._iYear != otherDT._iYear) {
                result = thisDT._iYear - otherDT._iYear;
            }
            else if (thisDT._iMonths != otherDT._iMonths) {
                result = thisDT._iMonths - otherDT._iMonths;
            }
            else if (thisDT._iDays != otherDT._iDays) {
                result = thisDT._iDays - otherDT._iDays;
            }
            else if (thisDT._iHours != otherDT._iHours) {
                result = thisDT._iHours - otherDT._iHours;
            }
            else if (thisDT._iMinutes != otherDT._iMinutes) {
                result = thisDT._iMinutes - otherDT._iMinutes;
            }
            else if (thisDT._iSeconds != otherDT._iSeconds) {
                result = thisDT._iSeconds - otherDT._iSeconds;
            }
            else if (thisDT._iFractionalSeconds != otherDT._iFractionalSeconds) {
                result = (thisDT._iFractionalSeconds < otherDT._iFractionalSeconds) ?  -1 : 1;
            }

            if (thisDT._isNegativeYear) {
                // Invert result for negative years
                result = -result;
            }

            return result;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
