package edu.depauw.janno.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.depauw.janno.AnnoDoc;
import edu.stanford.nlp.simple.Sentence;;

public class Window {
	private JFrame frame;
	private JFileChooser chooser;
	private JLabel status;
	private JScrollPane pageScrollPane;
	private JScrollPane listScrollPane;
	private JPanel controlPanel;
	private JPanel navPanel;
	private JButton btnPrevious;
	private JButton btnNext;
	private JList<Sentence> list;

	private AnnoDoc doc;
	private CurrentPage cp;

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

		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File in = chooser.getSelectedFile();
					try {
						if (doc != null) {
							doc.close();
						}
						doc = new AnnoDoc(in);
						cp = doc.getCurrentPage(pageScrollPane);
						list.setModel(doc.extractSentences());
						btnNext.setEnabled(true);
						btnPrevious.setEnabled(true);
					} catch (IOException e1) {
						showStatus(e1.getMessage());
					}
				}
			}
		});
		controlPanel.add(btnLoad);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout(0, 0));
		splitPane.setLeftComponent(leftPanel);

		pageScrollPane = new JScrollPane();
		leftPanel.add(pageScrollPane, BorderLayout.CENTER);

		JPanel page = new JPanel();
		pageScrollPane.setViewportView(page);

		navPanel = new JPanel();
		leftPanel.add(navPanel, BorderLayout.SOUTH);
		navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));

		btnPrevious = new JButton("Previous");
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cp.previous();
			}
		});
		btnPrevious.setEnabled(false);
		navPanel.add(btnPrevious);

		btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cp.next();
			}
		});
		btnNext.setEnabled(false);
		navPanel.add(btnNext);

		listScrollPane = new JScrollPane();
		splitPane.setRightComponent(listScrollPane);

		list = new JList<Sentence>();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (doc != null) {
					Sentence sentence = list.getSelectedValue();
					doc.selectSentence(sentence);
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

		status = new JLabel(" ");
		frame.getContentPane().add(status, BorderLayout.SOUTH);
	}

	public void showStatus(String message) {
		status.setText(message);
	}
}
