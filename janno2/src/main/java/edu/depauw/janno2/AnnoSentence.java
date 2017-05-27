package edu.depauw.janno2;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

public class AnnoSentence {
	private Sentence sentence;

	public AnnoSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public ListModel<String> extractPhrases() {
		DefaultListModel<String> phrases = new DefaultListModel<>();
		
		new Thread(() -> {
			System.out.println(sentence);

			App.showAnimatedStatus("Parsing");
			Tree tree = sentence.parse();
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
					phrases.addElement(sb.toString()); // TODO associate start/end positions
				}
			}
			
			App.stopAnimatedStatus();
			App.showStatus("Ready");
		}).start();
		
		return phrases;
	}
}
