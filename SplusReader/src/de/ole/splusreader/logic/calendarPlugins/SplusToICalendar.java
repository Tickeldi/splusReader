package de.ole.splusreader.logic.calendarPlugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Organizer;
import biweekly.property.Summary;
import de.ole.splusreader.base.SplusEventToCalendar;
import de.ole.splusreader.logic.SplusEvent;

public class SplusToICalendar implements SplusEventToCalendar{

	@Override
	public void writeToFile(List<SplusEvent> eventList, File file) 
			throws IOException  {
		OutputStream outputstream = new FileOutputStream(file);
		
		writeToStream(eventList, outputstream);
	}

	@Override
	public void writeToStream(List<SplusEvent> eventList, OutputStream outputstream) 
			throws IOException {
		ICalendar ical = new ICalendar();
		
		for(SplusEvent event:eventList) {
			VEvent vEvent = new VEvent();
			vEvent.setLocation(event.getLocation());
			vEvent.setOrganizer(new Organizer(event.getPresenter(), ""));
			
			String summaryString = event.getTitle();
			
			if(!event.getDescription().isEmpty())
				summaryString += " -- " + event.getDescription();
			
			Summary summary = vEvent.setSummary(summaryString);
			
			summary.setLanguage("de-de");
			
			vEvent.setDateStart(event.getStart());
			vEvent.setDateEnd(event.getEnd());
			
			ical.addEvent(vEvent);
		}
		
		Biweekly.write(ical).go(outputstream);
	}

}
