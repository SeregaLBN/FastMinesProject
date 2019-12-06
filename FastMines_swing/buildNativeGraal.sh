#!/bin/sh

# Steps:
# 1. Run app with native-image-agent
#   (build *.json files into ./src/main/resources/META-INF/native-image/FastMinesGame/FastMines_swing/)
#   (Click through the entire application and exit)
# 2. Edit native configuration
#    ./src/main/resources/META-INF/native-image/FastMinesGame/FastMines_swing/native-image.properties
# 3. Run make native
#    see gradle task :FastMines_swing:fastMinesSwingGraalNative


JAVA_HOME=$GRAALVM_HOME
PATH=$GRAALVM_HOME/bin:$PATH 

gradle --stop
gradle clean


# Step 1
gradle :FastMines_swing:build
java -agentlib:native-image-agent=config-output-dir=./src/META-INF/native-image/FastMinesGame/FastMines_swing/ \
     -jar ./build/libs/FastMines_swing-fat-2.1.1.jar

# Step 2
# see ./src/main/resources/META-INF/native-image/FastMinesGame/FastMines_swing/native-image.properties

# Step 3
$GRAALVM_HOME/bin/gu install native-image
gradle clean :FastMines_swing:fastMinesSwingGraalNative --info
