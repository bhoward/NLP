/*
 * Based on https://gist.github.com/joelkuiper/9eb52555e02edb653dcf#file-gistfile3-java
 */
package edu.depauw.janno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class TextLocationStripper extends PDFTextStripper {
	private final float verticalTolerance = 0.01f;
	private final float heightModifier = (float) 1.50;

	// parallel collections of characters and their document locations
	private StringBuilder text;
	private List<Location> locations;

	private void appendLocation(String str, PDRectangle rectangle) {
		Location location = new Location(getCurrentPageNo(), rectangle);
		for (int i = 0; i < str.length(); i++) {
			// don't add multiple spaces
			if (str.charAt(i) == ' ' && text.length() > 0 && text.charAt(text.length() - 1) == ' ')
				continue;

			text.append(str.charAt(i));
			locations.add(location);
		}
	}

	/**
	 * Instantiate a new TextLocationStripper object.
	 *
	 * @throws IOException
	 *             If there is an error constructing the PDFTextStripper.
	 */
	public TextLocationStripper(PDDocument doc) throws IOException {
		this.text = new StringBuilder();
		this.locations = new ArrayList<>();

		this.startDocument(doc);
		this.processPages(doc.getPages());
		this.endDocument(doc);
		
		// clean up column bottoms
		for (int i = 1; i < text.length() - 1; i++) {
			if (text.charAt(i) == '\n') {
				// check for previous location being at bottom of a column
				int prev = i - 1;
				Location prevLoc = locations.get(prev);
				while (prevLoc.rectangle == null && prev > 0) {
					prev--;
					prevLoc = locations.get(prev);
				}
				if (prevLoc.rectangle == null) continue;
				
				int next = i + 1;
				Location nextLoc = locations.get(next);
				while (nextLoc.rectangle == null && next < text.length() - 1) {
					next++;
					nextLoc = locations.get(next);
				}
				if (nextLoc.rectangle == null) continue;
				
				if (prevLoc.rectangle.getLowerLeftY() < nextLoc.rectangle.getLowerLeftY()) {
					// remove the paragraph break (\n) if so
					text.deleteCharAt(i);
					locations.remove(i);
				}
			}
		}
	}

	private PDRectangle getTextBoundingBox(TextPosition position) {
		if (position == null) return null;
		
		Matrix textPos = position.getTextMatrix();
		float height = position.getHeight() * heightModifier;
		float drop = (height - position.getHeight()) / 2f;
		float width = position.getWidth();
		float llx = textPos.getTranslateX();
		float lly = textPos.getTranslateY() - drop;
		return new PDRectangle(llx, lly, width, height);
	}

	public ListModel<AnnoSentence> extractSentences() {
		DefaultListModel<AnnoSentence> sentences = new DefaultListModel<>();

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit");
		props.setProperty("ssplit.newlineIsSentenceBreak", "always");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Annotation annDoc = new Annotation(text.toString());
		pipeline.annotate(annDoc);
		List<CoreMap> sents = annDoc.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sent : sents) {
			int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
			int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
			String s = text.substring(start, end);
			List<Location> locs = combineLocations(locations.subList(start, end));
			AnnoSentence sentence = new AnnoSentence(s, locs);
			sentences.addElement(sentence);
		}

		return sentences;
	}

	private List<Location> combineLocations(List<Location> locations) {
		List<Location> locs = new ArrayList<>();

		boolean first = true;
		float llx = 0, lly = 0, urx = 0, ury = 0;
		int page = 0;
		for (Location location : locations) {
			PDRectangle rect = location.rectangle;
			if (rect == null) continue;
			
			if (first) {
				llx = rect.getLowerLeftX();
				lly = rect.getLowerLeftY();
				urx = rect.getUpperRightX();
				ury = rect.getUpperRightY();
				page = location.pageIndex;
				first = false;
			} else if (Math.abs(rect.getLowerLeftY() - lly) <= verticalTolerance && location.pageIndex == page) {
				// still on the same line
				urx = rect.getUpperRightX();
				ury = rect.getUpperRightY();
			} else {
				// new line
				locs.add(new Location(page, new PDRectangle(llx, lly, urx - llx, ury - lly)));
				llx = rect.getLowerLeftX();
				lly = rect.getLowerLeftY();
				urx = rect.getUpperRightX();
				ury = rect.getUpperRightY();
				page = location.pageIndex;
			}
		}
		
		if (!first) {
			// finish up last rectangle
			locs.add(new Location(page, new PDRectangle(llx, lly, urx - llx, ury - lly)));
		}

		return locs;
	}

	@Override
	protected void writePageStart() throws IOException {
		// do nothing
	}

	@Override
	protected void writePageEnd() throws IOException {
		// do nothing
	}

	@Override
	protected void startArticle(boolean isLTR) throws IOException {
		// do nothing
	}

	@Override
	protected void endArticle() throws IOException {
		// do nothing
	}

	/**
	 * Write a space at the end of the line, or remove a previous space or hyphen.
	 */
	@Override
	protected void writeLineSeparator() {
		if (text.charAt(text.length() - 1) == ' ') {
			text.deleteCharAt(text.length() - 1);
			locations.remove(locations.size() - 1);
		}
		
		if (text.charAt(text.length() - 1) == '-') {
			text.deleteCharAt(text.length() - 1);
			locations.remove(locations.size() - 1);
		} else {
			appendLocation(" ", null);
		}
	}

	/**
	 * Write the word separator value to the text cache.
	 */
	@Override
	protected void writeWordSeparator() {
		appendLocation(" ", null);
	}

	/**
	 * Write the string in TextPosition to the text cache.
	 *
	 * @param position
	 *            The text to write to the stream.
	 */
	@Override
	protected void writeCharacters(final TextPosition position) {
		String character = position.getUnicode();
		appendLocation(character, getTextBoundingBox(position));
	}

	/**
	 * Write a string to the text cache. This implementation will ignore the
	 * <code>text</code> and just calls {@link #writeCharacters(TextPosition)} .
	 *
	 * @param text
	 *            The text to write to the stream.
	 * @param textPositions
	 *            The TextPositions belonging to the text.
	 */
	@Override
	protected void writeString(final String text, final List<TextPosition> textPositions) {
		for (final TextPosition textPosition : textPositions) {
			this.writeCharacters(textPosition);
		}
	}

	private boolean inParagraph;

	/**
	 * writes the paragraph separator string to the text cache.
	 */
	@Override
	protected void writeParagraphSeparator() {
		this.writeParagraphEnd();
		this.writeParagraphStart();
	}

	/**
	 * Write something (if defined) at the start of a paragraph.
	 */
	@Override
	protected void writeParagraphStart() {
		if (this.inParagraph) {
			this.writeParagraphEnd();
		}
		this.inParagraph = true;
	}

	/**
	 * Write something at the end of a paragraph.
	 */
	@Override
	protected void writeParagraphEnd() {
		appendLocation("\n", null);
		this.inParagraph = false;
	}
}