import java.time.{DayOfWeek, LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ArrayBuffer
import net.fortuna.ical4j.model.{Calendar, Component}
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.data.{CalendarBuilder, CalendarOutputter}
import net.fortuna.ical4j.model.component.VEvent
import scalafx.scene.paint.Color
import scala.jdk.CollectionConverters.*

class Calendar:

  val currentFileName = "TestiKalenteri.ics"
  var events: ArrayBuffer[Event] = ArrayBuffer()
  var categories: Map[String, Category] = Map()
  val eventsFromSave = CalendarIO.importFromFile(currentFileName)
  //print(eventsFromSave.toString)

  val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
  val secondFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

// lataa tapahtumia tiedostosta
  loadEventsFromFile()
  def loadEventsFromFile(): Unit =
    //events.clear()
    val iterator = eventsFromSave.getComponents(Component.VEVENT).iterator

    while(iterator.hasNext) do
      val event = iterator.next().asInstanceOf[VEvent]

      val name = event.getSummary.map(_.getValue).orElse("Untitled")

      val categoryName = if event.getCategories.isPresent then event.getCategories.get().getValue else "Other"
      if !categories.contains(categoryName) then
        categories += categoryName -> new Category(categoryName, Color.Black)
      val category = Category.all.find( _.name == categoryName).getOrElse(Category.Other)

      val location = event.getLocation.orElse(new Location("")).getValue
      val link = event.getUrl.map(_.getValue).orElse("")
      val participants = event.getAttendees.asScala.map(_.getValue).toSeq
      val description = event.getDescription.orElse(new Description("")).getValue

      val notificationMinutes = event.getProperty[net.fortuna.ical4j.model.Property]("X-NOTIFICATION-MINS")
        .map(_.getValue.toIntOption)
        .orElse(None)

      val startingString = event.getDateTimeStart.map(_.getValue).orElse("")
      val endingString = event.getDateTimeEnd.map(_.getValue).orElse("")

      if startingString.nonEmpty && endingString.nonEmpty then
        try
          val startingDateTime = try
            LocalDateTime.parse(startingString, formatter)
            catch
            case _: Exception => LocalDateTime.parse(startingString, secondFormatter)

          val endingDateTime = try
            LocalDateTime.parse(endingString, formatter)
            catch
            case e: Exception =>
              //e.printStackTrace()
              LocalDateTime.parse(endingString, secondFormatter)


          val newEvent = new Event(name, startingDateTime, endingDateTime,category,location,link,
            participants, description, notificationMinutes)
          println(newEvent)
          events += newEvent
        catch
          case e: Exception => e.printStackTrace()
      else

  println(events)

  //palauttaa true, jos palauttaminen onnistui ja false
  def addEvent(newEvent: Event): Boolean =
    if !events.contains(newEvent) then
      events = events :+ newEvent
      CalendarIO.savetoFile(events, currentFileName)
      true
    else
      false
  
  // etsii tapahtumia listasta ja palauttaa filtteröidyn Seq[Events], jos tapahtumat ovat annetulla päivällä ja tunneille
  def searchEvents(day: LocalDate, hour: Int): Seq[Event] =
    val targetTime = day.atTime(hour, 0)
    events.filter( event =>
      (targetTime.isEqual(event.startingDateTime) || targetTime.isAfter(event.startingDateTime)) && targetTime.isBefore(event.endingDateTime)).toSeq

  def checkNotification(): Seq[Event] =
    val now = LocalDateTime.now

    events.filter( e =>
      e.notificationMinutes match {
        case Some(min) =>
          val reminderStarts = e.startingDateTime.minusMinutes(min)
          now.isAfter(reminderStarts) && now.isBefore(e.startingDateTime)
        case None => false
      }).toSeq

  def getHolidays(year: Int): Map[LocalDate, String] =
    var holidays = Map[LocalDate, String]()

    holidays += LocalDate.of(year, 1,1) -> "Uudenvuodenpäivä"
    holidays += LocalDate.of(year, 1,6) -> "Loppiainen"
    holidays += LocalDate.of(year, 2, 14) -> "Ystävänpäivä"
    holidays += LocalDate.of(year, 5,1) -> "Vappu"
    holidays += LocalDate.of(year, 12,6) -> "itsenäisyyspäivä"
    holidays += LocalDate.of(year, 12,24) -> "Jouluaatto"
    holidays += LocalDate.of(year, 12,25) -> "Joulupäivä"
    holidays += LocalDate.of(year, 12,26) -> "Tapaninpäivä"

    val june19 = LocalDate.of(year, 6, 19)
    val midsummer = june19`with`(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    holidays += midsummer -> "Juhannuspäivä"
    holidays += midsummer.minusDays(1) -> "Juhannusaatto"

    def getEasterSunday(y: Int): LocalDate =
      val a = year % 19
      val b = year / 100
      val c = year % 100
      val d = b / 4
      val e = b % 4
      val f = (b + 8) / 25
      val g = (b - f + 1) / 3
      val h = (19 * a + b - d - g + 15) % 30
      val i = c / 4
      val k = c % 4
      val l = (32 + 2 * e + 2 * i - h - k) % 7
      val m = (a + 11 * h + 22 * l) / 451
      val month = (h + l - 7 * m + 114) / 31
      val day = ((h + l - 7 * m + 114) % 31) + 1

      LocalDate.of(year, month, day)

    holidays += getEasterSunday(year) -> "Pääsiäispäivä"
    holidays += getEasterSunday(year).plusDays(1) -> "2. Pääsiäispäivä"
    holidays += getEasterSunday(year).minusDays(2) -> "Pitkäperjantai"
    holidays += getEasterSunday(year).plusDays(39) -> "Helatorstai"
    holidays += getEasterSunday(year).plusDays(49) -> "Helluntai"

    holidays