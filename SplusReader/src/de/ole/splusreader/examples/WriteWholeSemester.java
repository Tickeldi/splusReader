package de.ole.splusreader.examples;

import java.io.File;
import java.io.IOException;

import de.ole.splusreader.base.SplusEventToCalendar;
import de.ole.splusreader.logic.MissingSettingException;
import de.ole.splusreader.logic.SplusEventFactory;
import de.ole.splusreader.logic.calendarPlugins.SplusToICalendar;

public class WriteWholeSemester {

	public static void main(String[] args) throws IOException, MissingSettingException {
		SplusEventFactory factory = new SplusEventFactory();
		
		//Set faculty to Informatik
		factory.setFaculty("Informatik");
		
		//Set plan to Semesterpläne
		factory.setPlan("Semesterpläne");
		
		//Set IT-Management 5th semester
		factory.setStudyPath(25); 
		
		//Instantiate the iCalWriter
		SplusEventToCalendar iCalWriter = new SplusToICalendar();
		
		//Write all Events between weeks 39 and 52 including themselves
		// to the file "semester.ical"
		iCalWriter.writeToFile(
				factory.getEventsBetweenWeeks(39,52), 
				new File("semester.ical"));

	}

}
