package dev.onebeastchris.skinapitest;

import org.cloudburstmc.protocol.bedrock.data.skin.AnimatedTextureType;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimationData;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimationExpressionType;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.PlayerListUtils;

import java.awt.Color;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Sends skins with an animated face. The skin API has no way to carry skin animations, so this builds the
 * player list entry itself using Geyser internals, the same way Geyser's SkinManager#sendSkinPacket does.
 */
final class AnimatedSkinSender {

    private AnimatedSkinSender() {
    }

    static void send(GeyserConnection viewer, UUID target, CubeModel model, FaceAnimation face) {
        GeyserSession session = (GeyserSession) viewer;
        // Same small delay as SkinManager#sendSkinPacket, which ensures skins are actually shown
        session.scheduleInEventLoop(() -> {
            PlayerEntity entity = target.equals(session.getPlayerEntity().uuid())
                ? session.getPlayerEntity()
                : session.getEntityCache().getPlayerEntity(target);
            if (entity == null) {
                return;
            }

            String skinId = "skinapitest:" + model.id();
            SerializedSkin skin = SerializedSkin.builder()
                .skinId(skinId)
                .fullSkinId(skinId)
                .skinResourcePatch("{\"geometry\":{\"default\":\"%s\",\"animated_face\":\"%s\"}}"
                    .formatted(model.geometryName(), face.geometryName()))
                .skinData(ImageData.of(model.texture()))
                .animations(List.of(new AnimationData(
                    ImageData.of(face.frameWidth(), face.frameHeight() * face.frames(), face.image()),
                    AnimatedTextureType.FACE,
                    face.frames(),
                    AnimationExpressionType.LINEAR
                )))
                .capeData(ImageData.EMPTY)
                .capeId("")
                .geometryData(CubeModel.geometryFile(model.geometryEntry(), face.geometryEntry()))
                .geometryDataEngineVersion(session.getClientData().getGameVersion())
                .premium(true)
                .overridingPlayerAppearance(true)
                .color(new Color(0, true))
                .trusted(true)
                .profileHash("")
                .build();

            PlayerListPacket.Entry entry = PlayerListUtils.buildEntryManually(session, entity.uuid(), entity.getUsername(), entity.geyserId(), skin);
            PlayerListUtils.sendSkinUsingPlayerList(session, entry, entity, entity.isListed());
        }, 100, TimeUnit.MILLISECONDS);
    }
}
