package com.jakemadethis.graphite.ui

import scala.swing._

object MainFrame {
  val windows = collection.mutable.HashSet[Frame]()
}
class MainFrame extends Frame {
  MainFrame.windows.add(this)
  
  override def closeOperation {
    MainFrame.windows.remove(this)
    if (MainFrame.windows.isEmpty) System.exit(0)
  }
  
  {
    import javax.swing.WindowConstants
    peer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
  }
}