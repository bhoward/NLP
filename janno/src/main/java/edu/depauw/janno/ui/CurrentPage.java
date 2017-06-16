package edu.depauw.janno.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.rendering.PDFRenderer;

public class CurrentPage {
	private PDDocument doc;
	private PDFRenderer renderer;
	private int index;
	private int maxIndex;
	private float scale;

	private JScrollPane scrollPane;
	private BufferedImage image;

	public CurrentPage(PDDocument doc, JScrollPane scrollPane) {
		this.doc = doc;
		this.renderer = new PDFRenderer(doc);
		this.index = 0;
		this.maxIndex = doc.getNumberOfPages() - 1;
		this.scale = 1.0f;
		this.scrollPane = scrollPane;

		updatePage();
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
            float ph = page.getMediaBox().getUpperRightY();
			PDRectangle position = new PDRectangle();
			position.setLowerLeftX(72);
			position.setLowerLeftY(ph - 90);
			position.setUpperRightX(144);
			position.setUpperRightY(ph - 72);
			ann.setRectangle(position);
			float[] quads = new float[8];
            quads[0] = position.getLowerLeftX();  // x1
            quads[1] = position.getUpperRightY()-2; // y1
            quads[2] = position.getUpperRightX(); // x2
            quads[3] = quads[1]; // y2
            quads[4] = quads[0];  // x3
            quads[5] = position.getLowerLeftY()-2; // y3
            quads[6] = quads[2]; // x4
            quads[7] = quads[5]; // y5
            ann.setQuadPoints(quads);
			PDColor red = new PDColor(new float[] { 1, 0, 0 }, PDDeviceRGB.INSTANCE);
            PDColor blue = new PDColor(new float[] { 0, 0, 1 }, PDDeviceRGB.INSTANCE);
            PDBorderStyleDictionary borderThin = new PDBorderStyleDictionary();
            borderThin.setWidth(1); // 1 point
	        ann.setColor(blue);
	        ann.setConstantOpacity(0.2f);
	        ann.setPrinted(true);
	        ann.setContents("FOO!");
			anns.add(ann);
			
			PDAnnotationSquareCircle aCircle = new PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_CIRCLE);
            aCircle.setContents("Circle Annotation");
            aCircle.setInteriorColor(red);  // Fill in circle in red
            aCircle.setColor(blue); // The border itself will be blue
            aCircle.setBorderStyle(borderThin);

            // Place the annotation on the page, we'll make this 1" round
            // 3" down, 1" in on the page
            position = new PDRectangle();
            position.setLowerLeftX(72);
            position.setLowerLeftY(ph - 4 * 72); // 1" height, 3" down
            position.setUpperRightX(2 * 72); // 1" in, 1" width
            position.setUpperRightY(ph - 3 * 72); // 3" down
            aCircle.setRectangle(position);
            anns.add(aCircle);
            
			doc.save(new File("/tmp/test.pdf"));
			renderer = new PDFRenderer(doc);
			updatePage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updatePage() {
		try {
			image = renderer.renderImage(index, scale);
			JLabel label = new JLabel(new ImageIcon(image));
			scrollPane.setViewportView(label);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
