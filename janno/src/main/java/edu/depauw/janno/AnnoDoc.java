package edu.depauw.janno;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import edu.depauw.janno.ui.CurrentPage;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

public class AnnoDoc {
	private PDDocument pdfDoc;
	private Document nlpDoc;
	private boolean working;

	public AnnoDoc(File in) throws IOException {
		pdfDoc = PDDocument.load(in);
		PDFTextStripper stripper = new PDFTextStripper();
		String text = stripper.getText(pdfDoc);
		text = text.replaceAll("-\n", ""); // rejoin hyphenated words
		text = text.replaceAll("[\\x00-\\x1F]", " "); // remove control characters
		text = text.replaceAll(" +", " "); // compress blanks
		nlpDoc = new Document(text);
	}

	public void close() throws IOException {
		pdfDoc.close();
	}

	public CurrentPage getCurrentPage(JScrollPane scrollPane) {
		return new CurrentPage(pdfDoc, scrollPane);
	}

	public ListModel<Sentence> extractSentences() {
		DefaultListModel<Sentence> listModel = new DefaultListModel<>();
		for (Sentence sentence : nlpDoc.sentences()) {
			listModel.addElement(sentence);
		}
		return listModel;
	}

	public void selectSentence(Sentence sentence) {
		if (!working) {
			working = true;
			new Thread(() -> {
				System.out.println(sentence);

				App.showAnimatedStatus("Parsing");
				Tree tree = sentence.parse();
				App.stopAnimatedStatus();
				App.showStatus("Ready");
				System.out.println(tree);

				for (Tree t : tree.subTreeList()) {
					if (t.value().equals("NP")) {
						List<Word> words = t.yieldWords();
						StringBuilder sb = new StringBuilder();
						boolean first = true;
						for (Word w : words) {
							if (!first) {
								sb.append(' ');
							} else {
								first = false;
							}
							sb.append(w);
						}
						System.out.println(sb);
					}
				}

				working = false;
			}).start();
		}
	}
}
