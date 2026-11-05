# Install Maven
- Download binary zip file
- Create folder in program files called "maven" and extract downloaded files into it
- Look for Windows Environment Variables and add a new System Variable: "MAVEN_HOME" with the path C:\Program Files\maven
- Edit "Path" variable and add a new path: "%MAVEN_HOME%\bin"
- Add path for Maven in VSCode settings
- Run mvn -version in terminal to check
 - Make sure Desktop is not inside of OneDrive
 - Go to OneDrive settings and stop syncing desktop to OneDrive, select to keep all files on local computer
 - Go to desktop folder in OneDrive, right click, and select Restore Default for the path
 - Make sure it moves every file to new desktop folder

# Install VSCode Maven Extension
- Either install Maven extension on its own or get Extension Pack for Java, which includes it

# Evil mvn install issues
- Change maven-shade-plugin (about line 48) in the pom.xml to version 3.6.2

For Later
- Git Stash (reverts changes and stores)
- Switch to other branch (queakieTest)
- Git Stash Pop (puts those changes in)
- - Can cause conflicts if there are other changes