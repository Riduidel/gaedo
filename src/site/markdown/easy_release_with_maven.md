Easy release with maven and Git/GitHub
======================================

To release gaedo with ease, please follow this n simple steps 

0. on windows, make sure required executables are here and configured (pageant with github key, git on the path)
1. run `mvn release:prepare  -DautoVersionSubmodules` with all code commited
2. run `mvn release:perform` and go drink some coffee
3. Go right to https://oss.sonatype.org and perform the close/release steps