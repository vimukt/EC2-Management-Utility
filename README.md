EC2 Management Utility:
This is utility code which will help you create, start, stop & terminate an EC2 instance.
 
Dependencies
Make sure you have Java (preferably java 8) & Maven installed and path configure on your system, if not follow the vendor instructions for installing them on your operating system.


Running Tests:
Make sure you have "aws_access_key_id" & "aws_secret_access_key" keyed in correctly :
<project folder>/conf/credentials (file).
	

After checking out the code build the project using :
Run command(inside the project folder where pom.xml file is located) : mvn clean install -DskipTests ( make sure maven is installed and configure) to install the dependent jars.

Running tests using eclipse : 
	1. Make sure you have eclipse with testng plugin installed.
	2. Go to Project : right click on  "testng.xml" and run as testng.
	
Running tests using command line( can be used when wish to run the tests in CI/CD) :

  1. Go to project folder(which has pom file)
  2. run maven command : mvn test
