import scalafx.scene.paint.Color
import scala.collection.mutable.ArrayBuffer

case class Category(var name: String, var color: Color)

object Category:
  var Work = Category("Work", Color.LightGreen)
  var School = Category("School", Color.Coral)
  var Other = Category("Other", Color.LightBlue) //toimii "irrallisena" kategoriana

  var all = ArrayBuffer(Work, School, Other)
