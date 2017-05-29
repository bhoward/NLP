package edu.depauw.janno2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.nlp.simple.Sentence;

public class Window {
	private JFrame frame;
	private JFileChooser chooser;
	private JLabel status;
	private JScrollPane listScrollPane;
	private JPanel controlPanel;
	private JList<Sentence> list;

	private AnnoDoc doc;
	private Thread animThread;
	private String animSuffix;
	private JTextPane textPane;
	private Action analyzeAction;

	/**
	 * Create the application.
	 */
	public Window() {
		initialize();
	}

	/**
	 * Display the window and start interacting with user.
	 */
	public void start() {
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @wbp.parser.entryPoint
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		chooser = new JFileChooser();

		controlPanel = new JPanel();
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

		Action loadAction = new AbstractAction("Load") {
			public void actionPerformed(ActionEvent e) {
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File in = chooser.getSelectedFile();
					try {
						doc = new AnnoDoc(in);
						list.setModel(doc.extractSentences());
					} catch (IOException e1) {
						showStatus(e1.getMessage());
					}
				}
			}
		};
		JButton btnLoad = new JButton(loadAction);
		controlPanel.add(btnLoad);

		analyzeAction = new AbstractAction("Analyze") {
			public void actionPerformed(ActionEvent e) {
				// TODO should this try to use the selected Sentence object itself, for document parsing context?
//				AnnoSentence as = new AnnoSentence(new Sentence(textPane.getText()));
				AnnoSentence as = new AnnoSentence(list.getSelectedValue());
				SentWindow sw = new SentWindow(as);
				sw.setVisible(true);
			}
		};
		analyzeAction.setEnabled(false);
		JButton btnAnalyze = new JButton(analyzeAction);
		controlPanel.add(btnAnalyze);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		frame.getContentPane().add(panel);

		listScrollPane = new JScrollPane();
		panel.add(listScrollPane, BorderLayout.CENTER);

		list = new JList<Sentence>();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (doc != null && !list.isSelectionEmpty()) {
					Sentence sentence = list.getSelectedValue();
					textPane.setText(sentence.text());
					analyzeAction.setEnabled(true);
				} else {
					analyzeAction.setEnabled(false);
				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				setText(value.toString());

				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					if (index % 2 == 0) {
						setBackground(new Color(250, 200, 150));
					} else {
						setBackground(list.getBackground());
					}
					setForeground(list.getForeground());
				}
				return this;
			}
		});
		listScrollPane.setViewportView(list);
		
		textPane = new JTextPane();		
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(textPane);
		panel.add(textPanel, BorderLayout.SOUTH);
		
		status = new JLabel(" ");
		frame.getContentPane().add(status, BorderLayout.SOUTH);
	}

	public void showStatus(String message) {
		status.setText(message);
	}
	
	public void showAnimatedStatus(String message) {
		stopAnimatedStatus();
		animSuffix = "";
		animThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				if (animSuffix.length() == 10) {
					animSuffix = "";
				}
				animSuffix = animSuffix + ".";
				showStatus(message + animSuffix);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		animThread.start();
	}
	
	public void stopAnimatedStatus() {
		if (animThread != null) {
			animThread.interrupt();
		}
	}
}
