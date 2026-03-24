package Client.UI.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Can create and add icons at panels with any size and path given
 */
public class LogoMaker {

    public static JLabel createLogo(String path, int width, int height) {
        try {
            InputStream is = LogoMaker.class.getResourceAsStream(path);

            if (is == null) {
                System.err.println("Couldn't find the file: " + path);
                return new JLabel("No Logo");
            }

            Image img = ImageIO.read(is);
            Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            return new JLabel(new ImageIcon(scaledImage));

        } catch (IOException e) {
            System.err.println("Logo Reading Error: " + e.getMessage());
            return new JLabel("Error");
        }
    }

    public static void addLogoTo(JPanel panel, String path, int width, int height, float alignmentX, int bottomPadding) {
        JLabel logo = createLogo(path, width, height);
        logo.setAlignmentX(alignmentX);
        panel.add(logo);
        if (bottomPadding > 0) {
            panel.add(Box.createVerticalStrut(bottomPadding));
        }
    }
}
