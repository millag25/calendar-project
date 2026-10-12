import java.time.LocalDateTime

class Event(val name: String,
            val startingDateTime: LocalDateTime,
            val endingDateTime: LocalDateTime,
            val category: Category = Category.Other,
            val location: String = "",
            val link: String = "",
            val participants: Seq[String] = Seq(),
            val description: String = "",
            val notificationMinutes: Option[Int] = None)
{
  
  //lopetus ajan on oltava aloitusajan jälkeen
  require(endingDateTime.isAfter(startingDateTime))
  
  //Ilmoittaa tunnin, jolla eventti alkaa
  def startingHour = startingDateTime.getHour
}
