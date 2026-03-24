package Client.UI.panels;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundImage extends JPanel {
    private Image backgroundImage;

    public BackgroundImage() {
        try {
            // Maven yapısında 'resources' kök dizindir.
            // Başındaki "/" resources'ın içinden başla demektir.
            InputStream is = getClass().getResourceAsStream("/movie-background.jpg");

            if (is != null) {
                this.backgroundImage = ImageIO.read(is);
            } else {
                System.err.println("❌ Arka plan resmi kaynaklarda bulunamadı: /assets/movie-background.jpg");
            }
        } catch (IOException e) {
            System.err.println("❌ Resim okuma hatası: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Arka planın düzgün çizilmesi için super çağrısı şart
        super.paintComponent(g);

        if (backgroundImage != null) {
            // Resmi panel boyutuna yayarak çiz
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}