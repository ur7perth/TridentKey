package com.autotrident;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AutoTridentConfig {
    /** زر فتح القائمة (ثابت ولا يظهر في Controls) */
    public static final int OPEN_MENU_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    public static boolean enabled = true;
    public static int key = GLFW.GLFW_KEY_R;

    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("autotrident.properties");

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(FILE)) {
                    p.load(in);
                }
                enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
                key = Integer.parseInt(p.getProperty("key", String.valueOf(GLFW.GLFW_KEY_R)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("enabled", String.valueOf(enabled));
            p.setProperty("key", String.valueOf(key));
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Auto Trident");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
