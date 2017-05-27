package edu.depauw.janno2;

import java.awt.EventQueue;

import edu.depauw.janno2.Window;

public class App {
	private static Window window;

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
}

/* TODO:
 * When sentence selected for analysis, bring up separate window to edit annotations.
 */