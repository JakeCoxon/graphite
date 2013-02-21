package com.jakemadethis.util

object Time {
  def apply[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    println("%,d microseconds".format(micros))
    result
  }
}