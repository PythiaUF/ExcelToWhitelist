package uf.pcbuilding.exceltowhitelist;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
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

        FileInputStream file;
        Workbook workbook;
        try {
            file = new FileInputStream(new File(dataFolder.getAbsoluteFile() + "/" + fileName));
            workbook = new XSSFWorkbook(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int index = -1;
        Sheet sheet = workbook.getSheetAt(0);

        Row firstRow = sheet.getRow(0);
        for (int i = 1; i < firstRow.getLastCellNum(); i++) {
            Cell cell = firstRow.getCell(i);
            if (cell.getStringCellValue().equalsIgnoreCase("Minecraft Username")) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new RuntimeException("No entry for Minecraft usernames!");
        }

        for (Row row : sheet) {
            Cell cell = row.getCell(index);
            String username = cell.getStringCellValue();

            if (username.equalsIgnoreCase("Minecraft Username")) {
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
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sender.sendPlainMessage("Parsed Excel sheet! Whitelisted users will be added in the background.");
        return true;
    }
}
