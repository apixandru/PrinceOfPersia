package framework;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Loader {

	private long frameTime;
	
	public Loader(long frameTime) {
		this.frameTime = frameTime;
	}
	
	public Hashtable<String, Animation> loadCharacterAnimations(String characterPath) {
		Hashtable<String, Animation> animations = new Hashtable<String, Animation>();
		
		/* Searches for .png files for each folder of characterPath */
		File dir = new File(characterPath);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			
			if (files != null) {
				for(File f : files) {
					if (f.isDirectory()) {
						
						/* folder f contains .png files */
						Animation anim = loadAnimation(f,false);
						animations.put(anim.getId(), anim);
					}
				}
			}
			
		}
		
		return animations;
	}
	
	/**
	 * 
	 * @param f directory containing all frames of
	 * one animation
	 * @return new animation loaded
	 */
	public Animation loadAnimation(File f, boolean infinite) {
		Animation animation = new Animation(f.getName(), infinite);
		
		File[] images = f.listFiles();
		for(File image : images) {
			
			/* Loads one image as a frame of the animation */
			String name = image.getName();
			if (name.substring(name.length() - 4, name.length()).equals(".png")) {
				
				Frame frame = loadFrame(image);
				animation.addFrame(frame, frameTime);
			}
		}
		
		return animation;
	}
	
	/**
	 * 
	 * @param image
	 * @return a new frame loaded from file image
	 */
	public Frame loadFrame(File image) {
		Frame frame = null;
		BufferedImage img = null;
		
		try{
			img = ImageIO.read(image);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		frame = new Frame(img, frameTime);
		return frame;
	}
	
	/**
	 * 
	 * @return a new level with its content loaded
	 */
	public Level loadLevel(int numLevel) {
		Level level = new Level(numLevel);
		String levelPath = "resources/Levels/level" + numLevel + ".txt";
		String roomContent = "";
		int row = 1;
		int col = 1;
		
		/* Reads the file that describes the level */
		File levelFile = new File(levelPath);
		Scanner readLevel;
		
		try {
			
			readLevel = new Scanner(levelFile);
			while (readLevel.hasNextLine()) {
				
				String line = readLevel.nextLine();
				
				if (line.contains("room")) {
					
					Scanner readLine = new Scanner(line);
					readLine.next();
					row = readLine.nextInt();
					col = readLine.nextInt();
					readLine.close();
					
					/* Reads the info of the new room (4 lines) */
					roomContent += readLevel.nextLine() + "\n";
					roomContent += readLevel.nextLine() + "\n";
					roomContent += readLevel.nextLine() + "\n";
					roomContent += readLevel.nextLine();
					
					Room newRoom = loadRoom(row, col, roomContent);
					level.addRoom(newRoom);
					
					roomContent = "";
				}
				
			}
			
			readLevel.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return level;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public Room loadRoom(int row, int col, String roomContent) {
		Room room = new Room(row, col);
		
		
		
		return room;
	}
}
