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
		EventQueue.invokeLater(() -> {
			try {
				window = new Window();
				window.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void showStatus(String message) {
		if (window != null) {
			EventQueue.invokeLater(() -> {
				window.showStatus(message);
			});
		}
	}

	private static Window window;
}
