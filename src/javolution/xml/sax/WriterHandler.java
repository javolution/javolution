/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

import java.io.IOException;
import java.io.Writer;

import j2me.lang.CharSequence;
import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.util.FastList;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * <p> This class generates xml documents from SAX2 events.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, March 11, 2005
 */
public class WriterHandler implements ContentHandler, Reusable {

	/** 
	 * Holds the length of intermediate buffer.
	 */
	private static final int BUFFER_LENGTH = 2048;

	/** 
	 * Holds the writer destination.
	 */
	private Writer _writer;

	/** 
	 * Holds the prolog.
	 */
	private CharSequence _prolog = Text.EMPTY;

	/** 
	 * Holds the indentation string.
	 */
	private CharSequence _indent = Text.EMPTY;

	/** 
	 * Holds the current prefix mapping (prefix followed by uri).
	 */
	private FastList _prefixMappings = new FastList();

	/** 
	 * Holds the current nesting level.
	 */
	private int _nesting = -1;

	/** 
	 * Indicates if the current element tag is still open (to be closed
	 * with ">" or "/>" whether it is empty or not).
	 */
	private boolean _isTagOpen;

	/** 
	 * Holds intermediate buffer.
	 */
	private final char[] _buffer = new char[BUFFER_LENGTH];

	/** 
	 * Holds the buffer current index.
	 */
	private int _index;

	/** 
	 * Default constructor.
	 */
	public WriterHandler() {
	}

	/** 
	 * Sets the xml document writer.
	 * 
	 * @param writer the document writer.
	 * @return <code>this</code>
	 */
	public WriterHandler setWriter(Writer writer) {
		_writer = writer;
		return this;
	}

	/** 
	 * Sets the indentation sequence (default none).
	 * 
	 * @param indent a character sequence containing spaces or a tabulation character.
	 */
	public void setIndent(CharSequence indent) {
		_indent = indent;
	}

	/** 
	 * Sets the prolog to write at the beginning of the xml document
	 * (default none).
	 * 
	 * @param prolog the character sequence to be written at the beginning 
	 *        of the document.
	 */
	public void setProlog(CharSequence prolog) {
		_prolog = prolog;
	}

	// Implements reusable.
	public void reset() {
		_writer = null;
		_indent = Text.EMPTY;
		_prolog = Text.EMPTY;
		_prefixMappings.clear();
		_nesting = -1;
		_isTagOpen = false;
		_index = 0;
	}

	// Implements ContentHandler
	public void setDocumentLocator(Locator locator) {
		// Do nothing.
	}

	// Implements ContentHandler
	public void startDocument() throws SAXException {
		if (_writer == null)
			throw new SAXException("Writer not set");
		try {
			writeNoEscape(_prolog);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	// Implements ContentHandler
	public void endDocument() throws SAXException {
		try {
			flushBuffer();
			_writer.close();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	// Implements ContentHandler
	public void startPrefixMapping(CharSequence prefix, CharSequence uri)
			throws SAXException {
		_prefixMappings.addLast(prefix);
		_prefixMappings.addLast(uri);
	}

	// Implements ContentHandler
	public void endPrefixMapping(CharSequence prefix) throws SAXException {
		// Do nothing.
	}

	// Implements ContentHandler
	public void startElement(CharSequence uri, CharSequence localName,
			CharSequence qName, Attributes atts) throws SAXException {
		try {
			if (_isTagOpen) { // The openned tag is not empty, close with ">" 
				writeNoEscape(">\n");
				_isTagOpen = false;
			}

			// Indents.
			_nesting++;
			indent();

			// Writes start tag.
			writeNoEscape('<');
			writeNoEscape(qName);

            // Writes namespaces if any.
            if (_prefixMappings.size() > 0) {
                writeNamespaces();
            }

			// Writes attributes
			final int attsLength = atts.getLength();
			for (int i = 0; i < attsLength; i++) {
				CharSequence attName = atts.getQName(i);
				CharSequence attValue = atts.getValue(i);
				writeNoEscape(' ');
				writeNoEscape(attName);
				writeNoEscape('=');
				writeNoEscape('"');
				write(attValue);
				writeNoEscape('"');
			}
			_isTagOpen = true;
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void indent() throws IOException {
		final int length = _indent.length();
		if (length > 0) {
			for (int i = 0; i < _nesting; i++) {
				writeNoEscape(_indent);
			}
		}
	}
    
    private void writeNamespaces() throws IOException {
        // Writes namespace declaration.
        for (FastList.Node n = _prefixMappings.headNode(), end = _prefixMappings
                .tailNode(); (n = n.getNextNode()) != end;) {
            CharSequence prefix = (CharSequence) n.getValue();
            CharSequence prefixUri = (CharSequence) (n = n.getNextNode())
                    .getValue();
            if (prefix.length() == 0) { // Default namespace.
                writeNoEscape(" xmlns=\"");
                write(prefixUri);
                writeNoEscape('"');
            } else {
                writeNoEscape(" xmlns:");
                writeNoEscape(prefix);
                writeNoEscape('=');
                writeNoEscape('"');
                write(prefixUri);
                writeNoEscape('"');
            }
        }
        _prefixMappings.clear();
    }

	// Implements ContentHandler
	public void endElement(CharSequence uri, CharSequence localName,
			CharSequence qName) throws SAXException {
		try {
			if (_isTagOpen) { // The openned tag is empty, close with "/>" 
				writeNoEscape("/>\n");
				_isTagOpen = false;
			} else {
				indent();
				writeNoEscape('<');
				writeNoEscape('/');
				writeNoEscape(qName);
				writeNoEscape('>');
				writeNoEscape('\n');
			}
			_nesting--;
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	// Implements ContentHandler
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			if (_isTagOpen) { // The openned tag is not empty, close with ">" 
				writeNoEscape('>');
				_isTagOpen = false;
			}
			writeNoEscape("<![CDATA[");
			flushBuffer();
			_writer.write(ch, start, length);
			writeNoEscape("]]>\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	// Implements ContentHandler
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// Do nothing.
	}

	// Implements ContentHandler
	public void processingInstruction(CharSequence target, CharSequence data)
			throws SAXException {
		// Do nothing.
	}

	// Implements ContentHandler
	public void skippedEntity(CharSequence name) throws SAXException {
		// Do nothing.
	}

	// Writes the specified characters. Use escape sequence when necessary.
	private void write(CharSequence csq) throws IOException {
		final int length = csq.length();
		for (int i = 0; i < length;) {
			char c = csq.charAt(i++);
			if ((c >= '@') || (c == ' ')) { // Most common case.
				_buffer[_index] = c;
				if (++_index == BUFFER_LENGTH) {
					flushBuffer();
				}
			} else { // Potential escape sequence.
				switch (c) {
				case '<':
					writeNoEscape("&lt;");
					break;
				case '>':
					writeNoEscape("&gt;");
					break;
				case '\'':
					writeNoEscape("&apos;");
					break;
				case '\"':
					writeNoEscape("&quot;");
					break;
				case '&':
					writeNoEscape("&amp;");
					break;
				default:
					if (c >= ' ') {
						writeNoEscape(c);
					} else {
						writeNoEscape("&#");
						writeNoEscape((char) ('0' + c / 10));
						writeNoEscape((char) ('0' + c % 10));
						writeNoEscape(';');
					}
				}
			}
		}
	}

	private void writeNoEscape(CharSequence csq) throws IOException {
		for (int i = 0, n = csq.length(); i < n;) {
			_buffer[_index] = csq.charAt(i++);
			if (++_index == BUFFER_LENGTH) {
				flushBuffer();
			}
		}
	}

	private void writeNoEscape(String csq) throws IOException {
		for (int i = 0, n = csq.length(); i < n;) {
			_buffer[_index] = csq.charAt(i++);
			if (++_index == BUFFER_LENGTH) {
				flushBuffer();
			}
		}
	}

	private final void writeNoEscape(char c) throws IOException {
		_buffer[_index] = c;
		if (++_index == BUFFER_LENGTH) {
			flushBuffer();
		}
	}

	private void flushBuffer() throws IOException {
		_writer.write(_buffer, 0, _index);
		_index = 0;
	}

}