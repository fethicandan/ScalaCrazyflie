name := "ScalaCrazyflie"

version := "1.0"

scalaVersion := "2.10.3"

// This almost worked, except usb4java has a dependency on javax.usb, which is evidently not hosted on any repo anywhere
//resolvers += "typesafe-artifactory" at "http://typesafe.artifactoryonline.com/typesafe/sonatype-snapshots-cache"
//libraryDependencies += "de.ailis.usb4java" % "usb4java" % "1.0.0-SNAPSHOT"
