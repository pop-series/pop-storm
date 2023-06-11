#!/bin/bash

# determine main script directory
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")"/$link"
  fi
done
SAVED="$(pwd)"
cd "$(dirname "$PRG")" >/dev/null
export SCRIPT_DIR="$(pwd -P)"
cd "$SAVED" >/dev/null


. "$SCRIPT_DIR/../../common-scripts/utils.sh"


setup() {
  set_env_var_if_missing "PROCESS_TYPE" "zookeeper"

  set_env_var_if_missing "STORM_DIST_DIR" "/opt/apache-storm"
  set_env_var_if_missing "STORM_BIN_DIR" "$STORM_DIST_DIR/bin"

  set_env_var_if_missing "ZK_PORT" "2181"
  set_env_var_if_missing "PROCESS_VAR_DIR" "/var/pop-storm/$PROCESS_TYPE"
  set_env_var_if_missing "DATA_DIR" "$PROCESS_VAR_DIR/data"
  set_env_var_if_missing "LOGS_DIR" "$PROCESS_VAR_DIR/logs"
  mkdir -p "$DATA_DIR"
  mkdir -p "$LOGS_DIR"

  set_env_var_if_missing "CONF_DIR" "/opt/pop-storm/$PROCESS_TYPE/conf"
  set_env_var_if_missing "LOGS_CONF_DIR" "/opt/pop-storm/$PROCESS_TYPE/log4j2"
  set_env_var_if_missing "STORM_TEMPLATE_YAML_PATH" "$CONF_DIR/storm.template.yaml"
  set_env_var_if_missing "STORM_YAML_PATH" "$PROCESS_VAR_DIR/storm.yaml"

  ZK_PORT="$ZK_PORT" \
  ZK_LOGS_DIR="$LOGS_DIR" \
  ZK_LOGS_CONF_DIR="$LOGS_CONF_DIR" \
  ZK_DATA_DIR="$DATA_DIR" \
  envsubst < "$STORM_TEMPLATE_YAML_PATH" > "$STORM_YAML_PATH"

  set_env_var_if_missing "EXTLIBS_DIR" "/opt/pop-storm/build/extlibs"
}

main() {
  "$STORM_BIN_DIR"/storm dev-zookeeper --config "$STORM_YAML_PATH" --jars "$EXTLIBS_DIR"
}

setup
main
