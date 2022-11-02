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
import java.nio.file.Paths;

public class DatapackWriter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String datapacksPath;
    private boolean wasInitiated = false;

    public DatapackWriter(MinecraftServer server) {
        this.datapacksPath = server.getSavePath(WorldSavePath.DATAPACKS).toString();
    }

    public void initDatapack() {
        try {
            Files.createDirectories(Paths.get(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions"));
            File tickFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\tick.json");
            File loadFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\minecraft\\tags\\functions\\load.json");
            File packFile = new File(this.datapacksPath + "\\VanillaCinematics\\pack.mcmeta");
            Files.createDirectories(Paths.get(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics"));
            File loadMcFuncFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\load.mcfunction");
            File mainMcFuncFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\main.mcfunction");
            if (tickFile.createNewFile() &&
                    loadFile.createNewFile() &&
                    packFile.createNewFile() &&
                    loadMcFuncFile.createNewFile() &&
                    mainMcFuncFile.createNewFile()) {
                LOGGER.info("Initial datapack files created");
            } else {
                LOGGER.info("Some initial datapack files already exist. That's fine");
            }

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
        }
        wasInitiated = true;
    }

    // TODO: not perfect positioning, fix it
    // TODO: hide return armor stand
    public void writeSequence(NodeSequence s) {
        if (!wasInitiated) initDatapack();

        try {
            // create folder for cinematic
            Files.createDirectories(Paths.get(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName()));

            // start.mcfunction
            File startMcFuncFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\start.mcfunction");
            if (startMcFuncFile.createNewFile()) {
                LOGGER.info("start.mcfunction created");
            } else {
                LOGGER.info("start.mcfunction already exists");
            }
            FileWriter startMcFuncWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\start.mcfunction");
            startMcFuncWriter.write(
                    """
                    execute at @a run summon minecraft:armor_stand ~ ~ ~ {Invisible: 1, NoGravity:1, Tags:['sequence_%s', 'sequence_node_player_pos']}
                    execute at @a run tp @e[tag=sequence_%s, tag=sequence_node_player_pos] ~ ~ ~ ~ ~
                    gamemode spectator @a
                    scoreboard objectives add in_sequence dummy
                    scoreboard players set @a[limit=1] in_sequence 1
                    function vanilla_cinematics:cinematics/%s/node0""".
                    formatted(s.getSequenceName(), s.getSequenceName(), s.getSequenceName()));
            startMcFuncWriter.close();

            // nodes
            int c = 0;
            for (; c < s.getCameraNodes().size(); c++) {
                Node n = s.getCameraNodes().get(c);
                File nodeMcFuncFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\node" + c + ".mcfunction");
                if (nodeMcFuncFile.createNewFile()) {
                    LOGGER.info("node " + c + ".mcfunction created");
                } else {
                    LOGGER.info("node" + c + ".mcfunction already exists");
                }

                FileWriter nodeWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\node" + c + ".mcfunction");
                if (c > 0) nodeWriter.write("tag @e[tag=sequence_node_%d] remove current_sequence_node\n".formatted(c - 1));
                nodeWriter.write("summon minecraft:armor_stand %f %f %f {Rotation:[%ff, %ff], Invisible:1, NoGravity:1, Tags:['sequence_%s', 'sequence_node_%d', 'current_sequence_node']}\n".formatted(n.getPos().x, n.getPos().y, n.getPos().z, n.getYaw(), n.getPitch(), s.getSequenceName(), c));
                nodeWriter.write("spectate @e[tag=current_sequence_node, limit=1] @a[limit=1]\n");
                if (c > 0) nodeWriter.write("kill @e[tag=sequence_%s, tag=sequence_node_%d]\n".formatted(s.getSequenceName(), c - 1));
                if (c == s.getCameraNodes().size() - 1) {
                    nodeWriter.write("schedule function vanilla_cinematics:cinematics/%s/preend %dt".formatted(s.getSequenceName(), n.getDelay()));
                } else {
                    nodeWriter.write("schedule function vanilla_cinematics:cinematics/%s/node%d %dt".formatted(s.getSequenceName(), c + 1, n.getDelay()));
                }
                nodeWriter.close();
            }

            // preend
            File preendMcFuncFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\preend.mcfunction");
            if (preendMcFuncFile.createNewFile()) {
                LOGGER.info("preend.mcfunction created");
            } else {
                LOGGER.info("preend.mcfuncton already exist");
            }
            FileWriter preendWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\preend.mcfunction");
            preendWriter.write(
                """
                kill @e[tag=sequence_%s, tag=sequence_node_%d]
                scoreboard players set @a[limit=1] in_sequence 0
                spectate @e[tag=sequence_%s, tag=sequence_node_player_pos, limit=1] @a[limit=1]
                schedule function vanilla_cinematics:cinematics/%s/end 5t""".
                formatted(s.getSequenceName(), c, s.getSequenceName(), s.getSequenceName()));
            preendWriter.close();

            // end
            File endMcFuncFile = new File(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\end.mcfunction");
            if (endMcFuncFile.createNewFile()) {
                LOGGER.info("end.mcfunction created");
            } else {
                LOGGER.info("end.mcfunction already exist");
            }
            FileWriter endWriter = new FileWriter(this.datapacksPath + "\\VanillaCinematics\\data\\vanilla_cinematics\\functions\\cinematics\\" + s.getSequenceName() + "\\end.mcfunction");
            endWriter.write(
                """
                gamemode adventure @a
                kill @e[tag=sequence_%s, tag=sequence_node_player_pos]
                """.
                formatted(s.getSequenceName()));
            endWriter.close();
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing new sequence to datapack", e);
        }
    }

    public void deleteDatapack() {
        try {
            FileUtils.deleteDirectory(new File(this.datapacksPath + "\\VanillaCinematics"));
            wasInitiated = false;
        } catch (IOException e) {
            LOGGER.error("An error occurred while deleting datapack", e);
        }
    }
}
