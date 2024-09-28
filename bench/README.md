# Benchmark

## Backends

### JVM

Build with:

```sh
sbt -J-Xmx16G 'benchJVM/assembly'
```

Run with:

```sh
time java -Xmx16G -jar ./bench/jvm/target/scala-3.5.1/bench-assembly-0.1.0-SNAPSHOT.jar -w 39 -h 32 -n 10000 -s 7
```

### Node.js

Build with:

```sh
sbt -J-Xmx16G 'benchJS/fullOptJS'
```

Run with:

```sh
time node --max-old-space-size=16384 ./bench/js/target/scala-3.5.1/bench-opt/main.js -w 39 -h 32 -n 10000 -s 7
```

### Native

Build with:

```sh
sbt -J-Xmx16G 'benchNative/nativeLink'
```

Run with:

```sh
time GC_MAXIMUM_HEAP_SIZE=16G ./bench/native/target/scala-3.5.1/bench-out -w 39 -h 32 -n 10000 -s 7
```
