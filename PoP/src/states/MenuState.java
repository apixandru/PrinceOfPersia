package states;

import entities.Torch;
import framework.Loader;
import framework.Writter;
import game.Game;
import input.Key;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import map.Background;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

import static framework.Loader.loadImage;
import static kuusisto.tinysound.TinySound.loadMusic;

public class MenuState extends State{

    private final Background bg;
    private final BufferedImage title;
    private final BufferedImage sword;
    private final BufferedImage[] options;

    private int currentChoice = 0;

    private final Sound moving;
    private final Sound choosing;
    private final Music menu;

    private final Torch t1;
    private final Torch t2;

    public MenuState(GameStateManager gsm, ConcurrentLinkedQueue<Key> keys, Hashtable<String, Integer> keysMapped, Loader loader, Writter writter) {
        super(gsm, keys, keysMapped, loader, writter);
        bg = new Background("/Sprites_400/Menu/room_won.png");
        title = loadImage("/Sprites_400/Title/main titles/game name.png");
        options = new BufferedImage[]{
                loadImage("/Sprites_400/Menu/campaign.png"),
                loadImage("/Sprites_400/Menu/versus.png"),
                loadImage("/Sprites_400/Menu/settings.png"),
                loadImage("/Sprites_400/Menu/exit.png")
        };
        sword = loadImage("/Sprites_400/Menu/sword.png");
        menu = loadMusic("/Music/intro_theme.ogg");
        moving = loader.getSound("sword moving");
        choosing = loader.getSound("sword vs sword");
        t1 = new Torch(232, 265, loader, true);
        t2 = new Torch(468, 265, loader, true);
    }

    @Override
	public void init() {
		menu.play(true);
	}

	@Override
	public void update(long elapsedTime) {
		manageKeys();
		t1.update(elapsedTime);
		t2.update(elapsedTime);
	}

	@Override
	public void draw(Graphics2D g) {
		bg.draw(g);
		g.drawImage(title, Game.WIDTH/2 - title.getWidth()/2,
				Game.HEIGHT/4 - title.getHeight()/2, null);
		
		for (int i = 0; i < options.length; i++) {
			g.drawImage(options[i], Game.WIDTH/2 - options[i].getWidth()/2,
					Game.HEIGHT/2 - 55*Game.SCALE + i*20*Game.SCALE + options[0].getHeight(),
					null);
		}
		
		
		t1.drawSelf(g);
		t2.drawSelf(g);
		
		g.drawImage(sword, 
				Game.WIDTH/2 - options[currentChoice].getWidth()/2 - sword.getWidth() - 10*Game.SCALE,
				Game.HEIGHT/2 - 47*Game.SCALE + currentChoice*20*Game.SCALE,null);
	}
	
	public void select(){
		if(currentChoice == 0){
			choosing.play();
			menu.stop();
			gsm.setState(GameStateManager.MAINGAMESTATE);
			//campaign
		}
		else if(currentChoice == 1){
			menu.stop();
			gsm.setState(GameStateManager.MULTIPLAYERMENUSTATE);
		}
		else if(currentChoice == 2){
			choosing.play();
			//start settings
		}
		else{
			choosing.play();
			System.exit(0);
		}
	}

	@Override
	public void manageKeys() {
		Object[] keys_used = keys.toArray();
		keys.clear();
		Key e;
		if(keys_used.length!=0){
			for (int i = 0; i < keys_used.length; i++) {
				e = (Key)keys_used[i];
				if(e.isPressed()){
					
					/* key pressed */
					int key_pressed = e.getKeycode();
					
					if(key_pressed == keys_mapped.get(Key.UP)||
							key_pressed == keys_mapped.get(Key.W)){
						moving.play();
						currentChoice = (currentChoice + 3)%4;
					} else if(key_pressed == keys_mapped.get(Key.DOWN) ||
							key_pressed == keys_mapped.get(Key.S)){
						moving.play();
						currentChoice = (currentChoice + 1)%4;
					} else if(key_pressed == keys_mapped.get(Key.LEFT)){
						
					} else if(key_pressed == keys_mapped.get(Key.RIGHT)){
						
					} else if(key_pressed == keys_mapped.get(Key.ENTER)|| 
							key_pressed == keys_mapped.get(Key.C)){
						select();
					}
				}
			}
		}
	}
}
