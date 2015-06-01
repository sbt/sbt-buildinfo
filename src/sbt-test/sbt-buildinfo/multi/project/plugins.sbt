{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % pluginVersion)
}

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")
