package edu.depauw.janno2;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.IntPair;

public class AnnoSentence {
	private Sentence sentence;

	public AnnoSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public ListModel<AnnoPhrase> extractPhrases() {
		DefaultListModel<AnnoPhrase> phrases = new DefaultListModel<>();

		new Thread(() -> {
			System.out.println(sentence);

			App.showAnimatedStatus("Parsing");
			Tree tree = sentence.parse();
			System.out.println(tree);
			tree.setSpans();

			for (Tree t : tree.subTreeList()) {
				if (t.value().equals("NP")) {
					IntPair p = t.getSpan();
					int left = sentence.characterOffsetBegin(p.get(0));
					int right = sentence.characterOffsetEnd(p.get(1));
					phrases.addElement(new AnnoPhrase(text().substring(left, right), left, right));
				}
			}

			App.stopAnimatedStatus();
			App.showStatus("Ready");
		}).start();

		return phrases;
	}

	public String text() {
		return sentence.text();
	}
}
