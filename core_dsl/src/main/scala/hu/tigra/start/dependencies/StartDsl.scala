package hu.tigra.start.dependencies


case class StartDslData(resourcePath: ResourcePath,
                        projectName: ProjectName,
                        items: List[Item],
                        dependencies: List[DependencyLine],
                        start: Start,
                        watch: Watch) {

  def iterateIt = items.flatMap(i =>
    i.files.map(f => start.s.replaceAll("\\$item", i.name).replaceAll("\\$file", f))
      ::: List(watch.s.replaceAll("\\$item", i.name))
  )

  override def toString: String =
    Seq(resourcePath, projectName, items.mkString("\n"), dependencies.mkString("\n"), start, watch).mkString("\n")
}

case class ResourcePath(s: String)

case class ProjectName(s: String)

case class Item(name: String, files: List[String])

case class DependencyLine(what: String, dependsOnWhat: String)

case class Start(s: String)

case class Watch(s: String)


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
      |
      |start:
      |"echo start $item $file"
      |
      |watch:
      |"echo watch $item
      |sleep 2"
      |""".stripMargin

    val result = MyParser.parse(test1).iterateIt
    result.foreach(println)

    import scala.sys.process._

    https :// www.scala - lang.org / api / current / scala / sys / process / ProcessBuilder.html
    result.foreach(_.!)
  }

  //noinspection TypeAnnotation
  object MyParser extends ExtParsers {

    def parse(s: String): StartDslData = parseAll(MyParser.dsl, s) match {
      case Success(m, _) =>
        m

      case NoSuccess(msg, next) =>
        println(msg)
        println(next)
        StartDslData(null, null, Nil, Nil, null, null)
    }

    def resourcePath = "resource" ~ "path" ~ ":" ~> file ^^ {
      ResourcePath
    }

    def header = header1 | header2

    def header1 = resourcePath ~ projectName

    def header2 = projectName ~ resourcePath

    def projectName = "project" ~ "name" ~ ":" ~> "[a-z0-9-]+".r ^^ {
      ProjectName
    }

    def dsl = (resourcePath $ projectName $ rep1(itemWithFiles) $ dependencies $ start $ watch) ^^ {
      case resourcePath $ projectName $ items $ dependencies $ start $ watch =>
        StartDslData(resourcePath, projectName, items, dependencies, start, watch)
    }

    def start = startColon ~ "\"" ~>"""[^"]+""".r <~ "\"" ^^ {
      Start
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

    def startColon = "start" ~ ":"

    def watch = watchColon ~ "\"" ~>"""[^"]+""".r <~ "\"" ^^ {
      Watch
    }

    def watchColon = "watch" ~ ":"

    def keywords = itemColon | dependenciesColon | startColon | watchColon
  }

}
