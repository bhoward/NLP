package edu.depauw.janno.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
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
import org.apache.pdfbox.rendering.PDFRenderer;

public class CurrentPage {
	private PDDocument doc;
	private PDFRenderer renderer;
	private int index;
	private int maxIndex;
	private float scale;

	private JScrollPane scrollPane;
	private BufferedImage image;
	private PDAppearanceDictionary highlightAppearance;

	public CurrentPage(PDDocument doc, JScrollPane scrollPane) {
		this.doc = doc;
		this.renderer = new PDFRenderer(doc);
		this.index = 0;
		this.maxIndex = doc.getNumberOfPages() - 1;
		this.scale = 1.0f;
		this.scrollPane = scrollPane;

		updatePage();
		highlightAppearance = createHighlightAppearance();
	}

	public void previous() {
		if (index > 0) {
			index--;
			updatePage();
		}
	}

	public void next() {
		if (index < maxIndex) {
			index++;
			updatePage();
		}
	}

	public void zoomIn() {
		scale *= 1.2;
		updatePage();
	}

	public void zoomOut() {
		scale /= 1.2;
		updatePage();
	}

	public void addAnnotation(float x, float y, float width, float height) {
		try {
			PDPage page = doc.getPage(index);
			List<PDAnnotation> anns = page.getAnnotations();
			PDAnnotationTextMarkup ann = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
			PDRectangle position = new PDRectangle();
			position.setLowerLeftX(x);
			position.setLowerLeftY(y);
			position.setUpperRightX(x + width);
			position.setUpperRightY(y - height);
			ann.setRectangle(position);
			float[] quads = new float[8];
			quads[0] = position.getLowerLeftX(); // x1
			quads[1] = position.getUpperRightY() - 2; // y1
			quads[2] = position.getUpperRightX(); // x2
			quads[3] = quads[1]; // y2
			quads[4] = quads[0]; // x3
			quads[5] = position.getLowerLeftY() - 2; // y3
			quads[6] = quads[2]; // x4
			quads[7] = quads[5]; // y5
			ann.setQuadPoints(quads);

			PDColor yellow = new PDColor(new float[] { 1, 1, 0 }, PDDeviceRGB.INSTANCE);
			ann.setColor(yellow);
			ann.setConstantOpacity(0.5f);
			ann.setPrinted(true);
			ann.setContents("FOO!");

			ann.setAppearance(highlightAppearance);
			anns.add(ann);

//			updatePage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private PDAppearanceDictionary createHighlightAppearance() {
		PDRectangle bb = doc.getPage(0).getBBox(); // assumes all pages same size

		PDAppearanceStream ap = new PDAppearanceStream(doc);
		ap.setBBox(bb);
		ap.setResources(new PDResources());

		try {
			PDPageContentStream contents = new PDPageContentStream(doc, ap, ap.getCOSObject().createOutputStream());

			PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
			graphicsState.setNonStrokingAlphaConstant(0.5f);

			contents.setGraphicsStateParameters(graphicsState);
			contents.setNonStrokingColor(255, 255, 0);
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

	void updatePage() {
		try {
			image = renderer.renderImage(index, scale);
			JLabel label = new JLabel(new ImageIcon(image));
			scrollPane.setViewportView(label);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
