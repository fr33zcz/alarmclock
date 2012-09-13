package alarmclock;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * SWT.Combo with support for MRU
 * 
 * @author --==[FReeZ]==--
 * @version 0.9
 */
public class MemoryCombo {
	/**
	 * Combo box for MemoryCombo which is used to select an MP3
	 */
	private Combo combo;
	
	/**
	 * List of recently played MP3s
	 */
	private List<String> list;
	
	/**
	 * Max size of MRU
	 */
	private int maxSize = 10;
	
	/**
	 * Name of the file to store the list of recently played MP3s to
	 */
	private String mruFile = "mru.xml";
	
	/**
	 * Create a new SWT.Combo with memory for maxSize of items
	 * 
	 * @param parent
	 * @param style
	 */
	public void create(Composite parent, int style) {
		 list = new ArrayList<String>();
		 combo = new Combo(parent, style);
		 list.clear();
		 loadFromFile(mruFile);
		 int max = list.size();
		 for (int i = 0; i < max; i++) {
			 String value = list.get(i);
			 if (value == null) {
				 break;
			 }
			 
			 combo.add(value);
		 }
		 
		 
		 combo.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				saveToFile(mruFile);
			}
		 });
		 
		 combo.addFocusListener(new FocusListener() {
			 private String originalText;
			 
			 public void focusGained(FocusEvent event) {
				 originalText = combo.getText();
			 }
			 
			 public void focusLost(FocusEvent event) {
				 final String text = combo.getText();
				 final File file = new File(text);
				 final int textLength = text.length() - 4;
				 if (!file.exists() || textLength < 1 || 0 != text.substring(textLength).compareToIgnoreCase(".mp3")) {
					 if (originalText == null) {
						 originalText = "";
					 }
					 
					 combo.setText(originalText);
				} else {
					storeValue(text);
				}
			 }
		 });
	}
	
	/**
	 * Store a value to list and reflect the change to combo
	 * 
	 * @param value
	 */
	public void storeValue(String value) {
		if (list.contains(value)) {
			return;
		}
		
		if (list.size() >= maxSize) {
			list.remove(maxSize - 1);
			combo.remove(maxSize - 1);
		}
		
		list.add(value);
		combo.add(value);
	}
	
	/**
	 * Remove all values from list, store new ones to list, and reflect the changes to combo
	 * 
	 * @param values
	 */
	public void storeValues(List<String> values) {
		combo.removeAll();
		int max = list.size() - 1;
		if (max > maxSize) {
			max = maxSize;
		}
		
		for (int i = 0; i < max; i++) {
			if (list.contains(values.get(i))) {
				return;
			}
			
			list.set(i, values.get(i));
			combo.add(list.get(i));
		}
	}
	
	/**
	 * Returns the values of list
	 * 
	 * @return values of list
	 */
	public List<String> restoreValues() {
		return list.subList(0, list.size() - 1);
	}
	
	/**
	 * Set the number of maximum values to keep on the list
	 *  
	 * @param maxSize
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Return the number of maximum values to keep on the list
	 * 
	 * @return maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}
	
	/**
	 * Set layout data of combo
	 * 
	 * @param layoutData
	 */
	public void setLayoutData(Object layoutData) {
		combo.setLayoutData(layoutData);
	}
	
	/**
	 * Set text content of combo
	 * 
	 * @param text
	 */
	public void setText(String text) {
		combo.setText(text);
	}
	
	/**
	 * Get text content of combo
	 * 
	 * @return text content
	 */
	public String getText() {
		return combo.getText();
	}
	
	/**
	 * Dispose all operating system resources allocated for combo
	 */
	public void dispose() {
		combo.dispose();
	}

	/**
	 * Set the name of file to store list in
	 * 
	 * @param filename
	 */
	public void setMruFilename(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null.");
		}
		
		mruFile = filename;
	}

	/**
	 * Get the name of file to store list in
	 * 
	 * @return filename
	 */
	public String getMruFilename() {
		return mruFile;
	}
	
	/**
	 * Save list to file
	 * 
	 * @param filename
	 */
	public void saveToFile(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null.");
		}
		
		final StringWriter writer = new StringWriter();
		final XmlWriter xmlWriter = new XmlWriter(writer);
		try {
			xmlWriter.writeEntity("root");
			xmlWriter.writeEntity("numberOfValues");
			final int max = list.size();
			xmlWriter.writeText(String.valueOf(max));
			xmlWriter.endEntity();
			for (int i = 0; i < max; i++) {
				xmlWriter.writeEntity("value" + i);
				xmlWriter.writeText(list.get(i));
				xmlWriter.endEntity();
			}
			
			xmlWriter.endEntity();
			xmlWriter.close();
			xmlWriter.saveToFile(writer, filename);
		} catch (Throwable e) {
			ErrorReporter.reportError(combo.getShell(), e.toString());
		}
	}
	
	/**
	 * Load list from file and reflect the changes to combo
	 * 
	 * @param filename
	 */
	public void loadFromFile(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null.");
		}
		
		list.clear();
		final Document dom;		
		try {
			dom = XmlReader.parseXmlFile(filename);
		} catch (IOException e) {
			ErrorReporter.reportError(combo.getShell(), e.toString());
			return;
		}
		
		final Node nodeNumberOfValues = dom.getElementsByTagName("numberOfValues").item(0);
		if (nodeNumberOfValues == null) {
			return;
		}
		
		final String strNumberOfValues = nodeNumberOfValues.getTextContent();
		if (strNumberOfValues == null) {
			return;
		}
				
		int numberOfValues = Integer.parseInt(strNumberOfValues);
		
		if (numberOfValues > maxSize) {
			numberOfValues = maxSize;
		}
		
		for (int i = 0; i < numberOfValues; i++) {
			final Node node = dom.getElementsByTagName("value" + i).item(0);
			if (node == null) {
				return;
			}
			
			list.add(node.getTextContent());
		}
	}
}
