package camera_sequence;

import camera_sequence.render.NodeRenderer;
import camera_sequence.sequence.Node;
import camera_sequence.sequence.NodeSequence;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.command.argument.Vec2ArgumentType.getVec2;
import static net.minecraft.command.argument.Vec2ArgumentType.vec2;
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SequenceCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(literal("sequence").
                then(literal("render").
                        then(literal("set").
                                then(argument("render", bool()).
                                        executes((c) -> render(c.getSource(), getBool(c, "render")))))).
                then(literal("load").
                        executes((c) -> load(c.getSource()))).
                then(literal("write").
                        executes((c) -> write(c.getSource()))).
                then(literal("new").
                        then(argument("name", word()).
                                executes((c) -> newCommand(c.getSource(), getString(c, "name"))))).
                then(argument("id", word()).
                        then(literal("append").
                                then(argument("delay", integer()).
                                        then(argument("pos", vec3()).
                                                then(argument("rotation", vec2()).
                                                        executes((c) -> appendCommand(
                                                                c.getSource(),
                                                                getString(c, "id"),
                                                                getInteger(c, "delay"),
                                                                getVec3(c, "pos"),
                                                                getVec2(c, "rotation"),
                                                                null)).
                                                        then(argument("function", string()).
                                                                executes((c) -> appendCommand(
                                                                        c.getSource(),
                                                                        getString(c, "id"),
                                                                        getInteger(c, "delay"),
                                                                        getVec3(c, "pos"),
                                                                        getVec2(c, "rotation"),
                                                                        getString(c, "function")))))))).
                        then(argument("index", integer()).
                                then(literal("delay").
                                        then(argument("delay", integer()).
                                                executes((c) -> delayCommand(
                                                        c.getSource(),
                                                        getString(c, "id"),
                                                        getInteger(c, "index"),
                                                        getInteger(c, "delay"))))).
                                then(literal("pos").
                                        then(argument("pos", vec3()).
                                                executes((c) -> posCommand(
                                                        c.getSource(),
                                                        getString(c, "id"),
                                                        getInteger(c, "index"),
                                                        getVec3(c, "pos"))))).
                                then(literal("rotation").
                                        then(argument("rotation", vec2()).
                                                executes((c) -> rotationCommand(
                                                        c.getSource(),
                                                        getString(c, "id"),
                                                        getInteger(c, "index"),
                                                        getVec2(c, "rotation"))))).
                                then(literal("command").
                                        then(argument("command", string()).
                                                executes((c) -> commandCommand(
                                                        c.getSource(),
                                                        getString(c, "id"),
                                                        getInteger(c, "index"),
                                                        getString(c, "string")))))).
                        then(literal("name").
                                then(argument("name", word()).
                                        executes((c) -> nameCommand(
                                                c.getSource(),
                                                getString(c, "id"),
                                                getString(c, "name"))))).
                        then(literal("delete").
                                executes((c) -> deleteCommand(
                                        c.getSource(),
                                        getString(c, "id"))))));
    }

    private static int newCommand(ServerCommandSource source, String id) {
        for (int i = 0; i < id.length(); i++) {
            if (Character.isUpperCase(id.charAt(i))) {
                ExampleMod.sendMessage(source, "Scene id can't contain uppercase letters");
                return 1;
            }
        }
        NodeSequence node = new NodeSequence(id);
        ExampleMod.sequences.add(node);
        ExampleMod.sendMessage(source, "Scene %s created".formatted(id));
        return 1;
    }

    private static int delayCommand(ServerCommandSource source, String id, int index, int delay) {
        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setDelay(delay);
                    ExampleMod.sendMessage(source, "Set delay of %s at node #%d to %d".formatted(id, index, delay));
                } else {
                    ExampleMod.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        ExampleMod.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int posCommand(ServerCommandSource source, String id, int index, Vec3d pos) {
        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setPos(pos);
                    ExampleMod.sendMessage(source, "Set position of %s at node #%d to %f, %f, %f".formatted(id, index, pos.x, pos.y, pos.z));
                } else {
                    ExampleMod.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        ExampleMod.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int rotationCommand(ServerCommandSource source, String id, int index, Vec2f rotation) {
        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setRotation(rotation);
                    ExampleMod.sendMessage(source, "Set rotation of %s at node #%d to %f, %f".formatted(id, index, rotation.x, rotation.y));
                } else {
                    ExampleMod.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        ExampleMod.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int commandCommand(ServerCommandSource source, String id, int index, String command) {
        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setCommand(command);
                    ExampleMod.sendMessage(source, "Set command of %s at node #%d to \"%s\"".formatted(id, index, command));
                } else {
                    ExampleMod.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        ExampleMod.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int nameCommand(ServerCommandSource source, String id, String name) {
        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                seq.setName(name);
                ExampleMod.sendMessage(source, "Changed name from '%s' to '%s'".formatted(id, name));
                return 1;
            }
        }
        ExampleMod.sendMessage(source, "No sequence was found");
        return 1;
    }

    private static int deleteCommand(ServerCommandSource source, String id) {
        int i = 0;
        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                ExampleMod.sequences.remove(i);
                return 1;
            }
            i++;
        }
        ExampleMod.sendMessage(source, "No sequence was found");
        return 1;
    }

    private static int appendCommand(ServerCommandSource source, String id, int delay, Vec3d pos, Vec2f rotation, @Nullable String function) {
        if (source.getPlayer() == null) return 0;

        for (NodeSequence seq : ExampleMod.sequences) {
            if (seq.getSequenceName().equals(id)) {
                // todo: encrease precision here
                final float armorStandEyeHeight = 1.7f; // i don't know actually
                final double yOffset = 0.198;           // i don't know this eather
                Vec3d standPos = new Vec3d(pos.x, pos.y - yOffset, pos.z);
                Vec3d eyePos = new Vec3d(standPos.x, standPos.y + armorStandEyeHeight, standPos.z);

                if (function != null) {
                    CommandDispatcher<ServerCommandSource> dispatcher = source.getServer().getCommandManager().getDispatcher();
                    ParseResults<ServerCommandSource> results = dispatcher.parse(function, source);
                    if (results.getReader().canRead()) {
                        ExampleMod.sendMessage(source, "Invalid command \"" + function + "\"");
                        return 1;
                    }
                }

                seq.appendCameraNode(new Node(standPos, eyePos, rotation.x, rotation.y, delay, function));
                ExampleMod.sendMessage(source, "Appended node to %s at position: %.2f, %.2f, %.2f".formatted(id, pos.x, pos.y, pos.z));
                return 1;
            }
        }
        ExampleMod.sendMessage(source, "No sequence %s was found".formatted(id));
        return 1;
    }

    private static int render(ServerCommandSource source, boolean shouldRender) {
        NodeRenderer.INSTANCE.shouldRender = shouldRender;
        if (shouldRender) {
            ExampleMod.sendMessage(source, "Rendering nodes: enabled");
        } else {
            ExampleMod.sendMessage(source, "Rendering nodes: disabled");
        }
        return 1;
    }

    private static int load(ServerCommandSource source) {
        ExampleMod.sequences.clear();
        if (DataStorage.INSTANCE.load() == 1) {
            ExampleMod.sendMessage(source, "Loaded from sequences.json");
        } else {
            ExampleMod.sendMessage(source, "Error while loading from sequences.json");
        }
        CommandManager manager = source.getServer().getCommandManager();
        manager.executeWithPrefix(source, "/reload");
        return 1;
    }

    private static int write(ServerCommandSource source) {
        for (NodeSequence s : ExampleMod.sequences) {
            if (DatapackWriter.INSTANCE.writeSequence(s) == 1) {
                ExampleMod.sendMessage(source, "Wrote " + s.getSequenceName() + " to the datapack");
            } else {
                ExampleMod.sendMessage(source, "Error occurred while writing to the datapack");
            }
        }
        if (DataStorage.INSTANCE.write() == 1) {
            ExampleMod.sendMessage(source, "Wrote to sequences.json");
        } else {
            ExampleMod.sendMessage(source, "Error while writing to sequences.json");
        }

        CommandManager manager = source.getServer().getCommandManager();
        manager.executeWithPrefix(source, "/reload");
        return 1;
    }
}
