import java.io.FileWriter;
import java.io.IOException;

// ======================= LogLevel =======================
enum LogLevel {
    DEBUG(1), INFO(2), ERROR(3);

    private final int value;

    LogLevel(int value) { this.value = value; }

    public int getValue() { return value; }

    public boolean isGreaterOrEqual(LogLevel other) {
        return this.value >= other.value;
    }
}

// ======================= LogMessage =======================
class LogMessage {
    private final LogLevel level;
    private final String message;
    private final long timestamp;

    public LogMessage(LogLevel level, String message) {
        this.level = level;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "[" + level + "] " + timestamp + " - " + message;
    }
}

// ======================= Strategy: Appender =======================
interface LogAppender {
    void append(LogMessage logMessage);
}

class ConsoleAppender implements LogAppender {
    public void append(LogMessage logMessage) {
        System.out.println(logMessage);
    }
}

class FileAppender implements LogAppender {
    private final String filePath;

    public FileAppender(String filePath) { this.filePath = filePath; }

    public void append(LogMessage logMessage) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(logMessage.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// ======================= Chain of Responsibility =======================
abstract class LogHandler {
    protected LogLevel level;
    protected LogHandler next;
    protected LogAppender appender;

    public LogHandler(LogLevel level, LogAppender appender) {
        this.level = level;
        this.appender = appender;
    }

    public void setNext(LogHandler next) { this.next = next; }

    public void handle(LogLevel logLevel, String message) {
        if (logLevel == this.level) {
            LogMessage logMsg = new LogMessage(logLevel, message);
            appender.append(logMsg);
            write(message);
        } else if (next != null) {
            next.handle(logLevel, message);
        }
    }

    protected abstract void write(String message);
}

class InfoLogger extends LogHandler {
    public InfoLogger(LogAppender appender) { super(LogLevel.INFO, appender); }
    protected void write(String message) { System.out.println("INFO: " + message); }
}

class DebugLogger extends LogHandler {
    public DebugLogger(LogAppender appender) { super(LogLevel.DEBUG, appender); }
    protected void write(String message) { System.out.println("DEBUG: " + message); }
}

class ErrorLogger extends LogHandler {
    public ErrorLogger(LogAppender appender) { super(LogLevel.ERROR, appender); }
    protected void write(String message) { System.out.println("ERROR: " + message); }
}

// ======================= LoggerConfig =======================
class LoggerConfig {
    private LogLevel logLevel;
    private LogAppender appender;

    public LoggerConfig(LogLevel logLevel, LogAppender appender) {
        this.logLevel = logLevel;
        this.appender = appender;
    }

    public LogLevel getLogLevel() { return logLevel; }
    public void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }

    public LogAppender getAppender() { return appender; }
    public void setAppender(LogAppender appender) { this.appender = appender; }
}

// ======================= Singleton Logger =======================
class Logger {
    private static Logger instance;
    private LoggerConfig config;
    private LogHandler chain;

    private Logger(LoggerConfig config) {
        this.config = config;

        // Build chain: INFO → DEBUG → ERROR
        LogHandler info = new InfoLogger(config.getAppender());
        LogHandler debug = new DebugLogger(config.getAppender());
        LogHandler error = new ErrorLogger(config.getAppender());

        info.setNext(debug);
        debug.setNext(error);
        this.chain = info;
    }

    public static Logger getInstance(LoggerConfig config) {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger(config);
                }
            }
        }
        return instance;
    }

    public void setConfig(LoggerConfig config) {
        synchronized (Logger.class) {
            this.config = config;
            // Rebuild chain with new appender
            LogHandler info = new InfoLogger(config.getAppender());
            LogHandler debug = new DebugLogger(config.getAppender());
            LogHandler error = new ErrorLogger(config.getAppender());
            info.setNext(debug);
            debug.setNext(error);
            this.chain = info;
        }
    }

    public void log(LogLevel level, String message) {
        if (level.isGreaterOrEqual(config.getLogLevel())) {
            chain.handle(level, message);
        }
    }

    public void info(String msg) { log(LogLevel.INFO, msg); }
    public void debug(String msg) { log(LogLevel.DEBUG, msg); }
    public void error(String msg) { log(LogLevel.ERROR, msg); }
}

// ======================= Main =======================
public class Main {
    public static void main(String[] args) {

        LoggerConfig config = new LoggerConfig(LogLevel.INFO, new ConsoleAppender());
        Logger logger = Logger.getInstance(config);

        logger.info("Application started");
        logger.debug("Debugging value x=10");  // Won't print (INFO threshold)
        logger.error("Something went wrong");

        // Switch to file logging and lower level
        logger.setConfig(new LoggerConfig(LogLevel.DEBUG, new FileAppender("logs.txt")));
        logger.debug("Debug info saved to file");
        logger.error("Error saved to file");
    }
}
