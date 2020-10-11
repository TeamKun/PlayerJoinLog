package net.teamfruit.playerjoinlog;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtils {
    public static Toml loadConfig(Class<?> resourcePath, Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                InputStream input = resourcePath.getResourceAsStream("/" + file.getName());
                Throwable t = null;

                try {
                    if (input != null) {
                        Files.copy(input, file.toPath());
                    } else {
                        file.createNewFile();
                    }
                } catch (Throwable e) {
                    t = e;
                    throw e;
                } finally {
                    if (input != null) {
                        if (t != null) {
                            try {
                                input.close();
                            } catch (Throwable ex) {
                                t.addSuppressed(ex);
                            }
                        } else {
                            input.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return (new Toml()).read(file);
    }
}
