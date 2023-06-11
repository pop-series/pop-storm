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
  set_env_var_if_missing "PROCESS_TYPE" "supervisor"

  set_env_var_if_missing "STORM_DIST_DIR" "/opt/apache-storm"
  set_env_var_if_missing "STORM_BIN_DIR" "$STORM_DIST_DIR/bin"

  set_env_var_if_missing "NIMBUS_SEEDS" '["pop-stormnimbus"]'
  set_env_var_if_missing "NIMBUS_THRIFT_PORT" "6627"
  set_env_var_if_missing "ZK_SERVERS" '["pop-stormzk"]'
  set_env_var_if_missing "ZK_PORT" "2181"
  set_env_var_if_missing "ZK_CHROOT" "/pop-storm"
  set_env_var_if_missing "SUPERVISOR_SLOT_PORTS" "[6700,6701]"
  set_env_var_if_missing "SUPERVISOR_CHILDOPTS" "-Xmx512M"
  set_env_var_if_missing "SUPERVISOR_ADDN_CHILDOPTS" ""
  set_env_var_if_missing "SUPERVISOR_MEM_CAPACITY_MB" "4096.0"
  set_env_var_if_missing "SUPERVISOR_CPU_CAPACITY" "100.0"
  set_env_var_if_missing "WORKER_HEAP_MEMORY_MB" "512"
  set_env_var_if_missing "WORKER_CHILDOPTS" "-Xmx%HEAP-MEM%m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=artifacts/heapdump -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1%ID% -Dcom.sun.management.jmxremote.rmi.port=1%ID%  -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=127.0.0.1 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5%ID%"
  set_env_var_if_missing "WORKER_ADDN_CHILDOPTS" ""
  set_env_var_if_missing "WORKER_GC_CHILDOPTS" "-Xlog:gc*=info,gc+age*=trace,gc+ergo+cset=trace:file=artifacts/gc.log:time,uptime,level,tags:filecount=10,filesize=1M"
  set_env_var_if_missing "WORKER_GC_ADDN_CHILDOPTS" ""

  set_env_var_if_missing "PROCESS_VAR_DIR" "/var/pop-storm/$PROCESS_TYPE"
  set_env_var_if_missing "DATA_DIR" "$PROCESS_VAR_DIR/data"
  set_env_var_if_missing "LOGS_DIR" "$PROCESS_VAR_DIR/logs"
  mkdir -p "$DATA_DIR"
  mkdir -p "$LOGS_DIR"

  set_env_var_if_missing "CONF_DIR" "/opt/pop-storm/$PROCESS_TYPE/conf"
  set_env_var_if_missing "LOGS_CONF_DIR" "/opt/pop-storm/$PROCESS_TYPE/log4j2"
  set_env_var_if_missing "STORM_TEMPLATE_YAML_PATH" "$CONF_DIR/storm.template.yaml"
  set_env_var_if_missing "STORM_YAML_PATH" "$PROCESS_VAR_DIR/storm.yaml"

  NIMBUS_SEEDS="$NIMBUS_SEEDS" \
  NIMBUS_THRIFT_PORT="$NIMBUS_THRIFT_PORT" \
  ZK_SERVERS="$ZK_SERVERS" \
  ZK_PORT="$ZK_PORT" \
  ZK_CHROOT="$ZK_CHROOT" \
  SUPERVISOR_DATA_DIR="$DATA_DIR" \
  SUPERVISOR_LOGS_DIR="$LOGS_DIR" \
  SUPERVISOR_LOGS_CONF_DIR="$LOGS_CONF_DIR" \
  SUPERVISOR_SLOT_PORTS="$SUPERVISOR_SLOT_PORTS" \
  SUPERVISOR_CHILDOPTS="$SUPERVISOR_CHILDOPTS $SUPERVISOR_ADDN_CHILDOPTS" \
  SUPERVISOR_MEM_CAPACITY_MB="$SUPERVISOR_MEM_CAPACITY_MB" \
  SUPERVISOR_CPU_CAPACITY="$SUPERVISOR_CPU_CAPACITY" \
  WORKER_HEAP_MEMORY_MB="$WORKER_HEAP_MEMORY_MB" \
  WORKER_CHILDOPTS="$WORKER_CHILDOPTS $WORKER_ADDN_CHILDOPTS" \
  WORKER_GC_CHILDOPTS="$WORKER_GC_CHILDOPTS $WORKER_GC_ADDN_CHILDOPTS" \
  envsubst < "$STORM_TEMPLATE_YAML_PATH" > "$STORM_YAML_PATH"

  set_env_var_if_missing "EXTLIBS_DIR" "/opt/pop-storm/build/extlibs"
}

main() {
  "$STORM_BIN_DIR"/storm "$PROCESS_TYPE" --config "$STORM_YAML_PATH" --jars "$EXTLIBS_DIR"
}

setup
main
