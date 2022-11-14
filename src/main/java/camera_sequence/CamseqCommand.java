package camera_sequence;

import camera_sequence.render.NodeRenderer;
import camera_sequence.sequence.Node;
import camera_sequence.sequence.NodeSequence;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CamseqCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        // todo: fix this mess
        dispatcher.register(literal("c").
                then(literal("newscene").
                        then(argument("scene name", word()).
                                executes((c) -> newCameraSequence(c.getSource(), getString(c, "scene name"))))));
        dispatcher.register(literal("c").
                then(literal("addnode").
                        then(argument("scene name", word()).
                                then(argument("delay", integer()).
                                        executes((c) -> addCameraNode(c.getSource(),
                                                getString(c, "scene name"),
                                                getInteger(c, "delay"),
                                                null)).
                                        then(argument("function", string()).
                                            executes((c) -> addCameraNode(c.getSource(),
                                                    getString(c, "scene name"),
                                                    getInteger(c, "delay"),
                                                    getString(c, "function"))))))));
        dispatcher.register(literal("c").
                then(literal("write").
                        executes((c) -> write(c.getSource()))));
        dispatcher.register(literal("c").
                then(literal("load").
                        executes((c) -> reload(c.getSource()))));
        dispatcher.register(literal("c").
                then(literal("exe").redirect(dispatcher.getRoot())));
        dispatcher.register(literal("c").
                then(literal("render").
                        then(argument("should_render", bool()).
                                executes((c) -> render(c.getSource(), getBool(c, "should_render"))))));
	dispatcher.register(literal("c").
			then(argument("scene name", word()).
				then(argument("node index", integer()).
					then(argument("delay", integer()).
						executes((c) -> editNodeDelay(
								c.getSource(),
								getString(c, "scene name"),
								getInteger(c, "node index"),
								getInteger(c, "delay")))))));
    }

    private static int editNodeDelay(ServerCommandSource source, String sceneName, int nodeIndex, int delay) {
	for (NodeSequence seq : ExampleMod.sequences) {
		if (seq.getSequenceName().equals(sceneName)) {
			source.sendFeedback(Text.of("Found scene " + sceneName + " " + nodeIndex + " " + delay), false);
		}
	}
	return 1;
    }

    private static int render(ServerCommandSource source, boolean shouldRender) {
        NodeRenderer.INSTANCE.shouldRender = shouldRender;
        if (shouldRender) {
            source.sendFeedback(Text.of("Rendering nodes: enabled"), false);
        } else {
            source.sendFeedback(Text.of("Rendering nodes: disabled"), false);
        }
        return 1;
    }

    private static int reload(ServerCommandSource source) {
        ExampleMod.sequences.clear();
        if (DataStorage.INSTANCE.load() == 1) {
            source.sendFeedback(Text.of("Loaded from sequences.json"), false);
        } else {
            source.sendFeedback(Text.of("Error while loading from sequences.json"), false);
        }
        CommandManager manager = source.getServer().getCommandManager();
        manager.executeWithPrefix(source, "/reload");
        return 1;
    }

    private static int write(ServerCommandSource source) {
        for (NodeSequence s : ExampleMod.sequences) {
            if (DatapackWriter.INSTANCE.writeSequence(s) == 1) {
                source.sendFeedback(Text.of("Wrote " + s.getSequenceName() + " to the datapack"), false);
            } else {
                source.sendFeedback(Text.of("Error occurred while writing to the datapack"), false);
            }
        }
        if (DataStorage.INSTANCE.write() == 1) {
            source.sendFeedback(Text.of("Wrote to sequences.json"), false);
        } else {
            source.sendFeedback(Text.of("Error while writing to sequences.json"), false);
        }

        CommandManager manager = source.getServer().getCommandManager();
        manager.executeWithPrefix(source, "/reload");
        return 1;
    }

    private static int newCameraSequence(ServerCommandSource source, String name) {
        ExampleMod.sequences.add(new NodeSequence(name));

        char c;
        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                source.sendFeedback(Text.of("Can't have uppercase letters in sequence names"), false);
                return 0;
            }
        }
        source.sendFeedback(Text.of("New sequence: '" + name + "' created"), false);
        return 1;
    }

    private static int addCameraNode(ServerCommandSource source, String cameraSequenceName, int delay, @Nullable String command) {
        if (source.getPlayer() != null) {
            // todo: fix this mess
            final float armorStandEyeHeight = 1.7f; // i don't know actually
            final double yOffset = 0.198;
            Vec3d tmpVec = source.getPlayer().getPos();
            Vec3d standPos = new Vec3d(tmpVec.x, tmpVec.y - yOffset, tmpVec.z);
            Vec3d eyePos = new Vec3d(standPos.x, standPos.y + armorStandEyeHeight, standPos.z);
            float yaw = source.getPlayer().getYaw();
            float pitch = source.getPlayer().getPitch();

            if (command != null) {
                CommandDispatcher<ServerCommandSource> dispatcher = source.getServer().getCommandManager().getDispatcher();
                ParseResults<ServerCommandSource> results = dispatcher.parse(command, source);
                if (results.getReader().canRead()) {
                    source.sendFeedback(Text.of("Invalid command \"" + command + "\""), false);
                }
            }
            Node node = new Node(standPos, eyePos, yaw, pitch, delay, command);

            for (NodeSequence s : ExampleMod.sequences) {
                if (s.getSequenceName().equals(cameraSequenceName)) s.appendCameraNode(node);
            }
            source.sendFeedback(Text.of("Appended node to " + cameraSequenceName + " at position: %.2f, %.2f, %.2f".formatted(standPos.x, standPos.y, standPos.z)), false);
        }
        return 1;
    }
}
