package com.jakemadethis.graphite.visualization.renderers

import javax.swing.JLabel
import javax.swing.JComponent
import java.awt.Font
import java.awt.Color
import javax.swing.border.EmptyBorder

object TextRenderer extends JLabel {
  
  setFocusable(false)
  setBorder(new EmptyBorder(0,0,0,0))
  
  def getComponent(vv : JComponent, value : Any, font : Font, color : Color) = {
    setForeground(color)
    if (font != null) setFont(font)
    setValue(value)
    this
  }
  
  def setValue(value : Any) = setText(
      if (value == null) "" else value.toString())
}