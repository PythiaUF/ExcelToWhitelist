package uf.pcbuilding.csvtowhitelist;

import org.bukkit.plugin.java.JavaPlugin;

public class CsvToWhitelist extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.getCommand("csv-to-whitelist").setExecutor(new MainCommand(this, getConfig(), getDataFolder()));
    }
}
