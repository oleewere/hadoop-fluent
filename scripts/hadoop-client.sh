#!/bin/bash

function print_help() {
  cat << EOF
   Usage: hadoop-client.sh --type <STORAGE_TYPE> --path <PATH> [additional options]

   -t, --type  <STORAGE_TYPE>                      available storage types: s3 | gcs | wasb | adlsv2 | hdfs
   -p, --path  <PATH>                              source path where data will be uploaded from to cloud storage
   -c, --core-site-folder <CORE_SITE_FOLDER_PATH>  core-site.xml path
   -h, --help                                      print help
EOF
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
    -p|--path)
          local UPLOAD_PATH="$2"
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

  if [[ -z "$UPLOAD_PATH" ]] ; then
    echo "path argument is required (-p or --path)."
    print_help
    exit 1
  fi
}

main ${1+"$@"}

