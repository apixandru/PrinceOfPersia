package entities;

import framework.Loader;

public class Base extends Entity{
	
	public Base(int x, int y, Loader loader, String base_type) {
		super("Base", x, y, loader);
		animations = loader.getAnimations("bases");
		currentAnimation = animations.get(base_type);
		
		/* Sets the bounding box */
		enableBoundingBox(this.x,this.y,currentAnimation.getImage().getWidth(),
				currentAnimation.getImage().getHeight());
	}

}
