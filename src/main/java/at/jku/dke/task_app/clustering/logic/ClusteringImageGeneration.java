package at.jku.dke.task_app.clustering.logic;

import at.jku.dke.task_app.clustering.config.AppConfig;
import at.jku.dke.task_app.clustering.data.entities.Cluster;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.DataPoint;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class ClusteringImageGeneration {

    public static String generateClusterImage(ClusteringTask task, boolean showSolution) {

        //Variablen aus config laden
        int width = AppConfig.getInt("image.width", 400);
        int height = AppConfig.getInt("image.height", 400);
        double padding = AppConfig.getDouble("image.padding", 40.0);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Grafikqualität verbessern
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hintergrund weiß
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        double maxX = 100;
        double maxY = 100;

        width = Math.max(width, 300);
        height = Math.max(height, 300);

        double scaleX = (width - padding) / maxX;
        double scaleY = (height - padding) / maxY;
        double scale = Math.min(scaleX, scaleY);

        int maxXInt = 100;
        int maxYInt = 100;

        // Achsen zeichnen
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, height, width, height); // x-Achse unten
        g.drawLine(0, 0, 0, height); // y-Achse links

        // Achsenbeschriftung alle 10
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= maxXInt; i += 10) {
            int x = (int) (i * scale);
            g.drawLine(x, height - 5, x, height); // kleine Markierung
            g.drawString(String.valueOf(i), x + 2, height - 8); // X-Beschriftung
        }
        for (int i = 0; i <= maxYInt; i += 10) {
            int y = height - (int) (i * scale);
            g.drawLine(0, y, 5, y);
            g.drawString(String.valueOf(i), 8, y - 2); // Y-Beschriftung
        }

        // Clusterfarben
        Color[] colors = {Color.GRAY};
        Color centroidColor = Color.BLACK;
        List<Cluster> clusters = task.getExerciseClusters();
        int colorIndex = 0;

        if(showSolution){
            colors = new Color[]{Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA};
            clusters = task.getSolutionClusters();
        }
        for (Cluster cluster : clusters) {
            Color color = Color.GRAY;
            if(showSolution){
                centroidColor = color = colors[colorIndex++ % colors.length];
            }
            g.setColor(color);

            for (DataPoint p : cluster.getDataPoints()) {
                int x = (int) (p.getX() * scale);
                int y = height - (int) (p.getY() * scale);
                g.fillOval(x - 4, y - 4, 8, 8);
            }

            // Zentroid
            g.setColor(centroidColor);
            int cx = (int) (cluster.getX() * scale);
            int cy = height - (int) (cluster.getY() * scale);
            g.fillRect(cx - 5, cy - 5, 10, 10);
        }

        g.dispose();

        try (var baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            String base64EncodedImage = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "<img src=\"data:image/png;base64," + base64EncodedImage + "\" alt=\"K-Means Result\">";
        } catch (IOException e) {
            return "<p>" + e.getMessage() + "</p>";
        }
    }

}
