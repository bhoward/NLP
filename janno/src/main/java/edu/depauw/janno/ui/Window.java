package edu.depauw.janno.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import edu.depauw.janno.AnnoDoc;
import edu.depauw.janno.PDFTextAnnotator;
import edu.stanford.nlp.simple.Sentence;;

public class Window {
	private JFrame frame;
	private JFileChooser chooser;
	private JLabel status;
	private JScrollPane pageScrollPane;
	private JScrollPane listScrollPane;
	private JPanel controlPanel;
	private JPanel navPanel;
	private JList<Sentence> list;

	private AnnoDoc doc;
	private CurrentPage cp;
	private Thread animThread;
	private String animSuffix;
	private JTextPane textPane;
	private Action previousAction;
	private Action zoomOutAction;
	private Action zoomInAction;
	private Action nextAction;
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
		FileFilter pdfFilter = new FileNameExtensionFilter("PDF file", "pdf");
		chooser.addChoosableFileFilter(pdfFilter);
		chooser.setFileFilter(pdfFilter);

		controlPanel = new JPanel();
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

		Action loadAction = new AbstractAction("Load") {
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
						
						nextAction.setEnabled(true);
						previousAction.setEnabled(true);
						zoomInAction.setEnabled(true);
						zoomOutAction.setEnabled(true);
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
				doc.selectSentence(new Sentence(textPane.getText())); // TODO should this try to use the selected Sentence object itself, for document context?
			}
		};
		analyzeAction.setEnabled(false);
		JButton btnAnalyze = new JButton(analyzeAction);
		controlPanel.add(btnAnalyze);
		
		controlPanel.add(new JButton(new AbstractAction("Test") {
			public void actionPerformed(ActionEvent e) {
				try {
					PDFTextAnnotator ta = new PDFTextAnnotator(doc);
					ta.initialize();
					ta.highlight("the");
					
					PDDocument pdf = doc.getPDDoc();
					pdf.save(new File("/tmp/test.pdf"));
					
					cp.updatePage();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}));

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

		previousAction = new AbstractAction("<") {
			public void actionPerformed(ActionEvent e) {
				cp.previous();
			}
		};
		previousAction.setEnabled(false);
		JButton btnPrevious = new JButton(previousAction);
		btnPrevious.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('<'), "previous");
		btnPrevious.getActionMap().put("previous", previousAction);
		navPanel.add(btnPrevious);

		zoomOutAction = new AbstractAction("-") {
			public void actionPerformed(ActionEvent e) {
				cp.zoomOut();
			}
		}; 
		zoomOutAction.setEnabled(false);
		JButton btnZoomOut = new JButton(zoomOutAction);
		btnZoomOut.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "zoomout");
		btnZoomOut.getActionMap().put("zoomout", zoomOutAction);
		navPanel.add(btnZoomOut);

		zoomInAction = new AbstractAction("+") {
			public void actionPerformed(ActionEvent e) {
				cp.zoomIn();
			}
		}; 
		zoomInAction.setEnabled(false);
		JButton btnZoomIn = new JButton(zoomInAction);
		btnZoomIn.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "zoomin");
		btnZoomIn.getActionMap().put("zoomin", zoomInAction);
		navPanel.add(btnZoomIn);

		nextAction = new AbstractAction(">") {
			public void actionPerformed(ActionEvent e) {
				cp.next();
			}
		};
		nextAction.setEnabled(false);
		JButton btnNext = new JButton(nextAction);
		btnNext.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('>'), "next");
		btnNext.getActionMap().put("next", nextAction);
		navPanel.add(btnNext);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout(0, 0));
		splitPane.setRightComponent(rightPanel);

		listScrollPane = new JScrollPane();
		rightPanel.add(listScrollPane, BorderLayout.CENTER);

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
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (index % 2 == 0 && !isSelected) {
					setBackground(new Color(250, 200, 150));
				}
				return this;
			}
		});
		listScrollPane.setViewportView(list);
		
		textPane = new JTextPane();
		rightPanel.add(textPane, BorderLayout.SOUTH);
		
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
