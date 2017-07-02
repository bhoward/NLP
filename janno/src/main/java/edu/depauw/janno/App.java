package edu.depauw.janno;

import java.awt.EventQueue;

import edu.depauw.janno.ui.Window;

/**
 * Main class for the JAnno application.
 */
public class App {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					window = new Window();
					window.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void showStatus(final String message) {
		if (window != null) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.showStatus(message);
				}
			});
		}
	}

	public static void showAnimatedStatus(final String message) {
		if (window != null) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.showAnimatedStatus(message);
				}
			});
		}
	}

	public static void stopAnimatedStatus() {
		if (window != null) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.stopAnimatedStatus();
				}
			});
		}
	}

	private static Window window;
}

// TODO When sentence selected for analysis, bring up separate window to edit
// annotations.