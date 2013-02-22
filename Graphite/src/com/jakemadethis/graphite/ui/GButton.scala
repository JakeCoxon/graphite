package com.jakemadethis.graphite.ui

import javax.swing.JButton

object GButton {
  def apply(label : String) = new JButton(label) {
    //setFocusPainted(false)
    setFocusable(false)
  }
}