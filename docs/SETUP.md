# Project Setup

> **_NOTE:_**  This file is under construction.

# Overview 

Thank you for your interest in helping develop MBC!
This project makes use of several external libraries. 
We use [Maven](https://maven.apache.org/) as our Java build tool.

Since I currently use **IntelliJ** and **WSL2 Ubuntu** (Linux) for development, these docs are written with those specifications in mind.
These are subject to future modifications and revisions for alternate software are anticipated.

## Workflow 

Development is currently done with a lax Git workflow, where changes are managed manually by our
small team.

Once given access to the organization, feel free to clone the environment onto your local machine and begin your local development.

# Setup 

## Java

You must setup and install a JDK for local Java development.
Currently, Minecraft requires **Java 17** for versions above 1.18, and **Java 21** for 1.20.5+.

An appropriate JDK can be downloaded from the [Oracle website](https://www.oracle.com/java/technologies/downloads/) for Windows or macOS developers.

> **TODO**: Guide for Windows and macOS users.

Windows and macOS users may need to inspect their environment variables or PATH. Confirm the correct java version is being used by running `java --version` in a terminal window.

Linux users should download Java through their package manager. For Ubuntu:
```
sudo apt install openjdk-<number>-jdk
sudo update-java-alternatives --list
sudo update-java-alternatives --set <appropriate path specified from previous list>
```
IntelliJ should help configure this for you-you just need to link the directory containing the JDK version.

## Maven

The pom.xml file contains the configuration settings for the build dependencies. 
If your pom.xml is complaining about repositories not being found, go to File > Invalidate Caches, and Restart.

If any of the dependencies are unable to be resolved, visit the corresponding repository linked in pom.xml and attempt to find the file you're looking for; it is likely the proper Maven import is provided.

## Building

The following [StackOverflow answer](https://stackoverflow.com/a/4901370) explains how to build the Jar file in IntelliJ.

