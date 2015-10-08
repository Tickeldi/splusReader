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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Option other = (Option) obj;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
