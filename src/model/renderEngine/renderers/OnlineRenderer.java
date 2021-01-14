package model.renderEngine.renderers;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import model.map.IMapManager;
import model.map.accessors.ITileIdConversion;
import model.map.accessors.TileConversion;

public class OnlineRenderer implements IRenderer {

    private final ITileIdConversion conversion;

    private final Matcher matcher;
    private final Random random;
    private final String extension;

    public OnlineRenderer(String tileServer) {
        if (!tileServer.endsWith("/"))
            tileServer += "/";
        conversion = new TileConversion();
        matcher = Pattern.compile("\\[[^\\]]+\\]").matcher(tileServer);
        random = new Random();

        extension = detectExtension();
    }

    private String detectExtension() {
        String[] extensions = new String[] { ".png", ".jpg" };
        try {
            for (final String extension : extensions) {
                final URL url = new URL(getTileServer() + "0/0/0" + extension);
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD"); // we’re not interested in the content
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    return extension;
            }
        } catch (IOException e) {}
        return null;
    }

    private synchronized String getTileServer() {
        final StringBuffer s = new StringBuffer();
        while (matcher.find()) {
            // choose a random item to transform [abcd].server.com into c.server.com
            final String group = matcher.group();
            final String characters = group.substring(1, group.length() - 1);
            matcher.appendReplacement(s,
                    new String(new char[] { characters.charAt(random.nextInt(characters.length())) }));
        }
        matcher.appendTail(s);
        matcher.reset();

        return s.toString();
    }

    @Override
    public boolean render(long id, Image image) {
        final int zoom = conversion.getZoom(id);
        final int row = conversion.getRow(id);
        final int column = conversion.getColumn(id);

        try {
            URL url = new URL(getTileServer() + zoom + "/" + column + "/" + row + extension);
            HttpURLConnection huc = ((HttpURLConnection) url.openConnection());
            BufferedImage img = ImageIO.read(huc.getInputStream());
            final Graphics g = image.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return true;
        } catch (IOException e) {}
        return false;
    }

    @Override
    public void setMapManager(IMapManager manager) {

    }
}
