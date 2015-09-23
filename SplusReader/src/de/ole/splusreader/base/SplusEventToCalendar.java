package de.ole.splusreader.base;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.ole.splusreader.logic.SplusEvent;

public interface SplusEventToCalendar {
	public void writeToFile(List<SplusEvent> eventList, File file)
			throws IOException;
	public void writeToStream(List<SplusEvent> eventList, OutputStream outputstream) 
			throws IOException;
}
