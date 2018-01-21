package map;

import framework.Loader;
import game.Game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Background {

    protected final BufferedImage image;
    protected int x, y;
    protected int dx, dy;

    public Background(String path) {
        this(Loader.loadImage(path));
    }

    private Background(BufferedImage image) {
        this.image = image;
    }

    Background(String s, int x, int y) {
        this(s);
        this.x = x;
        this.y = y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setVel(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void update(long elapsedTime) {
        this.x = (x + dx) % 640;
        this.y = y + dy;
    }

    public void draw(Graphics2D g) {
        g.drawImage(image, x, y, null);
        if (x < 0) {
            g.drawImage(image, x + Game.WIDTH, y, null);
        }
        if (x > 0) {
            g.drawImage(image, x - Game.WIDTH, y, null);
        }
    }

}
