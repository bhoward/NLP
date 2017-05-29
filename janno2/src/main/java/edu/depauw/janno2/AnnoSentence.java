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
			App.showAnimatedStatus("Parsing");
			Tree tree = sentence.parse();
			System.out.println(tree);
			System.out.println(sentence.lemmas());
			System.out.println(sentence.governors());
			System.out.println(sentence.coref());
			tree.setSpans();

			for (Tree t : tree.subTreeList()) {
				if (t.value().equals("NP")) {
					IntPair p = t.getSpan();
					int first = p.get(0);
					int last = p.get(1);

					int left = sentence.characterOffsetBegin(first) - sentence.characterOffsetBegin(0);
					int right = sentence.characterOffsetEnd(last) - sentence.characterOffsetBegin(0);

					int gov = -1;
					for (int i = first; i <= last; i++) {
						int g = sentence.governor(i).orElse(-2);
						if (g < first || g > last) {
							gov = g;
						}
					}
					if (gov == -1) {
						System.out.println(t + ": NONE");
					} else {
						System.out.println(t + ": " + sentence.lemma(gov));
					}
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
