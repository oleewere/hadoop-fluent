#!/bin/bash

function print_help() {
  cat << EOF
   Usage: hadoop-fluent.sh --type <STORAGE_TYPE> --path <PATH> [additional options]

   -t, --type  <STORAGE_TYPE>                      available storage types: s3 | gcs | wasb | abfs
   -s, --core-site-folder <CORE_SITE_FOLDER_PATH>  core-site.xml path
   -h, --help                                      print help
EOF
}

readlinkf(){
  # get real path on mac OSX
  perl -MCwd -e 'print Cwd::abs_path shift' "$1";
}

if [ "$(uname -s)" = 'Linux' ]; then
  SCRIPT_DIR="`dirname "$(readlink -f "$0")"`"
else
  SCRIPT_DIR="`dirname "$(readlinkf "$0")"`"
fi

HADOOP_FLUENT_ROOT_DIR="`dirname \"$SCRIPT_DIR\"`"

JVM="java"

if [ -x $JAVA_HOME/bin/java ]; then
  JVM=$JAVA_HOME/bin/java
fi

if [[ -z "$HADOOP_FLUENT_CONF" ]]; then
  HADOOP_FLUENT_CONF="$HADOOP_FLUENT_ROOT_DIR/conf/hadoop-fluent.conf"
fi

function run_app() {
  local sdk_type=$1
  local core_site_folder=$2

  $JVM -classpath "$HADOOP_FLUENT_ROOT_DIR/libs/core/*:/$HADOOP_FLUENT_ROOT_DIR/libs/$sdk_type/*:$core_site_folder" \
    com.cloudera.hadoop.cloud.Forward "$HADOOP_FLUENT_CONF"
}

function main() {
  while [[ $# -gt 0 ]]
  do
    key="$1"
    case $key in
    -t|--type)
          local STORAGE_TYPE="$2"
          shift 2
     ;;
    -c|--core-site-folder)
          local CORE_SITE_FOLDER="$2"
          shift 2
    ;;
    -h|--help)
          shift 1
          print_help
          exit 0
    ;;
    *)
      echo "Unknown option: $1"
      exit 1
    ;;
    esac
  done

  if [[ -z "$STORAGE_TYPE" ]] ; then
    echo "type argument is required (-t or --type)."
    print_help
    exit 1
  fi

  if [[ -z "$CORE_SITE_FOLDER" ]] ; then
    echo "path argument is required (-c or --core-site-folder)."
    print_help
    exit 1
  fi

  if [[ "$STORAGE_TYPE" == "s3" ]]; then
    run_app "s3" "$CORE_SITE_FOLDER"
  elif [[ "$STORAGE_TYPE" == "gcs" ]]; then
    run_app "gcs" "$CORE_SITE_FOLDER"
  elif [[ "$STORAGE_TYPE" == "abfs" ]]; then
    run_app "abfs" "$CORE_SITE_FOLDER"
  elif [[ "$STORAGE_TYPE" == "wasb" ]]; then
    run_app "wasb" "$CORE_SITE_FOLDER"
  else
    echo "Unsupported type:. $STORAGE_TYPE"
    exit 1
  fi
}

main ${1+"$@"}

