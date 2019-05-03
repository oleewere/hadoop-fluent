# Hadoop Fluent

![license](http://img.shields.io/badge/license-Apache%20v2-blue.svg)

Hadoop Fluent is a tool a Java application which can communicate with Fluentd (or Fluent-bit) agents and/or upload data through HDFS client to cloud storage (or HDFS).
The application can act as a Fluentd server (with forward protocol of Fluentd), or only run at the background and upload data that is buffered by Fluentd agents.

## Requirements

- Java 1.8+
- Python 2.7+
- Maven 3.3+

## Build

Build tarball: 

```bash
make package
```

Build deb package:

```bash
make deb
```

Build rpm package:

```bash
make rpm
```