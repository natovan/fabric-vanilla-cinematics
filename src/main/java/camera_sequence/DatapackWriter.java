package camera_sequence;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatapackWriter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String datapacksPath;

    public DatapackWriter(MinecraftServer server) {
        this.datapacksPath = server.getSavePath(WorldSavePath.DATAPACKS).toString();
    }

    public void initDatapack() {
        try {
            Files.createDirectories(Paths.get(this.datapacksPath +
                    "\\CameraSequence\\data\\minecraft\\tags\\functions"));
            File pack = new File(this.datapacksPath + "\\CameraSequence\\pack.mcmeta");
            if (pack.createNewFile()) {
                LOGGER.info("pack.mcmeta created");
            } else {
                LOGGER.warn("pack.mcmeta already exists.");
            }

            File tickJson = new File(this.datapacksPath +
                    "\\CameraSequence\\data\\minecraft\\tags\\functions\\tick.json");
            if (tickJson.createNewFile()) {
                LOGGER.info("tick.json created");
            } else {
                LOGGER.warn("tick.json already exists.");
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while creating datapack files.");
            return;
        }

        try {
            FileWriter writer = new FileWriter(this.datapacksPath + "\\CameraSequence\\pack.mcmeta");
            writer.write("{\"pack\": { \"pack_format\": 3, \"description\": \"Auto generated " +
                    "datapack for camera sequences\" }}");
            writer.close();
            LOGGER.info("Wrote to pack.mcmeta");

            FileWriter tickWriter = new FileWriter(this.datapacksPath +
                    "\\CameraSequence\\data\\minecraft\\tags\\functions\\tick.json");
            tickWriter.write("{\"values\":[\"c:main\"]}");
            tickWriter.close();
            LOGGER.info("Wrote to tick.json");
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing to datapack", e);
        }
    }
}
