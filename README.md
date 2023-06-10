# pop-storm:

a collection of storm applications

## setup:
in order to use this project create a directory named ".devlinks" in root dir of the repository along with similar such symlinks.

```
java_home -> /Users/6harat/installations/jdk-11.jdk/Contents/Home
```

change the "/path/to/repo/" path in the following line in gradle.properties.
```
org.gradle.java.home=/path/to/repo/.devlinks/java_home
```
(ensure that you do not stage this file in subsequent commits, if any)

and then execute
```
./gradlew install
```
