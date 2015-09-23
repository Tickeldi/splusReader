package de.ole.splusreader.logic;

import java.util.Date;

/**
 * This class represents an Event taken from the Splus Calendar.
 * Its comparability is derived from its start date.
 * 
 * @author Ole Goes
 */
public final class SplusEvent implements Comparable<SplusEvent> {
	private final Date start;
	private final Date end;
	
	private final String title;
	private final String description;
	
	private final String location;
	private final String presenter;
	
	public SplusEvent(
			Date start, 
			Date end, 
			String title, 
			String description, 
			String location, 
			String presenter
			) {
		this.start = start;
		this.end = end;
		this.title = title;
		this.description = description;
		this.location = location;
		this.presenter = presenter;
	}

	
	/**
	 * @return the Date object representing the start of the event
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * @return the Date object representing the end of the event
	 */
	public Date getEnd() {
		return end;
	}

	
	/**
	 * @return the title or headline of the event
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @return the description of the event
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the location of the event as a string
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the presenter of the event
	 */
	public String getPresenter() {
		return presenter;
	}
	
	public String toString() {
		return System.lineSeparator()
		+ "Begin:\t" + start + System.lineSeparator()
		+ "End:\t" + end + System.lineSeparator()
		+ "Title:\t" + title + System.lineSeparator()
		+ "Description:\t" + description + System.lineSeparator()
		+ "Location:\t" + location + System.lineSeparator()
		+ "Presenter:\t" + presenter + System.lineSeparator();
	}

	@Override
	public int compareTo(SplusEvent o) {
		return start.compareTo(o.start);
	}
}
