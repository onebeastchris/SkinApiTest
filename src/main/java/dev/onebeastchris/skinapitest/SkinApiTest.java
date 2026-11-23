package dev.onebeastchris.skinapitest;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.skin.SkinData;

import java.util.UUID;

/**
 * Commands (all Bedrock-only, run as {@code /skinapitest <command>}):
 * <ul>
 *     <li>{@code skin <java name or uuid> [player]} - apply a Java player's skin</li>
 *     <li>{@code cone [player]}, {@code pyramid [player]}, {@code sphere [player]} - turn into a shape</li>
 * </ul>
 * The optional player (a Bedrock or Java username of an online Geyser player) defaults to yourself.
 * The skin is sent to every online Geyser connection, so all Bedrock players see the change.
 */
public class SkinApiTest implements Extension {

    @Subscribe
    public void onDefineCommands(GeyserDefineCommandsEvent event) {
        event.register(Command.<GeyserConnection>builder(this)
            .source(GeyserConnection.class)
            .name("skin")
            .description("Apply a Java player's skin: skin <java name or uuid> [player]")
            .playerOnly(true)
            .bedrockOnly(true)
            .executor((source, command, args) -> {
                if (args.length < 1) {
                    source.sendMessage("Usage: /skinapitest skin <java name or uuid> [player]");
                    return;
                }
                GeyserConnection target = target(source, args, 1);
                if (target == null) {
                    return;
                }
                source.sendMessage("Fetching the skin of " + args[0] + "...");
                JavaSkinFetcher.fetch(args[0]).whenComplete((skinData, error) -> {
                    if (error != null) {
                        source.sendMessage("Could not fetch that skin: " + error.getMessage());
                        logger().error("Could not fetch the skin of " + args[0], error);
                        return;
                    }
                    apply(target, skinData);
                    source.sendMessage("Applied the skin of " + args[0] + " to " + target.bedrockUsername() + ".");
                });
            })
            .build());

        for (Shape shape : Shape.values()) {
            event.register(Command.<GeyserConnection>builder(this)
                .source(GeyserConnection.class)
                .name(shape.id())
                .description("Turn into a " + shape.id() + ": " + shape.id() + " [player]")
                .playerOnly(true)
                .bedrockOnly(true)
                .executor((source, command, args) -> {
                    GeyserConnection target = target(source, args, 0);
                    if (target == null) {
                        return;
                    }
                    apply(target, shape.skinData());
                    source.sendMessage(target.bedrockUsername() + " is now a " + shape.id() + ".");
                })
                .build());
        }
    }

    private GeyserConnection target(GeyserConnection source, String[] args, int index) {
        if (args.length <= index) {
            return source;
        }
        String name = args[index];
        for (GeyserConnection connection : geyserApi().onlineConnections()) {
            if (name.equalsIgnoreCase(connection.bedrockUsername()) || name.equalsIgnoreCase(connection.javaUsername())) {
                return connection;
            }
        }
        source.sendMessage("No online Geyser player called " + name + ".");
        return null;
    }

    /**
     * sendSkin only changes what one connection sees, so tell every Bedrock player about the new skin.
     */
    private void apply(GeyserConnection target, SkinData skinData) {
        UUID uuid = target.javaUuid();
        for (GeyserConnection viewer : geyserApi().onlineConnections()) {
            viewer.sendSkin(uuid, skinData);
        }
    }
}
