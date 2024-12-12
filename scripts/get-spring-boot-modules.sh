#!/bin/sh

set -eu

# Usage: sh get-springboot-modules.sh 1 2 3 4
# Ensure JAVA_HOME variable is set
# Parameters:
#   1: Full path to spring-boot fat jar
#   2: Jdk version (17)
#   3: Temp directory for spring-boot app extraction
#   4: Automatic modules: list of automatic modules, typically legacy libraries (multiple values separator is the comma)
# Example: get-springboot-modules.sh mymodule/target/mymodule.jar 17 mymodule/target/tmp "snakeyaml-1.28.jar,jakarta.annotation-api-1.3.5.jar,slf4j-api-1.7.32.jar"
readonly TARGET_JAR=$1
readonly TARGET_VER=$2
readonly TARGET_TMP=$3
readonly TARGET_AUTOMOD="$4"

#Directory to extract the jar
TMP_DIR="$TARGET_TMP/app-jar"
mkdir -p ${TMP_DIR}
trap 'rm -rf ${TMP_DIR}' EXIT

AUTOMATIC_MODULES=""

# Compute automatic modules list
INC_AUTO=0
if [ ! -z "$TARGET_AUTOMOD" ] ; then
  for EACH in `echo "$TARGET_AUTOMOD" | grep -o -e "[^,]*"`; do
     if  [ ! $INC_AUTO = "0" ] ; then
       AUTOMATIC_MODULES="$AUTOMATIC_MODULES:"
     fi
     AUTOMATIC_MODULES="$AUTOMATIC_MODULES$TMP_DIR/BOOT-INF/lib/$EACH"
     INC_AUTO=$((INC_AUTO+1))
  done
fi

echo "AUTOMATIC_MODULES: $AUTOMATIC_MODULES"

#Extract the jar
echo "Extract archive $1 to ${TMP_DIR}"
unzip -q "${TARGET_JAR}" -d "${TMP_DIR}"

echo "****************** Modules *********************"

if [ ! -z "$AUTOMATIC_MODULES" ] ; then
    ${JAVA_HOME}/bin/jdeps \
      -cp \'${TMP_DIR}/BOOT-INF/lib/*:${TMP_DIR}/BOOT-INF/classes:${TMP_DIR}\' \
      -quiet \
      --ignore-missing-deps \
      --module-path $AUTOMATIC_MODULES \
      --recursive \
      --multi-release ${TARGET_VER} \
      --print-module-deps \
      ${TMP_DIR}/org ${TMP_DIR}/BOOT-INF/classes ${TMP_DIR}/BOOT-INF/lib/*.jar
else
    ${JAVA_HOME}/bin/jdeps \
        -cp \'${TMP_DIR}/BOOT-INF/lib/*:${TMP_DIR}/BOOT-INF/classes:${TMP_DIR}\' \
        -quiet \
        --ignore-missing-deps \
        --recursive \
        --multi-release ${TARGET_VER} \
        --print-module-deps \
        ${TMP_DIR}/org ${TMP_DIR}/BOOT-INF/classes ${TMP_DIR}/BOOT-INF/lib/*.jar
fi
