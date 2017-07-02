package edu.depauw.janno;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JScrollPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

import edu.depauw.janno.ui.CurrentPage;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

public class AnnoDoc {
	private static final PDColor HIGHLIGHT_COLOR = new PDColor(new float[] {0.9843f, 0.9098f, 0.3879f}, PDDeviceRGB.INSTANCE);
	private static final float HIGHLIGHT_OPACITY = 0.5f;
	
	private PDAppearanceDictionary highlightAppearance;

	private PDDocument pdfDoc;
	private boolean working;
	
	public AnnoDoc(File in) throws IOException {
		pdfDoc = PDDocument.load(in);
		highlightAppearance = createHighlightAppearance();
	}

	public void close() throws IOException {
		pdfDoc.close();
	}

	public CurrentPage getCurrentPage(JScrollPane scrollPane) {
		return new CurrentPage(pdfDoc, scrollPane);
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
	
	public PDAnnotationTextMarkup addAnnotation(int pageIndex, List<PDRectangle> positions) throws IOException {
		PDPage page = pdfDoc.getPage(pageIndex);
		List<PDAnnotation> anns = page.getAnnotations();
		
		PDAnnotationTextMarkup ann = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
		ann.setRectangle(positions.get(0));
		
		float[] quads = new float[8 * positions.size()];
		for (int i = 0; i < positions.size(); i++) {
			PDRectangle position = positions.get(i);
			quads[8*i] = position.getLowerLeftX();
			quads[8*i + 1] = position.getUpperRightY();
			quads[8*i + 2] = position.getUpperRightX();
			quads[8*i + 3] = quads[8*i + 1];
			quads[8*i + 4] = quads[8*i];
			quads[8*i + 5] = position.getLowerLeftY();
			quads[8*i + 6] = quads[8*i + 2];
			quads[8*i + 7] = quads[8*i + 5];
		}
		ann.setQuadPoints(quads);
		
		ann.setColor(HIGHLIGHT_COLOR);
		ann.setConstantOpacity(HIGHLIGHT_OPACITY);
		ann.setPrinted(true);
		ann.setAppearance(highlightAppearance);
		ann.setContents("Foo!");
		
		anns.add(ann);
		return ann;
	}
	
	private PDAppearanceDictionary createHighlightAppearance() {
		PDRectangle bb = pdfDoc.getPage(0).getBBox(); // assumes all pages same size

		PDAppearanceStream ap = new PDAppearanceStream(pdfDoc);
		ap.setBBox(bb);
		ap.setResources(new PDResources());

		try {
			PDPageContentStream contents = new PDPageContentStream(pdfDoc, ap, ap.getCOSObject().createOutputStream());

			PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
			graphicsState.setNonStrokingAlphaConstant(HIGHLIGHT_OPACITY);

			contents.setGraphicsStateParameters(graphicsState);
			Color hl = new Color(HIGHLIGHT_COLOR.toRGB());
			contents.setNonStrokingColor(hl.getRed(), hl.getGreen(), hl.getBlue());
			contents.addRect(bb.getLowerLeftX(), bb.getLowerLeftY(), bb.getWidth(), bb.getHeight());
			contents.fill();

			contents.close();
		} catch (IOException e) {
			// can't really do anything if this fails...
			e.printStackTrace();
		}

		PDAppearanceDictionary appearance = new PDAppearanceDictionary();
		appearance.setNormalAppearance(ap);
		return appearance;
	}


	public PDDocument getPDDoc() {
		return pdfDoc;
	}

	public int getNumberOfPages() {
		return pdfDoc.getNumberOfPages();
	}
}
