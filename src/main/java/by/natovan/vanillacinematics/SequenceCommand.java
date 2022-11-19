package by.natovan.vanillacinematics;

import by.natovan.vanillacinematics.sequence.Node;
import by.natovan.vanillacinematics.sequence.NodeSequence;
import by.natovan.vanillacinematics.render.NodeRenderer;
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
                        then(argument("render", bool()).
                                executes((c) -> render(c.getSource(), getBool(c, "render"))))).
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
                                                        getString(c, "command")))))).
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
                VanillaCinematics.sendMessage(source, "Scene id can't contain uppercase letters");
                return 1;
            }
        }
        NodeSequence node = new NodeSequence(id);
        VanillaCinematics.sequences.add(node);
        VanillaCinematics.sendMessage(source, "Scene %s created".formatted(id));
        return 1;
    }

    private static int delayCommand(ServerCommandSource source, String id, int index, int delay) {
        for (NodeSequence seq : VanillaCinematics.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setDelay(delay);
                    VanillaCinematics.sendMessage(source, "Set delay of %s at node #%d to %d".formatted(id, index, delay));
                } else {
                    VanillaCinematics.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        VanillaCinematics.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int posCommand(ServerCommandSource source, String id, int index, Vec3d pos) {
        for (NodeSequence seq : VanillaCinematics.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setPos(pos);
                    VanillaCinematics.sendMessage(source, "Set position of %s at node #%d to %f, %f, %f".formatted(id, index, pos.x, pos.y, pos.z));
                } else {
                    VanillaCinematics.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        VanillaCinematics.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int rotationCommand(ServerCommandSource source, String id, int index, Vec2f rotation) {
        for (NodeSequence seq : VanillaCinematics.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setRotation(rotation);
                    VanillaCinematics.sendMessage(source, "Set rotation of %s at node #%d to %f, %f".formatted(id, index, rotation.x, rotation.y));
                } else {
                    VanillaCinematics.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        VanillaCinematics.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int commandCommand(ServerCommandSource source, String id, int index, String command) {
        for (NodeSequence seq : VanillaCinematics.sequences) {
            if (seq.getSequenceName().equals(id)) {
                if (seq.getCameraNodes().size() - 1 >= index) {
                    seq.getCameraNodes().get(index).setCommand(command);
                    VanillaCinematics.sendMessage(source, "Set command of %s at node #%d to \"%s\"".formatted(id, index, command));
                } else {
                    VanillaCinematics.sendMessage(source, "Node of index #%d at %s was not found".formatted(index, id));
                }
                return 1;
            }
        }
        VanillaCinematics.sendMessage(source, "No sequence and/or node was found");
        return 1;
    }

    private static int nameCommand(ServerCommandSource source, String id, String name) {
        for (NodeSequence seq : VanillaCinematics.sequences) {
            if (seq.getSequenceName().equals(id)) {
                seq.setName(name);
                VanillaCinematics.sendMessage(source, "Changed name from '%s' to '%s'".formatted(id, name));
                return 1;
            }
        }
        VanillaCinematics.sendMessage(source, "No sequence was found");
        return 1;
    }

    private static int deleteCommand(ServerCommandSource source, String id) {
        int i = 0;
        for (NodeSequence seq : VanillaCinematics.sequences) {
            if (seq.getSequenceName().equals(id)) {
                VanillaCinematics.sequences.remove(i);
                return 1;
            }
            i++;
        }
        VanillaCinematics.sendMessage(source, "No sequence was found");
        return 1;
    }

    private static int appendCommand(ServerCommandSource source, String id, int delay, Vec3d pos, Vec2f rotation, @Nullable String function) {
        if (source.getPlayer() == null) return 0;

        for (NodeSequence seq : VanillaCinematics.sequences) {
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
                        VanillaCinematics.sendMessage(source, "Invalid command \"" + function + "\"");
                        return 1;
                    }
                }

                seq.appendCameraNode(new Node(standPos, eyePos, rotation.x, rotation.y, delay, function));
                VanillaCinematics.sendMessage(source, "Appended node to %s at position: %.2f, %.2f, %.2f".formatted(id, pos.x, pos.y, pos.z));
                return 1;
            }
        }
        VanillaCinematics.sendMessage(source, "No sequence %s was found".formatted(id));
        return 1;
    }

    private static int render(ServerCommandSource source, boolean shouldRender) {
        NodeRenderer.INSTANCE.shouldRender = shouldRender;
        if (shouldRender) {
            VanillaCinematics.sendMessage(source, "Rendering nodes: enabled");
        } else {
            VanillaCinematics.sendMessage(source, "Rendering nodes: disabled");
        }
        return 1;
    }

    private static int load(ServerCommandSource source) {
        VanillaCinematics.sequences.clear();
        if (DataStorage.INSTANCE.load() == 1) {
            VanillaCinematics.sendMessage(source, "Loaded from sequences.json");
        } else {
            VanillaCinematics.sendMessage(source, "Error while loading from sequences.json");
        }
        CommandManager manager = source.getServer().getCommandManager();
        manager.executeWithPrefix(source, "/reload");
        return 1;
    }

    private static int write(ServerCommandSource source) {
        for (NodeSequence s : VanillaCinematics.sequences) {
            if (DatapackWriter.INSTANCE.writeSequence(s) == 1) {
                VanillaCinematics.sendMessage(source, "Wrote " + s.getSequenceName() + " to the datapack");
                s.markAsWritten();
            } else {
                VanillaCinematics.sendMessage(source, "Error occurred while writing to the datapack");
                return 1;
            }
        }
        if (DataStorage.INSTANCE.write() == 1) {
            VanillaCinematics.sendMessage(source, "Wrote to sequences.json");
        } else {
            VanillaCinematics.sendMessage(source, "Error while writing to sequences.json");
            return 1;
        }

        source.getServer().getCommandManager().executeWithPrefix(source, "/reload");
        return 1;
    }
}
