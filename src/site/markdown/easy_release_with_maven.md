Easy release with maven and Git/GitHub
======================================

To release gaedo with ease, please follow this n simple steps 

0. on windows, make sure required executables are here and configured (pageant with github key, git on the path)
1. run `mvn release:prepare  -DautoVersionSubmodules` with all code commited
2. run `mvn release:perform -Dgoals=deploy` and go drink some coffee. The `-Dgoals=deploy` is here to make sure the site won't be deployed during release, which usually causes weird troubles (apparently due to maven-javadoc-plugin requiring the `gaedo-informers-generator` to exist in public repositories while not yet released.
3. Go right to https://oss.sonatype.org and perform the close/release steps

Then don't forget to generate release site by

1. run `mvn versions:set -DnewVersion=TheReleaseVersion`
1. run `mvn site-deploy` to have site deployed to GitHub
1. run `mvn versions:revert` to go back to SNAPSHOT version.
1. Edit `README.md` in gaedo root folder to add link to new version site.