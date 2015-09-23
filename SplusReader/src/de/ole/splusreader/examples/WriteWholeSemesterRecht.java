package de.ole.splusreader.examples;

import java.io.File;
import java.io.IOException;

import de.ole.splusreader.base.SplusEventToCalendar;
import de.ole.splusreader.logic.MissingSettingException;
import de.ole.splusreader.logic.SplusEventFactory;
import de.ole.splusreader.logic.calendarPlugins.SplusToICalendar;

public class WriteWholeSemesterRecht {

	public static void main(String[] args) throws IOException, MissingSettingException {
		SplusEventFactory factory = new SplusEventFactory();
		
		//Set faculty to Informatik
		factory.setFaculty("Recht");
		
		//Set plan to Semesterpläne
		factory.setPlan("Studentensetpläne");
		
		//Set Wirtschaftsrecht 2. Semester
		factory.setStudyPath(14); 
		
		//Normally we could set the group here like
		//factory.setGroup(1);
		//But if the group only has one choice, it is choosen automatically
		
		//Instantiate the iCalWriter
		SplusEventToCalendar iCalWriter = new SplusToICalendar();
		
		//Write all Events between weeks 39 and 52 inclusively themselves
		// to the file "semester.ical"
		iCalWriter.writeToFile(
				factory.getEventsBetweenWeeks(39,52), 
				new File("semester.ical"));

	}

}
