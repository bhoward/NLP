package edu.depauw.janno2;

public class AnnoPhrase {
	private String phrase;
	private int left;
	private int right;

	public AnnoPhrase(String phrase, int left, int right) {
		this.phrase = phrase;
		this.left = left;
		this.right = right;
	}
	
	public String toString() {
		return phrase;
	}
	
	public int getLeft() {
		return left;
	}
	
	public int getRight() {
		return right;
	}
}
