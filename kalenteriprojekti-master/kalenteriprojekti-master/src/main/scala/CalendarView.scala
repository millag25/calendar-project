import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.layout.Region
import scalafx.scene.control.Label
import scalafx.scene.layout.StackPane
import scalafx.geometry.Pos
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import scalafx.scene.control.ScrollPane
import scalafx.scene.text.Font
import scalafx.geometry.Insets
import java.time.*
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import scalafx.scene.control.ScrollPane.ScrollBarPolicy.Never
import scalafx.scene.control.Button
import scalafx.scene.control.*
import scalafx.Includes.*
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.application.Platform
import scalafx.scene.control.Dialog
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.collections.ObservableBuffer

class CalendarView extends BorderPane:

  val calendardata = Calendar()
  val selectedCategories = ObservableBuffer[String](Category.all.map(_.name).toSeq*)
  val today = java.time.LocalDate.now()
  var firstDayOfTheWeek = today.`with`(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
  val holidays = calendardata.getHolidays(today.getYear)
  val dateDesigner = java.time.format.DateTimeFormatter.ofPattern("EE d.M")

  val mainTitle = new Label:
    text = "Milla's Calendar"
    font = Font("Centaur", 40)
    textFill = Color.Black

  val weekview = new HBox()
  var isWeekView = true

  val minicalendar = new HBox:
    for weekdays <- 1 to 7 do
      val daysInAMonth = new VBox:
        for day <- 1 to 4 do
          val box = new Rectangle:
            width = 15
            height = 20
            fill = Color.LightPink
            stroke = Color.White
          children += box
      children += daysInAMonth

  val timeColumn = new VBox:
    val spacer = new Region:
      prefHeight = 30
    children += spacer
    for hour <- 0 until 24 do
      val clockTime = new Label:
        text = f"$hour%02d:00"
        font = Font("Lucida Sans Typewriter", 15)
        prefHeight = 61
        prefWidth = 70
        alignment = Pos.CenterRight
        padding = Insets(0, 10, 0, 0)
      children += clockTime
    prefWidth = 70

  def updateDisplay() =
    weekview.children.clear()
    val daysToShow = if isWeekView then 0 to 6 else 0 to 0

    for weekday <- daysToShow do
      val thisDay = firstDayOfTheWeek.plusDays(weekday)
      val daycolumn = new VBox:
        maxWidth = if isWeekView then 125 else 850
        minWidth = if isWeekView then 125 else 850
        alignment = Pos.TopCenter

        val dayName = new Label:
          text = thisDay.format(dateDesigner)
          font = Font("Lucida Sans Typewriter", 20)
          prefWidth = if isWeekView then 125 else 850
          prefHeight = 30
          alignment = Pos.Center
        children += dayName

        for hour <- 0 until 24 do
          val thisEvents = calendardata.searchEvents(thisDay, hour).filter( e => selectedCategories.contains(e.category.name))
          val box = new StackPane:
            prefHeight = 61
            minHeight = 61
            maxHeight = 61

            val back = new Rectangle:
              width = if isWeekView then 125 else 850
              height = 60
              fill = Color.LightPink
              stroke = Color.White
              strokeType = scalafx.scene.shape.StrokeType.Inside
            children += back

            if hour == 0 && holidays.contains(thisDay) then
              children += new Rectangle:
                width = if isWeekView then 125 else 850
                height = 25
                fill = Color.Red
                alignment = Pos.Center

              children += new Label(holidays(thisDay)):
                style = "-fx-text-fill: black; -fx-font-size: 15px; -fx-font-weight: Broadway;"
                maxWidth = if isWeekView then 125 else 850
                alignment = Pos.TopCenter
                padding = Insets(2,0,0,0)

            if thisEvents.nonEmpty then
              val event = thisEvents.head
              val eventHighLight = new Rectangle:
                width = if isWeekView then 122 else 850
                height = 60
                fill = event.category.color
                mouseTransparent = true
              children += eventHighLight

              val isStartDay = thisDay.isEqual(event.startingDateTime.toLocalDate)
              val isStartHour = hour == event.startingDateTime.getHour

              if (isStartHour && isStartDay)  || (!isStartDay && hour == 0) then
                val nameLabel: VBox = new VBox:
                  alignment = Pos.Center
                  mouseTransparent = true
                  children = thisEvents.map(ev => new Label(ev.name) {
                    style = "-fx-font-weight: Arial; -fx-text-fill: black; -fx-font-size: 15 px;"
                })
                children += nameLabel
                
          if thisEvents.nonEmpty then
            box.onMouseClicked = (e) => showAddEventDialog(Some(thisEvents.head))
          children += box
      weekview.children += daycolumn

  def showError(h: String, c: String): Unit =
    val alert = new Alert(Alert.AlertType.Error):
      title = "Error in inpur"
      headerText = h
      contentText = c
    alert.showAndWait()

  def showAddEventDialog(existingEvent: Option[Event] = None): Unit =
    val dialog = new Dialog[Event]()
    dialog.title = if existingEvent.isDefined then "Edit the event" else "Add a new event"
    dialog.headerText = "Enter the information"

    val saveBtnType = new ButtonType("Save", ButtonData.OKDone)
    val deleteBtnType = new ButtonType("Delete", ButtonData.Other)
    dialog.dialogPane().buttonTypes =
      if existingEvent.isDefined then
        Seq(saveBtnType, deleteBtnType, ButtonType.Cancel)
      else
        Seq(saveBtnType, ButtonType.Cancel)
    val finnishFormat = java.time.format.DateTimeFormatter.ofPattern("d.M.yyyy")

    val nameField = new TextField:
      text = existingEvent.map(_.name).getOrElse("")
      promptText = "Name:"

    val startDateInput = new TextField:
      text = existingEvent.map(_.startingDateTime.toLocalDate.format(finnishFormat)).getOrElse("")
      promptText = "DD.MM.YYYY"

    val startTimeInput = new TextField:
      text = existingEvent.map(_.startingDateTime.toLocalTime.toString).getOrElse("")
      promptText = "10:00"

    val endDateInput = new TextField:
      text = existingEvent.map(_.endingDateTime.toLocalDate.format(finnishFormat)).getOrElse("")
      promptText = "DD.MM.YYYY"
      
    val endTimeInput = new TextField:
      text = existingEvent.map(_.endingDateTime.toLocalTime.toString).getOrElse("")
      promptText = "11:00"

    val locationField = new TextField:
      text = existingEvent.map(_.location).getOrElse("")
      promptText = "Location: (optional)"

    val linkField = new TextField:
      text = existingEvent.map(_.link).getOrElse("")
      promptText = "Link: (optional)"

    val participantsField = new TextField:
      text = existingEvent.map(_.participants.mkString).getOrElse("")
      promptText = "Participants: separate with comma (optional)"

    val descriptionField = new TextArea:
      text = existingEvent.map(_.description).getOrElse("")
      promptText = "Description: (optional)"
      prefRowCount = 3

    val categoryMenu = new ChoiceBox[String]:
      items = ObservableBuffer.from(Category.all.map(_.name))
      value = existingEvent.map(_.category.name).getOrElse(Category.Other.name)

    val useNotification = new CheckBox("Set a notification"):
      selected = existingEvent.exists(_.notificationMinutes.isDefined)
    val noticicationField = new TextField:
      text = existingEvent.flatMap(_.notificationMinutes).map(_.toString).getOrElse("")
      promptText = "Notification (optional): Set the time in minutes (e.g. 60 = 1h, 1440 = 1 d)"
      disable <== useNotification.selected.not()


    dialog.dialogPane().content = new VBox:
      padding = Insets(10)
      children ++= List(
        new Label("Name:"), nameField,
        new Label("Category:"), categoryMenu,
        new Label("Starts:"), new HBox(10, startDateInput, startTimeInput),
        new Label("Ends:"), new HBox(10, endDateInput, endTimeInput),
        new Label("Location:"), locationField,
        new Label("Link:"), linkField,
        new Label("Participants:"), participantsField,
        new Label("Description:"), descriptionField,
        useNotification,
        new Label("Notification:"), noticicationField)

    dialog.resultConverter = (buttonType) =>
      if (buttonType == saveBtnType)
        val selectCat = Category.all.find(_.name == categoryMenu.value.value).getOrElse(Category.Other)
        try
          val startTime = LocalDateTime.of(LocalDate.parse(startDateInput.text.value.trim, finnishFormat), LocalTime.parse(startTimeInput.text.value.trim))
          val endTime = LocalDateTime.of(LocalDate.parse(endDateInput.text.value.trim, finnishFormat), LocalTime.parse(endTimeInput.text.value.trim))

          val reminder = if useNotification.selected.value then
            Some(noticicationField.text.value.trim.toIntOption.getOrElse(15))
          else
            None

          if endTime.isBefore(startTime) then
            showError("Time error", "The event cannot end before it starts!")
            null
          else
          new Event(
            name = if nameField.text.value.isEmpty then "Untitled event" else nameField.text.value,
            startingDateTime = startTime,
            endingDateTime = endTime,
            location = locationField.text.value,
            category = selectCat,
            link = linkField.text.value,
            participants = if (participantsField.text.value.isEmpty) then Nil else participantsField.text.value.split(",").map(_.trim).toSeq,
            description = descriptionField.text.value,
            notificationMinutes = reminder)
        catch
          case e: java.time.format.DateTimeParseException =>
            showError("Format Error", "Please check the date (DD.M.YYYY) and the time (HH:MM) formats!")
            null
          case e: Exception =>
            showError("Invalid input", s"Please check your enteries: ${e.getMessage} !")
            null
      else if (buttonType == deleteBtnType)
        existingEvent match
          case Some(event) =>
            calendardata.events -= event
            CalendarIO.savetoFile(calendardata.events.toBuffer, calendardata.currentFileName)
          case _ => ()
        updateDisplay()
        null
      else
        null

    val result = dialog.showAndWait()
    result match
      case Some(newEvent: Event) =>
        existingEvent.foreach(old => calendardata.events -= old)
        if (newEvent != null) then
          calendardata.events += newEvent
          CalendarIO.savetoFile(calendardata.events.toBuffer, calendardata.currentFileName)
        else
          println("Input error")
        updateDisplay()
      case None =>
         if dialog.resultProperty().value == deleteBtnType then
            existingEvent.foreach(old => calendardata.events -= old)
            CalendarIO.savetoFile(calendardata.events.toBuffer, calendardata.currentFileName)
            updateDisplay()
            println("The event is deleted")
      case _ =>
        println("User cancelled the feed")

  def showAddCategoryDialog(): Unit =
    val dialog = new Dialog[Category]():
      title = "Create a new category"
      headerText = "Specify name and color"

    val saveBtnType = new ButtonType("Save", ButtonData.OKDone)
    dialog.dialogPane().buttonTypes = Seq(saveBtnType, ButtonType.Cancel)

    val nameField = new TextField:
      promptText = "Category name:"

    val colorPicker = new ColorPicker(Color.White)

    dialog.dialogPane().content = new VBox:
      padding = Insets(10)
      children ++= List(new Label("Name:"), nameField, new Label("Color:"), colorPicker)

    dialog.resultConverter = (buttonType) =>
      if (buttonType == saveBtnType && nameField.text.value.nonEmpty) then
        Category(nameField.text.value, colorPicker.value.value)
      else null

    val result = dialog.showAndWait()
    result match
      case Some(cat: Category) =>
        if (cat != null)
          Category.all += cat
          CalendarIO.savetoFile(calendardata.events.toBuffer, calendardata.currentFileName)
          updateDisplay()
          refreshCategoryFilters()
        else
          println("Input error")
      case _ =>
        println("User cancelled the feed")

  val notificationList = new ListView[String]:
      prefHeight = 170
      items = ObservableBuffer[String]()
      style = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 11px;"

  def updateNoticications(): Unit =
    val activeNotifications = calendardata.checkNotification()
    Platform.runLater {
      val newTexts = if activeNotifications.nonEmpty then
        activeNotifications.map(e => s"${e.name} at ${e.startingDateTime.format(dateDesigner)} ${e.startingDateTime.toLocalTime}")
       else
          Seq("No notifications")
      notificationList.items = ObservableBuffer(newTexts*) }

  val notificationTimer = new Timeline:
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(KeyFrame(scalafx.util.Duration(1000),
      onFinished = (e: javafx.event.ActionEvent) => updateNoticications()))

  val filterBox = new VBox:
      spacing = 5

  def refreshCategoryFilters(): Unit =
    filterBox.children = Category.all.map(cat =>
      val cb: CheckBox = new CheckBox(cat.name):
        selected = selectedCategories.contains(cat.name)
        style = "-fx-text-fill: black; -fx-font-weight: bold;"
        onAction = (e) =>
          if selected.value then
            if !selectedCategories.contains(cat.name) then selectedCategories += cat.name
          else
            selectedCategories -= cat.name
          updateDisplay()
      val editBtn = new Button("Edit"):
        style = "-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5px;"
        onAction = (e) => showEditCategoryDialog(cat)

      new HBox(5, cb, editBtn):
        alignment = Pos.CenterLeft

    )
  def showEditCategoryDialog(existingCat: Category): Unit =
    val dialog = new Dialog[Category]():
      title = "Edit Category"
      headerText = s"Editing: ${existingCat.name}"
    val saveBtnType = new ButtonType("Save", ButtonData.OKDone)
    val deleteBtnType = new ButtonType("Delete", ButtonData.Other)
    dialog.dialogPane().buttonTypes = Seq(saveBtnType, deleteBtnType, ButtonType.Cancel)

    val nameField = new TextField:
      text = existingCat.name
    val colorPicker = new ColorPicker(existingCat.color)

    dialog.dialogPane().content = new VBox(10, new Label("Name:"), nameField, new Label("Color:"), colorPicker)

    dialog.resultConverter =
      case saveBtnType =>
        existingCat.name = nameField.text.value
        existingCat.color = colorPicker.value.value
        existingCat
      case deleteBtnType =>
        Category.all -= existingCat
        null
      case _ => null

    val result = dialog.showAndWait()
    if result.isDefined || dialog.resultProperty().value == deleteBtnType then
      CalendarIO.savetoFile(calendardata.events.toBuffer, calendardata.currentFileName)
      refreshCategoryFilters()
      updateDisplay()

  top = new HBox:
    alignment = Pos.Center
    children += mainTitle

  val calenderSystem = new HBox:
    children ++= List(timeColumn, weekview)

  center = new ScrollPane:
    content = calenderSystem
    hbarPolicy = Never
    vbarPolicy = Never

  right = new VBox:
    spacing = 20
    padding = Insets(20)
    alignment = Pos.TopCenter
    prefWidth = 150

    val navigation = new HBox:
      spacing = 10
      alignment = Pos.TopCenter
      val prevWeekBtn = new Button("<")
      prevWeekBtn.style = "-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-font-size: 12px;"

      prevWeekBtn.onAction = (e) =>
        firstDayOfTheWeek = if isWeekView then firstDayOfTheWeek.minusWeeks(1) else firstDayOfTheWeek.minusDays(1)
        updateDisplay()

      val nextWeekBtn = new Button(">")
      nextWeekBtn.style = "-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-font-size: 12px;"
      children ++= List(prevWeekBtn, nextWeekBtn)

      nextWeekBtn.onAction = (e) =>
        firstDayOfTheWeek = if isWeekView then firstDayOfTheWeek.plusWeeks(1) else firstDayOfTheWeek.plusDays(1)
        updateDisplay()

    val addEventBtn = new Button("+"):
      style = "-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-font-size: 12px;"
      alignment = Pos.Center
      onAction = (event) =>
        showAddEventDialog()

    val addCatBtn = new Button("New Category"):
      style = "-fx-background-color: pink; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-font-size: 12px;"
      onAction = (e) =>
        showAddCategoryDialog()

    val viewToggle = new ToggleGroup()

    val weekBtn = new RadioButton("Week"):
      toggleGroup = viewToggle
      alignment = Pos.CenterLeft
      selected = true
      style = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;"
      onAction = (e) =>
        isWeekView = true
        updateDisplay()

    val dayBtn = new RadioButton("Day"):
      toggleGroup = viewToggle
      alignment = Pos.CenterLeft
      selected = false 
      style = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;"
      onAction = (e) =>
        isWeekView = false
        updateDisplay()

    val Not = new Label("Notifications"):
      style = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;"

    val Mot = new Label("Month View"):
      style = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;"

    children ++= List(navigation, addEventBtn, addCatBtn, filterBox, weekBtn, dayBtn,
      Not, notificationList, Mot , minicalendar)

  bottom = new HBox

  updateDisplay()
  updateNoticications()
  notificationTimer.play()
  refreshCategoryFilters()
