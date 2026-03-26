# Logging System

A flexible, production-ready logging framework for Java applications that combines multiple design patterns to provide powerful logging capabilities with minimal overhead.

## Overview

This project implements a comprehensive logging system that demonstrates several key design patterns:
- **Strategy Pattern**: Multiple logging output strategies (Console, File)
- **Chain of Responsibility**: Handler chain for processing logs at different levels
- **Singleton Pattern**: Thread-safe logger instances with configuration management

## Features

- **Multiple Log Levels**: DEBUG, INFO, and ERROR levels with configurable filtering
- **Flexible Appenders**: Log to console or files with extensible interface
- **Thread-Safe Singleton**: `ConcurrentHashMap`-based logger instances
- **Chain of Responsibility**: Hierarchical log handler processing
- **Timestamps**: Automatic timestamp tracking for all log messages
- **Configurable**: Dynamic configuration changes at runtime

## Architecture

### Core Components

#### LogLevel Enum
Defines log severity levels with integer values for comparison:
- `DEBUG` (1) - Detailed diagnostic information
- `INFO` (2) - General informational messages
- `ERROR` (3) - Error-level messages

#### LogMessage
Encapsulates log data:
- Log level
- Message text
- Timestamp (milliseconds)

#### LogAppender (Strategy Pattern)
Interface for different output strategies:
- **ConsoleAppender**: Outputs logs to standard output
- **FileAppender**: Writes logs to a file

#### LogHandler (Chain of Responsibility)
Abstract handler supporting chained log processing:
- `InfoLogger`, `DebugLogger`, `ErrorLogger` implementations
- Filters messages by log level
- Delegates to next handler if not handled

#### Logger (Singleton Pattern)
Thread-safe singleton with configuration management:
- Per-instance configuration
- Support for multiple appenders via factory method
- Convenience methods: `debug()`, `info()`, `error()`

#### LoggerConfig
Configuration holder for:
- Current log level
- Active appender

## Usage

### Basic Example

```java
// Create appenders
LogAppender consoleAppender = new ConsoleAppender();
LogAppender fileAppender = new FileAppender("logs.txt");

// Build handler chain
LogHandler loggerChain = getChainOfLoggers(consoleAppender);

// Log messages
loggerChain.logMessage(LogHandler.INFO, "Application started");
loggerChain.logMessage(LogHandler.DEBUG, "Debug information");
loggerChain.logMessage(LogHandler.ERROR, "An error occurred");
