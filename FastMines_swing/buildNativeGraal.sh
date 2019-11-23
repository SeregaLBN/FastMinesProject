#!/bin/sh

JAVA_HOME=$GRAALVM_HOME
PATH=$GRAALVM_HOME/bin:$PATH 

$GRAALVM_HOME/bin/gu install native-image


gradle --stop

gradle clean :FastMines_swing:fastMinesSwingGraalNative --info
