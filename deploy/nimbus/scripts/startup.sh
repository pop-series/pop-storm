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


. "$SCRIPT_DIR/../../common/scripts/utils.sh"


setup() {
  set_env_var_if_missing "PROCESS_TYPE" "nimbus"

  set_env_var_if_missing "STORM_DIST_DIR" "/opt/apache-storm"
  set_env_var_if_missing "STORM_BIN_DIR" "$STORM_DIST_DIR/bin"

  set_env_var_if_missing "NIMBUS_FQDN" "pop-stormnimbus"
  set_env_var_if_missing "NIMBUS_THRIFT_PORT" "6627"
  set_env_var_if_missing "NIMBUS_THRIFT_MAX_BUFFER_SIZE" "33554432"
  set_env_var_if_missing "NIMBUS_CHILDOPTS" "-Xmx256M"
  set_env_var_if_missing "NIMBUS_ADDN_CHILDOPTS" ""
  set_env_var_if_missing "ZK_SERVERS" '["pop-stormzk"]'
  set_env_var_if_missing "ZK_PORT" "2181"
  set_env_var_if_missing "ZK_CHROOT" "/pop-storm"

  set_env_var_if_missing "PROCESS_VAR_DIR" "/var/pop-storm/$PROCESS_TYPE"
  set_env_var_if_missing "DATA_DIR" "$PROCESS_VAR_DIR/data"
  set_env_var_if_missing "LOGS_DIR" "$PROCESS_VAR_DIR/logs"
  mkdir -p "$DATA_DIR"
  mkdir -p "$LOGS_DIR"

  set_env_var_if_missing "CONF_DIR" "/opt/pop-storm/$PROCESS_TYPE/conf"
  set_env_var_if_missing "LOGS_CONF_DIR" "/opt/pop-storm/$PROCESS_TYPE/log4j2"
  set_env_var_if_missing "STORM_TEMPLATE_YAML_PATH" "$CONF_DIR/storm.template.yaml"
  set_env_var_if_missing "STORM_YAML_PATH" "$PROCESS_VAR_DIR/storm.yaml"

  NIMBUS_FQDN="$NIMBUS_FQDN" \
  NIMBUS_DATA_DIR="$DATA_DIR" \
  NIMBUS_LOGS_DIR="$LOGS_DIR" \
  NIMBUS_LOGS_CONF_DIR="$LOGS_CONF_DIR" \
  ZK_SERVERS="$ZK_SERVERS" \
  ZK_PORT="$ZK_PORT" \
  ZK_CHROOT="$ZK_CHROOT" \
  NIMBUS_THRIFT_PORT="$NIMBUS_THRIFT_PORT" \
  NIMBUS_THRIFT_MAX_BUFFER_SIZE="$NIMBUS_THRIFT_MAX_BUFFER_SIZE" \
  NIMBUS_CHILDOPTS="$NIMBUS_CHILDOPTS $NIMBUS_ADDN_CHILDOPTS" \
  envsubst < "$STORM_TEMPLATE_YAML_PATH" > "$STORM_YAML_PATH"

  set_env_var_if_missing "EXTLIBS_DIR" "/opt/pop-storm/build/extlibs"
}

main() {
  "$STORM_BIN_DIR"/storm "$PROCESS_TYPE" --config "$STORM_YAML_PATH" --jars "$EXTLIBS_DIR"
}

setup
main
