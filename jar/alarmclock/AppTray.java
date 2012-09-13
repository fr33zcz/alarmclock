package alarmclock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

/**
 * Support for Tray icon of Alarmclock
 * 
 * @author --==[FReeZ]==--
 * @version 1.0
 */
public class AppTray {
	/**
	 * System tray and image in it
	 */
	private Tray tray = null;
	
	/**
	 * Tray items
	 */
	private TrayItem trayMainItem;
	private MenuItem trayExitAlarmclock, trayShowAlarmclock, trayHideAlarmclock,
					 traySettings, trayEnableAlarm, trayDisableAlarm, trayAbout;
	
	/**
	 * The instance of About dialog
	 */
	private About aboutDialog = null;
	
	/**
	 * Create the tray for application
	 * 
	 * @param alarmClock the instance of AlarmClock
	 * @param display    the instance of display
	 * @param shell      the instance of shell
	 * @param settings   the instance of settings
	 */
	AppTray(final AlarmClock alarmClock, Display display, final Shell shell, final Settings settings) {
		tray = display.getSystemTray();
		final AppTray self = this;
		if (tray == null) {
			System.out.println ("The system tray is not available");
		} else {
			final Image image = new Image (display, 16, 16);
			trayMainItem = new TrayItem (tray, SWT.NONE);
			trayMainItem.setToolTipText("Alarmclock");
			final Menu trayMenu = new Menu (shell, SWT.POP_UP);
			
			trayAbout = new MenuItem(trayMenu, SWT.PUSH);
			trayAbout.setText("Ab&out");
			trayAbout.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					aboutDialog = new About(alarmClock);
					aboutDialog.open();
				}
			});
			
			trayShowAlarmclock = new MenuItem (trayMenu, SWT.PUSH);
			trayShowAlarmclock.setText("Sh&ow alarmclock");
			trayShowAlarmclock.setEnabled(true);
			trayShowAlarmclock.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					showAlarmClock(shell);
				}
			});
			trayMenu.setDefaultItem(trayShowAlarmclock);
		    
			trayHideAlarmclock = new MenuItem (trayMenu, SWT.PUSH);
			trayHideAlarmclock.setText("H&ide alarmclock");
			trayHideAlarmclock.setEnabled(false);
			trayHideAlarmclock.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					hideAlarmClock(shell);
				}
			});
			
			trayEnableAlarm = new MenuItem (trayMenu, SWT.PUSH);
			trayEnableAlarm.setText("&Enable alarm");
			trayEnableAlarm.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					alarmClock.setAlarmCanRun(false, self);
				}
			});
			
			trayDisableAlarm = new MenuItem (trayMenu, SWT.PUSH);
			trayDisableAlarm.setText("&Disable alarm");
			trayDisableAlarm.setEnabled(false);
			trayDisableAlarm.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					alarmClock.setAlarmCanRun(true, self);
				}
			});
			
			traySettings = new MenuItem (trayMenu, SWT.PUSH);
			traySettings.setText("Se&ttings");
			traySettings.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					settings.open();
				}
			});
			
			trayExitAlarmclock = new MenuItem (trayMenu, SWT.PUSH);
			trayExitAlarmclock.setText("E&xit alarmclock");
			trayExitAlarmclock.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					if (!alarmClock.exitConfirmed()) {
						return;
					}
					
					shell.dispose();
				}
			});
			
			// addListener instead of addSelectoinListener to handle the right mouse button
			trayMainItem.addListener(SWT.MenuDetect, new Listener () {
				public void handleEvent(Event event) {
					trayMenu.setVisible(true);
				}
			});
			trayMainItem.setImage(image);
			image.dispose();
		}
	}
	
	/**
	 * Return the instance of Tray
	 * 
	 * @return the instance of current tray or null
	 */
	public Tray getTray() {
		return tray;
	}
	
	/**
	 * Hide alarm clock
	 * 
	 * @param shell
	 */
	public void hideAlarmClock(Shell shell) {
		shell.setVisible(false);
		trayHideAlarmclock.setEnabled(false);
		trayShowAlarmclock.setEnabled(true);
	}

	/**
	 * Show alarm clock
	 * 
	 * @param shell
	 */
	public void showAlarmClock(Shell shell) {
		shell.setVisible(true);
		trayHideAlarmclock.setEnabled(true);
		trayShowAlarmclock.setEnabled(false);
	}
	
	/**
	 * Set the alarm state to started
	 */
	public void setAlarmStarted() {
		if (tray == null) {
			return;
		}

		trayEnableAlarm.setEnabled(false);
		trayDisableAlarm.setEnabled(true);
	}
	
	/**
	 * Set the alarm state to stopped
	 */
	public void setAlarmStopped() {
		if (tray == null) {
			return;
		}
		
		trayEnableAlarm.setEnabled(true);
		trayDisableAlarm.setEnabled(false);
	}
	
	/**
	 * Set the image as tray icon
	 * 
	 * @param  image   any image of your choice
	 * @return success
	 */
	public boolean setImage(Image image) {
		if (tray != null) {
			trayMainItem.setImage(image);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Disposes of the operating system's resources associated with the receiver and all its descendants.
	 * 
	 * @return success
	 */
	public boolean dispose() {
		if (tray != null && !trayMainItem.isDisposed()) {
			trayMainItem.dispose();
			tray.dispose();
			return true;
		}
		
		return false;
	}
}