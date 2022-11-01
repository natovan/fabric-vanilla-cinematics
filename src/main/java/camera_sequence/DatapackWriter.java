package camera_sequence;

import camera_sequence.sequence.Node;
import camera_sequence.sequence.NodeSequence;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatapackWriter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String datapacksPath;
    private boolean wasInitiated = false;

    public DatapackWriter(MinecraftServer server) {
        this.datapacksPath = server.getSavePath(WorldSavePath.DATAPACKS).toString();
    }

    public boolean initDatapack() {
        try {
            Files.createDirectories(Paths.get(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions"));
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\tick.json"));
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\load.json"));
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\pack.mcmeta"));

            Files.createDirectories(Paths.get(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics"));
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\load.mcfunction"));
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\main.mcfunction"));
        } catch (IOException e) {
            LOGGER.error("An error occurred while creating initial datapack files", e);
            return false;
        }

        try {
            FileWriter packWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\pack.mcmeta");
            packWriter.write("{\"pack\":{\"pack_format\": 3, \"description\":" + "\"Auto generated datapack for camera sequences\"}}");
            packWriter.close();

            FileWriter tickWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\tick.json");
            tickWriter.write("{\"values\":[\"vanilla_cinematics:main\"]}");
            tickWriter.close();

            FileWriter loadWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\load.json");
            loadWriter.write("{\"values\":[\"vanilla_cinematics:load\"]}");
            loadWriter.close();

            FileWriter mainFuncWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\main.mcfunction");
            mainFuncWriter.write("execute if score @a[limit=1] in_sequence matches 1 run spectate @e[tag=current_sequence_node, limit=1] @a[limit=1]");
            mainFuncWriter.close();

            FileWriter loadFuncWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\load.mcfunction");
            loadFuncWriter.write("tellraw @a {\"text\":\"Vanilla Cinematics datapack loaded\",\"color\":\"#FFD866\"}");
            loadFuncWriter.close();
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing to datapack", e);
            return false;
        }
        wasInitiated = true;
        return true;
    }

    // TODO: not perfect positioning, fix it
    // TODO: hide return armor stand
    public void writeSequence(NodeSequence sequence) {
        if (!wasInitiated) {
            if (!initDatapack()) {
                LOGGER.error("Failed to create datapack");
            } else {
                LOGGER.info("Empty datapack created");
            }
        }

        try {
            // create folder for cinematic
            Files.createDirectories(Paths.get(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName()));

            // start.mcfunction
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\start.mcfunction"));
            FileWriter startMcFuncWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\start.mcfunction");
            startMcFuncWriter.write(String.format("execute at @a run summon minecraft:armor_stand ~ ~ ~ {Invisible: 1, NoGravity:1, Tags:['sequence_%s', 'sequence_node_player_pos']}\n", sequence.getSequenceName()));
            startMcFuncWriter.write(String.format("execute at @a run tp @e[tag=sequence_%s, tag=sequence_node_player_pos] ~ ~ ~ ~ ~\n", sequence.getSequenceName()));
            startMcFuncWriter.write("gamemode spectator @a\n");
            startMcFuncWriter.write("scoreboard objectives add in_sequence dummy\n");
            startMcFuncWriter.write("scoreboard players set @a[limit=1] in_sequence 1\n");
            startMcFuncWriter.write(String.format("function vanilla_cinematics:cinematics/%s/node0", sequence.getSequenceName()));
            startMcFuncWriter.close();

            // nodes
            int i = 0;
            for (int c = 0; c < sequence.getCameraNodes().size(); c++) {
                Node n = sequence.getCameraNodes().get(c);
                Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\node" + i + ".mcfunction"));
                FileWriter nodeWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\node" + i + ".mcfunction");
                if (c > 0) nodeWriter.write(String.format("tag @e[tag=sequence_node_%d] remove current_sequence_node\n", i - 1));
                nodeWriter.write(String.format("summon minecraft:armor_stand %f %f %f {Rotation:[%ff, %ff], Invisible:1, NoGravity:1, Tags:['sequence_%s', 'sequence_node_%d', 'current_sequence_node']}\n", n.getPos().x, n.getPos().y, n.getPos().z, n.getYaw(), n.getPitch(), sequence.getSequenceName(), i));
                nodeWriter.write("spectate @e[tag=current_sequence_node, limit=1] @a[limit=1]\n");
                if (c > 0) nodeWriter.write(String.format("kill @e[tag=sequence_%s, tag=sequence_node_%d]\n", sequence.getSequenceName(), i - 1));
                // if last node, route to preend function
                if (c == sequence.getCameraNodes().size() - 1) {
                    nodeWriter.write(String.format("schedule function vanilla_cinematics:cinematics/%s/preend %dt", sequence.getSequenceName(), n.getDelay()));
                    nodeWriter.close();
                    break;
                } else {
                    nodeWriter.write(String.format("schedule function vanilla_cinematics:cinematics/%s/node%d %dt", sequence.getSequenceName(), i + 1, n.getDelay()));
                }
                nodeWriter.close();
                i++;
            }

            // preend
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\preend.mcfunction"));
            FileWriter preendWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\preend.mcfunction");
            preendWriter.write(String.format("kill @e[tag=sequence_%s, tag=sequence_node_%d]\n", sequence.getSequenceName(), i));
            preendWriter.write("scoreboard players set @a[limit=1] in_sequence 0\n");
            preendWriter.write(String.format("spectate @e[tag=sequence_%s, tag=sequence_node_player_pos, limit=1] @a[limit=1]\n", sequence.getSequenceName()));
            preendWriter.write(String.format("schedule function vanilla_cinematics:cinematics/%s/end 5t", sequence.getSequenceName()));
            preendWriter.close();

            // end
            Files.createFile(Path.of(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\end.mcfunction"));
            FileWriter endWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + sequence.getSequenceName() + "\\end.mcfunction");
            endWriter.write("gamemode adventure @a\n");
            endWriter.write(String.format("kill @e[tag=sequence_%s, tag=sequence_node_player_pos]", sequence.getSequenceName()));
            endWriter.close();


        } catch (IOException e) {
            LOGGER.error("An error occurred while writing new sequence to datapack", e);
        }
    }

    public boolean deleteDatapack() {
        try {
            FileUtils.deleteDirectory(new File(this.datapacksPath + "\\VanillaCinematics"));
            return true;
        } catch (IOException e) {
            LOGGER.error("An error occurred while deleting datapack", e);
            return false;
        }
    }
}
