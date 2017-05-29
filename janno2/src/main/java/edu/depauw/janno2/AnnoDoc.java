package edu.depauw.janno2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.tika.parser.ParsingReader;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class AnnoDoc {
	private Document nlpDoc;

	public AnnoDoc(File in) throws IOException {
		try (BufferedReader r = new BufferedReader(new ParsingReader(in)); Stream<String> lines = r.lines()) {
			String text = lines.map(s -> s + " ").collect(Collectors.joining());
			text = text.replaceAll("-\n", ""); // rejoin hyphenated words
			text = text.replaceAll("[\\x00-\\x1F]", " "); // remove control
															// characters
			text = text.replaceAll(" +", " "); // compress blanks
			nlpDoc = new Document(text);
		}
	}

	public ListModel<Sentence> extractSentences() {
		DefaultListModel<Sentence> listModel = new DefaultListModel<>();
		for (Sentence sentence : nlpDoc.sentences()) {
			listModel.addElement(sentence);
		}
		return listModel;
	}
}
