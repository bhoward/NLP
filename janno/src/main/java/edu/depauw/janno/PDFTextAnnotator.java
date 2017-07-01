/*
 * Based on https://gist.github.com/joelkuiper/9eb52555e02edb653dcf#file-gistfile3-java
 */
package edu.depauw.janno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

public class PDFTextAnnotator extends PDFTextStripper {

	private float verticalTolerance = 0.01f;
	private float heightModifier = (float) 1.50;

	private class Match {
		public final List<TextPosition> positions;

		public Match(List<TextPosition> positions) {
			this.positions = positions;
		}
	}

	/**
	 * Internal class that keeps a mapping from the text contents to their
	 * TextPositions. This is needed to compute bounding boxes. The data is
	 * stored on a per-page basis (keyed on the 1-based pageNo)
	 */
	private class TextCache {
		private final Map<Integer, StringBuilder> texts = new HashMap<Integer, StringBuilder>();
		private final Map<Integer, ArrayList<TextPosition>> positions = new HashMap<Integer, ArrayList<TextPosition>>();

		public StringBuilder obtainStringBuilder(Integer pageNo) {
			StringBuilder sb = texts.get(pageNo);
			if (sb == null) {
				sb = new StringBuilder();
				texts.put(pageNo, sb);
			}
			return sb;
		}

		public ArrayList<TextPosition> obtainTextPositions(Integer pageNo) {
			ArrayList<TextPosition> textPositions = positions.get(pageNo);
			if (textPositions == null) {
				textPositions = new ArrayList<TextPosition>();
				positions.put(pageNo, textPositions);
			}
			return textPositions;
		}

		public String getText(Integer pageNo) {
			return obtainStringBuilder(pageNo).toString();
		}

		public void append(String str, TextPosition pos) {
			int currentPage = getCurrentPageNo();
			ArrayList<TextPosition> positions = obtainTextPositions(currentPage);
			StringBuilder sb = obtainStringBuilder(currentPage);

			for (int i = 0; i < str.length(); i++) {
				sb.append(str.charAt(i));
				positions.add(pos);
			}
		}

		public List<TextPosition> getTextPositions(Integer pageNo) {
			return obtainTextPositions(pageNo);
		}

		public List<Match> getTextPositions(Integer pageNo, Pattern pattern) {
			Matcher matcher = pattern.matcher(getText(pageNo));
			List<Match> matches = new ArrayList<Match>();

			while (matcher.find()) {
				List<TextPosition> elements = this.getTextPositions(pageNo).subList(matcher.start(), matcher.end());
				matches.add(new Match(elements));
			}
			return matches;
		}
	}

	private TextCache textCache;
	private AnnoDoc doc;

	/**
	 * Instantiate a new PDFTextAnnotator object. This object will load
	 * properties from PDFTextAnnotator.properties.
	 *
	 * @throws IOException
	 *             If there is an error reading the properties.
	 */
	public PDFTextAnnotator(AnnoDoc doc) throws IOException {
		this.doc = doc;
	}

	/**
	 * Computes a series of bounding boxes from the TextPositions. It will
	 * create a new bounding box if the vertical tolerance is exceeded
	 * 
	 * @param matches
	 * @throws IOException
	 */
	private List<PDRectangle> getTextBoundingBoxes(List<TextPosition> matches) {
		List<PDRectangle> boundingBoxes = new ArrayList<PDRectangle>();

		float lowerLeftX = 0, lowerLeftY = 0, upperRightX = 0, upperRightY = 0;
		boolean first = true;
		for (int i = 0; i < matches.size(); i++) {
			TextPosition position = matches.get(i);
			if (position == null) {
				continue;
			}
			Matrix textPos = position.getTextMatrix();
			float height = (float) (position.getHeight() * heightModifier);
			float drop = (float) (position.getHeight() * (heightModifier - 1) / 2);
			if (first) {
				lowerLeftX = textPos.getTranslateX();
				upperRightX = lowerLeftX + position.getWidth();

				lowerLeftY = textPos.getTranslateY() - drop;
				upperRightY = lowerLeftY + height;
				first = false;
				continue;
			}

			// we are still on the same line
			if (Math.abs(textPos.getTranslateY() - drop - lowerLeftY) <= getVerticalTolerance()) {
				upperRightX = textPos.getTranslateX() + position.getWidth();
				upperRightY = textPos.getTranslateY() - drop + height;
			} else {
				PDRectangle boundingBox = boundingBox(lowerLeftX, lowerLeftY, upperRightX, upperRightY);
				boundingBoxes.add(boundingBox);

				// new line
				lowerLeftX = textPos.getTranslateX();
				upperRightX = lowerLeftX + position.getWidth();

				lowerLeftY = textPos.getTranslateY() - drop;
				upperRightY = lowerLeftY + height;
			}
		}
		if (!(lowerLeftX == 0 && lowerLeftY == 0 && upperRightX == 0 && upperRightY == 0)) {
			PDRectangle boundingBox = boundingBox(lowerLeftX, lowerLeftY, upperRightX, upperRightY);
			boundingBoxes.add(boundingBox);
		}
		return boundingBoxes;
	}

	private PDRectangle boundingBox(float lowerLeftX, float lowerLeftY, float upperRightX, float upperRightY) {
		PDRectangle boundingBox = new PDRectangle();
		boundingBox.setLowerLeftX(lowerLeftX);
		boundingBox.setLowerLeftY(lowerLeftY);
		boundingBox.setUpperRightX(upperRightX);
		boundingBox.setUpperRightY(upperRightY);
		return boundingBox;
	}

	/**
	 * Highlights a pattern within the PDF with the default color. Returns the
	 * list of added annotations for further modification. Note: it will process
	 * every page, but cannot process patterns that span multiple pages. Note: it
	 * will not work for top-bottom text (such as Chinese)
	 * 
	 * @param pattern
	 *            String that will be converted to Regex pattern
	 * @throws IOException
	 */
	public List<PDAnnotationTextMarkup> highlight(final String pattern) throws IOException {
		return highlight(Pattern.compile(pattern));
	}

	/**
	 * Highlights a pattern within the PDF with the default color. Returns the
	 * list of added annotations for further modification. Note: it will process
	 * every page, but cannot process patterns that span multiple pages. Note: it
	 * will not work for top-bottom text (such as Chinese)
	 * 
	 * @param pattern
	 *            Pattern (regex)
	 * @throws IOException 
	 */
	public List<PDAnnotationTextMarkup> highlight(Pattern pattern) throws IOException {
		if (textCache == null) {
			throw new RuntimeException("TextCache was not initilized, please run initialize on the document first");
		}

		ArrayList<PDAnnotationTextMarkup> highlights = new ArrayList<PDAnnotationTextMarkup>();

		for (int pageIndex = 1; pageIndex <= doc.getNumberOfPages(); pageIndex++) {
			if (pageIndex < getStartPage() || pageIndex > getEndPage())
				continue;

			List<Match> matches = this.textCache.getTextPositions(pageIndex, pattern);

			for (Match match : matches) {
				List<PDRectangle> textBoundingBoxes = getTextBoundingBoxes(match.positions);

				if (textBoundingBoxes.size() > 0) {
					PDAnnotationTextMarkup highlight = doc.addAnnotation(pageIndex - 1, textBoundingBoxes);
					highlights.add(highlight);
				}
			}
		}
		return highlights;
	}

	public float getVerticalTolerance() {
		return this.verticalTolerance;
	}

	public void setVerticalTolerance(float tolerance) {
		this.verticalTolerance = tolerance;
	}

	public void initialize() throws IOException {
		this.textCache = new TextCache();
		PDDocument pdf = doc.getPDDoc();

		if (this.getAddMoreFormatting()) {
			this.setParagraphEnd(this.getLineSeparator());
			this.setPageStart(this.getLineSeparator());
			this.setArticleStart(this.getLineSeparator());
			this.setArticleEnd(this.getLineSeparator());
		}
		this.startDocument(pdf);
		this.processPages(pdf.getPages());
		this.endDocument(pdf);
	}

	/**
	 * Start a new article, which is typically defined as a column on a single
	 * page (also referred to as a bead). Default implementation is to do
	 * nothing. Subclasses may provide additional information.
	 *
	 * @param isltr
	 *            true if primary direction of text is left to right.
	 * @throws IOException
	 *             If there is any error writing to the stream.
	 */
	@Override
	protected void startArticle(final boolean isltr) throws IOException {
		String articleStart = this.getArticleStart();
		this.textCache.append(articleStart, null);
	}

	/**
	 * End an article. Default implementation is to do nothing. Subclasses may
	 * provide additional information.
	 *
	 * @throws IOException
	 *             If there is any error writing to the stream.
	 */
	@Override
	protected void endArticle() throws IOException {
		String articleEnd = this.getArticleEnd();
		this.textCache.append(articleEnd, null);
	}

	/**
	 * Start a new page. Default implementation is to do nothing. Subclasses may
	 * provide additional information.
	 *
	 * @param page
	 *            The page we are about to process.
	 *
	 * @throws IOException
	 *             If there is any error writing to the stream.
	 */
	@Override
	protected void startPage(final PDPage page) throws IOException {
		// do nothing
	}

	/**
	 * End a page. Default implementation is to do nothing. Subclasses may
	 * provide additional information.
	 *
	 * @param page
	 *            The page we are about to process.
	 *
	 * @throws IOException
	 *             If there is any error writing to the stream.
	 */
	@Override
	protected void endPage(final PDPage page) throws IOException {
		// do nothing
	}

	/**
	 * Write the line separator value to the text cache.
	 *
	 * @throws IOException
	 *             If there is a problem writing out the lineseparator to the
	 *             document.
	 */
	@Override
	protected void writeLineSeparator() {
		String lineSeparator = this.getLineSeparator();
		this.textCache.append(lineSeparator, null);
	}

	/**
	 * Write the word separator value to the text cache.
	 *
	 * @throws IOException
	 *             If there is a problem writing out the wordseparator to the
	 *             document.
	 */
	@Override
	protected void writeWordSeparator() {
		String wordSeparator = this.getWordSeparator();
		this.textCache.append(wordSeparator, null);

	}

	/**
	 * Write the string in TextPosition to the text cache.
	 *
	 * @param text
	 *            The text to write to the stream.
	 * @throws IOException
	 *             If there is an error when writing the text.
	 */
	@Override
	protected void writeCharacters(final TextPosition text) {
		String character = text.getUnicode();
		this.textCache.append(character, text);
	}

	/**
	 * Write a string to the text cache. This implementation will ignore
	 * the <code>text</code> and just calls
	 * {@link #writeCharacters(TextPosition)} .
	 *
	 * @param text
	 *            The text to write to the stream.
	 * @param textPositions
	 *            The TextPositions belonging to the text.
	 * @throws IOException
	 *             If there is an error when writing the text.
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
	 *
	 * @throws IOException
	 *             if something went wrong
	 */
	@Override
	protected void writeParagraphSeparator() {
		this.writeParagraphEnd();
		this.writeParagraphStart();
	}

	/**
	 * Write something (if defined) at the start of a paragraph.
	 *
	 * @throws IOException
	 *             if something went wrong
	 */
	@Override
	protected void writeParagraphStart() {
		if (this.inParagraph) {
			this.writeParagraphEnd();
			this.inParagraph = false;
		}

		String paragraphStart = this.getParagraphStart();
		this.textCache.append(paragraphStart, null);
		this.inParagraph = true;
	}

	/**
	 * Write something (if defined) at the end of a paragraph.
	 *
	 * @throws IOException
	 *             if something went wrong
	 */
	@Override
	protected void writeParagraphEnd() {
		String paragraphEnd = this.getParagraphEnd();
		this.textCache.append(paragraphEnd, null);

		this.inParagraph = false;
	}
	// TODO add a marker for paragraph end, then when normalizing the extracted text
	// remove that marker if at the bottom of a column (if next position is higher on
	// the page?). Paragraph markers should force a sentence break (to handle title stuff).

	/**
	 * Write something (if defined) at the start of a page.
	 *
	 * @throws IOException
	 *             if something went wrong
	 */
	@Override
	protected void writePageStart() {
		String pageStart = this.getPageStart();
		this.textCache.append(pageStart, null);
	}

	/**
	 * Write something (if defined) at the start of a page.
	 *
	 * @throws IOException
	 *             if something went wrong
	 */
	@Override
	protected void writePageEnd() {
		String pageEnd = this.getPageEnd();
		this.textCache.append(pageEnd, null);
	}
}