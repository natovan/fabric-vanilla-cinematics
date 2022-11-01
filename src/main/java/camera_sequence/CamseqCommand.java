package camera_sequence;

import camera_sequence.sequence.Node;
import camera_sequence.sequence.NodeSequence;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CamseqCommand {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(literal("c").
                then(literal("new").
                        then(argument("sequence name", word()).
                                executes((c) -> newCameraSequence(c.getSource(), getString(c, "sequence name"))))).
                then(literal("add_node").
                        then(argument("sequence name", word()).
                                executes((c) -> addCameraNode(c.getSource(), getString(c, "sequence name"))))).
                then(literal("print").
                        then(argument("sequence name", word()).
                                executes((c) -> printNodes(c.getSource(), getString(c, "sequence name"))))).
                then(literal("spawn").
                        then(argument("sequence name", word()).
                                executes((c) -> spawnSequence(c.getSource(), getString(c, "sequence name"))))).
                then(literal("despawn").
                        then(argument("sequence name", word()).
                                executes((c) -> despawnSequence(c.getSource(), getString(c, "sequence name"))))).
                then(literal("write").
                        then(argument("sequence name", word()).
                                executes((c) -> writeToDatapack(c.getSource(), getString(c, "sequence name"))))));
    }

    private static int writeToDatapack(ServerCommandSource source, String name) {
        for (NodeSequence s : ExampleMod.sequences) {
            if (s.getSequenceName().equals(name)) {
                ExampleMod.datapackWriter.writeSequence(s);
                source.sendFeedback(Text.of("Trying to write to sequence"), false);
            }
        }
        return 1;
    }

    private static int newCameraSequence(ServerCommandSource source, String name) {
        ExampleMod.sequences.add(new NodeSequence(name));
        source.sendFeedback(Text.of("NEW SEQ: '" + name + "'"), false);

        return 1;
    }

    private static int addCameraNode(ServerCommandSource source, String cameraSequenceName) {
        if (source.getPlayer() != null) {
            Vec3d pos = source.getPlayer().getPos();
            float yaw = source.getPlayer().getYaw();
            float pitch = source.getPlayer().getPitch();
            Node node = new Node(pos, yaw, pitch, 20);
            for (NodeSequence s : ExampleMod.sequences) {
                if (s.getSequenceName().equals(cameraSequenceName)) s.appendCameraNode(node);
            }
            source.sendFeedback(Text.of("APPENDED NODE AT POS: " +
                    pos.x + " " + pos.y + " " + pos.z), false);
        }
        return 1;
    }

    private static int printNodes(ServerCommandSource source, String cameraSequenceName) {
        for (NodeSequence s : ExampleMod.sequences) {
            if (s.getSequenceName().equals(cameraSequenceName)) {
                int c = 0;
                for (Node n : s.getCameraNodes()) {
                    c++;
                    String fmt = String.format("%d. Yaw: %f Pitch: %f Pos: %f %f %f", c,
                            n.getYaw(), n.getPitch(), n.getPos().x, n.getPos().y, n.getPos().z);
                    source.sendFeedback(Text.of(fmt), false);
                }
            }
        }
        return 1;
    }

    private static int spawnSequence(ServerCommandSource source, String cameraSequenceName) {
        CommandManager manager = source.getServer().getCommandManager();
        for (NodeSequence s : ExampleMod.sequences) {
            if (s.getSequenceName().equals(cameraSequenceName)) {

                // Sequence found
                int i = 0;
                for (Node n : s.getCameraNodes()) {
                    manager.executeWithPrefix(source, String.format(
                            "/summon minecraft:armor_stand %f %f %f {NoGravity:1, Tags:['sequence_%s', 'sequence_node_%d']}",
                            n.getPos().x, n.getPos().y, n.getPos().z, s.getSequenceName(), i));
                    manager.executeWithPrefix(source, String.format(
                            "/tp @e[tag=sequence_%s, tag=sequence_node_%d] %f %f %f %f %f",
                            s.getSequenceName(), i, n.getPos().x, n.getPos().y, n.getPos().z, n.getYaw(), n.getPitch()));
                    i++;
                }
            }
        }
        return 1;
    }

    private static int despawnSequence(ServerCommandSource source, String cameraSequenceName) {
        CommandManager manager = source.getServer().getCommandManager();
        for (NodeSequence s : ExampleMod.sequences) {
            if (s.getSequenceName().equals(cameraSequenceName)) {

                // Sequence found
                // @TODO kill even if out of simulation distance
                manager.executeWithPrefix(source, String.format("/kill @e[tag=sequence_%s]", s.getSequenceName()));
            }
        }
        return 1;
    }
}
