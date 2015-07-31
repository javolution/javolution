/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javolution.io.AppendableWriter;
import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.xml.internal.stream.XMLStreamWriterImpl;

import org.junit.Before;
import org.junit.Test;

public class XMLStreamWriterImplTest {

	private static final CharArray TEST_INT_ELEMENT = new CharArray("testIntElement");
	private static final CharArray TEST_LONG_ELEMENT = new CharArray("testLongElement");
	private static final CharArray TEST_ELEMENT = new CharArray("testElement");
	private static final CharArray TEST_ROOT = new CharArray("testRoot");

	private XMLStreamWriterImpl _xmlStreamWriterImpl;
	
	@Before
	public void init(){
		_xmlStreamWriterImpl = new XMLStreamWriterImpl();		
	}

	@Test
	public void testWriteXMLWithStream() throws XMLStreamException, URISyntaxException, IOException{
		URL xmlUrl = this.getClass().getResource("/test-stax.xml");
		File xmlFile = new File(xmlUrl.toURI());
		String xmlString = new String(Files.readAllBytes(xmlFile.toPath()), 
				StandardCharsets.UTF_8).replace("\t", "").replace("\n", "");
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		_xmlStreamWriterImpl.setOutput(byteArrayOutputStream);

		writeStaxXML();
		
		assertEquals("Written / Read XML Is Equal", xmlString, 
				byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));
	}
	
	@Test
	public void testWriteXMLWithWriter() throws XMLStreamException, URISyntaxException, IOException{
		URL xmlUrl = this.getClass().getResource("/test-stax.xml");
		File xmlFile = new File(xmlUrl.toURI());
		String xmlString = new String(Files.readAllBytes(xmlFile.toPath()), 
				StandardCharsets.UTF_8).replace("\t", "").replace("\n", "");
		
		TextBuilder builder = new TextBuilder(4096);
		AppendableWriter writer = new AppendableWriter(builder);
		
		_xmlStreamWriterImpl.setOutput(writer);

		writeStaxXML();
		
		assertEquals("Written / Read XML Is Equal", xmlString, builder.toString());
	}
		
	private void writeStaxXML() throws XMLStreamException{
		_xmlStreamWriterImpl.setRepairingNamespaces(true);
		_xmlStreamWriterImpl.setDefaultNamespace("http://javolution.org/xml/schema/javolution");
		_xmlStreamWriterImpl.writeStartDocument("UTF-8", "1.0");
		_xmlStreamWriterImpl.writeStartElement("http://javolution.org/xml/schema/javolution",TEST_ROOT);
		_xmlStreamWriterImpl.writeAttribute("type", "Test1");
			
		for(int j = 0; j < 1000; j++){
			_xmlStreamWriterImpl.writeStartElement(TEST_ELEMENT);
			_xmlStreamWriterImpl.writeStartElement(TEST_INT_ELEMENT);
			_xmlStreamWriterImpl.writeCharacters(String.valueOf('1'));
			_xmlStreamWriterImpl.writeEndElement();
			_xmlStreamWriterImpl.writeStartElement(TEST_LONG_ELEMENT);
			_xmlStreamWriterImpl.writeCharacters(String.valueOf('2'));
			_xmlStreamWriterImpl.writeEndElement();
			_xmlStreamWriterImpl.writeEndElement();
		}
			
		_xmlStreamWriterImpl.writeEndElement();
		_xmlStreamWriterImpl.writeEndDocument();
	}
	
}
