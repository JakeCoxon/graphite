package com.jakemadethis.graphite.ui

import scala.swing._
import com.jakemadethis.graphite.graph._
import javax.swing.text.DocumentFilter
import javax.swing.text.AttributeSet
import javax.swing.text.AbstractDocument

case class EdgeDialogSuccess(sizing : Int, label : String, termination : Termination)

private object defaults {
  var sizing = "2"
  var label = "A"
  var terminal = true
}
class AddEdgeDialog(owner : Window, success : EdgeDialogSuccess => Unit) extends Dialog(owner) {
  
  object IntegralFilter extends DocumentFilter {
    override def insertString(fb: DocumentFilter.FilterBypass, offs: Int, str: String, a: AttributeSet){
       if (str.forall { (c) => c.isDigit } )
         super.insertString(fb, offs, str, a)
    }    
    override def replace(fb: DocumentFilter.FilterBypass, offs: Int, l: Int, str: String, a: AttributeSet){
       if (str.forall { (c) => c.isDigit } )
         super.replace(fb, offs, l, str, a)
    }
  }
  
  lazy val confirm = Button("Add edge") {
    
    defaults.sizing = inputs.sizing.text
    defaults.label = inputs.label.text
    defaults.terminal = inputs.terminal.selected
    
    val s = inputs.sizing.text.toInt
    val t = Termination.terminal(inputs.terminal.selected)
    val l = if (t.isNonTerminal) 
        inputs.label.text.toUpperCase()
      else
        inputs.label.text.toLowerCase()
    
    close
    success(new EdgeDialogSuccess(s, l, t))
    
    
  }
  
  object inputs {
    lazy val sizing = new TextField(defaults.sizing) { 
      peer.getDocument().asInstanceOf[AbstractDocument].setDocumentFilter(IntegralFilter) 
    }
    lazy val label = new TextField(defaults.label)
    lazy val terminal = new CheckBox() { selected = defaults.terminal }
  }
  
  lazy val main = new GridPanel(3, 2) {
    contents ++= 
      new Label("Size:", null, Alignment.Right) :: inputs.sizing :: 
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