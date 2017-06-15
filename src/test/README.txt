A continuous integration system can execute these tests using an automated deploy / test framework such as Jenkins.
This could also be used with Dockers to deploy an isolated environment with which to run the tests on.
Jenkins can be configured to build projects and execute tests, and even execute custom scripts to change values in project
configuration files.  There is a lot of support for setting up JUnit tests with a framework such as Jenkins so that whenever a master
build is merged or committed to on git, it will automatically build the project and execute all tests, returning a result of the build / tests.
It also contains details on the build in case there is a point of failure that needs to be investigated.
Using Dockers, environments can be setup, tested, taken down and exchanged, which also helps to facilitate a platform that must ensure uptime.
Dockers and Jenkins together can combine these strengths to create a fairly robust platform for testing in a continuous integration environment. 