package alarmclock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Read XML from a file or string to create DOM document
 * 
 * @author --==[FReeZ]==--
 * 
 * @version 1.0
 */
public class XmlReader {
	/**
	 * Parse the XML string to DOM document
	 * 
	 * @param  XML
	 * @return DOM document or null 
	 */
	public static Document parse(String xml) {
		Document doc = null;
		ByteArrayInputStream is = null;
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			is = new ByteArrayInputStream(xml.getBytes());
			doc = db.parse(is);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return doc;
	}
	
	/**
	 * Parse XML file to DOM document
	 * 
	 * @param filename
	 * @return DOM document
	 * 
	 * @throws IOException
	 */
	public static Document parseXmlFile(String filename) throws IOException {
		final String lineSep = System.getProperty("line.separator");
		final FileReader fr = new FileReader(filename);
		final BufferedReader br = new BufferedReader(fr);
		String nextLine = "";
		final StringBuffer sb = new StringBuffer();
		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine);
			sb.append(lineSep);
		}
		
		br.close();
		fr.close();
		return parse(sb.toString());
	}
}
