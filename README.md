# TITP ISO 8583 Server

A test server that processes ISO 8583 messages over TCP socket connections using the j8583 library. This server returns success responses for all requests and supports full ISO 8583 message parsing.

## Features

- Processes ISO 8583 messages over TCP sockets using j8583 library
- Supports multiple message types (0100, 0200, 0400, 0800, etc.)
- Returns success responses for all requests (test server)
- Multi-threaded client handling with clean architecture
- Comprehensive logging with detailed message analysis
- Full ISO 8583 message parsing and formatting
- Configurable via XML configuration
- Automatic 5-byte header stripping for TITP protocol
- Modular, maintainable codebase with separation of concerns

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory.

## Running the Server

### Default port (8080)
```bash
java -jar target/jpos-server-1.0.0.jar
```

### Custom port
```bash
java -jar target/jpos-server-1.0.0.jar 9090
```

## Architecture

The server uses a clean, modular architecture with separation of concerns:

### Core Components:
- **TITPServer**: Main server class that accepts connections and manages the thread pool
- **ClientHandler**: Orchestrates client connection handling and message flow

### Processing Components:
- **MessageProcessor**: Handles ISO 8583 message parsing and response creation
- **HeaderStripper**: Removes 5-byte headers from incoming messages
- **MessageLogger**: Provides comprehensive logging of received and sent messages
- **MessageSender**: Manages sending responses to clients

### Benefits:
- ✅ **Single Responsibility**: Each class has one clear purpose
- ✅ **Testability**: Components can be tested independently
- ✅ **Maintainability**: Easy to modify or extend individual components
- ✅ **Readability**: Clear separation of concerns makes code easier to understand

## Configuration

The server uses the `config_titp_simple.xml` file for ISO 8583 message format configuration. This file defines:

- Message headers for different MTIs
- Field definitions and types (NUMERIC, ALPHA, LLVAR, BINARY, etc.)
- Message parsing rules for each MTI type

## Message Types Supported

- **0100/0110**: Authorization request/response
- **0200/0210**: Financial request/response  
- **0400/0410**: Reversal request/response
- **0800/0810**: Network management request/response

## Protocol

The server expects messages in the following format:
1. 2-byte message length (big-endian)
2. Message bytes (may include 5-byte header + ISO 8583 message)

### Message Processing:
- **Header Detection**: Automatically detects and strips 5-byte headers (e.g., "02020")
- **ISO Parsing**: Parses the remaining bytes as ISO 8583 messages according to configuration
- **Response Generation**: Creates appropriate ISO 8583 responses

Responses are sent in the same format with proper ISO 8583 field formatting.

## Logging

Logs are written to console only with real-time output. The server provides detailed logging of:

- **Received Messages**: Complete breakdown of incoming ISO 8583 messages including:
  - Message ID and timestamp
  - MTI (Message Type Indicator)
  - Raw message length
  - Client IP address
  - All present fields with values (truncated for readability)

- **Response Messages**: Detailed information about outgoing responses including:
  - Response MTI
  - All response fields with values
  - Message processing status

- **Connection Events**: Client connections, disconnections, and errors

## Testing

You can test the server manually using tools like netcat or by creating custom ISO 8583 messages that follow the configured format.

## Example Client Usage

The server accepts TCP connections and processes ISO 8583 messages. Clients should:

1. Connect to the server port
2. Send messages with 2-byte length prefix followed by ISO 8583 message bytes
3. Read responses with 2-byte length prefix followed by ISO 8583 response bytes

## Response Codes

- **00**: Approval
- **96**: System malfunction (error cases)

## Shutdown

The server can be stopped by:
- Sending SIGTERM signal (Ctrl+C)
- The server will gracefully close all connections
