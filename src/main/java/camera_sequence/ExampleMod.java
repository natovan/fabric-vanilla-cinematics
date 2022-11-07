package camera_sequence;

import camera_sequence.sequence.NodeSequence;
//import com.mojang.logging.LogUtils;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
//import org.slf4j.Logger;
import java.util.ArrayList;

public class ExampleMod implements ModInitializer {
//	public static final Logger LOGGER = LogUtils.getLogger();
	public static ArrayList<NodeSequence> sequences = new ArrayList<>();
	public static DatapackWriter datapackWriter;

	@Override
	public void onInitialize() {
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> datapackWriter = new DatapackWriter(server));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CamseqCommand.register(dispatcher));
	}
}
