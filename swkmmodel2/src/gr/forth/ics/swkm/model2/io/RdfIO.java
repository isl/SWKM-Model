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


package gr.forth.ics.swkm.model2.io;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import gr.forth.ics.swkm.model2.BlankNode;
import gr.forth.ics.swkm.model2.LiteralNode;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.Uri.Delimiter;
import gr.forth.ics.swkm.model2.Uri.UriFormatException;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterRegistry;
import org.springframework.util.Assert;

/**
 * A class containing utilities for reading and writing RDF content in various serialization
 * {@linkplain Format formats}.
 *
 * <p><h3>Reading</h3>
 *
 * This class allows reading, using various serialization {@linkplain Format formats},
 * from a {@linkplain File}, a {@linkplain URL},
 * a {@linkplain String}, a {@linkplain Reader} or an {@linkplain InputStream}. In case of
 * reading from a File or a URL, the method signatures allow the option of using a
 * default base URI (derived from the File or the URL, respectively). In the rest of the cases, a
 * base URI to be used <em>must</em> be specified. If the base URI is explicitly defined,
 * it overrides any base URI declaration inlined in the input document.
 *
 * <p>During parsing, triples that are not explicitly defined in a named graph (in a Format-specific way,
 * if such a way exists at all for a given serialization format), are put into the named graph
 * given with the {@link InputWithBase#withDefaultNamedGraph(Uri)} method. If this method
 * is not called, then the default named graph is assumed to be {@link RdfSuite#DEFAULT_GRAPH_URI}.
 * Triples that have an explicitly defined named graph are only added to that one, not the default named graph.
 *
 * <h4>Examples of reading</h4>
 *
 * <p>All the following examples assume the existence of a {@code model}
 * variable of type {@linkplain Model}.
 *
 * <p>Reading from an RDFXML file and putting the triples into a specific named graph of a model:
 *{@code RdfIO.read(new File("input.rdf"), Format.RDFXML).withBase("http://base#").withDefaultNamedGraph("http://myNamedGraph").into(model);}
 *
 * <p><h3>Writing</h3>
 *
 * This class allows writing, using the contents of a {@linkplain Model} to
 * a {@linkplain File}, to an {@linkplain OutputStream}, to a {@linkplain Writer} or to a String.
 *
 * <h4>Examples of writing</h4>
 *
 * <p>All the following examples assume the existence of a {@code model}
 * variable of type {@linkplain Model}.
 *
 * <p>Writing an {@code RDF/XML} document to a {@linkplain File}:
 *<pre>{@code
 *RdfIO.write(model, Format.RDFXML).withBase("http://example.com/baseURI").toFile(new File("path/to/output/file.rdf");
 *}</pre>
 * Or without setting a base URI:
 *<pre>{@code
 *RdfIO.write(model, Format.RDFXML).toFile(new File("path/to/output/file.rdf");
 *}</pre>
 *
 * <p>Writing a {@code TriG} document to an {@linkplain OutputStream}:
 *<pre>{@code
 *OutputStream out = ...;
 *RdfIO.write(model, Format.TRIG).withBase("http://example.com/baseURI").toStream(out);
 *}</pre>
 * Or without setting a base URI:
 *<pre>{@code
 *RdfIO.write(model, Format.TRIG).toStream(out);
 *}</pre>
 *
 * <p>Writing a {@code TRIX} document to an {@linkplain Writer}:
 *<pre>{@code
 *Writer writer = ...;
 *RdfIO.write(model, Format.TRIX).withBase("http://example.com/baseURI").toStream(writer);
 *}</pre>
 * Or without setting a base URI:
 *<pre>{@code
 *RdfIO.write(model, Format.TRIX).toStream(writer);
 *}</pre>
 *
 * <p>Writing a {@code NTRIPLES} document to a String:
 *<pre>{@code
 *String string = RdfIO.write(model, Format.RDFXML).withBase("http://example.com/baseURI").toString();
 *}</pre>
 * Or without setting a base URI:
 *<pre>{@code
 *String string = RdfIO.write(model, Format.RDFXML).toString();
 *}</pre>
 *
 * <p>All methods of this class throw {@linkplain NullPointerException} for null arguments, unless otherwise specified.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class RdfIO {
    private RdfIO() { }

    /**
     * Prepares a parsing of the specified InputStream, using the specified serialization format.
     * The actual parsing happens later, using the returned object.
     *
     * @param in the InputStream to parse
     * @param format the serialization format to use
     * @return an object that handles rest of the workflow of the parsing
     * @see RdfIO for usage examples.
     */
    public static InputWithoutBase read(InputStream in, Format format) {
        return read(new InputStreamReader(in), format);
    }

    /**
     * Prepares a parsing of the specified Reader, using the specified serialization format.
     * The actual parsing happens later, using the returned object.
     *
     * @param in the Reader to parse
     * @param format the serialization format to use
     * @return an object that handles rest of the workflow of the parsing
     */
    public static InputWithoutBase read(Reader in, Format format) {
        Assert.notNull(in, "Reader");
        Assert.notNull(format, "Format");
        return new InputWithoutBase(in, format);
    }

    /**
     * Prepares a parsing of the specified File, using the specified serialization format.
     * The actual parsing happens later, using the returned object.
     *
     * @param file the File to parse
     * @param format the serialization format to use
     * @return an object that handles rest of the workflow of the parsing
     * @throws FileNotFoundException if the file does not exist
     */
    public static InputWithoutBase read(File file, Format format) throws FileNotFoundException {
        Assert.notNull(file, "File");
        Assert.notNull(format, "Format");
        return new InputWithoutBase(
                new InputStreamReader(new BufferedInputStream(new FileInputStream(file))),
                format);
    }

    /**
     * Prepares a parsing of the specified URL, using the specified serialization format.
     * The actual parsing happens later, using the returned object.
     *
     * @param url the URL to parse
     * @param format the serialization format to use
     * @return an object that handles rest of the workflow of the parsing
     * @throws IOException if opening a stream to the URL throws this exception
     * @see RdfIO for usage examples.
     */
    public static InputWithoutBase read(URL url, Format format) throws IOException {
        Assert.notNull(url, "URL");
        Assert.notNull(format, "Format");
        return new InputWithoutBase(
                new InputStreamReader(new BufferedInputStream(url.openStream())),
                format);
    }

    /**
     * Prepares a parsing of the specified String, using the specified serialization format.
     * The actual parsing happens later, using the returned object.
     *
     * @param text the String to parse
     * @param format the serialization format to use
     * @return an object that handles rest of the workflow of the parsing
     */
    public static InputWithoutBase read(String text, Format format) {
        return read(new StringReader(text), format);
    }

    /**
     * A class that handles part of the workflow of a parsing; particularly
     * the part that the user optionally specifies the base URI to be used.
     */
    public static class InputWithoutBase {
        final Reader in;
        final Format format;

        private InputWithoutBase(Reader in, Format format) {
            this.in = in;
            this.format = format;
        }

        /**
         * Sets the base URI of the parsing to be performed.
         *
         * <p>See {@linkplain #withBase(Uri)} for a description of the expected
         * form of URIs given with {@code rdf:ID}.</p>
         *
         * @param baseUri the base URI to use
         * @return an object that handles the rest of the workflow of the parsing
         * @throws UriFormatException if {@code baseUri} cannot be parsed
         */
        public InputWithBase withBase(String baseUri) throws UriFormatException {
            return withBase(new Uri(baseUri, ""));
        }

        /**
         * Sets the base URI of the parsing to be performed.
         *
         * <p>According to the <a href="http://www.w3.org/TR/rdf-syntax-grammar/">
         * RDF/XML syntax specification</a> {@code rdf:ID="name"} is equivalent
         * to {@code rdf:about="#name"}. The additional hash ({@code #}) in {@code rdf:about}
         * is required in order for the given base URI to be used correctly.</p>
         *
         * @param baseUri the base URI to use
         * @return an object that handles the rest of the workflow of the parsing
         */
        public InputWithBase withBase(Uri baseUri) {
            Assert.notNull(baseUri, "Base URI");
            return new InputWithBase(in, format, baseUri);
        }
    }

    /**
     * A class that performs the actual parsing and adds the produced triples to a model.
     */
    public static class InputWithBase {
        private final Reader in;
        private final Format format;
        private final Uri baseUri;

        private Uri targetNamedGraph;

        private BlankNodesPolicy blankNodesPolicy;

        private InputWithBase(Reader in, Format format, Uri baseUri) {
            this.in = in;
            this.format = format;
            this.baseUri = baseUri;
        }

        public void into(Model model) throws IOException {
            into(model, Predicates.alwaysTrue());
        }

        /**
         * Parses the prepared input into triples that are added to the specified model.
         *
         * @param model the model that the produced triples will be added to
         * @param statementFilter a predicate that returns true to indicate that the triple
         * presented to it is accepted and should be included in the model. Note that {@code Statement}
         * is part of the {@code Sesame API}, of which the parsing support is used
         * @throws IOException if this exception is thrown during the parsing
         */
        public void into(Model model, Predicate<? super Statement> statementFilter) throws IOException {
            Preconditions.checkNotNull(model);
            Preconditions.checkNotNull(statementFilter);
            RDFParser parser = RDFParserRegistry.getInstance().get(format.toSesameFormat()).getParser();
//            parser.setParseLocationListener(new ParseLocationListener() {
//                public void parseLocationUpdate(int line, int column) {
//                    System.out.println(line + " " + column);
//                }
//            });
            BlankNodesPolicy policy = this.blankNodesPolicy;
            if (policy == null) policy = BlankNodesPolicy.globallyUnique();
            parser.setPreserveBNodeIDs(true); //we do our own blank node identifier handling
            parser.setValueFactory(policy.valueFactory());
            parser.setRDFHandler(new ModelFeeder(model, targetNamedGraph, statementFilter));
            try {
                parser.parse(in, baseUri.toString(Delimiter.WITHOUT));
            } catch (RDFParseException e) {
                throw (IOException)new IOException().initCause(e);
            } catch (RDFHandlerException e) {
                throw (IOException)new IOException().initCause(e);
            }
            in.close();
        }

        /**
         * Sets the policy to handle blank node identifiers of the input. Normally, such identifiers
         * are defined only locally (e.g. in the scope of the file that uses them), and loading them
         * verbatim, when doing multiple input loads into a single model, has the risk of colliding
         * identifiers just because they had the same name. The default blank node policy is
         * {@link BlankNodesPolicy#globallyUnique()}, which does not have that problem.
         *
         * @param blankNodesPolicy the policy to use when processing blank node indentifiers
         * @return this
         */
        public InputWithBase withBlankNodesPolicy(BlankNodesPolicy blankNodesPolicy) {
            if (this.blankNodesPolicy != null) {
                throw new IllegalStateException("BlankNodesPolicy has already been set");
            }
            this.blankNodesPolicy = Preconditions.checkNotNull(blankNodesPolicy);
            return this;
        }

        /**
         * Adds a target named graph where all the parsed triples will be added.
         * If no target named graph
         * is defined, a {@link RdfSuite#DEFAULT_GRAPH_URI default} is used. This method
         * can be called at most once.
         *
         * @param namedGraphUri the URI of a named graph where all triples will be added into
         * @return this
         * @throws UriFormatException if {@code namedGraphUri} cannot be parsed
         */
        public InputWithBase withDefaultNamedGraph(String namedGraphUri) throws UriFormatException {
            return withDefaultNamedGraph(Uri.parse(namedGraphUri));
        }

        /**
         * Adds a target named graph where all the parsed triples will be added.
         * If no target named graph
         * is defined, a {@link RdfSuite#DEFAULT_GRAPH_URI default} is used. This method
         * can be called at most once.
         *
         * @param namedGraphUri the URI of a named graph where all triples will be added into
         * @return this
         */
        public InputWithBase withDefaultNamedGraph(Uri namedGraphUri) {
            if (targetNamedGraph != null) {
                throw new IllegalStateException("Default named graph has already been set");
            }
            targetNamedGraph = Preconditions.checkNotNull(namedGraphUri);
            return this;
        }
    }

    /**
     * Prepares the serialization of a model, using the specified format.
     * The actual serialization happens later, using the returned object.
     *
     * @param model the model to be serialized
     * @param format the format to use for the serialization
     * @return an object that handles rest of the workflow of the serialization
     */
    public static ModelWriterWithoutBase write(Model model, Format format) {
        Assert.notNull(model, "Model");
        Assert.notNull(format, "Format");
        return new ModelWriterWithoutBase(model, format.toSesameFormat());
    }

    /**
     * A class that handles part of the workflow of serialization; particularly
     * the part that the user optionally specifies the base URI to be used.
     */
    public static class ModelWriterWithoutBase extends ModelWriterWithBase {
        private ModelWriterWithoutBase(Model model, RDFFormat format) {
            super(model, format);
        }

        /**
         * Specifies the base URI to be used for the serialization. The base URI can be omitted
         * and perform the serialization directly.
         *
         * @param baseUri the base URI to be used for the serialization
         * @return an object that handles rest of the workflow of the serialization
         * @throws UriFormatException if {@code baseUri} cannot be parsed
         */
        public ModelWriterWithBase withBase(String baseUri) throws UriFormatException{
            return withBase(Uri.parse(baseUri));
        }

        /**
         * Specifies the base URI to be used for the serialization. The base URI can be omitted
         * and perform the serialization directly.
         *
         * @param baseUri the base URI to be used for the serialization
         * @return an object that handles rest of the workflow of the serialization
         */
        public ModelWriterWithBase withBase(Uri baseUri) {
            Assert.notNull(baseUri);
            return new ModelWriterWithBase(model, format, baseUri);
        }

        /**
         * Does nothing.
         */
        @Override
        protected final void handleBaseUri(RDFWriter writer) {
        }
    }

    /**
     * A class that handles part of the workflow of serialization; particularly
     * the part that the actual serialization is performed.
     */
    public static class ModelWriterWithBase {
        protected final Model model;
        protected final RDFFormat format;
        private final Uri baseUri;

        private ModelWriterWithBase(Model model, RDFFormat format) {
            this(model, format, null);
        }

        private ModelWriterWithBase(Model model, RDFFormat format, Uri baseUri) {
            this.model = model;
            this.format = format;
            this.baseUri = baseUri;
        }

        /**
         * Serializes the prepared model to the specified file.
         *
         * @param file the file to which to serialize the model (which is overwritten)
         * @throws IOException if this exception is thrown during serialization
         */
        public void toFile(File file) throws IOException {
            toStream(new BufferedOutputStream(new FileOutputStream(file)));
        }

        /**
         * Serializes the prepared model to the specified stream. The stream
         * is not closed after the serialization.
         *
         * @param out the stream to which to serialize the model
         * @throws IOException if this exception is thrown during serialization
         */
        public void toStream(OutputStream out) throws IOException {
            Assert.notNull(out, "OutputStream");
            write(RDFWriterRegistry.getInstance().get(format).getWriter(out));
        }

        /**
         * Serializes the prepared model to the specified writer. The writer
         * is not closed after the serialization.
         *
         * @param out the writer to which to serialize the model
         * @throws IOException if this exception is thrown during serialization
         */
        public void toWriter(Writer out) throws IOException {
            Assert.notNull(out, "Writer");
            write(RDFWriterRegistry.getInstance().get(format).getWriter(out));
        }

        /**
         * Serializes the prepared model to a string.
         *
         * @return the String which contains the serialized model
         */
        @Override
        public String toString() {
            StringWriter sw = new StringWriter(1024);
            try {
                toWriter(sw);
            } catch (IOException e) {
                throw new AssertionError(e); //IOException won't happen for an in-memory writer
            }
            return sw.toString();
        }

        /**
         * Handles the definition of the base URI during serialization. This method
         * handles it by actually writing the base URI if one is defined.
         *
         * <p>Current implementation does nothing due to lack of underlying Sesame support.
         *
         * @param writer the writer to which the serialization is happening
         * @throws IOException if the writer throws this exception
         * @throws RDFHandlerException if an internal error occurs
         */
        protected void handleBaseUri(RDFWriter writer) throws IOException, RDFHandlerException {
//            try {
//                writer.getClass().getMethod("setBaseURI", String.class).invoke(writer,
//                        baseUri.toString(Hash.WITH));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
        }

        private void write(RDFWriter writer) throws IOException {
            try {
                handleBaseUri(writer);
                writer.startRDF();
                for (RdfNode node : model.findNodes(RdfType.NAMED_GRAPH)) {
                    gr.forth.ics.swkm.model2.Resource graph = (gr.forth.ics.swkm.model2.Resource)node;
                    for (Triple t : model.triples().g(graph).fetch()) {
                        writer.handleStatement(toStatement(graph, t));
                    }
                }
                writer.endRDF();
            } catch (RDFHandlerException e) {
                throw (IOException)new IOException().initCause(e);
            }
        }
    }

    private static Statement toStatement(gr.forth.ics.swkm.model2.Resource namedGraph, Triple triple) {
        Resource c = namedGraph == namedGraph.owner().defaultNamedGraph() ?
            null : toURI(namedGraph);

        Resource s = triple.subject().isResource() ?
            toURI((gr.forth.ics.swkm.model2.Resource)triple.subject()) :
            toBNode((BlankNode)triple.subject());

        URI p = toURI(triple.predicate());

        Value o = triple.object().isResource() ? toURI((gr.forth.ics.swkm.model2.Resource)triple.object())
                : (triple.object().isBlankNode() ? toBNode((BlankNode)triple.object()) :
                    toLiteral((LiteralNode)triple.object()));
        return new ContextStatementImpl(s, p, o, c);
    }

    private static URI toURI(gr.forth.ics.swkm.model2.Resource resource) {
        return new URIImpl(resource.getUri().toString());
    }

    private static BNode toBNode(gr.forth.ics.swkm.model2.BlankNode blankNode) {
        return new BNodeImpl(blankNode.getId());
    }

    private static Literal toLiteral(LiteralNode literal) {
        String value = literal.getLiteral().getValue();
        if (literal.getLiteral().hasType()) {
            return new LiteralImpl(value,
                    new URIImpl(literal.getLiteral().getType().toString()));
        } else if (literal.getLiteral().hasLanguage()) {
            return new LiteralImpl(value,
                    literal.getLiteral().getLanguage());
        } else {
            return new LiteralImpl(value);
        }
    }
}
