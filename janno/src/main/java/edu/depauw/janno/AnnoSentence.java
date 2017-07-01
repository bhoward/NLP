package edu.depauw.janno;

import java.util.List;

public class AnnoSentence {
	public String text;
	public List<Location> locations;
	
	public AnnoSentence(String text, List<Location> locations) {
		this.text = text;
		this.locations = locations;
	}
}
