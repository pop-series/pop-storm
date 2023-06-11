#!/bin/bash

for i in "$@"; do
  case $i in
    -t=*|--topology=*)
      TOPOLOGY_YAML_PATH="${i#*=}"
      shift # past argument=value
      ;;
    -j=*|--appjar=*)
      APP_JAR_PATH="${i#*=}"
      shift # past argument=value
      ;;
    *)
      # unknown option
      ;;
  esac
done

"$STORM_BIN_DIR"/storm jar "$APP_JAR_PATH" "$FLUX_MAIN" --config "$STORM_YAML_PATH" --no-splash --no-detail "$TOPOLOGY_YAML_PATH"
