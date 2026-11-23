package dev.onebeastchris.skinapitest;

import org.geysermc.geyser.api.skin.Cape;
import org.geysermc.geyser.api.skin.Skin;
import org.geysermc.geyser.api.skin.SkinData;
import org.geysermc.geyser.api.skin.SkinGeometry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks up a Java player's skin through Mojang's API and turns it into Geyser {@link SkinData}.
 */
final class JavaSkinFetcher {
    static final Cape NO_CAPE = new Cape("", "no-cape", new byte[0], true);

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F-]{32,36})\"");
    private static final Pattern TEXTURES = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SKIN_URL = Pattern.compile("\"SKIN\"\\s*:\\s*\\{\\s*\"url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SLIM = Pattern.compile("\"model\"\\s*:\\s*\"slim\"");
    private static final Pattern UUID_ARG = Pattern.compile("[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}");

    private JavaSkinFetcher() {
    }

    /**
     * @param nameOrUuid a Java username, or a Java UUID (with or without dashes)
     */
    static CompletableFuture<SkinData> fetch(String nameOrUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String id = UUID_ARG.matcher(nameOrUuid).matches() ? nameOrUuid.replace("-", "") : lookUpId(nameOrUuid);

                String profile = get("https://sessionserver.mojang.com/session/minecraft/profile/" + id);
                String textures = new String(Base64.getDecoder().decode(find(TEXTURES, profile, "textures")), StandardCharsets.UTF_8);
                String skinUrl = find(SKIN_URL, textures, "skin url");
                boolean slim = SLIM.matcher(textures).find();

                byte[] png = HTTP.send(HttpRequest.newBuilder(URI.create(skinUrl)).build(), HttpResponse.BodyHandlers.ofByteArray()).body();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
                if (image == null) {
                    throw new IOException("Skin texture is not a readable image: " + skinUrl);
                }

                return new SkinData(new Skin(skinUrl, toRgba(image)), NO_CAPE, slim ? SkinGeometry.SLIM : SkinGeometry.WIDE);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private static String lookUpId(String name) throws IOException, InterruptedException {
        return find(ID, get("https://api.mojang.com/users/profiles/minecraft/" + name), "player " + name);
    }

    private static String get(String url) throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " from " + url);
        }
        return response.body();
    }

    private static String find(Pattern pattern, String body, String what) throws IOException {
        Matcher matcher = pattern.matcher(body);
        if (!matcher.find()) {
            throw new IOException("Could not find " + what);
        }
        return matcher.group(1);
    }

    /**
     * Geyser expects skin bytes as RGBA, row by row.
     */
    static byte[] toRgba(BufferedImage image) {
        byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
        int i = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                data[i++] = (byte) (argb >> 16);
                data[i++] = (byte) (argb >> 8);
                data[i++] = (byte) argb;
                data[i++] = (byte) (argb >> 24);
            }
        }
        return data;
    }
}
