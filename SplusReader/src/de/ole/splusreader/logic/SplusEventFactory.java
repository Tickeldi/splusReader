package de.ole.splusreader.logic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.ole.splusreader.base.SplusEventToCalendar;
import de.ole.splusreader.logic.calendarPlugins.SplusToICalendar;

/**
 * This Class extracts data from the ostfalia splus calendar
 * (http://splus.ostfalia.de/) into according SplusEvent objects.
 * 
 * To be able to do that it needs certain settings to be made which are analog
 * to the obligatory choices to be made on the page.
 * 
 * If there are not sufficient options selected, trying to get data will raise
 * a MissingSettingException which is pretty self explanatory.
 * 
 * @author Ole Goes
 */
public class SplusEventFactory {
	
	private Option faculty;
	private Option plan;
	private Option studyPath;
	private Option group;
	
	private static List<Option> getHyperlinkListFromURL(URL url) 
			throws IOException{
		List<Option> list = new ArrayList<>();
		Document doc = Jsoup.parse(url, 1000);

		for(Element liElement:doc.getElementsByTag("li")) {
			Element element = liElement.getElementsByAttribute("href").first();
			list.add(
					new Option(
							list.size(), 
							element.text(), 
							element.attr("href"))
					);
		}
		
		return list;
	}
	
	private static Option getOptionFromListWhereString(
			List<Option> list, String string
			) {
		for(Option option:list) {
			if(option.getTitle().equals(string)) {
				return option;
			}
		}
		return null;
	}
	
	private static int giveNumberOfWeeksForYear(String year) {
		return giveNumberOfWeeksForYear(Integer.parseInt(year));
	}
	
	private static int giveNumberOfWeeksForYear(int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		return calendar.getWeeksInWeekYear();
		
	}
	
	private static SplusEvent getEventFromStrings(
			String dayOfWeek,
			String title,
			String description,
			String presenter,
			String location,
			String time,
			String year,
			String yearOfFirstWeek,
			int lengthInMinutes,
			int week) {
				
		if(!year.equals(yearOfFirstWeek)) {
			int numberOfWeeksOfFirstYear = giveNumberOfWeeksForYear(yearOfFirstWeek);
			if(week > numberOfWeeksOfFirstYear)
				week = week - numberOfWeeksOfFirstYear;
		}
		
		Map<String, Integer> weekDayToNumber = new HashMap<>();
		weekDayToNumber.put("Mo", 1);
		weekDayToNumber.put("Di", 2);
		weekDayToNumber.put("Mi", 3);
		weekDayToNumber.put("Do", 4);
		weekDayToNumber.put("Fr", 5);
		weekDayToNumber.put("Sa", 6);
		
		DateFormat format = new SimpleDateFormat("ww u kk:mm yyyy", Locale.GERMAN);
		
		String weekNumber = String.valueOf(week);
		
		if(weekNumber.length() == 1)
			weekNumber = "0" + weekNumber;
		
		String dayOfWeekNumber = String.valueOf(weekDayToNumber.get(dayOfWeek));
		
		Date begin = null;
		try {
			begin = format.parse(weekNumber + " " + dayOfWeekNumber + " " + time + " " + year);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date end = null;
		
		if(begin != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(begin);
			cal.add(Calendar.MINUTE, lengthInMinutes);
			end = cal.getTime();
		}
		
		return new SplusEvent(begin, end, title, description, location, presenter);
	}
	
	/**
	 * @return              A list of Faculties to choose from
	 * @throws IOException  If there is no connection to splus.ostfalia.de for whatever reason
	 */
	public static List<Option> getFaculties() throws IOException {
		return getHyperlinkListFromURL(new URL("http://splus.ostfalia.de/"));
	}
	
	private static List<Option> getOptionsFromForm(Document doc, String formName) {
		List<Option> list = new ArrayList<>();
		Elements formOptions = null;
		
		for(Element tag:doc.getElementsByTag("form")) {
			if(tag.attr("name").equals(formName)) {
				formOptions = tag.getElementsByTag("option");
				break;
			}
		}
		
		if(formOptions == null)
			return list;
		
		for(Element formOption:formOptions) {
			list.add(new Option(
					list.size(), 
					formOption.text(), 
					formOption.attr("value")
					));
		}
		
		return list;
	}
	
	/**
	 * @return                           A list of plans to choose from
	 * @throws IOException               If there is no connection to splus.ostfalia.de for whatever reason
	 * @throws MissingSettingException   If a faculty has not been set
	 */
	public List<Option> getPlans() 
			throws IOException, MissingSettingException {
		if(faculty == null) {
			throw new MissingSettingException();
		}
		return getHyperlinkListFromURL(
				new URL("http://splus.ostfalia.de/" + faculty.getValue())
				);		
	}
	
	/**
	 * @return                          A list of study paths to choose from
	 * @throws MissingSettingException  If a plan has not been set
	 * @throws IOException              If there is no connection to
                                        splus.ostfalia.de for whatever reason
	 */
	public List<Option> getStudyPaths() 
			throws MissingSettingException, IOException {
		List<Option> list = new ArrayList<>();
		
		if(plan == null) {
			throw new MissingSettingException();
		}
		
		Document doc = Jsoup.parse(
				new URL("http://splus.ostfalia.de/" + plan.getValue())
				, 1000
				);
		
		if(plan.getTitle().equals("Studentensetpläne")) {
			return getOptionsFromForm(doc, "formfilter");
		}
		
		
		for(Element element:doc.getElementsByAttributeValueContaining("value", "SPLUS")) {
			list.add(new Option(list.size(), element.text(), element.attr("value")));
		}
		
		return list;
	}
	
	/**
	 * @return                          A list of groups to choose from
	 * @throws MissingSettingException  If a study path or plan haven't been set
	 * @throws IOException				If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public List<Option> getGroups() 
			throws MissingSettingException, IOException {
		
		if(plan == null || studyPath == null) {
			throw new MissingSettingException();
		}
		
		String url = "http://splus.ostfalia.de/"
				+ plan.getValue();

		Document doc = 
				Jsoup.connect(url)
				.data("filter", studyPath.getValue())
				.post();
		
		List<Option> list = getOptionsFromForm(doc, "form33");
					
		return list;
	}
	
	/**
	 * @return the chosen faculty
	 */
	public Option getFaculty() {
		return faculty;
	}

	/**
	 * @param faculty the faculty option to be set
	 */
	public void setFaculty(Option faculty) {
		this.faculty = faculty;
	}
	
	/**
	 * Sets the nth faculty.
	 * 
	 * @param number        The index number of the faculty (starting with 0)
	 * @throws IOException  If there is no connection to 
	 *                      splus.ostfalia.de for whatever reason
	 */
	public void setFaculty(int number) throws IOException {
		setFaculty(getFaculties().get(number));
	}
	
	/**
	 * Sets the faculty with the given name.
	 * 
	 * @param name			The name of the faculty
	 * @throws IOException  If there is no connection to 
	 *                      splus.ostfalia.de for whatever reason
	 */
	public void setFaculty(String name) throws IOException {
		setFaculty(getOptionFromListWhereString(getFaculties(), name));
	}
	
	
	/**
	 * @return the kind of plan to be fetched
	 */
	public Option getPlan() {
		return plan;
	}
	
	/**
	 * @param plan the kind of plan to be set
	 */
	public void setPlan(Option plan) {
		this.plan = plan;
	}
	
	/**
	 * Sets the nth plan.
	 * 
	 * @param number                    The index number of the faculty (starting with 0)
	 * @throws IOException              If there is no connection to splus.ostfalia.de for whatever reason
	 * @throws MissingSettingException  If faculty has not been set
	 */
	public void setPlan(int number) 
			throws IOException, MissingSettingException {
		setPlan(getPlans().get(number));
	}
	
	/**
	 * Sets the plan with the given name
	 * 
	 * @param name                      The name of the plan
	 * @throws IOException              If there is no connection to splus.ostfalia.de for whatever reason
	 * @throws MissingSettingException  If faculty has not been set
	 */
	public void setPlan (String name) 
			throws IOException, MissingSettingException {
		setPlan(getOptionFromListWhereString(getPlans(), name));
	}
	
	/**
	 * @return the chosen study path.
	 */
	public Option getStudyPath() {
		return studyPath;
	}
	
	
	/**
	 * @param studyPath the study path to be set
	 */
	public void setStudyPath(Option studyPath) {
		this.studyPath = studyPath;
	}
	
	/**
	 * Sets the nth study path.
	 * 
	 * @param number                    The index number of the study path 
	 *                                  (beginning with 0)
	 * @throws MissingSettingException  If plan has not been set
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public void setStudyPath(int number) 
			throws MissingSettingException, IOException {
		setStudyPath(getStudyPaths().get(number));
	}
	
	/**
	 * @param name                      The name of the study path
	 * @throws MissingSettingException  if plan has not been set
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public void setStudyPath (String name) 
			throws MissingSettingException, IOException {
		setStudyPath(getOptionFromListWhereString(getStudyPaths(), name));
	}
	
	/**
	 * @return The chosen group
	 */
	public Option getGroup() {
		return group;
	}
	
	/**
	 * @param The group to be set
	 */
	public void setGroup(Option group) {
		this.group = group;
	}
	
	/**
	 * @param name                      The name of the group
	 * @throws MissingSettingException  If plan or study path have not been set
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public void setGroup(String name) 
			throws MissingSettingException, IOException {
		setGroup(getOptionFromListWhereString(getGroups(), name));
	}
	
	/**
	 * Sets the nth group.
	 * 
	 * @param number                    The index number of the group
	 *                                  (starting with 0)
	 * @throws MissingSettingException  If plan or study path have not been set
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public void setGroup(int number) 
			throws MissingSettingException, IOException {
		setGroup(getGroups().get(number));
	}
	
	private Document getDocumentFromWeek(int week) 
			throws MissingSettingException, IOException {
		if(plan == null || studyPath == null) {
			throw new MissingSettingException();
		}
		
		String url = "http://splus.ostfalia.de/"
				+ plan.getValue();
		
		if(plan.getTitle().equals("Studentensetpläne")) {			
			if(group == null) {
				Document doc = 
						Jsoup.connect(url)
						.data("filter", studyPath.getValue())
						.post();

				List<Option> optionsFromForm = getOptionsFromForm(doc, "form33");
				
				if(optionsFromForm.size() == 2) {
					group = optionsFromForm.get(1);
				}
				else
					throw new MissingSettingException();
				
			}

			return Jsoup.connect(url)
					
					.data("identifier%5B%5D", 
							group.getValue().replace("#", "%23"))
					.data("filter", studyPath.getValue())
					.data("weeks",String.valueOf(week))
					.post();
		}
		
		url = url 
				+ "&identifier="
				+ studyPath.getValue().replace("#", "%23");
		
		return Jsoup.connect(url)
				.data("weeks",String.valueOf(week)).post();
	}
	
	/**
	 * Returns a list of events from a certain week in the splus calendar.
	 * 
	 * @param week                      The number of the week in the year
	 * @return                          The list of events in that week
	 * @throws MissingSettingException  If the plan or study path have not been
	 *                                  set or, if there is more than one,
	 *                                  the group hasn't been set. 
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public List<SplusEvent> getEventsFromWeek(int week) 
			throws MissingSettingException, IOException {

		List<SplusEvent> listOfEvents = new ArrayList<>();
		Document doc = getDocumentFromWeek(week);
		
		//get Year of selection and year of first week
		Elements weeks = doc.getElementsByAttributeValue("name", "weeks")
				.first().getElementsByTag("option");
		
		String yearOfFirstWeek = weeks.first().text();
			   yearOfFirstWeek = yearOfFirstWeek.substring(yearOfFirstWeek.length() -10, yearOfFirstWeek.length() -6);

			   String year = null;
				
		for(Element option: weeks) {
			if(option.hasAttr("selected")) {
				year = option.text();
				year = year.substring(year.length() - 11, year.length() - 7);
			}
		}
		
		
		Element table = doc
				.getElementsByAttributeValue("class", "grid-border-args")
				.first();
		
		String time = null;
		List<String> weekDay = new ArrayList<>();
		weekDay.add("");
		
		//get Event Content
		for(Element row: table.getElementsByTag("tr")) {
			for(Element column:row.getElementsByTag("td")) {
				if(column.hasClass("col-label-one")) {
					for(int i = 0; i < Integer.parseInt(column.attr("colspan")); i++) {
						weekDay.add(column.text());
					}
				}
				else if(column.hasClass("row-label-one")) {
					time = column.text();
				}
				else if(column.hasClass("object-cell-border")) {
					String dayOfWeek = weekDay.get(column.elementSiblingIndex());
					String title = null;
					String description = null;
					String presenter = null;
					String location = null;
					int lengthInMinutes = Integer.parseInt(column.attr("rowspan")) * 15;

					for(Element tbody:column.getElementsByTag("tbody")) {
						for(Element data:tbody.getElementsByTag("td")) {
							if(data.attr("align").equals("center")) {
								if(title == null) {
									title = data.text();
								}
								else {
									description = data.text();
								}
							}
							else if(data.attr("align").equals("left")) {
								location = data.text();
							}
							else {
								presenter = data.text();
							}
						}
					}
					
					// create and add event
					listOfEvents.add(
							getEventFromStrings(
									dayOfWeek,
									title, 
									description,
									presenter,
									location,
									time,
									year,
									yearOfFirstWeek, 
									lengthInMinutes, 
									week));
					
				}
			}
		}
		
		return listOfEvents;
	}
	
	/**
	 * Get all events between two dates
	 * 
	 * @param from                      The start date
	 * @param to                        The end date
	 * @return                          A list of all events between start and 
	 *                                  end
	 * @throws MissingSettingException  If the plan or study path haven't been
	 *                                  set or, if there is more than one,
	 *                                  the group hasn't been set.
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public List<SplusEvent> getEventsBetweenDates(Date from, Date to) 
			throws MissingSettingException, IOException {
		
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(from);
		int firstWeek = cal.get(Calendar.WEEK_OF_YEAR);
		System.out.println(firstWeek);
		
		cal.setTime(to);
		int lastWeek = cal.get(Calendar.WEEK_OF_YEAR);
		System.out.println(lastWeek);
		
		return getEventsBetweenWeeks(firstWeek, lastWeek);
	}
	
	/**
	 * Get all events between two weeks (inclusively)
	 * 
	 * @param firstWeek                 The first week for all events
	 * @param lastWeek                  The last week for all events
	 * @return                          The list of all events between the 
	 *                                  first and last week
	 * @throws MissingSettingException  If the plan or study path haven't been
	 *                                  set or, if there is more than one,
	 *                                  the group hasn't been set.
	 * @throws IOException              If there is no connection to 
	 *                                  splus.ostfalia.de for whatever reason
	 */
	public List <SplusEvent> getEventsBetweenWeeks(int firstWeek, int lastWeek) 
			throws MissingSettingException, IOException {
		List<SplusEvent> events = new ArrayList<>();
		
		for(int week = firstWeek; week <= lastWeek; week++) {
			events.addAll(getEventsFromWeek(week));
		}
		
		return events;
	}

}
