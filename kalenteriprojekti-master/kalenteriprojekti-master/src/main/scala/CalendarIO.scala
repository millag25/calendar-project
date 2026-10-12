import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.data.{CalendarBuilder, CalendarOutputter}
import net.fortuna.ical4j.model.Calendar
import java.io.{FileInputStream, FileOutputStream}
import java.time.ZoneId

object CalendarIO:

  // tallentaa tapahtumat annettuun tiedostoon
  def savetoFile(events: collection.mutable.Buffer[Event], filename: String) =
    try
      val iCalendar = new Calendar()
      iCalendar.add(new ProdId("-//Milla//FI"))
      iCalendar.add(new Version("2.0", "2.0"))

      for e <- events do
        val startInstant = e.startingDateTime.atZone(ZoneId.systemDefault()).toLocalDateTime
        val endInstant = e.endingDateTime.atZone(ZoneId.systemDefault()).toLocalDateTime

        val vEvent = new VEvent(startInstant, endInstant, e.name)

        vEvent.add(new Categories(e.category.name))
        vEvent.add(new Location(e.location))
        if e.link.nonEmpty then vEvent.add(new Url(java.net.URI.create(e.link)))
        for p <- e.participants do
          vEvent.add(new Attendee(p))
        vEvent.add(new Description(e.description))
        vEvent.add(new XProperty("X-REMINDER-MINS", e.notificationMinutes.getOrElse(0).toString))

        iCalendar.add(vEvent)
 
      val out = new FileOutputStream(filename)
      val outputter = new CalendarOutputter()
      outputter.output(iCalendar, out)
      out.close()
    catch
      case e: Exception => ()

  def importFromFile(filename: String): Calendar =
    val input = new FileInputStream(filename)
    val calendarb = new CalendarBuilder()
    val calendar = calendarb.build(input)
    calendar






