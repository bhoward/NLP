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

	public static void showAnimatedStatus(String message) {
		if (window != null) {
			EventQueue.invokeLater(() -> {
				window.showAnimatedStatus(message);
			});
		}
	}

	public static void stopAnimatedStatus() {
		if (window != null) {
			EventQueue.invokeLater(() -> {
				window.stopAnimatedStatus();
			});
		}
	}

	private static Window window;
}

/* TODO:
 * Allow opening other kinds of documents (txt, rtf, doc?)? Use Apache Tika?
 * When sentence selected for analysis, bring up separate window to edit annotations.
 */