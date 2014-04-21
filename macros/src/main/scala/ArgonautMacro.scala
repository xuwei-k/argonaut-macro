package argonaut.macros

import argonaut.{Json, JsonObject, JsonParser}
import scala.reflect.macros.blackbox.Context
import language.experimental.macros

object ArgonautMacro {

  def apply(jsonSource: String): Json = macro ArgonautMacro.applyImpl

  implicit class JsonContext(val c: StringContext) extends AnyVal {
    def json(): Json = macro ArgonautMacro.jsonInterpolationImpl
  }

}

private final class ArgonautMacro(val c: Context) {
  import c.universe._

  def jsonInterpolationImpl(): c.Expr[Json] = {
    c.prefix.tree match {
      case Apply(_,List(Apply(_,List(Literal(Constant(str: String)))))) =>
        jsonString2tree(str)
    }
  }

  def applyImpl(jsonSource: c.Expr[String]): c.Expr[Json] = {
    val Literal(Constant(jsonString: String)) = jsonSource.tree
    jsonString2tree(jsonString)
  }

  private implicit val jsonLiftable: Liftable[Json] = new Liftable[Json] {
    def apply(json: Json) = json.fold[Tree](
      jsonNull   = q"argonaut.Json.jNull",
      jsonBool   = value => {
        if (value) q"argonaut.Json.jTrue"
        else q"argonaut.Json.jFalse"
      },
      jsonNumber = value => q"argonaut.Json.jNumber($value)",
      jsonString = value => q"argonaut.Json.jString($value)",
      jsonArray  = value => q"argonaut.Json.array(..$value)",
      jsonObject = value => q"argonaut.Json.obj(..${ value.toMap })"
    )
  }

  private def jsonString2tree(string: String): c.Expr[Json] = {
    val json = JsonParser.parse(string).fold(
      error => sys.error(error.toString),
      identity
    )

    c.Expr[Json](q"$json")
  }
}

