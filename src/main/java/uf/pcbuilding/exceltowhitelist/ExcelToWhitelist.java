package uf.pcbuilding.exceltowhitelist;

import org.bukkit.plugin.java.JavaPlugin;

public class ExcelToWhitelist extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.getCommand("excel-to-whitelist").setExecutor(new MainCommand(this, getConfig(), getDataFolder()));
    }
}
