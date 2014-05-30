package com.equalexperts.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

public class FilesystemStackTraceProcessor implements StackTraceProcessor {
    private final Path destination;
    private final ThrowableFingerprintCalculator fingerprintCalculator;

    public FilesystemStackTraceProcessor(Path destination, ThrowableFingerprintCalculator fingerprintCalculator) {
        this.destination = destination;
        this.fingerprintCalculator = fingerprintCalculator;
    }

    @Override
    public void process(Throwable throwable, StringBuilder output) throws Exception {
        Path stackTraceFile = calculateFilenameForException(throwable);
        writeStracktraceToPathIfNecessary(throwable, stackTraceFile);
        printSubstituteMessage(output, throwable, stackTraceFile);
    }

    private void writeStracktraceToPathIfNecessary(Throwable throwable, Path stackTraceFile) throws IOException {
        if (Files.notExists(stackTraceFile)) {
            try(PrintStream out = new PrintStream(Files.newOutputStream(stackTraceFile, CREATE_NEW, WRITE))) {
                throwable.printStackTrace(out);
            } catch (FileAlreadyExistsException ignore) {
                //the exception is being written to (probably right now)
            }
        }
    }

    private void printSubstituteMessage(StringBuilder output, Throwable throwable, Path stackTraceFile) {
        output.append(throwable.getMessage());
        output.append(" (");
        output.append(stackTraceFile.toUri().toString());
        output.append(")");
    }

    private Path calculateFilenameForException(Throwable throwable) {
        String fingerprint = fingerprintCalculator.calculateFingerprint(throwable);
        String filePath = "stacktrace_" + throwable.getClass().getSimpleName() + "_" + fingerprint + ".txt";
        return destination.resolve(filePath);
    }
}
