package ganymedes01.etfuturum.elytra;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class ModKeybindings {

    public static final KeyBinding TOGGLE_ENABLED = new KeyBinding(
            "key.do_a_barrel_roll.toggle_enabled",
            Keyboard.KEY_I,
            "category.do_a_barrel_roll.do_a_barrel_roll"
    );
    public static final KeyBinding TOGGLE_THRUST = new KeyBinding(
            "key.do_a_barrel_roll.toggle_thrust",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll"
    );
    public static final KeyBinding OPEN_CONFIG = new KeyBinding(
            "key.do_a_barrel_roll.open_config",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll"
    );

    public static final KeyBinding PITCH_UP = new KeyBinding(
            "key.do_a_barrel_roll.pitch_up",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding PITCH_DOWN = new KeyBinding(
            "key.do_a_barrel_roll.pitch_down",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding YAW_LEFT = new KeyBinding(
            "key.do_a_barrel_roll.yaw_left",
            Keyboard.KEY_A,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding YAW_RIGHT = new KeyBinding(
            "key.do_a_barrel_roll.yaw_right",
            Keyboard.KEY_D,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding ROLL_LEFT = new KeyBinding(
            "key.do_a_barrel_roll.roll_left",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding ROLL_RIGHT = new KeyBinding(
            "key.do_a_barrel_roll.roll_right",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding THRUST_FORWARD = new KeyBinding(
            "key.do_a_barrel_roll.thrust_forward",
            Keyboard.KEY_W,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );
    public static final KeyBinding THRUST_BACKWARD = new KeyBinding(
            "key.do_a_barrel_roll.thrust_backward",
            Keyboard.KEY_NONE,
            "category.do_a_barrel_roll.do_a_barrel_roll.movement"
    );

    public static final List<KeyBinding> ALL = Lists.asList(
        TOGGLE_ENABLED,
            new KeyBinding[]{
            TOGGLE_THRUST,
//            OPEN_CONFIG,
            PITCH_UP,
            PITCH_DOWN,
            YAW_LEFT,
            YAW_RIGHT,
            ROLL_LEFT,
            ROLL_RIGHT,
            THRUST_FORWARD,
            THRUST_BACKWARD
    });

    public static void clientTick(Minecraft client) {
        while (TOGGLE_ENABLED.isPressed()) {
            /*if (!ClientNetworking.HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::forceEnabled).orElse(false)) {
                ModConfig.INSTANCE.setModEnabled(!ModConfig.INSTANCE.getModEnabled());
                ModConfig.INSTANCE.save();

                if (client.thePlayer != null) {
                    client.thePlayer.sendMessage(
                            Text.translatable(
                                    "key.do_a_barrel_roll." +
                                            (ModConfig.INSTANCE.getModEnabled() ? "toggle_enabled.enable" : "toggle_enabled.disable")
                            ),
                            true
                    );
                }
            } else*/ {
                if (client.thePlayer != null) {
                    client.thePlayer.addChatMessage(
                        new ChatComponentText("key.do_a_barrel_roll.toggle_enabled.disallowed")
                    );
                }
            }
        }
        while (TOGGLE_THRUST.isPressed()) {
            /*if (ClientNetworking.HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::allowThrusting).orElse(false)) {
                ModConfig.INSTANCE.setEnableThrust(!ModConfig.INSTANCE.getEnableThrust());
                ModConfig.INSTANCE.save();

                if (client.thePlayer != null) {
                    client.thePlayer.sendMessage(
                            Text.translatable(
                                    "key.do_a_barrel_roll." +
                                            (ModConfig.INSTANCE.getEnableThrust() ? "toggle_thrust.enable" : "toggle_thrust.disable")
                            ),
                            true
                    );
                }
            } else */{
                if (client.thePlayer != null) {
                    client.thePlayer.addChatMessage(
                        new ChatComponentText("key.do_a_barrel_roll.toggle_thrust.disallowed")
                    );
                }
            }
        }
    }
}