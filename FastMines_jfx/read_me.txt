Eclipse

Java 11
1. Download JavaFX 11 ea https://gluonhq.com/products/javafx/
2. Create a User Library: Eclipse -> Window -> Preferences -> Java -> Build Path -> User Libraries -> New.
   Name it JavaFX11 and include the jars under the lib folder from JavaFX 11-ea.
   Define system environment variable: example JAVAFX_HOME = youPath/Java/javafx-sdk-11.0.2
3. Add runtime arguments. Edit the project's run configuration, and add these VM arguments:
    a. open 'Run Configurations...'
    b. in tab 'Arguments' in block 'VM arguments' add next
       --module-path "${env_var:JAVAFX_HOME}/lib" --add-modules=javafx.controls