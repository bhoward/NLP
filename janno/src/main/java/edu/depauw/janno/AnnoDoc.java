package edu.depauw.janno;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
		text = text.replaceAll("-\n", "");
		text = text.replaceAll("[^\\x20-\\x7E]", " ");
		nlpDoc = new Document(text);

		// new Thread(() -> {
		// // start parsing the first sentence, because it will take a while to
		// compute the references across the whole document
		// App.showStatus("Parsing...");
		// nlpDoc.sentence(0).parse();
		// App.showStatus("Ready");
		// }).start();;
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

				// App.showAnimatedStatus("Parsing");
				// List<Optional<Integer>> gs = sentence.governors();
				// for (int i = 0; i < gs.size(); i++) {
				// String w = sentence.word(i);
				// Optional<Integer> g = gs.get(i);
				// Optional<String> label = sentence.incomingDependencyLabel(i);
				// if (g.isPresent()) {
				// int n = g.get();
				// if (n >= 0) {
				// System.out.println(w + ": " + sentence.word(n));
				// } else {
				// System.out.println(w + ": ROOT");
				// }
				// } else {
				// System.out.println(w + ": NONE");
				// }
				// System.out.println(label.orElse("NO LABEL"));
				// }
				// App.stopAnimatedStatus();
				// App.showStatus("Ready");

				// SentenceAlgorithms al = sentence.algorithms();
				// for (String phrase : al.keyphrases()) {
				// System.out.println(phrase);
				// }

				// System.out.println(sentence.dependencyGraph());

				// for (RelationTriple rt : sentence.openieTriples()) {
				// System.out.println(rt);
				// }

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
							String s = w.value();
							if (!first && Character.isAlphabetic(s.charAt(0))) {
								sb.append(' ');
							}
							sb.append(s);
							first = false;
						}
						System.out.println(sb.toString());
					}
				}

				working = false;
			}).start();
		}
	}
}
