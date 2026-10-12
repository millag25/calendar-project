import scalafx.application.JFXApp3
import scalafx.scene.Scene

object Main extends JFXApp3:

  def start() =

    stage = new JFXApp3.PrimaryStage:
      title = "Calendar - project"
      width = 1150
      height = 700
      scene = new Scene:
        root = new CalendarView()

  end start
end Main

