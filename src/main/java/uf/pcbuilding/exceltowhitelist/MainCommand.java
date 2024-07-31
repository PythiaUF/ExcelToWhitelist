package uf.pcbuilding.exceltowhitelist;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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
        String columnTitle = config.getString("columnTitle");

        FileInputStream file;
        ReadableWorkbook workbook;
        try {
            file = new FileInputStream(new File(dataFolder.getAbsoluteFile() + "/" + fileName));
            workbook = new ReadableWorkbook(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int index = -1;
        List<Row> rows;
        try {
            rows = workbook.getFirstSheet().read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Row firstRow = rows.getFirst();
        for (int i = 0; i < firstRow.getCellCount(); i++) {
            Cell cell = firstRow.getCell(i);
            if (cell.getRawValue().toLowerCase().startsWith(columnTitle.toLowerCase())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new RuntimeException("No entry for Minecraft usernames!");
        }

        boolean isFirstRow = true;

        for (Row row : rows) {
            Cell cell = row.getCell(index);
            String username = cell.getRawValue().strip();

            if (isFirstRow) {
                isFirstRow = false;
                continue;
            }

            UsernameToUUID.getUUID(username, (uuid -> {
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
                        sender.getServer().getLogger().warning("Error when whitelisting " + username);
                    }
                });
            }));
        }

        try {
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sender.sendPlainMessage("Parsed Excel sheet! Whitelisted users will be added in the background.");
        return true;
    }
}
