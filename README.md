# splusReader
This library extracts Events from the Ostfalia splus calendar (http://splus.ostfalia.de).

To avoid the MissingSettingException keep in mind the following dependencies.

Normally:
setFaculty() --> setPlan() --> setStudyPath() --> data

If setPlan("Studentensetpläne") and there is more than one group to select:
setFaculty() --> setPlan() --> setStudyPath() --> setGroup() --> data



##Example:
```

SplusEventFactory factory = new SplusEventFactory();

//Set faculty to Informatik
factory.setFaculty("Informatik");
		
//Set plan to Semesterpläne
factory.setPlan("Semesterpläne");
		
//Set IT-Management 5th semester (26th option in the list)
factory.setStudyPath(25); 
		
//Instantiate the iCalWriter
SplusEventToCalendar iCalWriter = new SplusToICalendar();
		
//Write all Events between weeks 39 and 52 including themselves
// to the file "semester.ical"
iCalWriter.writeToFile(
	factory.getEventsBetweenWeeks(39,52), 
	new File("semester.ical")
	);
```
