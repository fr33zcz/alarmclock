package alarmclock;

import java.io.IOException;
import java.io.StringWriter;
import java.util.TimeZone;

import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Settings of the application
 * 
 * @author --==[FReeZ]==--
 * @version 0.9
 */
public class Settings {
	/**
	 * inSettings is used in decision whether to open the settings dialog or not
	 */
	private boolean inSettings = false;
	
	/**
	 * Form controls
	 */
	private Shell shell;
	private Button buttonPlay;
	private MemoryCombo comboFilename;
	private Spinner spinnerHour;
	private Spinner spinnerMinute;
	private Spinner spinnerSecond;
	private Button buttonConfirm;
	private Button checkboxExitRequiresConfirmation;
	private Combo comboTimezone;
	
	/**
	 * Configuration
	 */
	private int alarmHour = 0, alarmMinute = 0, alarmSecond = 0;
	private boolean confirmExit = true;
	private String alarmFilename = "";
	private int alarmTimeZone = 0;
	
	/**
	 * Holds the instance of AlarmClock
	 */
	private final AlarmClock alarmClock;
	
	/**
	 * Holds the instance of Mp3Player
	 */
	private Mp3Player player;
	
	/**
	 * Create the shell for application settings and load the settings from AlarmClock.CONFIGURATION_FILE
	 * 
	 * @param the instance of AlarmClock
	 * @param the instance of Mp3Player
	 */
	Settings(AlarmClock alarmClock, Mp3Player player) {
		this.alarmClock = alarmClock;
		this.player = player;
		try {
			load(alarmClock.CONFIGURATION_FILE);
		} catch (IOException e) {
			// we continue with default values set in the Configuration above
		}
	}
	
	/**
	 * Returns the hour of current alarm
	 * 
	 * @return hour when the alarm should be invoked
	 */
	public int getAlarmHour() {
		return alarmHour;
	}
	
	/**
	 * Returns the minute of current alarm
	 * 
	 * @return minute when the alarm should be invoked
	 */
	public int getAlarmMinute() {
		return alarmMinute;
	}
	
	/**
	 * Returns the second of current alarm
	 * 
	 * @return second when the alarm should be invoked
	 */
	public int getAlarmSecond() {
		return alarmSecond;
	}
	
	/**
	 * Returns the name of mp3 to play
	 * 
	 * @return filename (should be canonical)
	 */
	public String getAlarmFilename() {
		return alarmFilename;
	}
	
	/**
	 * Returns the value of confirm exit
	 * 
	 * @return true when the confirmation of exit is needed
	 */
	public boolean getConfirmExit() {
		return confirmExit;
	}
	
	/**
	 * Returns the current timezone
	 * 
	 * @return ID of timezone
	 */
	public int getTimezone() {
		return alarmTimeZone;
	}
	
	/**
	 * Save application settings from the configuration variables to specified file in XML format
	 * 
	 * @param  filename relative or absolute filename like configuration.xml, for example
	 * 
	 * @throws IOException when the file couldn't be deleted or created
	 */
	private void save(String filename) throws IOException {
		StringWriter stringWriter = new StringWriter();
		XmlWriter xmlWriter = new XmlWriter(stringWriter);
		
		try {
			xmlWriter.writeEntity("root");
			xmlWriter.writeEntity("alarmHour");
			xmlWriter.writeText(String.valueOf(alarmHour));
			xmlWriter.endEntity();
			
			xmlWriter.writeEntity("alarmMinute");
			xmlWriter.writeText(String.valueOf(alarmMinute));
			xmlWriter.endEntity();
			
			xmlWriter.writeEntity("alarmSecond");
			xmlWriter.writeText(String.valueOf(alarmSecond));
			xmlWriter.endEntity();
			
			xmlWriter.writeEntity("alarmTimeZone");
			xmlWriter.writeText(String.valueOf(alarmTimeZone));
			xmlWriter.endEntity();
						
			xmlWriter.writeEntity("alarmFilename");
			xmlWriter.writeText(alarmFilename);
			xmlWriter.endEntity();
			
			xmlWriter.writeEntity("confirmExit");
			xmlWriter.writeText(confirmExit ? "true" : "false");
			xmlWriter.endEntity();
			
			xmlWriter.endEntity();
			xmlWriter.close();
			xmlWriter.saveToFile(stringWriter, filename);
		} catch (Throwable e) {
			ErrorReporter.reportError(shell, "XmlWriter was unable to build the entities with configuration.");
		}
		
		stringWriter.close();
	}
	
	/**
	 * Read the text content of node that is found using getElementsByTagname and return always string
	 *  
	 * @param domDocument
	 * @param elementTagName
	 * @return text content of node or empty string
	 */
	private String safeDomRead(Document domDocument, String elementTagName) {
		NodeList nodeList = domDocument.getElementsByTagName(elementTagName);
		if (nodeList.getLength() == 0) {
			return "";
		}
		
		Node node = nodeList.item(0);
		if (node == null) {
			return "";
		}
		
		return node.getTextContent();
	}
	
	/**
	 * Load application settings from specified file in XML format to configuration variables
	 * 
	 * @param  filename  relative or absolute path to the XML file with configuration
	 * 
	 * @throws IOException when file couldn't be read or contains more than 65535 bytes
	 */
	public void load(String filename) throws IOException, IllegalStateException {
		Document domDocument = XmlReader.parseXmlFile(alarmClock.CONFIGURATION_FILE);
		if (domDocument == null) {
			throw new IllegalStateException("DOM document null");
		}
		
		alarmHour     = Integer.parseInt(safeDomRead(domDocument, "alarmHour"));
		alarmMinute   = Integer.parseInt(safeDomRead(domDocument, "alarmMinute"));
		alarmSecond   = Integer.parseInt(safeDomRead(domDocument, "alarmSecond"));
		alarmTimeZone = Integer.parseInt(safeDomRead(domDocument, "alarmTimeZone"));
		confirmExit   = Boolean.valueOf(safeDomRead(domDocument, "confirmExit"));
		alarmFilename = safeDomRead(domDocument, "alarmFilename");
	}
	
	/**
	 * Open the settings dialog, fill the controls with data from configuration variables.
	 * After Apply button is clicked the configuration is saved using save() to 
	 * AlarmClock.CONFIGURATION_FILE
	 *  
	 * @return success (False is returned mainly in case the dialog has been already opened)
	 */
	public boolean open() {
		if (inSettings) {
			return false;
		}
		
		shell = new Shell(SWT.CENTER | SWT.CLOSE | SWT.RESIZE | SWT.BORDER);
		shell.setMinimumSize(350, 240);
		shell.setSize(600, 240);
		alarmClock.centerShell(shell);
		
		FormData formData = new FormData();
		inSettings = true;
		shell.setText("AlarmClock - settings");
		shell.addDisposeListener(new DisposeListener() {
			public final void widgetDisposed(DisposeEvent e) {
				inSettings = false;
			}
		});
		
		final FormLayout formLayout = new FormLayout();
		shell.setLayout(formLayout);
		
		final Label labelMp3Filename = new Label(shell, SWT.LEFT);
		formData = new FormData();
		formData.left = new FormAttachment(1);
		formData.top = new FormAttachment(3, 11);
		formData.width = 120;
		formData.height = 25;
		labelMp3Filename.setLayoutData(formData);
		labelMp3Filename.setText("MP3 filename");
		
		comboFilename = new MemoryCombo();
		comboFilename.setMaxSize(10);
		comboFilename.setMruFilename("mru.xml");
		comboFilename.create(shell, SWT.LEFT | SWT.BORDER | SWT.SHADOW_ETCHED_IN);
		
		formData = new FormData();
		formData.left = new FormAttachment(labelMp3Filename);
		formData.top = new FormAttachment(3, 8);
		FormAttachment tmp = new FormAttachment(100, - alarmClock.BUTTON_WIDTH - 20);
		formData.right = tmp;
		formData.height = 15;
		comboFilename.setLayoutData(formData);
		if (alarmFilename != null) {
			comboFilename.setText(alarmFilename);
		}
		
		final Button buttonChoose = new Button(shell, SWT.PUSH);
		formData = new FormData();
		formData.left = new FormAttachment(100, - alarmClock.BUTTON_WIDTH - 10);
		formData.top = new FormAttachment(3, 8);
		formData.width = alarmClock.BUTTON_WIDTH;
		formData.height= alarmClock.BUTTON_HEIGHT;
		buttonChoose.setLayoutData(formData);
		buttonChoose.setText("Choo&se");
		
		/**
		 * Open the file selection dialog with filter *.mp3 after Choose button clicked
		 */
		buttonChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String[] allowedExtensions = new String[1];
				allowedExtensions[0] = "*.mp3";
				FileDialog fd = new FileDialog(shell);
				fd.setFilterExtensions(allowedExtensions);
				String filename = fd.open();
				if (filename != null) {
					comboFilename.setText(filename);
					buttonConfirm.setEnabled(true);
				}
			}
		});
		
		final Label labelAlarmtime = new Label(shell, SWT.LEFT);
		formData = new FormData();
		formData.left = new FormAttachment(1);
		formData.top = new FormAttachment(labelMp3Filename, 5);
		formData.width = 120;
		formData.height = 20;
		labelAlarmtime.setLayoutData(formData);
		labelAlarmtime.setText("Alarm time:");
		
		spinnerHour = new Spinner(shell, SWT.LEFT);
		formData = new FormData();
		formData.left = new FormAttachment(1);
		formData.top = new FormAttachment(labelAlarmtime);
		formData.width = 25;
		formData.height = 20;
		spinnerHour.setMinimum(0);
		spinnerHour.setMaximum(23);
		spinnerHour.setSelection(alarmHour);
		spinnerHour.setLayoutData(formData);

		spinnerMinute = new Spinner(shell, SWT.LEFT);
		formData = new FormData();
		formData.left = new FormAttachment(spinnerHour);
		formData.top = new FormAttachment(labelAlarmtime);
		formData.width = 25;
		formData.height = 20;
		spinnerMinute.setMaximum(59);
		spinnerMinute.setMinimum(0);
		spinnerMinute.setSelection(alarmMinute);
		spinnerMinute.setLayoutData(formData);

		spinnerSecond = new Spinner(shell, SWT.NONE);
		formData = new FormData();
		formData.left = new FormAttachment(spinnerMinute);
		formData.top = new FormAttachment(labelAlarmtime);
		formData.width = 25;
		formData.height = 20;
		spinnerSecond.setMaximum(59);
		spinnerSecond.setMinimum(0);
		spinnerSecond.setSelection(alarmSecond);
		spinnerSecond.setLayoutData(formData);
		
		final Label labelPreview = new Label(shell, SWT.LEFT);
		formData = new FormData();
		formData.left = new FormAttachment(100, - (alarmClock.BUTTON_WIDTH * 2 - 20) );
		formData.top = new FormAttachment(labelMp3Filename, 5);
		formData.width = 120;
		formData.height = 20;
		labelPreview.setLayoutData(formData);
		labelPreview.setText("MP3 preview:");
		
		final Button buttonStop = new Button(shell, SWT.PUSH);
		buttonPlay = new Button(shell, SWT.PUSH);
		buttonPlay.setText("Pl&ay");
		formData = new FormData();
		formData.left = new FormAttachment(100, -150);
		formData.top  = new FormAttachment(labelAlarmtime);
		formData.width = 70;
		formData.height = 25;
		buttonPlay.setLayoutData(formData);
		buttonPlay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final String fileToPlay = comboFilename.getText();
				if (fileToPlay != null) {
					try {
						player.setFileToPlay(fileToPlay);
						player.addPlaybackListener(new PlaybackListener() {
							public void playbackStarted(PlaybackEvent e) {
								Display display = shell.getDisplay();
								display.syncExec(new Runnable() {
									public void run() {
										buttonPlay.setEnabled(false);
										buttonStop.setEnabled(true);
									}
								});
							}
							
							public void playbackFinished(PlaybackEvent e) {
								final Display display = buttonPlay.getDisplay();
								if (display == null) {
									return;
								}
								
								display.syncExec(new Runnable() {
									public void run() {
										player.playbackFinished();
										buttonPlay.setEnabled(true);
										buttonStop.setEnabled(false);
									}
								});
							}
						});
						player.play();
					} catch (Throwable exception) {
						ErrorReporter.reportError(shell, e.toString());
					}
				}
			}
		});
		
		buttonStop.setText("St&op");
		buttonStop.setEnabled(false);
		formData = new FormData();
		formData.left = new FormAttachment(100, -75);
		formData.top = new FormAttachment(labelAlarmtime);
		formData.width = 70;
		formData.height = 25;
		buttonStop.setLayoutData(formData);
		buttonStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				player.stop();
			}
		});
		
		//String[][] list = TimeZoneParser.split(TimeZone.getAvailableIDs());
		
		comboTimezone = new Combo(shell, SWT.READ_ONLY);
		comboTimezone.setItems(TimeZone.getAvailableIDs());
		formData = new FormData();
		formData.left = new FormAttachment(1);
		formData.top = new FormAttachment(spinnerHour, 10);
		formData.right = new FormAttachment(100, -5);
		formData.height = 25;
		comboTimezone.setLayoutData(formData);
		comboTimezone.select(alarmTimeZone);
		
		checkboxExitRequiresConfirmation = new Button(shell, SWT.CHECK);
		checkboxExitRequiresConfirmation.setText("Exit requires confirmation");
		checkboxExitRequiresConfirmation.setSelection(confirmExit);
		formData = new FormData();
		formData.left = new FormAttachment(1);
		formData.top = new FormAttachment(comboTimezone, 10);
		formData.right = new FormAttachment(100, -5);
		formData.height = alarmClock.BUTTON_HEIGHT;
		checkboxExitRequiresConfirmation.setLayoutData(formData);
				
		buttonConfirm = new Button(shell, SWT.PUSH);
		formData = new FormData();
		formData.left = new FormAttachment(1);
		formData.top = new FormAttachment(80);
		formData.width = alarmClock.BUTTON_WIDTH;
		formData.height = alarmClock.BUTTON_HEIGHT;
		buttonConfirm.setText("C&onfirm");
		if (null == alarmFilename) {
			buttonConfirm.setEnabled(false);
		}
		
		buttonConfirm.setLayoutData(formData);
		buttonConfirm.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				alarmHour = spinnerHour.getSelection();
				alarmMinute = spinnerMinute.getSelection();
				alarmSecond = spinnerSecond.getSelection();
				alarmFilename = comboFilename.getText();
				confirmExit = checkboxExitRequiresConfirmation.getSelection();
				alarmTimeZone = comboTimezone.getSelectionIndex();
				try {
					save(alarmClock.CONFIGURATION_FILE);
				} catch (IOException exception) {
					ErrorReporter.reportError(shell,
						"Couldn't save the configuration to " +
						alarmClock.CONFIGURATION_FILE + "\n" +
						exception.toString()
					);
				}
				
				alarmClock.setNewTimezone();
				alarmClock.updateAlarmTime(getAlarmHour(), getAlarmMinute(), getAlarmSecond());
				inSettings = false;
				shell.close();
			}
		});
		
		final Button btn2 = new Button(shell, SWT.PUSH);
		formData = new FormData();
		formData.left = new FormAttachment(buttonConfirm, 5);
		formData.top = new FormAttachment(80);
		formData.width = alarmClock.BUTTON_WIDTH;
		formData.height = alarmClock.BUTTON_HEIGHT;
		btn2.setText("&Cancel");
		btn2.setLayoutData(formData);
		btn2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				inSettings = false;
				shell.close();
			}
		});
		
		shell.open();
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (player != null && player.isPlaying()) {
					player.stop();
				}
				
				inSettings = false;
			}
		});
		
		return true;
	}
	
	/**
	 * Get the instance of shell created in open()
	 * 
	 * @return the instance of shell or null
	 */
	public Shell getShell() {
		return (inSettings) ? shell : null;
	}
	
	/**
	 * Return true when settings dialog is open
	 * 
	 * @return settings dialog open
	 */
	public boolean inSettings() {
		return inSettings;
	}
	
	/**
	 * Stop player if it's playing
	 */
	public void stopPlayer() {
		if (player != null) {
			player.stop();
		}
	}
}
