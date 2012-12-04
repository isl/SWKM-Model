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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.springframework.util.Assert;

/**
 * A immutable representation of a URI, with a namespace part and an optional local name.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
public final class Uri {
    /*
     * Note that namespace and localName strings are interned, so it is safe to
     * compare using "==".
     *
     * The delimiter is the last character of the namespace part.
     */
    private final String namespace;
    private final String localName;

    /**
     * An enumeration of the options of including or not the delimiter of the namespace and local part
     * of a {@code Uri} in the textual representation when it has an empty local part.
     * Delimiter characher is one of the symbols {@code #}, {@code /} or {@code :}.
     */
    public static enum Delimiter {
        /**
         * Include the delimiter symbol in the textual representation of a Uri,
         * even if the Uri has an empty local part.
         */
        WITH,

        /**
         * Do not include the delimiter symbol in the textual representation of a Uri
         * if the Uri has an empty local part.
         */
        WITHOUT;
    }

    private Uri(String namespace, String localName, boolean validate) {
        namespace = maskNull(namespace);
        localName = maskNull(localName);
        if (validate) {
            String newLocal = localName;
            if (localName.startsWith("#")) {
                newLocal = localName.substring(1);
                namespace = namespace + "#";
            }
            if (!isValidDelimiter(namespace.charAt(namespace.length() - 1))  &&
                    !localName.isEmpty()) {
                namespace = namespace + "#";
            }
            checkDoesNotContainSharp(namespace.substring(0, namespace.length() - 1), "Base");
            checkDoesNotContainSharp(newLocal, "Local part");
            localName = newLocal;
        }
        //equals() depends on strings being interned
        this.namespace = namespace.intern();
        this.localName = localName.intern();
    }
    
    /**
     * Creates a Uri instance with the specified namespace and local name.
     * 
     * @param namespace the namespace of the created Uri
     * @param localName the localName of the created Uri
     * @throws UriFormatException if the namespace part contains a hash ({@code #}) symbol
     * in any place except its last character, <em>or</em> if the local name part 
     * contains a hash ({@code #}) symbol in any place except its first character
     */
    public Uri(String namespace, String localName) throws UriFormatException {
        this(namespace, localName, true);
    }
    
    /**
     * Parses the textual representation of a URI and creates a Uri instance for it.
     *
     * <p>The expected form of a URI is:<BR>
     * <em>namespacePart</em>[#<em>localNamePart</em>]<BR>
     * <em>or</em><BR>
     * <em>namespacePart</em>[/<em>localNamePart</em>]<BR>
     * <em>or</em><BR>
     * <em>namespacePart</em>[:<em>localNamePart</em>]<BR>
     * where namespacePart and localNamePart consist of any character apart from "#".
     * 
     * @param uri the textual representation of a URI to parse
     * @return a Uri instance that represents the specified URI, guaranteed to be non-null
     * @throws UriFormatException if the specified URI cannot be parsed
     */
    public static Uri parse(String uri) throws UriFormatException {
        Assert.notNull(uri);
        Uri parsedUri = tryParse(uri);
        if (parsedUri == null) {
            throw new UriFormatException("Cannot parse the given string as URI: '" + uri + "'");
        }
        return parsedUri;
    }
    
    /**
     * Parses the textual representation of a URI if possible and creates a Uri instance for it,
     * or returns {@code null} if the URI cannot be parsed.
     * 
     * <p>See {@linkplain #parse(String)} for a description of the expected textual
     * form of a URI to be parsed.
     * 
     * @param uri the textual representation of a URI to parse
     * @return a Uri instance that represents the specified URI, or null if the URI cannot be parsed
     */
    public static Uri tryParse(String uri) {
        if (uri == null) {
            return null;
        }
        // if there are more than one hashes on the uri, an error will occur in
        // the Uri object creation: no need to check now
        int ns = uri.lastIndexOf('#');
        
        if (ns == -1) {
            ns = uri.lastIndexOf('/');
        }

        if (ns == -1) {
            ns = uri.lastIndexOf(':');
        }
        
        if (ns == -1) {
            return new Uri(uri, "");
        }
        try {
            return new Uri(uri.substring(0, ns + 1), uri.substring(ns + 1));
        } catch (UriFormatException e) {
            return null;
        }
    }
    
    private static void checkDoesNotContainSharp(String s, String name) {
        if (s.contains("#")) {
            throw new UriFormatException(name + " cannot contain '#'. Was: '" + s + "'");
        }
    }
    
    private static boolean isValidDelimiter(char delimiter) {
        return delimiter == '#' || delimiter == '/' || delimiter == ':';
    }
    
    private static String maskNull(String s) {
        return s != null ? s : "";
    }
    
    /**
     * Returns the namespace of this Uri. Note that the namespace
     * does not end with a hash ({@code #}) symbol.
     * 
     * @return the namespace part of this Uri
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the namespace of this Uri, as a Uri object itself.
     *
     * @return the namespace part of this Uri
     */
    public Uri getNamespaceUri() {
        return new Uri(namespace, "", false);
    }
    
    /**
     * Appends the {@linkplain #getNamespace() namespace} of this Uri to the specified appendable.
     * 
     * @param appendable the appendable on which to append the namespace part
     * @throws NullPointerException if appendable is null
     * @throws RuntimeException if an {@linkplain IOException} occurs (the thrown exception
     * will enclose the IOException)
     */
    public void appendNamespace(Appendable appendable) {
        try {
            appendable.append(namespace);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the local name of this Uri. Note that the local name
     * does not start with a hash ({@code #}) symbol.
     * 
     * @return the namespace part of this Uri
     */
    public String getLocalName() {
        return localName;
    }
    
    /**
     * Appends the {@linkplain #getLocalName() local name} of this Uri to the specified appendable.
     * 
     * @param appendable the appendable on which to append the namespace part
     * @throws NullPointerException if appendable is null
     * @throws RuntimeException if an {@linkplain IOException} occurs (the thrown exception
     * will enclose the IOException)
     */
    public void appendLocalName(Appendable appendable) {
        try {
            appendable.append(localName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns whether {@code this.getNamespace().equals(uri.getNamespace())}.
     * 
     * @param uri the uri to test whether it has an equal namespace with this one
     * @return whether {@code this.getNamespace().equals(uri.getNamespace())}
     * @throws NullPointerException if uri is null
     */
    public boolean hasEqualNamespace(Uri uri) {
        return this.namespace == uri.namespace;
    }
    
    /**
     * Returns whether {@code this.getLocalName().equals(uri.getLocalName())}.
     * 
     * @param uri the uri to test whether it has an equal namespace with this one
     * @return whether {@code this.getLocalName().equals(uri.getLocalName())}
     * @throws NullPointerException if uri is null
     */
    public boolean hasEqualLocalName(Uri uri) {
        return this.localName == uri.localName;
    }
    
    /**
     * Returns whether this URI is valid or not. 
     * 
     * <p>A URI is valid if it does not violate 
     * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>,
     * as argumented by the deviations in {@link java.net.URI}.</p>
     *
     * @return true if this URI is valid; false otherwise.
     */
    public boolean isValid() {
        try {
            new URI(toString(Delimiter.WITH));
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Returns whether this URI is absolute or not.
     *
     * <p>A URI is absolute when it has a scheme component, as defined in RFC 2396</p>
     *
     * @return true if this URI is absolute; false otherwise.
     */
    public boolean isAbsolute() {
        isValid();
        return namespace.contains(":");
    }

    /**
     * Returns whether the specified object is a Uri instance with equal namespace and
     * local part as this Uri.
     * 
     * @param obj {@inheritDoc}
     * @return whether the specified object is a Uri instance with equal namespace and
     * local part as this Uri.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Uri other = (Uri) obj;
        return this.namespace == other.namespace && this.localName == other.localName;
    }
    
    private static final Pattern validNamespacePattern = Pattern.compile("\\p{Alpha}([\\w_\\-/?:.~])*[#/:]?");
    private static final Pattern validLocalNamePattern = Pattern.compile("(\\p{Alpha}([\\w_\\-/?:.~])*)?");
    
    /**
     * @see <a href="https://139.91.183.101/ICSTrac/wiki/RdfSuiteSpecification.txt">RdfSuiteSpecification for character restrictions</a>
     * @return true if and only if this Uri contains only valid characters
     */
    public boolean validateCharacters() {
        return validNamespacePattern.matcher(namespace).matches()
                && validLocalNamePattern.matcher(localName).matches();
    }

    /**
     * Returns a hashCode based on the namespace and local name of this Uri.
     * 
     * @return a hashCode based on the namespace and local name of this Uri
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
        hash = 31 * hash +
                (this.localName != null ? this.localName.hashCode() : 0);
        return hash;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(Delimiter.WITH);
    }
    
    /**
     * Returns a textual representation of this Uri, with an optional delimiter
     * symbol if the local name is empty and {@code delimiter == Delimiter.WITH}.
     * If the local name is not empty, the delimiter is always included, separating
     * the namespace and the local name.
     * 
     * @param hash Hash.WITH to append a hash; Hash.WITHOUT otherwise
     * @return the textual representation of this Uri
     */
    public String toString(Delimiter delimiter) {
        if (localName.isEmpty()) {
            if (delimiter == Delimiter.WITH) {
                return namespace;
            } else {
                char d = namespace.charAt(namespace.length() - 1);
                if (d == '#' || d == '/' || d == ':') {
                    return namespace.substring(0, namespace.length() - 1);
                }
                return namespace;
            }
        } else {
            return namespace + localName;
        }
    }
    
    /**
     * Appends a textual representation of this Uri to the specified Appendable,
     * without a hash ({@code #}) symbol if the local name is empty. 
     * If the local name is not empty, the hash
     * is always included, separating the namespace and the local name.
     * 
     * <p>Equivalent to {@code toString(appendable, Hash.WITHOUT}.
     * 
     * @param appendable the appendable where to append the textual representation
     * of this Uri
     */
    public void toString(Appendable appendable) {
        toString(appendable, Delimiter.WITHOUT);
    }
    
    /**
     * Appends a textual representation of this Uri to the specified Appendable,
     * with an optional hash ({@code #}) symbol if the local name is empty and
     * {@code hash == Hash.WITHOUT}. If the local name is not empty, the hash
     * is always included, separating the namespace and the local name.
     * 
     * @param appendable the appendable where to append the textual representation
     * of this Uri
     * @param hash Hash.WITH to append a hash; Hash.WITHOUT otherwise
     */
    public void toString(Appendable appendable, Delimiter delimiter) {
        try {
            if (localName.isEmpty()) {
                if (delimiter == Delimiter.WITH) {
                    appendable.append(namespace);
                } else {
                    char d = namespace.charAt(namespace.length() - 1);
                    if (d == '#' || d == '/' || d == ':') {
                        appendable.append(namespace.substring(0, namespace.length() - 1));
                    } else {
                        appendable.append(namespace);
                    }
                }
            } else {
                appendable.append(namespace).append(localName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns a Uri that has the same namespace as this Uri, and the specifed local name.
     * 
     * <p>Note that Uri is immutable, i.e. its namespace and local name cannot change.
     * 
     * @param localName the local name of the created Uri
     * @return a Uri that has the same namespace as this Uri, and the specifed local name
     */
    public Uri withLocalName(String localName) {
        return new Uri(namespace, localName);
    }

    /**
     * Returns a Uri that has the same local name as this Uri, and the specifed namespace.
     * 
     * <p>Note that Uri is immutable, i.e. its namespace and local name cannot change.
     * 
     * @param namespace the namespace of the created Uri
     * @return a Uri that has the same local name as this Uri, and the specifed namespace
     */
    public Uri withNamespace(String namespace) {
        return new Uri(namespace, localName);
    }
    
    /**
     * An exceptio thrown when a string cannot be parsed to a Uri.
     */
    public static class UriFormatException extends RuntimeException {
        /**
         * Creates a UriFormatException with a message.
         * 
         * @param message the message of the exception
         */
        public UriFormatException(String message) {
            super(message);
        }

        /**
         * Creates a UriFormatException with a message and a cause.
         * 
         * @param message the message of the exception
         * @param cause the cause of the exception
         */
        public UriFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates a UriFormatException with a cause.
         * 
         * @param cause the cause of the exception
         */
        public UriFormatException(Throwable cause) {
            super(cause);
        }
        
        /**
         * Creates a UriFormatException.
         */
        public UriFormatException() {
        }
    }
}
