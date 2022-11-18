package by.natovan.vanillacinematics;

import by.natovan.vanillacinematics.sequence.Node;
import by.natovan.vanillacinematics.sequence.NodeSequence;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// todo: write to tmp files first
public class DatapackWriter {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DatapackWriter INSTANCE = new DatapackWriter();
    private boolean wasInitiated = false;

    public DatapackWriter() {}

    public void initDatapack() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server == null) return;
        String datapacksPath = server.getSavePath(WorldSavePath.DATAPACKS).toString();

        try {
            Files.createDirectories(Paths.get(datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions"));
            File tickFile = new File(datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\tick.json");
            File loadFile = new File(datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\load.json");
            File packFile = new File(datapacksPath + "\\VanillaCinematics\\pack.mcmeta");
            Files.createDirectories(Paths.get(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics"));
            File loadMcFuncFile = new File(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\load.mcfunction");
            File mainMcFuncFile = new File(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\main.mcfunction");
            if (tickFile.createNewFile() &&
                    loadFile.createNewFile() &&
                    packFile.createNewFile() &&
                    loadMcFuncFile.createNewFile() &&
                    mainMcFuncFile.createNewFile()) {
                LOGGER.info("Initial datapack files created");
            } else {
                LOGGER.info("Some initial datapack files already exist. That's fine");
            }

            FileWriter packWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\pack.mcmeta");
            packWriter.write("{\"pack\":{\"pack_format\": 3, \"description\":" + "\"Auto generated datapack for camera sequences\"}}");
            packWriter.close();

            FileWriter tickWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\tick.json");
            tickWriter.write("{\"values\":[\"vanilla_cinematics:main\"]}");
            tickWriter.close();

            FileWriter loadWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\load.json");
            loadWriter.write("{\"values\":[\"vanilla_cinematics:load\"]}");
            loadWriter.close();

            FileWriter mainFuncWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\main.mcfunction");
            mainFuncWriter.write("execute if score @a[limit=1] in_sequence matches 1 run spectate @e[tag=current_sequence_node, limit=1] @a[limit=1]\n");
            mainFuncWriter.close();

            FileWriter loadFuncWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\load.mcfunction");
            loadFuncWriter.write("tellraw @a {\"text\":\"Vanilla Cinematics datapack loaded\",\"color\":\"#FFD866\"}");
            loadFuncWriter.close();
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing to datapack", e);
        }
        wasInitiated = true;
    }

    // TODO: not perfect positioning, fix it
    public int writeSequence(NodeSequence s) {
        if (!wasInitiated) initDatapack();

        final MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server == null) return 0;
        final String datapacksPath = server.getSavePath(WorldSavePath.DATAPACKS).toString();
        final String seqName = s.getSequenceName();

        try {
            // create folder for cinematic
            Files.createDirectories(Paths.get(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName));

            // start.mcfunction
            File startMcFuncFile = new File(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName + "\\start.mcfunction");
            if (startMcFuncFile.createNewFile()) {
                LOGGER.info("start.mcfunction created");
            } else {
                LOGGER.info("start.mcfunction already exists");
            }
            FileWriter startMcFuncWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName + "\\start.mcfunction");
            startMcFuncWriter.write(
                    """
                    execute at @a run summon minecraft:area_effect_cloud ~ ~ ~ {Tags:['sequence_%1$s', 'sequence_node_player_pos'], Radius:0, Duration:216374}
                    execute at @a run summon minecraft:area_effect_cloud ~ ~ ~ {Tags:['sequence_%1$s', 'sequence_node_player_looking_at'], Radius:0, Duration:216374}
                    execute at @a run tp @e[tag=sequence_%1$s, tag=sequence_node_player_looking_at] ^ ^ ^4
                    gamemode spectator @a
                    scoreboard objectives add in_sequence dummy
                    scoreboard players set @a[limit=1] in_sequence 1
                    function vanilla_cinematics:cinematics/%1$s/node0""".
                    formatted(seqName));
            startMcFuncWriter.close();

            // nodes
            int nodeIndex = 0;
            for (; nodeIndex < s.getCameraNodes().size(); nodeIndex++) {
                Node n = s.getCameraNodes().get(nodeIndex);
                File nodeMcFuncFile = new File(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName + "\\node" + nodeIndex + ".mcfunction");
                if (nodeMcFuncFile.createNewFile()) {
                    LOGGER.info("node " + nodeIndex + ".mcfunction created");
                } else {
                    LOGGER.info("node" + nodeIndex + ".mcfunction already exists");
                }

                FileWriter nodeWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName + "\\node" + nodeIndex + ".mcfunction");
                if (nodeIndex > 0) nodeWriter.write("tag @e[tag=sequence_node_%d] remove current_sequence_node\n".formatted(nodeIndex - 1));
                nodeWriter.write("summon minecraft:armor_stand %f %f %f {Rotation:[%ff, %ff], Invisible:1, NoGravity:1, Tags:['sequence_%s', 'sequence_node_%d', 'current_sequence_node']}\n".formatted(n.getStandPos().x, n.getStandPos().y, n.getStandPos().z, n.getYaw(), n.getPitch(), seqName, nodeIndex));
                nodeWriter.write("spectate @e[tag=current_sequence_node, limit=1] @a[limit=1]\n");
                if (n.getCommand() != null) nodeWriter.write(n.getCommand() + "\n");
                if (nodeIndex > 0) nodeWriter.write("kill @e[tag=sequence_%s, tag=sequence_node_%d]\n".formatted(seqName, nodeIndex - 1));
                if (nodeIndex == s.getCameraNodes().size() - 1) {
                    nodeWriter.write("schedule function vanilla_cinematics:cinematics/%s/preend %dt".formatted(seqName, n.getDelay()));
                } else {
                    nodeWriter.write("schedule function vanilla_cinematics:cinematics/%s/node%d %dt".formatted(seqName, nodeIndex + 1, n.getDelay()));
                }
                nodeWriter.close();
            }

            // end
            File endMcFuncFile = new File(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName + "\\end.mcfunction");
            if (endMcFuncFile.createNewFile()) {
                LOGGER.info("end.mcfunction created");
            } else {
                LOGGER.info("end.mcfunction already exist");
            }
            FileWriter endWriter = new FileWriter(datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + seqName + "\\preend.mcfunction");
            endWriter.write(
                """
                kill @e[tag=sequence_%1$s, tag=sequence_node_%2$d]
                scoreboard players set @a[limit=1] in_sequence 0
                execute at @a run tp @a @e[tag=sequence_%s, tag=sequence_node_player_pos, limit=1]
                execute at @a run tp @a ~ ~ ~ facing entity @e[tag=sequence_%1$s, tag=sequence_node_player_looking_at, limit=1]
                gamemode adventure @a
                kill @e[tag=sequence_%1$s, tag=sequence_node_player_pos]
                kill @e[tag=sequence_%1$s, tag=sequence_node_player_looking_at]""".
                formatted(seqName, nodeIndex - 1));
            endWriter.close();
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing new sequence to datapack", e);
            return 0;
        }
        return 1;
    }

    public void deleteDatapack() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            String datapacksPath = server.getSavePath(WorldSavePath.DATAPACKS).toString();
            try {
                FileUtils.deleteDirectory(new File(datapacksPath + "\\VanillaCinematics"));
                wasInitiated = false;
            } catch (IOException e) {
                LOGGER.error("An error occurred while deleting datapack", e);
            }
        }
    }
}
