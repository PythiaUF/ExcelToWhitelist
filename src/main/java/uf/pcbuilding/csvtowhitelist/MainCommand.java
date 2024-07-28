package uf.pcbuilding.csvtowhitelist;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MainCommand implements CommandExecutor {
    public JavaPlugin plugin;
    public FileConfiguration config;
    public File dataFolder;

    MainCommand(JavaPlugin plugin, FileConfiguration config, File dataFolder) {
        this.plugin = plugin;
        this.config = config;
        this.dataFolder = dataFolder;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String fileName = config.getString("fileName");

        CSVReader reader;

        try {
            reader = new CSVReaderBuilder(new FileReader(dataFolder.getAbsoluteFile() + "/" + fileName)).build();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (String[] entry : reader) {
            String username = entry[10];

            if (username.equals("Minecraft Username")) {
                continue;
            }

            UsernameToUUID.getUUID(username.strip(), (uuid -> {
                if (uuid == null) {
                    sender.getServer().getLogger().warning("Could not whitelist " + username + " - invalid username");
                    return;
                }

                sender.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        if (player.isWhitelisted()) {
                            return;
                        }
                        player.setWhitelisted(true);
                        sender.getServer().getLogger().info("Whitelisted " + username);
                    } catch (IllegalArgumentException e) {
                        sender.getServer().getLogger().warning("Could not whitelist " + username);
                    }
                });
            }));
        }

        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sender.sendPlainMessage("Parsed CSV! Whitelisted users will be added in the background.");
        return true;
    }
}
