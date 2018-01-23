
Eclipse
Fix error: Access restriction: The type 'javafx.*' is not API (restriction on required library '%JAVA_HOME%/jre/lib/ext/jfxrt.jar')
Answer: Right-click on the project -> Properties -> Java Build Path -> tab 'Libraries' -> expand 'JRE System Library' -> select 'Access Rules' -> click button 'Edit...' -> click button 'Add' -> 'Resolution' choose 'Accessible' -> in 'Rule Pattern' enter 'javafx/**' -> Ok -> Ok -> Apply and Close 