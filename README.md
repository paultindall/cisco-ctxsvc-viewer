# cisco-ctxsvc-viewer

Simple graphical tool to view/add/update customers and retrieve PODs

Before building load the context service dependencies into your local repository.
Navigate to the directory containing pom.xml and execute the command:

mvn -U install:install-file -Dfile=src/main/resources/context-service-sdk-1.0.6.jar -DgroupId=com.cisco.thunderhead -DartifactId=context-service-sdk -Dversion=1.0.6 -Dpackaging=jar -DpomFile=src/main/resources/context-service-sdk-pom.xml

Build the web app using mvn install.
Deploy the war file.
Locate connection.data (containing access key from the registration process) in default directory.
Locate connector.property (containing plugin location) in default directory.


