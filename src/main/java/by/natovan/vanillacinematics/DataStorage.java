package by.natovan.vanillacinematics;


import by.natovan.vanillacinematics.sequence.NodeSequence;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * I don't really know what I'm doing for now, I just like name DataStorage
 * I will store data per world for now as it is not very heavy
 */
public class DataStorage {
    public static final DataStorage INSTANCE = new DataStorage();
    public static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final String dataFolderName = VanillaCinematics.UNDERSCORE_MODID;
    private String currentWorldPath;

    public DataStorage() {}

    public int write() {
        if (this.mc == null || this.mc.getServer() == null) {
            return 0;
        }
        currentWorldPath = this.mc.getServer().getSavePath(WorldSavePath.ROOT).toString();
        File path = new File(currentWorldPath + "/" + this.dataFolderName);
        if ((path.exists() && path.isDirectory()) || path.mkdirs()) {
            JsonObject root = new JsonObject();
            JsonArray sequences = new JsonArray();

            for (NodeSequence seq : VanillaCinematics.sequences) {
                sequences.add(seq.toJson());
            }
            root.add("sequences", sequences);
            File sequencesFile = new File(path, "sequences.json");
            if (JsonUtils.writeJsonToFile(root, sequencesFile)) {
                LOGGER.info("Successfully wrote to sequences.json");
            } else {
                LOGGER.error("Error while writing to sequences.json");
                return 0;
            }
        }
        return 1;
    }

    public int load() {
        if (this.mc == null || this.mc.getServer() == null) {
            return 0;
        }
        currentWorldPath = this.mc.getServer().getSavePath(WorldSavePath.ROOT).toString();
        // there is a dot at the end of path, I don't know where it came from
        currentWorldPath = currentWorldPath.substring(0, currentWorldPath.length() - 2);

        File path = new File(currentWorldPath + "\\" + this.dataFolderName + "\\sequences.json");
        JsonElement element = JsonUtils.parseJsonFile(path);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            JsonArray sequences = object.get("sequences").getAsJsonArray();
            VanillaCinematics.sequences.clear();
            for (JsonElement e : sequences) {
                VanillaCinematics.sequences.add(new NodeSequence(e.getAsJsonObject()));
            }
            LOGGER.info("Successfully read from sequences.json");
            return 1;
        } else {
            LOGGER.error("Error while reading from sequences.json");
            return 0;
        }
    }

    public void unload() {}

    public void deleteData() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            File path = new File(currentWorldPath + "/" + this.dataFolderName);
            String dataPath = path.getPath();
            try {
                FileUtils.deleteDirectory(new File(dataPath));
            } catch (IOException e) {
                LOGGER.error("An error occurred while deleting data", e);
            }
        }
    }
}
