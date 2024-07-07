package dev.xalphabet.limitedregion;

import dev.xalphabet.limitedregion.listeners.LimitedRegionListeners;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class LimitedRegion extends JavaPlugin {
package dev.xalphabet.limitedregion;

import dev.xalphabet.limitedregion.listeners.LimitedRegionListeners;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

    public class LimitedRegion extends JavaPlugin {

        @Override
        public void onEnable() {

            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            if (!new File(getDataFolder(), "config.yml").exists()) {
                try {
                    copyStreamToFile(getResource("config.yml"), new File(getDataFolder(), "config.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            getLogger().info("LimitedRegion has been enabled.");
            getServer().getPluginManager().registerEvents(new LimitedRegionListeners(this), this);
        }

        public void copyStreamToFile(InputStream source, File destination) throws IOException {
            try (OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = source.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } finally {
                if (source != null) {
                    source.close();
                }
            }
        }

        @Override
        public void onDisable() {
            getLogger().info("LimitedRegion has been disabled.");
        }
    }

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        if (!new File(getDataFolder(), "config.yml").exists()) {
            try {
                copyStreamToFile(getResource("config.yml"), new File(getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        getLogger().info("LimitedRegion has been enabled.");
        getServer().getPluginManager().registerEvents(new LimitedRegionListeners(this), this);
    }

    public void copyStreamToFile(InputStream source, File destination) throws IOException {
        try (OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("LimitedRegion has been disabled.");
    }
}
