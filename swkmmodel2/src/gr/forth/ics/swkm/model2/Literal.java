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


package gr.forth.ics.swkm.model2;

import com.google.common.base.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;

/**
 * A literal value. 
 * 
 * <p> A literal can have one of three forms:
 * <ul>
 * <li>"<em>value</em>"
 * <li>"<em>value</em>"@<em>language</em>
 * <li>"<em>value</em>"^^<em>type</em>
 * </ul>
 * 
 * <p>The <em>type</em> of a literal must, if it exists, to be a URI.
 * 
 * <em>Value</em> can be accessed with {@linkplain #getValue()}, <em>language</em>
 * can be accessed with {@linkplain #getLanguage()} (and test through {@linkplain #hasLanguage()}
 * if there is a language part), and <em>type</em>
 * can be accessed with {@linkplain #getType()} (and test through {@linkplain #hasType()}
 * if there is a language part)
 * 
 * @see <a href="http://www.w3.org/TR/rdf-concepts/#section-Literals">The specification of literals</a>
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Literal {
    private final String value;
    private final String lang;
    private final Uri type;

    Literal(String value, String lang, Uri type) {
        Assert.notNull(value, "value");
        Assert.isTrue(!(lang != null && type != null), "Cannot set both lang and type");
        this.value = value;
        this.lang = lang;
        this.type= type;
    }
    
    /**
     * Returns the <em>value</em> of this literal.
     * 
     * @return the <em>value</em> of this literal
     * @see Literal for details on the different forms and parts of a literal.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Returns whether this literal has a <em>language</em> part.
     * 
     * @return whether this literal has a <em>language</em> part
     * @see Literal for details on the different forms and parts of a literal.
     */
    public boolean hasLanguage() {
        return lang != null;
    }
    
    /**
     * Returns the <em>language</em> part of this literal, or null if this literal hasn't one.
     * 
     * @return the <em>language</em> part of this literal, or null if this literal hasn't one
     * @see Literal for details on the different forms and parts of a literal.
     */
    public String getLanguage() {
        return lang;
    }
    
    /**
     * Returns whether this literal has a <em>type</em> part.
     * 
     * @return whether this literal has a <em>type</em> part
     * @see Literal for details on the different forms and parts of a literal.
     */
    public boolean hasType() {
        return type != null;
    }
    
    /**
     * Returns the <em>type</em> part of this literal, or null if this literal hasn't one.
     * 
     * @return the <em>type</em> part of this literal, or null if this literal hasn't one
     * @see Literal for details on the different forms and parts of a literal.
     */
    public Uri getType() {
        return type;
    }
    
    /**
     * Returns whether the characters contained in this literal's parts are valid.
     * 
     * @see <a href="../../../../../docs/SupportedCharacters.pdf">Supported characters specification.</a>
     * @return whether the characters contained in this literal's parts are valid
     */
    public boolean validateCharacters() {
        for (int i = 0; i < value.length(); i++) {
            if (!validateCharacter(value.charAt(i))) {
                return false;
            }
        }
        if (lang != null) {
            for (int i = 0; i < lang.length(); i++) {
                if (!validateCharacter(lang.charAt(i))) {
                    return false;
                }
            }
        }
        if (type != null) {
            return type.validateCharacters();
        }
        return true;
    }

    private static boolean validateCharacter(char c) {
        if (c >= 0x0010 && c <= 0xFFFF) { //space and beyond
            return true;
        }
        return c == 0x0009; //tab character
    }
    
    @Override
    public String toString() {
        if (hasLanguage()) {
            return "\"" + value + "\"@" + getLanguage();
        } else if (hasType()) {
            return "\"" + value + "\"^^" + getType();
        } else {
            return "\"" + value + "\"";
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value, lang, type);
    }
    
    /**
     * Returns true if and only if the specified object is a Literal with equal
     * <em>value</em>, <em>lang</em>, and <em>type</em>.
     * @param obj {@inheritDoc }
     * @return true if and only if the specified object is a Literal with equal
     * <em>value</em>, <em>lang</em>, and <em>type</em>
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) {
            return false;
        }
        Literal other = (Literal)obj;
        return value.equals(other.value) &&
                Objects.equal(lang, other.lang) &&
                Objects.equal(type, other.type);
    }

    //"literal"
    //"literal"@language
    //"literal"^^type
    private static final Pattern literalPattern = Pattern.compile("\\s*\"(.*?)\"(?:(?:@(.*))|(?:\\^\\^(.*)))?", Pattern.DOTALL);

    /**
     * Creates a Literal by parsing the specified string, or throws an exception if the
     * string cannot be parsed.
     * 
     * @param literal the string to parse in order to create a Literal
     * @return a Literal created by parsing the specified string
     * @throws LiteralFormatException if the specified string cannot be parsed
     */
    public static Literal parse(String literal) {
        Assert.notNull(literal);
        Matcher m = literalPattern.matcher(literal);
        if (!m.find()) {
            throw new LiteralFormatException("Unparsable literal: [" + literal +"], expected forms: \"value\" or \"value\"@lang or " +
                    "\"value\"^^type");
        }
        String value = m.group(1);
        String lang = m.group(2);
        String type = m.group(3);
        return newLiteral(value, lang, type);
    }

    /**
     * Creates a Literal by parsing the specified string if possible, or returns null if the string
     * cannot be parsed.
     * 
     * @param literal the string to parse in order to create a Literal
     * @return a Literal created by parsing the specified string, or null if it cannot be parsed
     */
    public static Literal tryParse(String literal) {
        if (literal == null) {
            return null;
        }
        Matcher m = literalPattern.matcher(literal);
        if (m.matches()) {
            String value = m.group(1);
            String lang = m.group(2);
            String type = m.group(3);
            return newLiteral(value, lang, type);
        } else {
            return null;
        }
    }

    private static Literal newLiteral(String value, String lang, String type) {
        return new Literal(value, lang, type != null ? Uri.parse(type) : null);
    }

    /**
     * Creates a literal that will only have a <em>value</em> part. Note that the value
     * of a literal does not include the quotes.
     * 
     * <p>The textual representation of the created literal will be
     * {@code "value"}.
     * 
     * @param value the <em>value</em> that the created literal will have
     * @return a literal with the specified <em>value</em>, and null
     * <em>language</em> and <em>type</em> parts
     */
    public static Literal create(String value) {
        Assert.notNull(value);
        return new Literal(value, null, null);
    }

    /**
     * Creates a literal that will have a <em>value</em> and a <em>language</em> part. Note that the value
     * of a literal does not include the quotes.
     * 
     * <p>The textual representation of the created literal will be
     * {@code "value"@language}.
     * 
     * @param value the <em>value</em> that the created literal will have
     * @param lang the <em>language</em> that the created literal will have
     * @return a literal with the specified <em>value</em> and <em>language</em> parts,
     * and null <em>type</em> part
     */
    public static Literal createWithLanguage(String value, String lang) {
        Assert.notNull(value);
        return new Literal(value, lang, null);
    }

    /**
     * Creates a literal that will have a <em>value</em> and a <em>type</em> part. Note that the value
     * of a literal does not include the quotes.
     * 
     * <p>The textual representation of the created literal will be
     * {@code "value"^^type}.
     * 
     * @param value the <em>value</em> that the created literal will have
     * @param type the <em>type</em> that the created literal will have
     * @return a literal with the specified <em>value</em> and <em>type</em> parts,
     * and null <em>language</em> part
     */
    public static Literal createWithType(String value, Uri type) {
        Assert.notNull(value);
        return new Literal(value, null, type);
    }

    /**
     * Returns whether this literal value is valid with regards to the validation rules
     * (if any) of its defined type (if defined).
     *
     * @return whether this literal value is valid with regards to the validation rules (if any) of its defined type (if defined)
     */
    public boolean isValid() {
        if (type == null) {
            return true;
        }
        return XmlDatatypeUtil.isValidValue(value, type);
    }

    /**
     * An exception thrown when a string cannot be parsed to a Literal.
     * 
     * @see Literal for details on the different forms and parts of a literal.
     */
    public static class LiteralFormatException extends RuntimeException {
        /**
         * Creates a LiteralFormatException with a cause.
         * 
         * @param cause the cause of the exception
         */
        public LiteralFormatException(Throwable cause) {
            super(cause);
        }

        /**
         * Creates a LiteralFormatException with a message and a cause.
         * 
         * @param message the message of the exception
         * @param cause the cause of the exception
         */
        public LiteralFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates a LiteralFormatException with a message.
         * 
         * @param message the message of the exception
         */
        public LiteralFormatException(String message) {
            super(message);
        }

        /**
         * Creates a LiteralFormatException.
         */
        public LiteralFormatException() {
        }
    }
}