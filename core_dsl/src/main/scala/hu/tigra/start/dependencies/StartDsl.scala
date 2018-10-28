package hu.tigra.start.dependencies

import scala.util.parsing.combinator.JavaTokenParsers


case class DependencyLine(what: String, dependsOnWhat: String)

case class ResourcePath(s: String)

case class ProjectName(s: String)

case class Item(name: String, files: List[String])


object StartDsl {

  def main(args: Array[String]): Unit = {

    // It was 2 hours work to make the parser which recognizes the sample below,
    // but does not do anything else.
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

    MyParser.parse(test1).foreach(println)
  }

  //noinspection TypeAnnotation
  object MyParser extends JavaTokenParsers {

    def resourcePath = "resource" ~ "path" ~ ":" ~> file ^^ {
      ResourcePath
    }

    def header = header1 | header2

    def header1 = resourcePath ~ projectName

    def header2 = projectName ~ resourcePath

    def projectName = "project" ~ "name" ~ ":" ~> "[a-z0-9-]+".r ^^ {
      ProjectName
    }

    def parse(s: String) = parseAll(MyParser.dsl, s) match {
      case Success(m, _) =>
        m

      case NoSuccess(msg, next) =>
        println(msg)
        println(next)
        Map()
    }


    def keywords = itemColon | dependenciesColon

    def dsl = header ~ rep1(itemWithFiles) ~ dependencies ^^ {
      case header ~ items ~ dependencies => Map(
        header._1.getClass -> header._1,
        header._2 -> header._2,
        items.head.getClass -> items,
        dependencies.head.getClass -> dependencies
      )
    }

    def itemWithFiles = itemColon ~ rep1(file) ^^ {
      case name ~ files => Item(name, files)
    }

    def item = """[\w-/]+""".r

    def file = not(keywords) ~>"""[\w\.\-/]+""".r

    def itemColon = not(dependenciesColon) ~> item <~ ":"

    def dependenciesColon = "dependencies" ~ ":"

    //noinspection ScalaUnusedSymbol
    def dependencyLine: Parser[DependencyLine] = item ~ "depends" ~ "on" ~ item ^^ {
      case a ~ depends ~ on ~ b => DependencyLine(a, b)
    }

    def dependencies = dependenciesColon ~> rep1(dependencyLine)
  }

}
