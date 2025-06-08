package at.jku.dke.task_app.clustering.logic;

import at.jku.dke.task_app.clustering.config.AppConfig;
import at.jku.dke.task_app.clustering.data.entities.Cluster;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.DataPoint;
import org.springframework.context.MessageSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class ClusteringImageGeneration {

    public static String generateClusterImage(ClusteringTask task, boolean showSolution, MessageSource messageSource, Locale locale) {

        //Variablen aus config laden
        int baseWidth = AppConfig.getInt("image.width", 400);
        int height = AppConfig.getInt("image.height", 400);
        double padding = AppConfig.getDouble("image.padding", 40.0);

        int extraLegendWidth = 170;
        baseWidth = Math.max(baseWidth, 300);
        height = Math.max(height, 300);
        int width = baseWidth + extraLegendWidth;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Grafikqualität verbessern
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hintergrund weiß
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        double maxX = 100;
        double maxY = 100;

        double scaleX = (baseWidth - padding) / maxX;
        double scaleY = (height - padding) / maxY;
        double scale = Math.min(scaleX, scaleY);

        int maxXInt = 100;
        int maxYInt = 100;

        // Achsen zeichnen
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, height, baseWidth, height); // x-Achse unten
        g.drawLine(0, 0, 0, height); // y-Achse links

        // Achsenbeschriftung und dünne gepunktete Gitterlinien alle 10 Einheiten
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        Stroke defaultStroke = g.getStroke();
        Stroke dottedStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2f}, 0);

        // Vertikale Linien und Beschriftung
        for (int i = 0; i <= maxXInt; i += 10) {
            int x = (int) (i * scale);

            // Dotted line
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(dottedStroke);
            g.drawLine(x, 0, x, height);

            // Beschriftung
            g.setColor(Color.GRAY);
            g.setStroke(defaultStroke);
            g.drawLine(x, height - 5, x, height);
            g.drawString(String.valueOf(i), x + 2, height - 8);
        }

        // Horizontale Linien und Beschriftung
        for (int i = 0; i <= maxYInt; i += 10) {
            int y = height - (int) (i * scale);

            // Dotted line
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(dottedStroke);
            g.drawLine(0, y, baseWidth, y);

            // Beschriftung
            g.setColor(Color.GRAY);
            g.setStroke(defaultStroke);
            g.drawLine(0, y, 5, y);
            g.drawString(String.valueOf(i), 8, y - 2);
        }
        // Clusterfarben
        Color[] colors = {Color.GRAY};
        Color centroidColor = Color.BLACK;
        List<Cluster> clusters = task.getExerciseClusters();
        int colorIndex = 0;

        if(showSolution){
            //Set of colors that is unambiguous both to colorblinds and non-colorblinds
            //Have to be kept in order
            colors = new Color[] {
                new Color(230, 159, 0),  // Orange
                new Color(86, 180, 233),  // Sky Blue
                new Color(0, 158, 115),   // Bluish Green
                new Color(240, 228, 66),   // Yellow
                new Color(0, 114, 178),   // Blue
                new Color(213, 94, 0),   // Vermilion (red-orange)
                new Color(204, 121, 167), // Reddish Purple
            };
            clusters = task.getSolutionClusters();
        }

        for (Cluster cluster : clusters) {
            double maxDistance = 0;
            Color color = Color.GRAY;
            if(showSolution){
                centroidColor = color = colors[colorIndex++ % colors.length];
            }
            g.setColor(color);

            for (DataPoint p : cluster.getDataPoints()) {
                int x = (int) (p.getX() * scale);
                int y = height - (int) (p.getY() * scale);
                g.fillOval(x - 4, y - 4, 8, 8);

                // Linie nach rechts oben
                int lineLength = 12;
                int labelOffsetX = 4;
                int labelOffsetY = -4;

                g.drawLine(x, y, x + lineLength, y - lineLength);

                // Buchstabe zeichnen
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g.drawString(String.valueOf(p.getName()), x + lineLength + labelOffsetX, y - lineLength + labelOffsetY);

                //Für den Kreis
                double dx = p.getX() - cluster.getX();
                double dy = p.getY() - cluster.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance > maxDistance)
                    maxDistance = distance;
            }

            // Zentroid
            g.setColor(centroidColor);
            int cx = (int) (cluster.getX() * scale);
            int cy = height - (int) (cluster.getY() * scale);
            g.fillRect(cx - 5, cy - 5, 10, 10);

            // Linie nach links oben
            int lineLength = 12;
            int labelOffsetX = -10;
            int labelOffsetY = -4;

            g.drawLine(cx, cy, cx - lineLength, cy - lineLength);

            // Buchstabe zeichnen
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.drawString(String.valueOf(cluster.getName()), cx - lineLength + labelOffsetX, cy - lineLength + labelOffsetY);


            if (showSolution) {
                //draw cluster circle
                double paddingRadius = 5.0;
                int pixelRadius = (int) ((maxDistance + paddingRadius) * scale);

                g.setColor(color);
                g.setStroke(new BasicStroke(0.5f));
                g.drawOval(cx - pixelRadius, cy - pixelRadius, pixelRadius * 2, pixelRadius * 2);
            }
        }


        // X-Achsenbeschriftung
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("X", baseWidth - 15, height - 10);

        // Y-Achsenbeschriftung
        g.drawString("Y", 10, 15);

        //Legende
        showLegend(showSolution, messageSource, locale, baseWidth, g, clusters, colors);

        g.dispose();

        try (var baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            String base64EncodedImage = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "<img src=\"data:image/png;base64," + base64EncodedImage + "\" alt=\"K-Means Result\">";
        } catch (IOException e) {
            return "<p>" + e.getMessage() + "</p>";
        }
    }

    private static void showLegend(boolean showSolution, MessageSource messageSource, Locale locale, int baseWidth, Graphics2D g, List<Cluster> clusters, Color[] colors) {
        int legendX = baseWidth + 20;
        int legendY = 30;
        int lineHeight = 20;

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString(messageSource.getMessage("legend.title", null, locale), legendX, legendY);

        if (showSolution) {
            for (int i = 0; i < clusters.size(); i++) {
                g.setColor(colors[i]);
                g.fillOval(legendX, legendY + 10 + i * lineHeight, 10, 10);
                g.setColor(Color.BLACK);
                g.drawString(messageSource.getMessage("legend.clusterDataPoints",
                    new Object[]{i + 1}, locale), legendX + 20, legendY + 20 + i * lineHeight);
            }
        }
        else {
            g.setColor(Color.GRAY);
            g.fillOval(legendX, legendY + 10, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString(messageSource.getMessage("legend.dataPoint", null, locale),
                legendX + 20, legendY + 20);
        }

        // Zentrum gilt immer
        g.setColor(Color.BLACK);
        g.fillRect(legendX, legendY + 10 + (showSolution ? clusters.size() : 1) * lineHeight, 10, 10);
        g.drawString(messageSource.getMessage("legend.centroid", null, locale),
            legendX + 20, legendY + 20 + (showSolution ? clusters.size() : 1) * lineHeight);
    }

}
