package map;

import game.Game;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static framework.Loader.loadImage;

public class MobileBackground extends Background {

    private ArrayList<BufferedImage> backgrounds;
    private int posAuxX;
    private int posAuxY;
    private int currentBackground;
    private BufferedImage arrow_left;
    private BufferedImage arrow_right;
    private boolean drawArrows;

    public MobileBackground(String s) {
        super(s);
        // TODO Auto-generated constructor stub
        backgrounds = new ArrayList<BufferedImage>();
        backgrounds.add(super.image);
        posAuxX = 0;
        posAuxY = 0;
        currentBackground = 0;
        arrow_left = loadImage("/Sprites_400/Menu/Scenaries/arrow_left.png");
        arrow_right = loadImage("/Sprites_400/Menu/Scenaries/arrow_right.png");
    }

    public MobileBackground(String s, int x, int y) {
        super(s, x, y);
        backgrounds = new ArrayList<BufferedImage>();
        backgrounds.add(super.image);
        posAuxX = 0;
        posAuxY = 0;
        currentBackground = 0;
        arrow_left = loadImage("/Sprites_400/Menu/Scenaries/arrow_left.png");
        arrow_right = loadImage("/Sprites_400/Menu/Scenaries/arrow_right.png");
    }

    public void addImage(String imagePath) {
        backgrounds.add(loadImage(imagePath));
    }

    @Override
    public void setVel(int dx, int dy) {
        super.setVel(dx, dy);
        if (dx < 0) {
            arrow_right = loadImage("/Sprites_400/Menu/Scenaries/arrow_right.png");
        } else {
            arrow_left = loadImage("/Sprites_400/Menu/Scenaries/arrow_left.png");
        }
    }

    @Override
	public void update(long elapsedTime){
		this.x = (x + dx) % (640*backgrounds.size());
		this.posAuxX = posAuxX + dx;
		this.posAuxY = posAuxY + dy;
		this.y = y + dy;
		if(Math.abs(posAuxY) == backgrounds.get(currentBackground).getHeight()){
			posAuxY = 0;
			this.x = 0;
			this.y = 0;
			currentBackground = (currentBackground + 1) % backgrounds.size();
		}
		else if(Math.abs(posAuxX) == 640){
			posAuxX = 0;
			this.x = 0;
			this.y = 0;
			if(dx < 0){
				currentBackground = (currentBackground + 1)%backgrounds.size();
			} else{
				currentBackground = (currentBackground + backgrounds.size() -1)%backgrounds.size();
			}
			this.dx = 0;
			this.dy = 0;
			try {
				arrow_right = ImageIO.read(getClass().getResourceAsStream("/Sprites_400/Menu/Scenaries/arrow_right.png"));
				arrow_left = ImageIO.read(getClass().getResourceAsStream("/Sprites_400/Menu/Scenaries/arrow_left.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void draw(Graphics2D g){
		g.drawImage(backgrounds.get(currentBackground), x, y, null);

		if(y < 0){
			g.drawImage(backgrounds.get((currentBackground+1)%backgrounds.size()),
					x, y + Game.HEIGHT,	null);
		}
		if(x < 0){
			g.drawImage(backgrounds.get((currentBackground + 1)%backgrounds.size()),
					x + Game.WIDTH, y, null);
		}
		if(x > 0){
			g.drawImage(backgrounds.get((currentBackground + backgrounds.size() - 1)%backgrounds.size()),
					x - Game.WIDTH, y, null);
		}

		if(drawArrows){
			g.drawImage(arrow_left, 10,
					Game.HEIGHT/2 - arrow_left.getHeight()/2, null);
			g.drawImage(arrow_right, Game.WIDTH - arrow_right.getWidth() - 10,
					Game.HEIGHT/2 - arrow_right.getHeight()/2, null);
		}

	}

	public void setDrawArrows(boolean drawArrows){
		this.drawArrows = drawArrows;
	}

	public void setCurrentBackground(int currentBackground){
		this.currentBackground = currentBackground;
	}

	public int getCurrentBackground(){
		return this.currentBackground;
	}

	public int getVelx(){
		return this.dx;
	}

}
