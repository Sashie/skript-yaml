package me.sashie.skriptyaml.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker implements Listener {

    private final JavaPlugin plugin;
    private final String currentVersion;
    private String latestVersion;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        checkForUpdate();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("skriptyaml.update.check") && latestVersion != null) {
            if (isVersionOutdated(currentVersion.replaceAll("[^0-9.]", ""), latestVersion)) {
                player.sendMessage(" ");
                player.sendRichMessage("<dark_gray>[<red>Skript<white>-<blue>Yaml<dark_gray>] <white>Skript-Yaml is <red><bold>OUTDATED</bold><white>!");
                player.sendRichMessage("<dark_gray>[<red>Skript<white>-<blue>Yaml<dark_gray>] <white>New version: <green>" + latestVersion);
                player.sendRichMessage("<dark_gray>[<red>Skript<white>-<blue>Yaml<dark_gray>] <white>Download: <green><click:open_url:https://github.com/Sashie/Skript-Yaml/releases><hover:show_text:'<green>Click here to get the latest version!'>here<white>!</click>");
                player.sendMessage(" ");
            }
        }
    }

    private void checkForUpdate() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/Sashie/Skript-Yaml/releases/latest"))
                .build();

        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        responseFuture.thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    JsonObject jsonResponse = new Gson().fromJson(body, JsonObject.class);
                    if (jsonResponse != null && jsonResponse.has("tag_name")) {
                        latestVersion = jsonResponse.get("tag_name").getAsString().replaceAll("[^0-9.]", "");

                        if (isVersionOutdated(currentVersion.replaceAll("[^0-9.]", ""), latestVersion)) {
                            Utilities.log("&cSkript-Yaml is not up to date!");
                            Utilities.log("&f - Current version: &cv" + currentVersion);
                            Utilities.log("&f - Available update: &a" + latestVersion);
                            Utilities.log("&f - Download available at: &ahttps://github.com/Sashie/Skript-Yaml/releases");
                        } else {
                            Utilities.log("&aSkript-Yaml is up to date!");
                        }
                    } else {
                        Bukkit.getLogger().severe("Failed to check for updates: Unexpected JSON format.");
                    }
                })
                .exceptionally(e -> {
                    Bukkit.getLogger().severe("Failed to check for updates: " + e.getMessage());
                    return null;
                });
    }

    private boolean isVersionOutdated(String current, String latest) {
        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");
        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            int currentVersion = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int latestVersion = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

            if (currentVersion < latestVersion) {
                return true;
            } else if (currentVersion > latestVersion) {
                return false;
            }
        }
        return false;
    }
}
