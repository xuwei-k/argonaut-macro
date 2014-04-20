package argonaut

import argonaut.macros.ArgonautMacro
import argonaut.macros.ArgonautMacro._

object Main{
  def main(args: Array[String]){
    val json1 = json"""
      {
         "a":[ 1 , 2.3 , true , "foo" , null ],
         "b" : [],
         "c" : {}
      }
    """

    println(json1)

    val json2 = ArgonautMacro("""
      {
         "a":[ 1 , 2.3 , true , "foo" , null ],
         "b" : [],
         "c" : {}
      }
    """)

    println(json1)
  }
}

