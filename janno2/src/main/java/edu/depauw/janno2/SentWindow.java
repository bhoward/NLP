package edu.depauw.janno2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

@SuppressWarnings("serial")
public class SentWindow extends JFrame {
	private JPanel contentPane;
	private JList<AnnoPhrase> list;
	private JTextPane textPane;

	/**
	 * Create the frame.
	 */
	public SentWindow(AnnoSentence as) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane listScrollPane = new JScrollPane();
		contentPane.add(listScrollPane, BorderLayout.CENTER);

		list = new JList<AnnoPhrase>();
		list.addListSelectionListener(new ListSelectionListener() {
			private DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
					Color.YELLOW);

			public void valueChanged(ListSelectionEvent e) {
				if (!list.isSelectionEmpty()) {
					AnnoPhrase ap = list.getSelectedValue();
					try {
						textPane.getHighlighter().removeAllHighlights();
						textPane.getHighlighter().addHighlight(ap.getLeft(), ap.getRight(), highlightPainter);
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// TODO
				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (index % 2 == 0 && !isSelected) {
					setBackground(new Color(250, 200, 150));
				}
				return this;
			}
		});
		listScrollPane.setViewportView(list);

		list.setModel(as.extractPhrases());

		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setText(as.text());
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(textPane);
		contentPane.add(textPanel, BorderLayout.NORTH);
	}
}
