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

### Configuration Management:
- **ServerConfig**: Centralized server configuration with builder pattern
- **MessageFactoryManager**: Manages ISO 8583 message factory initialization and validation

### Processing Components:
- **MessageProcessor**: Handles ISO 8583 message parsing and response creation
- **HeaderStripper**: Removes 5-byte headers from incoming messages
- **MessageLogger**: Provides comprehensive logging of received and sent messages
- **MessageSender**: Manages sending responses to clients
- **MTI Processors**: Template pattern implementation for different message types
  - **AuthorizationProcessor**: Handles MTI 0100 (Authorization requests)
  - **FinancialProcessor**: Handles MTI 0200 (Financial transactions)
  - **NetworkManagementProcessor**: Handles MTI 0800 (Network management)
  - **ProcessorFactory**: Factory pattern for processor management

### Benefits:
- ✅ **Single Responsibility**: Each class has one clear purpose
- ✅ **Testability**: Components can be tested independently
- ✅ **Maintainability**: Easy to modify or extend individual components
- ✅ **Readability**: Clear separation of concerns makes code easier to understand
- ✅ **Custom Responses**: Processors can return custom IsoMessage responses with specific fields
- ✅ **Template Pattern**: Consistent processing flow with customizable business logic
- ✅ **Configuration Management**: Centralized configuration with builder pattern
- ✅ **Error Handling**: Improved error handling and recovery mechanisms

## Configuration

The server uses the `config_titp_simple.xml` file for ISO 8583 message format configuration. This file defines:

- Message headers for different MTIs
- Field definitions and types (NUMERIC, ALPHA, LLVAR, BINARY, etc.)
- Message parsing rules for each MTI type

## Message Types Supported

### Processed with Template Pattern:
- **0100/0110**: Authorization request/response
  - Validates PAN, processing code, and amount
  - Simulates authorization logic with fraud detection
  - Supports purchase, refund, and withdrawal transactions
  - **Custom Response**: Returns authorization ID and approval status
- **0200/0210**: Financial request/response  
  - Processes purchase, refund, and cash withdrawal transactions
  - Validates transaction amounts and merchant information
  - Implements transaction limits and business rules
- **0800/0810**: Network management request/response
  - Handles sign-on, sign-off, echo test, and cutover requests
  - Validates transmission date/time
  - Supports network connectivity management

### Supported but using Default Processing:
- **0400/0410**: Reversal request/response
- **0420/0421**: Reversal request/response
- **0430**: Reversal request/response
- **0500/0510**: Reconciliation request/response

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
