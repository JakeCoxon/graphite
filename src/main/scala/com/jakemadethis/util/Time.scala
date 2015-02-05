package com.jakemadethis.util

object Time {
  /** Prints the time in microseconds it took for a function to compute **/
  def apply[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    println("%,d microseconds".format(micros))
    result
  }
  
  /** Returns a tuple (result, micros), the time in microseconds it took for a 
   * function to compute **/
  def get[A](a : => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    (result, micros)
  }
}