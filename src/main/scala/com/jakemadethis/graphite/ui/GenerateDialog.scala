package com.jakemadethis.graphite.ui
import scala.swing._
import javax.swing.text._


case class GenerateDialogObject(size : Int, number : Int)

private object GenerateDialog {
  var default : GenerateDialogObject = new GenerateDialogObject(10, 100)
}
class GenerateDialog(owner : Window)(success : GenerateDialogObject => Unit) extends Dialog(owner){
  import GenerateDialog._
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
  object inputs {
    
    lazy val size = new TextField(default.size.toString) { 
      peer.getDocument().asInstanceOf[AbstractDocument].setDocumentFilter(IntegralFilter) 
    }
    lazy val number = new TextField(default.number.toString)
  }
   
  lazy val confirm = new Button() {
    action = Action("Generate") {
      
      val s = inputs.size.text.toInt
      val n = inputs.number.text.toInt
      val obj = new GenerateDialogObject(s, n)
      default = obj
      
      close
      success(obj)
    }
    focusable = false
  }
  lazy val main = new GridPanel(2, 2) {
    contents ++= 
      new Label("Size of graph:", null, Alignment.Right) :: inputs.size :: 
      new Label("Number of graphs:", null, Alignment.Right) :: inputs.number :: 
      Nil
  }
  
  title = "Generate Graphs"
  contents = new BoxPanel(Orientation.Vertical) {
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