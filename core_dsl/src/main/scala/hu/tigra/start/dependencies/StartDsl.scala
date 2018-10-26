package hu.tigra.start.dependencies

import scala.util.parsing.combinator.JavaTokenParsers

case class DependencyLine(what: String, dependsOnWhat: String)

object StartDsl {

  def main(args: Array[String]): Unit = {
    val test1 =
      """
        |resource path: tm-env/kubernetes
        |
        |project name: sample-dsl-1
        |
        |kieServerPostgre:
        |kie-pgdata-persistentvolumeclaim.yaml
        |postgresql-kie-deployment.yaml
        |postgresql-kie-service.yaml
        |
        |kieServer:
        |kie-server-deployment.yaml
        |kie-server-service.yaml
        |
        |dependencies:
        |kieServer depends on kieServerPostgre
        |""".stripMargin

    println(
      MyParser.parseAll(MyParser.dsl, test1)
    )
  }

  //noinspection TypeAnnotation
  object MyParser extends JavaTokenParsers {

    def dsl = header ~ rep1(itemWithFiles) ~ dependencies

    def header = header1 | header2

    def header1 = resourcePath ~ projectName

    def header2 = projectName ~ resourcePath

    def resourcePath = "resource" ~ "path" ~ ":" ~ file

    def projectName = "project" ~ "name" ~ ":" ~ "[a-z0-9-]+".r


    def keywords = itemColon | dependenciesColon


    def itemWithFiles = itemColon ~ rep1(file)

    def itemColon = not(dependenciesColon) ~> item ~ ":"

    def item = """[\w-/]+""".r

    def file = not(keywords) ~>"""[\w\.\-/]+""".r

    def dependencies = dependenciesColon ~ rep1(dependencyLine)

    def dependenciesColon = "dependencies" ~ ":"

    //noinspection ScalaUnusedSymbol
    def dependencyLine: Parser[DependencyLine] = item ~ "depends" ~ "on" ~ item ^^ {
      case a ~ depends ~ on ~ b => DependencyLine(a, b)
    }
  }

}
