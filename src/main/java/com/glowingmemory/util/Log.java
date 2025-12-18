package com.glowingmemory.util;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Log {
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object LOCK = new Object();

    private static PrintWriter writer;
    private static JTextArea textArea;

    private Log() {}

    public static void init() {
        synchronized (LOCK) {
            if (writer != null) return;
            try {
                Path logsDir = Path.of("logs");
                Files.createDirectories(logsDir);
                String fileName = LocalDateTime.now().format(FILE_FORMATTER) + ".log";
                Path logFile = logsDir.resolve(fileName);
                writer = new PrintWriter(Files.newBufferedWriter(logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
                createWindow();
                info("Logging initialized. Writing to " + logFile.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void info(String message) {
        log("INFO", message, null);
    }

    public static void warn(String message) {
        log("WARN", message, null);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }

    private static void log(String level, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
        String line = "[" + timestamp + "][" + level + "] " + message;

        System.out.println(line);
        synchronized (LOCK) {
            if (writer != null) {
                writer.println(line);
                if (throwable != null) {
                    throwable.printStackTrace(writer);
                }
                writer.flush();
            }
        }
        appendToWindow(line, throwable);
    }

    private static void createWindow() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Glowing Memory Log");
            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.add(scrollPane);
            frame.setSize(800, 600);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private static void appendToWindow(String line, Throwable throwable) {
        if (textArea == null) return;
        SwingUtilities.invokeLater(() -> {
            textArea.append(line).append("\n");
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                textArea.append(sw.toString());
            }
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
