Eclipse

Java 8
Fix error: Access restriction: The type 'javafx.*' is not API (restriction on required library '%JAVA_HOME%/jre/lib/ext/jfxrt.jar')
Answer: Right-click on the project -> Properties -> Java Build Path -> tab 'Libraries' -> expand 'JRE System Library' ->
        select 'Access Rules' -> click button 'Edit...' -> click button 'Add' -> 'Resolution' choose 'Accessible' ->
        in 'Rule Pattern' enter 'javafx/**' -> Ok -> Ok -> Apply and Close
    PS: Additionally mark as 'Forbidden'
         fmg/swing/**
         java/awt/**
         javax/swing/**

Java 11
1. Download JavaFX 11 ea https://gluonhq.com/products/javafx/
2. Create a User Library: Eclipse -> Window -> Preferences -> Java -> Build Path -> User Libraries -> New.
   Name it JavaFX11 and include the jars under the lib folder from JavaFX 11-ea.
3. Add runtime arguments. Edit the project's run configuration, and add these VM arguments:
    --module-path C:\Users\<user>\Downloads\javafx-sdk-11\lib --add-modules=javafx.controls