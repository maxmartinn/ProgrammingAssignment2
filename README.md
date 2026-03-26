# CIS4930 Internet Storage Systems Programming Assignment 2

## Overview

A client-server file transfer application that allows a client to request files from a remote server over TCP sockets. The server sends files as binary messages, and the client downloads them into a local `downloads/` folder. RTT (Round-Trip Time) statistics are tracked and displayed.

## Compilation

Compile all Java files with preview features enabled (required for pattern matching):

```bash
javac --enable-preview --release 17 *.java
```

## Running the Server

Start the server on a specific port (e.g., 5050):

```bash
java --enable-preview server 5050
```

The server will listen for incoming client connections and serve files from the current directory.

## Running the Client

In a separate terminal, start the client pointing to the server's host and port:

```bash
java --enable-preview client localhost 5050
```

Replace `localhost` with the server's IP address if running on a different machine.
