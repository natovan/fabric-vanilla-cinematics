package camera_sequence;

import camera_sequence.sequence.CameraSequence;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ExampleMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
	public static ArrayList<CameraSequence> sequences = new ArrayList<>();
	public static DatapackWriter datapackWriter;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello fabric world!");
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> datapackWriter = new DatapackWriter(server));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CamseqCommand.register(dispatcher));
	}
}
