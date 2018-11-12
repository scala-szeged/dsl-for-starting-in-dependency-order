package hu.tigra.start.dependencies.openshift

import hu.tigra.start.dependencies.StartDsl.MyParser


object DeployOnOpenShift {

  def main(args: Array[String]): Unit = {

    val test1 =
      """
        |resource path: open_shift_starter/sample-dsl/tm-env/kubernetes
        |
        |project name: sample-dsl-1
        |
        |postgresql-kie:
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
        |"oc create -f $file"
        |
        |watch:
        |"oc get pod"
        |""".stripMargin

    // TODO "oc get pod | grep postgresql-kie | cut -d' ' -f1" and "oc logs"

    val startDslData = MyParser.parse(test1)

    //startDslData.commandList.foreach(println)
    //result.foreach(_.!)

    val basePath = os.Path(new java.io.File(startDslData.resourcePath.s).getCanonicalPath)
    val pwd = os.proc("cat", "kie-pgdata-persistentvolumeclaim.yaml").call(cwd = basePath)
    println(pwd.out.trim)

    startDslData.commandList.foreach { cmd =>
      println
      println
      println(cmd)
      println
      val invoked = os.proc(cmd.split(" ")).call(cwd = basePath)
      println(invoked.out.trim)
      println(invoked.err.trim)
    }
  }
}
