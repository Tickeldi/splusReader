package de.ole.splusreader.logic;

public final class Option {
	
	private final String title;
	private final String value;
	private final int number;
	
	public Option(int number, String title, String value) {
		this.title = title;
		this.number = number;
		this.value = value;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return number + " " + title + " (" + value + ")";
	}
}
