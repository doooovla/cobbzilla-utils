package org.cobbzilla.util.system;

import org.apache.commons.exec.*;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.string.StringUtil.UTF8;

public class CommandShell {

    protected static final String EXPORT_PREFIX = "export ";

    public static Map<String, String> loadShellExports (String userFile) throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + userFile);
        if (!file.exists()) {
            throw new IllegalArgumentException("file does not exist: "+file.getAbsolutePath());
        }
        return loadShellExports(file);
    }

    public static Map<String, String> loadShellExports (File f) throws IOException {
        final Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line, key, value;
            int eqPos;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue;
                if (line.startsWith(EXPORT_PREFIX)) {
                    line = line.substring(EXPORT_PREFIX.length()).trim();
                    eqPos = line.indexOf('=');
                    if (eqPos != -1) {
                        key = line.substring(0, eqPos).trim();
                        value = line.substring(eqPos+1).trim();
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    public static MultiCommandResult exec (Collection<String> commands) throws IOException {
        final MultiCommandResult result = new MultiCommandResult();
        for (String command : commands) {
            exec(command, result);
            if (result.hasException()) return result;
        }
        result.setSuccess(true);
        return result;
    }

    public static CommandResult exec (String command) throws IOException {
        MultiCommandResult result = exec(command, null);
        return result.getResults().values().iterator().next();
    }

    public static MultiCommandResult exec (String command, MultiCommandResult result) throws IOException {
        if (result == null) result = new MultiCommandResult();
        final CommandLine cmdLine = CommandLine.parse(command);
        final DefaultExecutor executor = new DefaultExecutor();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final ExecuteStreamHandler handler = new PumpStreamHandler(out, err);
        executor.setStreamHandler(handler);
        final int exitValue;
        try {
            exitValue = executor.execute(cmdLine);
            if (exitValue != 0) {
                result.exception(command, new IllegalStateException("non-zero value ("+exitValue+") returned from command: "+command));
                return result;
            }
            result.add(command, new CommandResult(exitValue, out.toString(UTF8), err.toString(UTF8)));

        } catch (Exception e) {
            result.exception(command, e);
        }
        return result;
    }

    public static int chmod (File file, String perms) throws IOException {
        return chmod(file.getAbsolutePath(), perms);
    }

    public static int chmod (String file, String perms) throws IOException {
        CommandLine commandLine = new CommandLine("chmod");
        commandLine.addArgument(perms);
        commandLine.addArgument(file);
        Executor executor = new DefaultExecutor();
        return executor.execute(commandLine);
    }

}