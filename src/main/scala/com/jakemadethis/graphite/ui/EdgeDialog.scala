package com.jakemadethis.graphite.ui

import scala.swing._
import com.jakemadethis.graphite.graph._
import javax.swing.text.DocumentFilter
import javax.swing.text.AttributeSet
import javax.swing.text.AbstractDocument

case class EdgeDialogObject(sizing : Int, label : String, termination : Termination)

private object EdgeDialog {
  var default : EdgeDialogObject = new EdgeDialogObject(2, "A", NonTerminal)
}
class EdgeDialog(owner : Window, values : EdgeDialogObject=null)(success : EdgeDialogObject => Unit) extends Dialog(owner) {
  val isNewEdge = values == null
  
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
  
  lazy val confirm = new NoFocusButton(Action("Add edge") {
    
    val s = inputs.sizing.text.toInt
    val t = Termination.terminal(inputs.terminal.selected)
    val l = if (t.isNonTerminal) 
        inputs.label.text.toUpperCase()
      else
        inputs.label.text.toLowerCase()
        
    val obj = new EdgeDialogObject(s, l, t)
    if (isNewEdge) {
      EdgeDialog.default = obj
    }
    
    close
    success(obj)
    
    
  })
  
  object inputs {
    lazy val default = if (isNewEdge) EdgeDialog.default else values
    
    lazy val sizing = new TextField(default.sizing.toString) { 
      peer.getDocument().asInstanceOf[AbstractDocument].setDocumentFilter(IntegralFilter) 
    }
    lazy val label = new TextField(default.label)
    lazy val terminal = new CheckBox() { selected = default.termination.isTerminal }
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
        new FlowPanel(new NoFocusButton(Action("Cancel") { close() }), confirm) :: Nil
    }
  defaultButton = confirm
  
  resizable = false
  
  {
    import javax.swing.WindowConstants
    peer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
  }
}