/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import javolution.xml.ObjectReader;
import javolution.xml.ObjectWriter;
import javolution.xml.XmlInputStream;
import javolution.xml.XmlOutputStream;

/**
 * <p> This class holds {@link javolution.xml} benchmark.</p>
 * <p> This benchmark <b>will not compile</b> on all platforms. </p>
 *
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 2.0, November 26, 2004
 */
final class Perf_Xml extends Javolution implements Runnable {

	private static final InetAddress LOCAL_HOST;

	private static final int SERIAL_PORT = 4321;

	private static final int IO_PORT = 4322;

	private static final int NIO_PORT = 4323;

	private static final int NBR_OBJECTS = 100; // Nbr of objects transmitted.

	private static final int OBJECT_SIZE = 1000; // Nbr of strings per
													// object.

	private static final int BYTE_BUFFER_SIZE = 50 * OBJECT_SIZE + 200;

	private Object[] _objects = new Object[NBR_OBJECTS];

	private Object[] _objectsSerial = new Object[NBR_OBJECTS];

	private Object[] _objectsIo = new Object[NBR_OBJECTS];

	private Object[] _objectsNio = new Object[NBR_OBJECTS];
	static {
		try {
			LOCAL_HOST = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new Javolution.InternalError(e);
		}
	}

	private abstract class Server extends Thread {
		public abstract void serve() throws IOException;

		public Server() {
			this.setDaemon(true);
		}

		public void run() {
			try {
				serve();
			} catch (IOException e) {
				throw new Javolution.InternalError(e);
			}
		}
	}

	private class SerialServer extends Server {
		public void serve() throws IOException {
			ServerSocket ss = new ServerSocket(SERIAL_PORT);
			try {
				// Multiple connections.
				for (int i = 0; i < _objects.length; i++) {
					Socket s = ss.accept();
					OutputStream out = s.getOutputStream();
                    // Use buffer to accelerate socket I/O.
                    BufferedOutputStream bout = new BufferedOutputStream(out);
					ObjectOutput oo = new ObjectOutputStream(bout);
					oo.writeObject(_objects[i]);
                    bout.close();
					s.close();
				}
				// Permanent connection.
				Socket s = ss.accept();
				OutputStream out = s.getOutputStream();
                // Use buffer to accelerate socket I/O.
                BufferedOutputStream bout = new BufferedOutputStream(out);
				ObjectOutput oo = new ObjectOutputStream(bout);
				for (int i = 0; i < _objects.length; i++) {
					oo.writeObject(_objects[i]);
				}
                bout.close();
				s.close();
			} finally {
				ss.close();
			}
		}
	}

	private class IoServer extends Server {
		public void serve() throws IOException {
			ObjectWriter ow = ObjectWriter.newInstance();
			ow.setNamespace("", "java.lang");
			ServerSocket ss = new ServerSocket(IO_PORT);
			try {
				// Multiple connections.
				for (int i = 0; i < _objects.length; i++) {
					Socket s = ss.accept();
					ow.write(_objects[i], s.getOutputStream());
				}
				// Permanent connection.
				Socket s = ss.accept();
				OutputStream out = s.getOutputStream();
				ObjectOutput oo = XmlOutputStream.newInstance(out);
				for (int i = 0; i < _objects.length; i++) {
					oo.writeObject(_objects[i]);
				}
				s.close();
			} finally {
				ss.close();
			}
		}
	}

	private class NioServer extends Server {
		public void serve() throws IOException {
			ByteBuffer bb = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
			ObjectWriter ow = ObjectWriter.newInstance();
			ow.setNamespace("", "java.lang");
			ServerSocketChannel ssc = ServerSocketChannel.open();
			try {
				ssc.socket().bind(new InetSocketAddress(NIO_PORT));
				for (int i = 0; i < _objects.length; i++) {
					SocketChannel sc = ssc.accept();
					ow.write(_objects[i], bb);
					bb.flip();
					sc.write(bb);
					bb.clear();
					sc.close();
				}
			} finally {
				ssc.close();
			}
		}
	}

	/**
	 * Executes benchmark.
	 */
	public void run() {
		// Create dummy objects, each of 100 Strings.
	    ArrayList all = new ArrayList(NBR_OBJECTS);
		for (int i = 0; i < NBR_OBJECTS; i++) {
			ArrayList list = new ArrayList();
			for (int j = 0; j < OBJECT_SIZE; j++) {
				list.add("This is the string: " + i + ", " + j);
			}
			_objects[i] = list;
			all.add(list);
		}
        
        println("");
		println("-- Java(TM) Serialization (file stream)--");
		try {
    		ObjectOutput oo = new ObjectOutputStream(new FileOutputStream("C:/objects.serial"));
			print("Write Time: ");
			startTime();
    		oo.writeObject(all);
   		    oo.close();
			endTime(1);
            ObjectInput oi = new ObjectInputStream(new FileInputStream("C:/objects.serial"));
            print("Read Time: ");
            startTime();
            Object obj = oi.readObject();
            oi.close();
            endTime(1);
            if (!all.equals(obj)) {
                throw new Error("SAVE ERROR");
            }
		} catch (Throwable e) {
			throw new Javolution.InternalError(e);
		}
		
        println("");
        println("-- XML Serialization (file stream)--");
        try {
            ObjectWriter ow = ObjectWriter.newInstance();
            ow.setNamespace("", "java.lang");
            OutputStream out = new FileOutputStream("C:/objects.xml");
            print("Write Time: ");
            startTime();
            ow.write(all, out);
            endTime(1);
            ObjectReader or = ObjectReader.newInstance();
            InputStream in = new FileInputStream("C:/objects.xml");
            print("Read Time: ");
            startTime();
            Object obj = or.read(in);
            endTime(1);
            if (!all.equals(obj)) {
                throw new Error("SAVE ERROR");
            }
        } catch (Throwable e) {
            throw new Javolution.InternalError(e);
        }
        
        println("");
        println("-- Java(TM) Serialization (ByteArray stream)--");
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(BYTE_BUFFER_SIZE);
            ObjectOutput oo = new ObjectOutputStream(bo);
            print("Write Time: ");
            startTime();
            oo.writeObject(all);
            oo.close();
            endTime(1);
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInput oi = new ObjectInputStream(bi);
            print("Read Time: ");
            startTime();
            Object obj = oi.readObject();
            oi.close();
            endTime(1);
            if (!all.equals(obj)) {
                throw new Error("SAVE ERROR");
            }
        } catch (Throwable e) {
            throw new Javolution.InternalError(e);
        }

        println("");
        println("-- XML Serialization (ByteArray stream)--");
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(BYTE_BUFFER_SIZE);
            ObjectWriter ow = ObjectWriter.newInstance();
            ow.setNamespace("", "java.lang");
            print("Write Time: ");
            startTime();
            ow.write(all, bo);
            endTime(1);
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectReader or = ObjectReader.newInstance();
            print("Read Time: ");
            startTime();
            Object obj = or.read(bi);
            endTime(1);
            if (!all.equals(obj)) {
                throw new Error("SAVE ERROR");
            }
        } catch (Throwable e) {
            throw new Javolution.InternalError(e);
        }

        println("");
		println("-- Java(TM) Serialization (client-server) --");
		try {
			SerialServer server = new SerialServer();
			server.start();
			Thread.sleep(1000);

			print("Time (multiple connections): ");
			startTime();
			for (int i = 0; i < NBR_OBJECTS; i++) {
				ObjectInput oi = null;
				try {
					Socket s = new Socket(LOCAL_HOST, SERIAL_PORT);
					InputStream in = s.getInputStream();
					oi = new ObjectInputStream(in);
					_objectsSerial[i] = oi.readObject();
				} finally {
					if (oi != null)
						oi.close();
				}
			}
			endTime(1);

			// Checks results.
			for (int i = 0; i < NBR_OBJECTS; i++) {
				// println(_objects[i]);
				if (!_objects[i].equals(_objectsSerial[i])) {
					throw new Error("TRANSMISSION ERROR");
				}
			}
			Socket s = new Socket(LOCAL_HOST, SERIAL_PORT);
			InputStream in = s.getInputStream();
			ObjectInput oi = new ObjectInputStream(in);
			print("Time (permanent connection): ");
			startTime();
			try {
				for (int i = 0; i < NBR_OBJECTS; i++) {
					_objectsSerial[i] = oi.readObject();
				}
			} finally {
				oi.close();
			}
			endTime(1);

			// Checks results.
			for (int i = 0; i < NBR_OBJECTS; i++) {
				// println(_objects[i]);
				if (!_objects[i].equals(_objectsSerial[i])) {
					throw new Error("TRANSMISSION ERROR");
				}
			}
		} catch (Throwable e) {
			throw new Javolution.InternalError(e);
		}
		println("");

		println("-- XML Serialization (client-server) --");
		try {
			IoServer server = new IoServer();
			server.start();
			Thread.sleep(1000);

			ObjectReader or = ObjectReader.newInstance();
			print("Time (multiple connections): ");
			startTime();
			for (int i = 0; i < NBR_OBJECTS; i++) {
				Socket s = null;
				try {
					s = new Socket(LOCAL_HOST, IO_PORT);
					_objectsIo[i] = or.read(s.getInputStream());
				} finally {
					if (s != null)
						s.close();
				}
			}
			endTime(1);

			// Checks results.
			for (int i = 0; i < NBR_OBJECTS; i++) {
				// println(_objects[i]);
				if (!_objects[i].equals(_objectsIo[i])) {
					throw new Error("TRANSMISSION ERROR");
				}
			}
			Socket s = new Socket(LOCAL_HOST, IO_PORT);
			InputStream in = s.getInputStream();
			ObjectInput oi = XmlInputStream.newInstance(in);
			print("Time (permanent connection): ");
			startTime();
			try {
				for (int i = 0; i < NBR_OBJECTS; i++) {
					_objectsSerial[i] = oi.readObject();
				}
			} finally {
				oi.close();
			}
			endTime(1);

			// Checks results.
			for (int i = 0; i < NBR_OBJECTS; i++) {
				// println(_objects[i]);
				if (!_objects[i].equals(_objectsIo[i])) {
					throw new Error("TRANSMISSION ERROR");
				}
			}
		} catch (Throwable e) {
			throw new Javolution.InternalError(e);
		}
		println("");

		println("-- XML Serialization (client-server, multiple connections, NIO) --");
		try {
			NioServer server = new NioServer();
			server.start();
			Thread.sleep(1000);

			ByteBuffer bb = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
			ObjectReader or = ObjectReader.newInstance();
			InetSocketAddress isa = new InetSocketAddress(LOCAL_HOST, NIO_PORT);
			print("Time: ");
			startTime();
			for (int i = 0; i < NBR_OBJECTS; i++) {
				SocketChannel sc = null;
				try {
					sc = SocketChannel.open(isa);
					int pos;
					do {
						pos = bb.position();
						sc.read(bb);
						// println("POS: " + bb.position());
					} while (pos != bb.position());
					bb.flip();
					_objectsNio[i] = or.read(bb);
					bb.clear();
				} finally {
					if (sc != null)
						sc.close();
				}
			}
			endTime(1);

			// Checks results.
			for (int i = 0; i < NBR_OBJECTS; i++) {
				if (!_objects[i].equals(_objectsNio[i])) {
					throw new Error("TRANSMISSION ERROR");
				}
			}
		} catch (Throwable e) {
			throw new Javolution.InternalError(e);
		}
		println("");
	}
}
