package edu.depauw.janno2;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class SentWindow extends JFrame {
	private JPanel contentPane;
	private JList<String> list;

	/**
	 * Create the frame.
	 */
	public SentWindow(AnnoSentence as) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		list = new JList<String>();
		contentPane.add(list, BorderLayout.CENTER);

		list.setModel(as.extractPhrases());
	}
}
