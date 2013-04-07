package com.jakemadethis.util

class NumUtil(num : BigInt) {
  def toStdForm = {
    val str = num.toString
    if (str.length <= 8) {
      "%,d".format(num)
    } else {
      val a = str.charAt(0)
      val b = str.substring(1, 3)
      val exp = str.length - 1
      a + "." + b + " * 10^" + exp
    }
  }
}

object NumUtil {
  implicit def toNumUtil(num : BigInt) = new NumUtil(num)
}