package net.teamfruit.playerjoinlog;

import com.velocitypowered.api.proxy.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

// セッション
public class SessionBuilder {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public final UUID owner;
    public final File path;

    public SessionBuilder(UUID owner, File path) {
        this.owner = owner;
        this.path = path;
    }

    public Session begin(String server, LocalDate time) throws IOException {
        File f = new File(path, String.format("%s-%s.csv", formatter.format(time), server));
        return new Session(this.owner, f, server, new Log(f));
    }

    public Session end(Session session) {
        if (session != null) {
            try {
                session.log.close();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public boolean isOwner(Player owner) {
        return Objects.equals(this.owner, owner.getUniqueId());
    }
}
