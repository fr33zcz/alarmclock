package alarmclock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple application to wake you at the preset time
 * 
 * @author --==[FReeZ]==-- 
 * @version 0.9 - licensed under GNU/GPL v2
 * 
 * TODO 
 * - add more combo boxes to make the TimeZone selection more comfortable
 * - consider skins
 * - clean up even more
 */
public class AlarmClock {
	/**
	 * The name of configuration file to use
	 */
	public final String CONFIGURATION_FILE = "configuration.xml";
	
	/**
	 * Default button width and height
	 */
	public final int BUTTON_WIDTH  = 85;
	public final int BUTTON_HEIGHT = 27;
	
	/**
	 * Form controls
	 */
	private final Label labelCurrentTime;
	private final Button buttonStartStop;
	private final Label labelAlarmTime;
	private Shell shell;
	private Display display;
	private Settings settings;	
	
	/**
	 * Calendar and time zone
	 */
	private final Calendar calendar;
	private TimeZone timezone;
	
	/**
	 * The instance of mp3 player
	 */
	Mp3Player player = new Mp3Player();
	
	/**
	 * Thread which outputs the current time to lbl1 and invokes alarm
	 */
	private Thread updateTime = null;
	private Runnable performTimeUpdating = null;
	
	/**
	 * These variables are used to handle the start / stop actions of PlayMP3
	 */
	private boolean alarmStarted = false;
	private boolean cannotRunAlarm = false;
	
	/**
	 * Used with TrayApp to hide / show the main shell
	 */
	private boolean shellMinimized = false;
	
	/**
	 * Application tray icon with menu
	 */
	private final AppTray appTray;
	
	/**
	 * Main routine of the class
	 */
	AlarmClock() {
		createShellAndDisplay();
		appTray = new AppTray(this, display, shell, settings);
		if (appTray.getTray() != null) {
			shellMinimized = true;
			shell.addListener(SWT.Iconify, new Listener() {
				public void handleEvent(Event event) {
					if (shellMinimized) {
						return;
					}
					
					shellMinimized = false;
					shell.setMinimized(false);
					appTray.hideAlarmClock(shell);
					shellMinimized = true;
				}
			});
			
			shell.addListener(SWT.Deiconify, new Listener() {
				public void handleEvent(Event event) {
					if (!shellMinimized) {
						return;
					}
					
					shellMinimized = false;
				}
			});
		}
		
		// Start / stop button
		buttonStartStop = new Button(shell, SWT.PUSH);
		FormData formData = new FormData();
		formData.left = new FormAttachment(2);
		formData.top = new FormAttachment(1);
		formData.width = BUTTON_WIDTH;
		formData.height = BUTTON_HEIGHT;
		buttonStartStop.setLayoutData(formData);
		buttonStartStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAlarmCanRun(!cannotRunAlarm, appTray);
			}
		});
		setAlarmCanRun(true, appTray);
		
		// Settings button
		final Button buttonSettings = new Button(shell, SWT.PUSH);
		formData = new FormData();
		formData.left = new FormAttachment(2);
		formData.top = new FormAttachment(buttonStartStop, 5);
		formData.width = BUTTON_WIDTH;
		formData.height = BUTTON_HEIGHT;
		buttonSettings.setLayoutData(formData);
		buttonSettings.setText("S&ettings");
		buttonSettings.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!settings.open()) {
					final Shell shellSettings = settings.getShell();
					if (null != shellSettings) {
						shellSettings.forceActive();
					}
				}
			}
		});
		
		// Group to show the current time and the time when alarm is invoked
		final Group groupboxTime = new Group(shell, SWT.SHADOW_ETCHED_OUT);
		formData = new FormData();
		formData.left = new FormAttachment(buttonStartStop, 20);
		formData.top = new FormAttachment(1);
		formData.right = new FormAttachment(100, -20);
		formData.height = 45;
		groupboxTime.setLayoutData(formData);
		groupboxTime.setText("Current time / alarm time");
		
		// Label with current time
		labelCurrentTime = new Label(groupboxTime, SWT.LEFT);
		labelCurrentTime.setText("hh:mm:ss");
		labelCurrentTime.setBounds(7, 20, 100, 20);
		
		try {
			settings.load(CONFIGURATION_FILE);
		} catch (IOException exception) {
			ErrorReporter.reportError(shell, "Unable to read the configuration from '" + CONFIGURATION_FILE + "'");
		}
		
		calendar = Calendar.getInstance();
		setNewTimezone();
		
		labelAlarmTime = new Label(groupboxTime, SWT.LEFT);
		labelAlarmTime.setBounds(7, 40, 100, 20);
		updateAlarmTime(settings.getAlarmHour(), settings.getAlarmMinute(), settings.getAlarmSecond());
				
		createTimeUpdater();
		groupboxTime.pack();
		startTimeUpdater();
		shell.open();
		InputStream is = AlarmClock.class.getResourceAsStream("16x16.gif");
		if (is == null) {
			ErrorReporter.reportError(shell, "Unable to find 16x16.gif");
		} else if (appTray.setImage(new Image(display, is))) {
			shell.setVisible(false);
		}
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		appTray.dispose();
		display.dispose();
	}
	
	/**
	 * Create a display and shell for this application, also create the instance of settings
	 * and set shell title to Alarm clock
	 */
	private void createShellAndDisplay() {
		display = new Display();
		shell = new Shell(SWT.BORDER | SWT.CLOSE);
		shell.setSize(350, 100);
		shell.setText("Alarm clock");
		shell.setLayout(new FormLayout());
		centerShell(shell);
		addOnCloseListener(shell);
		settings = new Settings(this, player);
	}
	
	/**
	 * Update a time in label1 and when system time matches the preset alarm time, play  
	 * MP3 + show a MessageBox which waits until OK is hit to stop the player.
	 */
	private void createTimeUpdater() {
		performTimeUpdating = new Runnable() {
			public void run() {
				final Calendar cal = Calendar.getInstance(timezone);
				labelCurrentTime.setText(String.format("%1$tH:%1$tM:%1$tS", cal));
				
				if (!cannotRunAlarm && !alarmStarted &&
					cal.get(Calendar.HOUR_OF_DAY) == settings.getAlarmHour() &&
					cal.get(Calendar.MINUTE) >= settings.getAlarmMinute() &&
					cal.get(Calendar.SECOND) >= settings.getAlarmSecond())
				{
					if (settings.inSettings()) {
						settings.stopPlayer();
					}
					
					alarmStarted = true;
					Runnable runnable = new Runnable() {
						public void run() {
							Thread thread = new Thread() {
								public void run() {
									try {
										player.setFileToPlay(settings.getAlarmFilename());
										player.addPlaybackListener(null);
										player.play();
									} catch (Throwable e) {
										if (player.isPlaying()) {
											player.stop();
										}
										
										ErrorReporter.reportError(shell, "Mp3Player failed.\nDetail: " + e.toString());
										return;
									}
									
									final MessageBox msg1 = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.TOP);
									msg1.setText("The alarm has been executed.");
									msg1.setMessage("After you wake up, press this button.");
									msg1.open();
									
									player.stop();
									setAlarmCanRun(true, appTray);
									alarmStarted = false;
									cannotRunAlarm = true;
								}
							};
							
							display.asyncExec(thread);
						}
					};
					
					runnable.run();
				}
			}
		};
		
		/**
		 * The thread to perform time updating every second
		 */
		updateTime = new Thread() {
			public void run() {
				while (null != updateTime && display != null && !display.isDisposed()) {
					if (display == null) {
						return;
					}
					
					display.syncExec(performTimeUpdating);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
				};
			}
		};
	}
	
	/**
	 * Start the time updating Thread (it's not started in the constructor to allow extending)
	 */
	private void startTimeUpdater() {
		if (updateTime == null) {
			throw new IllegalStateException();
		}
		
		updateTime.start();
	}
	
	/**
	 * Set new timezone for updateTime
	 */
	public void setNewTimezone() {
		final String tmp[] = TimeZone.getAvailableIDs();
		timezone = TimeZone.getDefault();
		if (settings.getTimezone() >= 0 && settings.getTimezone() < tmp.length) {
			timezone = TimeZone.getTimeZone(tmp[settings.getTimezone()]);
			calendar.setTimeZone(timezone);
		}
	}
	
	/**
	 * Asks the user whether to exit or not when settings.getConfirmExit() is true
	 * 
	 * @return boolean exit
	 */
	public boolean exitConfirmed() {
		if (settings.getConfirmExit()) {
			MessageBox msg1 = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			msg1.setText("Exit alarmclock");
			msg1.setMessage("Are you sure you want to exit alarmclock?");
			if (SWT.YES != msg1.open()) {
				return false;
			}
		}
		
		if (player.isPlaying()) {
			player.stop();
		}
		
		player.dispose();
		return true;
	}
	
	/**
	 * Adds on close listener that denies exit when it's not confirmed
	 * 
	 * @param shell
	 */
	public void addOnCloseListener(final Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent event) {
				if (!exitConfirmed()) {
					event.doit = false;
				}
			}
		});
	}
	
	/**
	 * If alarm can run, set true. Tray will be updated when possible.
	 * 
	 * @param canRun  can run alarm
	 * @param appTray application tray
	 */
	public void setAlarmCanRun(boolean canRun, AppTray appTray) {
		if (canRun) {
			if (cannotRunAlarm) {
				return;
			}

			cannotRunAlarm = true;
			buttonStartStop.setText("&Enable");
			appTray.setAlarmStopped();			
		} else {
			if (!cannotRunAlarm) {				
				return;
			}
			
			cannotRunAlarm = false;
			buttonStartStop.setText("&Disable");
			appTray.setAlarmStarted();
		}
	}
	
	/**
	 * Update the time when alarm is invoked
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public void updateAlarmTime(int hour, int minute, int second) {
		if (calendar == null) {
			throw new IllegalStateException();
		}
		
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		labelAlarmTime.setText(String.format("%1$tH:%1$tM:%1$tS", calendar));
	}
	
	/**
	 * Main routine
	 * 
	 * @param argv an array of application arguments
	 */
	public static void main(String argv[]) {
		new AlarmClock();
	}
	
	/**
	 * Center shell on the screen
	 *  
	 * @param shell the instance of shell
	 */
	public void centerShell(Shell shell) {
		if (display == null) {
			throw new IllegalStateException();
		}
		
		final Monitor monitor = display.getPrimaryMonitor();
		final Rectangle monitorBounds = monitor.getBounds();
		final Rectangle shellBounds = shell.getBounds();
		final int x = monitorBounds.width - shellBounds.width >> 1;
		final int y = monitorBounds.height - shellBounds.height >> 1;
		shell.setLocation(x, y);
	}
}
