# endit-tss

The endit-tss.properties file should be in the /etc directory.

Executed line: java -Xms500m -Xmx500m --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED -Djdk.lang.Process.launchMechanism=POSIX_SPAWN -XX:+UseParallelGC -XX:+UseCompressedOops -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/endit-tss/java_heap.hprof -jar target/endit-tss-2.0.0.jar <Read or Write> 

