package edu.depauw.janno.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class CurrentPage {
	private PDFRenderer renderer;
	private int index;
	private int maxIndex;
	private float scale;

	private JScrollPane scrollPane;
	private BufferedImage image;

	public CurrentPage(PDDocument doc, JScrollPane scrollPane) {
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
