package alarmclock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * Simplifies XML writing.
 *
 * @author <a href="mailto:bayard@generationjava.com">Henri Yandell</a>
 * @author --=[FReeZ]==--
 * @version 0.3
 */
public class XmlWriter implements IXmlWriter {
	/**
	 * underlying writer
	 */
	private Writer writer;
	
	/**
	 * Stack of XML entity names
	 */
	private Stack<String> stack;
	
	/**
	 * Current attribute string
	 */
	private StringBuffer attrs;
	
	/**
	 * True when the current node is empty
	 */
	private boolean empty;
	
	/**
	 * True when the current node is closed
	 */
	private boolean closed;
	
	/**
	 * Creates an XmlWriter on top of an existing java.io.Writer
	 * 
	 * @param  writer  the instance of Writer
	 */
	public XmlWriter(Writer writer) {
		this.writer = writer;
		closed = true;
		stack  = new Stack<String>();
	}

	public XmlWriter writeEntity(String name) throws IOException {
		closeOpeningTag();
		closed = false;
		writer.write("<");
		writer.write(name);
		stack.add(name);
		empty = true;
		return this;
	}

	public XmlWriter writeAttribute(String attr, String value) {
		if (attrs == null) {
			attrs = new StringBuffer();
		}
		
		attrs.append(" ");
		attrs.append(attr);
		attrs.append("=\"");
		attrs.append(escapeXml(value));
		attrs.append("\"");
		return this;
	}

	public XmlWriter endEntity() throws IOException {
		if (stack.empty()) {
			throw new IOException("Called endEntity too many times. ");
		}
		
		final String name = (String)stack.pop();
		if (name != null) {
			if (empty) {
				writeAttributes();
				writer.write("/>");
			} else {
				writer.write("</");
				writer.write(name);
				writer.write(">");
			}
				
			empty = false;
		}
	        
		return this;
	}

	public void close() throws IOException {
		if (!stack.empty()) {
			throw new IOException("Tags are not all closed. Possibly, " + stack.pop() + " is unclosed. ");
		}
	}

	public XmlWriter writeText(String text) throws IOException {
		closeOpeningTag();
		empty = false;
		writer.write(escapeXml(text));
		return this;
	}

	public void saveToFile(Writer writer, String filename) throws IOException {
		final File file = new File(filename);
		if ((file.exists() && !file.delete()) || !file.createNewFile()) {
			throw new IOException("Couldn't replace file '" + file.getCanonicalFile() + "'");
		}
		
		final FileOutputStream os = new FileOutputStream(file);
		os.write(writer.toString().getBytes());
		os.flush();
		os.close();
	}
	
	/**
	 * closes the opening tag
	 * 
	 * @throws IOException when writer.write(">") fails
	 */
	private void closeOpeningTag() throws IOException {
		if (!closed) {
			writeAttributes();
			closed = true;
			writer.write(">");
		}
	}

	/**
	 * Writes out all current attributes
	 * 
	 * @throws IOException when writer.write() fails
	 */
	private void writeAttributes() throws IOException {
		if (attrs != null) {
			writer.write(attrs.toString());
			attrs.setLength(0);
			empty = false;
		}
	}
	
	/**
	 * Transcodes "&<>\'" characters to XML entities
	 */
	private static String escapeXml(String str) {
		str = replaceString(str, "&", "&amp;");
		str = replaceString(str, "<", "&lt;");
		str = replaceString(str, ">", "&gt;");
		str = replaceString(str, "\"" ,"&quot;");
		str = replaceString(str, "'", "&apos;");
		return str;
	}

	/**
	 * Replaces string
	 * 
	 * @param  String destination
	 * @param  String needle
	 * @param  String haystack
	 * @return String replaced
	 */
	private static String replaceString(String text, String repl, String with) {
		return replaceString(text, repl, with, -1);
	}
	
	/**
	 * Replaces string with another string inside a larger string, for
	 * the first n values of the search string.
	 *
	 * @param  text  source string
	 * @param  repl  search for
	 * @param  with  replace with
	 * @param  max   max. replacements
	 *
	 * @return string with "max" or less values replaced 
	 */
	private static String replaceString(String text, String repl, String with, int max) {
		if (text == null) {
			return null;
		}
	 
		final StringBuffer buffer = new StringBuffer(text.length());
		int start = 0;
		int end   = 0;
		while ((end = text.indexOf(repl, start)) != -1) {
			buffer.append(text.substring(start, end)).append(with);
			start = end + repl.length();
	 
			if (--max == 0) {
				break;
			}
			
		}
		
		buffer.append(text.substring(start));
		return buffer.toString();
	}
}
