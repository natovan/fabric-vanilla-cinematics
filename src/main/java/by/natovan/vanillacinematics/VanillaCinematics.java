package by.natovan.vanillacinematics;

import by.natovan.vanillacinematics.sequence.NodeSequence;
import by.natovan.vanillacinematics.render.NodeRenderer;
import com.mojang.logging.LogUtils;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VanillaCinematics implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static List<NodeSequence> sequences = new ArrayList<>();

    @Override
    public void onInitialize() {
        Locale.setDefault(Locale.US);
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SequenceCommand.register(dispatcher));
        ClientLifecycleEvents.CLIENT_STOPPING.register((client -> this.onClose()));
    }

    private void onClose() {
        LOGGER.info("Stopping");
        NodeRenderer.INSTANCE.deleteGlResources();
    }

    public static void sendMessage(ServerCommandSource source, String message) {
        source.sendMessage(Text.of(message));
    }
}
