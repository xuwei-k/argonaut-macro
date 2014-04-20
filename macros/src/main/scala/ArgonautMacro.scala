package argonaut.macros

import argonaut.{Json, JsonObject, JsonParser}
import scala.reflect.macros.Context
import language.experimental.macros

object ArgonautMacro {
  def apply(jsonSource: String): Json = macro applyImpl

  implicit class JsonContext(val c: StringContext) extends AnyVal {
    def json(): Json = macro jsonInterpolationImpl
  }

  def jsonInterpolationImpl(c: Context)(): c.Expr[Json] = {
    import c.universe._
    c.prefix.tree match {
      case Apply(_,List(Apply(_,List(Literal(Constant(str: String)))))) =>
        jsonString2tree(c)(str)
    }
  }

  def applyImpl(c: Context)(jsonSource: c.Expr[String]): c.Expr[Json] = {
    import c.universe._
    val Literal(Constant(jsonString: String)) = jsonSource.tree
    jsonString2tree(c)(jsonString)
  }

  private def jsonString2tree(c: Context)(string: String): c.Expr[Json] = {
    import c.universe._

    def json2tree(j: Json): c.Tree = j.fold(
      jsonNull   = q"argonaut.Json.jNull",
      jsonBool   = value => {
        if(value) q"argonaut.Json.jTrue"
        else q"argonaut.Json.jFalse"
      },
      jsonNumber = value => q"argonaut.Json.jNumber($value)",
      jsonString = value => q"argonaut.Json.jString($value)",
      jsonArray  = value => {
        val xs = value.map(v => json2tree(v))
        q"argonaut.Json.array(..$xs)"
      },
      jsonObject = value => {
        val xs: List[c.Tree] = value.toMap.map{
          case (k, v) => q"($k, ${json2tree(v)})"
        }(collection.breakOut)
        q"argonaut.Json.obj(..$xs)"
      }
    )

    val json = JsonParser.parse(string).fold(
      error => sys.error(error.toString),
      identity
    )

    c.Expr[Json](json2tree(json))
  }
}

