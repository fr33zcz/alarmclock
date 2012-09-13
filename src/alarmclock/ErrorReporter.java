package alarmclock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple global error reporter
 * 
 * @author --==[FReeZ]==--
 * @version 1.0
 */
public class ErrorReporter {
	/**
	 * Empty private constructor to prevent instantiation
	 */
	private ErrorReporter() {
		throw new AssertionError();
	}
	
	/**
	 * Creates a message box to inform about an exception
	 * 
	 * @param shell   the instance of shell that is used to create a MessageBox
	 * @param message the message of message box
	 */
	public static void reportError(Shell shell, String message) {
		final MessageBox msg1 = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		msg1.setText("An exception has occurred in the application.");
		msg1.setMessage(message);
		msg1.open();
	}
}
