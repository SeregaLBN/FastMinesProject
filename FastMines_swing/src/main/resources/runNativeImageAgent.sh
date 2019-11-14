#!/bin/sh

# META-INF/native-image/${groupId}/${artifactId}/native-image.properties

/usr/app/graalvm/bin/java \
    -agentlib:native-image-agent=config-output-dir=./META-INF/native-image/FastMinesGame/FastMines_swing/ \
    -jar ./././build/libs/FastMines_swing-fat-2.1.1.jar