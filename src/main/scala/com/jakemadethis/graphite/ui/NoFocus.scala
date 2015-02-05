package com.jakemadethis.graphite.ui

import scala.swing._

class NoFocusButton(action: Action) extends Button(action) {
  focusable = false
}