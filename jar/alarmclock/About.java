package alarmclock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * About dialog of Alarmclock
 * 
 * @author --==[FReeZ]==--
 * @version 1.0
 */
public class About {
	/**
	 * This variable is used in decision whether to open new shell or not
	 */
	private static boolean inAbout = false;
	
	/**
	 * Holds the instance of shell 
	 */
	private final Shell shell;
	
	/**
	 * Instantiate the about dialog
	 * 
	 * @param alarmClock instance of AlarmClock
	 */
	About(AlarmClock alarmClock) {
		shell = new Shell(SWT.CLOSE | SWT.BORDER);
		shell.setSize(300, 110);
		shell.setText("About");
		shell.setLayout(new FormLayout());
		
		FormData formData;
		final Link link1 = new Link(shell, SWT.CENTER);
		final String url = "http://freez.security-portal.cz";
		link1.setText("Copyright (c) 2008 <a href=\"" + url + "\">--==[FReeZ]==--</a>");
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.height = 20;
		link1.setLayoutData(formData);
		link1.setToolTipText("Click here to visit my site.");
		link1.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				boolean browserOpened = false;
				final String[] commands = {
						"firefox -remote openURL(" + url + ")",
						"mozilla -remote openURL(" + url + ")",
						"opera " + url,
						"safari " + url,
						"iexplore " + url,
						"/bin/open " + url
				};
				for (String command : commands) {
					if (Program.launch(command)) {
						browserOpened = true;
						break;
					}
				}
				
				if (!browserOpened) {
					ErrorReporter.reportError(shell, "Unable to execute a browser, because it's not in global PATH.");
				}
			}
		});
		
		final Label lbl2 = new Label(shell, SWT.CENTER);
		lbl2.setText("Licensed under GNU/GPL v2");
		formData = new FormData();
		formData.top = new FormAttachment(link1, 5);
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.height = 20;
		lbl2.setLayoutData(formData);
		
		final Button btnOk = new Button(shell, SWT.PUSH | SWT.CENTER);
		btnOk.setText("&OK");
		formData = new FormData();
		formData.left = new FormAttachment(50, -40);
		formData.top  = new FormAttachment(lbl2, 10);
		formData.width = 80;
		formData.height = 25;
		btnOk.setLayoutData(formData);
		
		btnOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				inAbout = false;
			}
		});

		alarmClock.centerShell(shell);
	}
	
	/**
	 * Open the about dialog
	 */
	public void open() {
		if (inAbout) {
			return;
		}
		
		inAbout = true;
		shell.open();
	}	
}
