package com.jakemadethis.graphite.ui

import scala.swing._
import com.jakemadethis.graphite.graph._

case class EdgeDialogSuccess(sizing : Int, label : String, termination : Termination)

private object defaults {
  var sizing = "2"
  var label = "A"
  var terminal = true
}
class AddEdgeDialog(owner : Window, success : EdgeDialogSuccess => Unit) extends Dialog(owner) {
  
  lazy val confirm = Button("Add edge") {
    try {
      defaults.sizing = inputs.sizing.text
      defaults.label = inputs.label.text
      defaults.terminal = inputs.terminal.selected
      
      val s = inputs.sizing.text.toInt
      val l = inputs.label.text
      val t = Termination.terminal(inputs.terminal.selected)
      
      close
      success(new EdgeDialogSuccess(s, l, t))
    } catch { case _ => }
    
  }
  
  object inputs {
    lazy val sizing = new TextField(defaults.sizing)
    lazy val label = new TextField(defaults.label)
    lazy val terminal = new CheckBox() { selected = defaults.terminal }
  }
  
  lazy val main = new GridPanel(3, 2) {
    contents ++= new Label("Type:", null, Alignment.Right) :: inputs.sizing :: 
      new Label("Label:", null, Alignment.Right) :: inputs.label ::
      new Label("Terminal:", null, Alignment.Right) :: inputs.terminal ::
      Nil
  }
  
  title = "Add new edge"
  contents = 
    new BoxPanel(Orientation.Vertical) {
      contents ++= main :: 
      new FlowPanel(Button("Cancel") { close() }, confirm) :: Nil
    }
  defaultButton = confirm
  resizable = false
  
  {
    import javax.swing.WindowConstants
    peer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
  }
}