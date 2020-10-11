package net.teamfruit.playerjoinlog;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Plugin(id = "playerjoinlog",
        name = "PlayerJoinLog",
        version = "${project.version}",
        description = "Log when player join and leave",
        authors = {"Kamesuta"}
)
public class PlayerJoinLog {
    private final ProxyServer server;
    private final Logger logger;

    private SessionBuilder sessionBuilder;
    private Session session;
    private ZoneId timezoneDate = ZoneId.of("Etc/GMT");
    private ZoneId timezone = ZoneId.of("Asia/Tokyo");

    @Inject
    public PlayerJoinLog(ProxyServer server, Logger logger, @DataDirectory Path folder) {
        this.server = server;
        this.logger = logger;

        Toml toml = ConfigUtils.loadConfig(getClass(), folder);
        if (toml == null)
            throw new RuntimeException("Failed to load config.toml.");

        UUID owner = UUID.fromString(toml.getString("owner"));
        File path = new File(folder.toFile(), "data/log/");

        if (!path.isDirectory() && !path.mkdirs())
            throw new RuntimeException("Failed to create directory.");

        sessionBuilder = new SessionBuilder(owner, path);

        logger.info("Plugin has enabled!");
    }

    @Subscribe
    public void onExit(DisconnectEvent event) {
        onStateUpdate(event.getPlayer(), null);
    }

    @Subscribe
    public void onMove(ServerConnectedEvent event) {
        onStateUpdate(event.getPlayer(), event.getServer().getServerInfo().getName());
    }

    public void onStateUpdate(Player member, String after) {
        try {
            if (sessionBuilder.isOwner(member)) {
                // 主

                // 退出
                if (session != null && !session.isTarget(after)) {
                    LocalDateTime now = LocalDateTime.now(timezone);

                    // 残っている人
                    for (Player m : server.getServer(session.server).map(RegisteredServer::getPlayersConnected).orElseGet(Collections::emptyList))
                        if (!Objects.equals(m, member))
                            session.log.log(now, m, false);

                    // 主
                    session.log.log(now, member, false);

                    session = sessionBuilder.end(session);
                }

                // 参加
                if (after != null) {
                    LocalDateTime now = LocalDateTime.now(timezone);
                    LocalDate nowDate = LocalDate.now(timezoneDate);

                    session = sessionBuilder.end(session);
                    session = sessionBuilder.begin(after, nowDate);

                    // 主
                    session.log.log(now, member, true);

                    // 既にいる人
                    for (Player m : server.getServer(after).map(RegisteredServer::getPlayersConnected).orElseGet(Collections::emptyList))
                        if (!Objects.equals(m, member))
                            session.log.log(now, m, true);
                }
            } else if (session != null) {
                // 参加勢

                LocalDateTime now = LocalDateTime.now(timezone);

                // 参加
                if (session.isTarget(after)) {
                    session.log.log(now, member, true);
                }

                // 退出
                else {
                    session.log.log(now, member, false);
                }
            }
        } catch (IOException e) {
            logger.warn("Log failed.", e);
        }
    }
}