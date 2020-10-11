package net.teamfruit.playerjoinlog;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class Session {
    public final UUID owner;
    public final File path;
    public final String server;
    public final Log log;

    public Session(UUID owner, File path, String server, Log log) {
        this.server = server;
        this.path = path;
        this.owner = owner;
        this.log = log;
    }

    public boolean isTarget(String server) {
        return Objects.equals(this.server, server);
    }
}
