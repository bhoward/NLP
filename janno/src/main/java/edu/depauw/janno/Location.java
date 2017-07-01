package edu.depauw.janno;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class Location {
	public int pageIndex;
	public PDRectangle rectangle;

	public Location(int pageIndex, PDRectangle rectangle) {
		this.pageIndex = pageIndex;
		this.rectangle = rectangle;
	}
}