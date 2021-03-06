package entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Hashtable;

import framework.Loader;
import game.Game;
import input.Key;
import kuusisto.tinysound.Music;

import static kuusisto.tinysound.TinySound.loadMusic;

public class Player extends Character {

	public enum PlayerState {IDLE, MOVE, JUMP, COMBAT, COLLIDED, DIED};
	
	/* Constants */
	private final String RUNNING_START = "running start";
	private final String RUNNING = "running";
	private final int FRAME_DURATION = 5;
	private final int MOVE_SPEED = 2;
	private final int SAFE_FALL_DISTANCE = 100;
	private final int RISKY_FALL_DISTANCE = 250;
	
	private boolean up_pressed;
	private boolean down_pressed;
	private boolean right_pressed;
	private boolean left_pressed;
	private boolean shift_pressed;
	
	private PlayerState currentState;
	private Entity cornerToClimb;
	private Entity cornerToClimbDown;
	private boolean cornerReached;
	
	private boolean changed_position;
	private String newOrientation;
	
	private boolean canMakeStep;
	private boolean canWalkCrouched;
	private boolean canClimb;
	private boolean canClimbDown;
	private boolean startsClimbing;
	private boolean cornerPositionFixed;
	private boolean hanged;
	private boolean forcedToStop;
	private boolean onTheEdge;
	private boolean canLand;
	private boolean canLandScalingDown;
	private boolean canLongLand;
	private boolean longLand;
	private boolean canSimpleLand;
	private boolean simpleLand;
	private boolean fallCollided;
	private boolean straightFall;
	private boolean canDrink;
	private boolean drinkingPotion;
	private boolean canPickSword;
	private boolean pickingSword;
	private int fallDistance;
	private boolean screaming;
	private boolean landingFall;
	private boolean riskyLand;
	private boolean recovered;
	private boolean canPotionSplash;
	private boolean canSwordSplash;
	
	private boolean hasSword;
	
	private boolean enemySaw;	
	private boolean combatStepRight;
	private boolean combatStepLeft;
	private boolean combatAttack;
	private boolean combatDefense;
	private boolean combatCanMove;
	private boolean combatCanAttack;
	private boolean combatCanDefense;
	private boolean wantCombat;
	private boolean beenBlocked;
	private boolean hasBlocked;
	private boolean goingToBlock;
	private boolean goingToAttack;
	private boolean goingToCounter;
	
    private final Music drinking_song;
	
	private int doorx;
	private int doory;
	private boolean finalDoorOpened;
	private boolean finished;
	
	public Player(int x, int y, Loader loader, int hp, String orientation) {
		super(x, y, loader, orientation);
		animations = loader.getAnimations("Dastan");
		
		currentAnimation = animations.get("idle_" + this.orientation);
		currentState = PlayerState.IDLE;
		
		boundingBox = new Rectangle(x,y,currentAnimation.getImage().getWidth(),
				currentAnimation.getImage().getHeight());
		
		this.changed_position = false;
		this.newOrientation = orientation;
		
		this.right_pressed = false;
		this.left_pressed = false;
		this.shift_pressed = false;
		this.up_pressed = false;
		this.down_pressed = false;
		
		this.canMakeStep = true;
		this.canWalkCrouched = true;
		this.canClimb = false;
		this.startsClimbing = false;
		this.cornerPositionFixed = false;
		this.hanged = false;
		this.forcedToStop = false;
		this.onTheEdge = false;
		this.canLand = false;
		this.canLandScalingDown = false;
		this.canLongLand = false;
		this.fallCollided = false;
		this.fallDistance = 0;
		this.canDrink = false;
		this.screaming = false;
		this.landingFall = false;
		this.riskyLand = false;
		this.canPickSword = false;
		this.canPotionSplash = false;
		this.canSwordSplash = false;
		
		this.enemySaw = false;
		
		this.hp = hp;
		this.maxHp = this.hp;
		
		this.combatStepRight = false;
		this.combatStepLeft = false;
		this.combatAttack = false;
		this.combatDefense = false;
		this.combatCanMove = true;
		this.combatCanAttack = true;
		this.combatCanDefense = true;
		this.wantCombat = true;
		this.beenBlocked = false;
		this.hasBlocked = false;
		this.goingToBlock = false;
		this.goingToAttack = false;
		this.goingToCounter = false;
		
		this.doorx = 0;
		this.doory = 0;
		this.finalDoorOpened = false;
		this.finished = false;
		
		// player starts without a sword to fight
		this.hasSword = false;
		
		this.splash = new Splash(0,0,0,0,loader,"red");
		this.life = new Life[this.maxHp];
		for(int i = 0; i < this.maxHp; i++){
			if(i < this.hp){
				this.life[i] = new Life(20 + i*16, Game.HEIGHT - 5, 0, 0, loader, "prince_full");
				this.life[i].setVisible(true);
			} else{
				this.life[i] = new Life(20 + i*16, Game.HEIGHT - 5, 0, 0, loader, "prince_empty");
				this.life[i].setVisible(true);
			}
		}
		
		this.typeOfEntity = "Player";
		
        drinking_song = loadMusic("/Music/potion.ogg");
	}
	
	@Override
	public void update(long elapsedTime) {
		super.update(elapsedTime);
		if(!right_pressed && !left_pressed && !up_pressed && currentState != PlayerState.COLLIDED
				&& currentState != PlayerState.COMBAT && currentState != PlayerState.DIED){
			currentState = PlayerState.IDLE;
		} 
		if(this.currentState != PlayerState.COLLIDED && this.currentState != PlayerState.COMBAT
				&& this.currentState != PlayerState.DIED){
			if(right_pressed || left_pressed){
				if(right_pressed && !left_pressed){
					if(this.getOrientation().equals("left") && !changed_position){
						this.currentState = PlayerState.IDLE;
					} else{
						this.currentState = PlayerState.MOVE;
					}
				} else if(!right_pressed && left_pressed && !changed_position){
					if(this.getOrientation().equals("right")){
						this.currentState = PlayerState.IDLE;
					} else{
						this.currentState = PlayerState.MOVE;
					}
				} 
			}
		}
		if(up_pressed && currentState != PlayerState.COLLIDED && currentState != PlayerState.COMBAT
				&& currentState != PlayerState.DIED){
			this.currentState = PlayerState.JUMP;
		}
		if(sword!=null){
			sword.update(elapsedTime);
		}
		manageAnimations();
		updateSpeed();
		this.moveCharacter();
		this.moveSword();
		this.enableBoundingBox();
		
		// player's life
		for(int i = 0; i < this.maxHp; i++){
			if(i < this.hp){
				this.life[i] = new Life(20 + i*16, Game.HEIGHT - 5, 0, 0, loader, "prince_full");
				this.life[i].setVisible(true);
			} else{
				this.life[i] = new Life(20 + i*16, Game.HEIGHT - 5, 0, 0, loader, "prince_empty");
				this.life[i].setVisible(true);
			}
		}
	}
	
	@Override
	public void drawSelf(Graphics g){
		super.drawSelf(g);
	}
	
	public void drawLife(Graphics g){
		for (int i = 0; i < life.length; i++) {
			life[i].drawSelf(g);
		}	
	}
	
	public void setCollided(){
		if(this.currentState != PlayerState.COMBAT){
			this.currentState = PlayerState.COLLIDED;
		}
	}
	
	public void manageKeyPressed(int key_pressed, Hashtable<String,Integer> keys_mapped){
		if(key_pressed == keys_mapped.get(Key.UP)){
			if(this.currentState != PlayerState.DIED){
				if(this.currentState != PlayerState.COMBAT){
					up_pressed = true;
					currentState = PlayerState.JUMP;
				} else{
					if(!this.combatDefense){
						combatDefense = true;
						combatCanDefense = true;
					}
				}
			}
			
		} else if(key_pressed == keys_mapped.get(Key.RIGHT)){
			if(this.currentState != PlayerState.DIED){
				if(this.currentState != PlayerState.COMBAT){
					if(!right_pressed){
						canMakeStep = true;
						canWalkCrouched = true;
					}
					if(this.currentState != PlayerState.JUMP){
						currentState = PlayerState.MOVE;
						if(this.getOrientation().equals("left")){
							this.changed_position = true;
							this.newOrientation = "right";
						}
					}
					right_pressed = true;
				} else{
					if(!this.combatStepRight){
						this.combatStepRight = true;
						this.combatCanMove = true;
					}
				}
			}
			
		} else if(key_pressed == keys_mapped.get(Key.LEFT)){
			if(this.currentState != PlayerState.DIED){
				if(this.currentState != PlayerState.COMBAT){
					if(!left_pressed){
						canMakeStep = true;
						canWalkCrouched = true;
					}
					if(this.currentState != PlayerState.JUMP){
						currentState = PlayerState.MOVE;
						if(this.getOrientation().equals("right")){
							this.changed_position = true;
							this.newOrientation = "left";
						}
					}
					left_pressed = true;
				} else{
					if(!this.combatStepLeft){
						this.combatStepLeft = true;
						this.combatCanMove = true;
					}
				}
			}
			
		} else if(key_pressed == keys_mapped.get(Key.DOWN)){
			down_pressed = true;
			
		} else if(key_pressed == keys_mapped.get(Key.SHIFT)){
			if(this.currentState != PlayerState.DIED){
				if(this.currentState != PlayerState.COMBAT){
					if(enemySaw){
						wantCombat = true;
					}
					shift_pressed = true;
				} else{
					if(!this.combatAttack){
						this.combatCanAttack = true;
						this.combatAttack = true;
					}
				}
			}
			
		} else if(key_pressed == keys_mapped.get(Key.D)){
			
			System.out.println("State: " + this.currentState + "\n" + 
					"Animation: " + this.getCurrentAnimation().getId() + "\n" + 
					"Orientation: " + this.getOrientation() + "\n" +
					"left_pressed: " + left_pressed + "\n" + 
					"right_pressed: " + right_pressed + "\n" + 
					"up_pressed: " + up_pressed + "\n" + 
					"down_pressed: " + down_pressed + "\n" +
					"x: " + this.x + "\n" + 
					"y: " + this.y + "\n");
		}
	}
	
	public void manageKeyReleased(int key_released, Hashtable<String,Integer> keys_mapped){
		if(key_released == keys_mapped.get(Key.UP)){
			if(currentState != PlayerState.COMBAT && this.currentState != PlayerState.DIED){
				this.currentState = PlayerState.IDLE;
			}
			up_pressed = false;
			combatDefense = false;
			
		} else if(key_released == keys_mapped.get(Key.RIGHT)){
			if(currentState != PlayerState.JUMP && currentState != PlayerState.COMBAT
					&& this.getOrientation().equals("right") && this.currentState != PlayerState.DIED){
				this.currentState = PlayerState.IDLE;
			}
			right_pressed = false;
			combatStepRight = false;
			
		} else if(key_released == keys_mapped.get(Key.LEFT)){
			if(currentState != PlayerState.JUMP && currentState != PlayerState.COMBAT 
					&& this.getOrientation().equals("left") && this.currentState != PlayerState.DIED){
				this.currentState = PlayerState.IDLE;
			}
			left_pressed = false;
			combatStepLeft = false;
			
		} else if(key_released == keys_mapped.get(Key.DOWN)){
			down_pressed = false;
			
		} else if(key_released == keys_mapped.get(Key.SHIFT)){
			shift_pressed = false;
			combatAttack = false;
		}
	}
	
	private void manageAnimations(){
		
		switch(currentAnimation.getId()){
		
		case "clipping_left":
		case "clipping_right":
			
			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					canClimb = false;
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					canClimb = false;
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					canClimb = false;
				}
				break;
				
			case COLLIDED:
				System.out.println("COLLIDED EN POS TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "climbing down_left":
		case "climbing down_right":
			
			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				System.out.println("COLLIDED EN POS TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "crouching down_left":
		case "crouching down_right":
			
			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					if (this.isRiskyLand()) {
						
						// player has to recover from risky fall
						this.setCurrentAnimation("crouching recovery_" + orientation, FRAME_DURATION);
						this.setRiskyLand(false);
					}
					else {
						
						// player lands correctly
						if (this.isDrinkingPotion()) {
							loader.getSound("drinking").play();
							this.setCurrentAnimation("drinking_" + orientation, FRAME_DURATION);
						} else if(this.isPickingSword()){
							this.hasSword = true;
							this.setCurrentAnimation("got sword splash_" + orientation, FRAME_DURATION);
							this.manageSword("got sword", 0, true);
						}
						else {
							this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
						}
					}
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					if (this.isRiskyLand()) {
						
						// player has to recover from risky fall
						this.setCurrentAnimation("crouching recovery_" + orientation, FRAME_DURATION);
						this.setRiskyLand(false);
					}
					else {
					this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					if (this.isRiskyLand()) {
						
						// player has to recover from risky fall
						this.setCurrentAnimation("crouching recovery_" + orientation, FRAME_DURATION);
						this.setRiskyLand(false);
					}
					else {
						if(changed_position){
							changed_position = false;
							this.currentState = PlayerState.IDLE;
							this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
						}
						if(canWalkCrouched && !this.isLandingFall()){
							canWalkCrouched = false;
							this.setCurrentAnimation("crouching walk_" + orientation, FRAME_DURATION);
						} else{
							this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
						}
					}
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "crouching idle_left":
		case "crouching idle_right":
			
			this.setLandingFall(false);

			switch(currentState){
			case IDLE:
				if(this.isCanDrink() && shift_pressed){
					loader.getSound("drinking").play();
					this.setCurrentAnimation("drinking_" + orientation, FRAME_DURATION);
					this.setDrinkingPotion(true);
				} else if(this.isCanPickSword() && shift_pressed){
					this.hasSword = true;
					this.setPickingSword(true);
					this.setCurrentAnimation("got sword splash_" + orientation, FRAME_DURATION);
					manageSword("got sword",7,true);
				}
				else if(!down_pressed){
					this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
				} 
				break;
				
			case JUMP:
				this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
				break;
				
			case MOVE:
				if(canWalkCrouched){
					canWalkCrouched = false;
					if(changed_position){
						changed_position = false;
					} else{
						this.setCurrentAnimation("crouching walk_" + orientation, FRAME_DURATION);
					}
				}
				if(!down_pressed){
					this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				System.out.println("COLLIDED EN POS TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "crouching up_left":
		case "crouching up_right":

			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					canWalkCrouched = true;
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					canWalkCrouched = true;
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					canWalkCrouched = true;
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				if(this.getOrientation().equals("left")){

				} else{

				}
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "crouching walk_left":
		case "crouching walk_right":

			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					if(!down_pressed){
						this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
					} else{
						this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "crouching recovery_left":
		case "crouching recovery_right":
			
			if (currentAnimation.isOver(false)) {
				this.setRecovered(false);
			}
			
			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					if(!down_pressed){
						this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
					} else{
						this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("crouching up_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("crouching idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "dead spiked_left":
		case "dead spiked_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:
				System.out.println("COLLIDED EN ANIMATION TO RARA");
				break;
				
				
			case COMBAT:
				break;
				
			case DIED:
				//this.setmovespeed(0);
				
				break;
			default:
				
				break;
			}
			break;
			
		case "dieing_left":
		case "dieing_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:
				System.out.println("COLLIDED EN ANIMATION TO RARA");
				break;
				
				
			case COMBAT:
				break;
				
			case DIED:
				//this.setmovespeed(0);
				if(this.getCurrentAnimation().getCurrentFrame() == 0){
					setSplashVisible(true);
				} else if(this.getCurrentAnimation().getCurrentFrame() == 1){
					setSplashVisible(false);
					setCanShowSplash(false);
				}
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("normal dead_" + orientation, FRAME_DURATION);
					//TODO: CHANGE POSITION WHEN DEAD
				}
				break;
				
				
			default:
				
				break;
			}
			break;
	
		case "drinking_left":
		case "drinking_right":

			if (this.getCurrentAnimation().isOver(false)) {
				
				// Restores player's health in 1 point
				int maxHealth = this.getMaxHp();
				int currentHealth = this.getHp();
				
				if (currentHealth < maxHealth) {
					currentHealth = currentHealth + 1;
					this.setHp(currentHealth);
				}
				this.setDrinkingPotion(false);				
				this.setCurrentAnimation("drinking splash_" + orientation, FRAME_DURATION);
				drinking_song.play(false);
			}
			
			break;
			
		case "drinking splash_left":
		case "drinking splash_right":
			
			if (this.getCurrentAnimation().isOver(false)) {
				this.setCurrentAnimation("drinking to idle_" + orientation, FRAME_DURATION);
				this.setCanPotionSplash(false);
			}
			
			break;
			
		case "drinking to idle_left":
		case "drinking to idle_right":

			if (this.getCurrentAnimation().isOver(false)) {
				this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
			}
			break;
			
		case "falling_left":
		case "falling_right":
			
			if (this.isCanClimb() && shift_pressed) {
				
				// player reaches a corner in mid air
				this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
			}

			if (this.getCurrentAnimation().isOver(true)) {
				this.setCurrentAnimation("falling idle_" + orientation, FRAME_DURATION);
				
//				System.out.println("Starts free_fall");
				this.setFreeFall(true);
			}
			break;
			
		case "falling idle_left":
		case "falling idle_right":
			
			if (this.isCanClimb() && shift_pressed) {
				
				// player reaches a corner in mid air
				this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
			}
			break;
			
		case "got sword splash_left":
		case "got sword splash_right":
			
			manageSword("got sword",0,false);
			if(this.getCurrentAnimation().isOver(false)){
				this.setCurrentAnimation("got sword_" + orientation, FRAME_DURATION);
				manageSword("got sword",0,false);
				this.setCanSwordSplash(false);
			}
			break;	

		case "got sword_left":
		case "got sword_right":
			
			manageSword("got sword",this.getCurrentAnimation().getCurrentFrame(),false);
			if(this.getCurrentAnimation().isOver(false)){
				this.setCurrentAnimation("putting down sword_" + orientation, FRAME_DURATION);
				this.setPickingSword(false);
				this.sword = null;
			}
			break;
			
			
		case "hanging backwards mini_left":
		case "hanging backwards mini_right":

			switch(currentState){
			case IDLE:
				if (currentAnimation.isOver(false)) {
					setHanged(true);
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if (currentAnimation.isOver(false)) {
					this.setCurrentAnimation("clipping_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if (currentAnimation.isOver(false)) {
					setHanged(true);
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "hanging backwards_left":
		case "hanging backwards_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "hanging forward_left":
		case "hanging forward_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "hanging idle_left":
		case "hanging idle_right":

			switch(currentState){
			case IDLE:
				if (currentAnimation.isOver(false) && !shift_pressed) {
					this.setCurrentAnimation("scaling down_" + orientation, FRAME_DURATION);
					setHanged(false);
				}
				else if (currentAnimation.isOver(false) && shift_pressed && !isHanged()) {
					this.setCurrentAnimation("hanging backwards mini_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if (currentAnimation.isOver(false)) {
					this.setCurrentAnimation("clipping_" + orientation, FRAME_DURATION);
					setHanged(false);
				}
				break;
				
			case MOVE:
				if (currentAnimation.isOver(false) && !shift_pressed) {
					this.setCurrentAnimation("scaling down_" + orientation, FRAME_DURATION);
					setHanged(false);
				}
				else if (currentAnimation.isOver(false) && shift_pressed && !isHanged()) {
					this.setCurrentAnimation("hanging backwards mini_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "idle_left":
		case "idle_right":

			switch(currentState){
			case IDLE:
				if(!enemySaw || !wantCombat){
					if(changed_position){
						changed_position = false;
					} else if(down_pressed){
						
						// cancels edge settings
						this.setForcedToStop(false);
						this.setOnTheEdge(false);
						
						if (isCanClimbDown()) {
							this.setCurrentAnimation("climbing down_" + orientation, FRAME_DURATION);
						}
						else {
							this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
						}
					} else if (this.isCanDrink() && shift_pressed) {
						this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
						this.setDrinkingPotion(true);
					} else if (this.isCanPickSword() && shift_pressed) {
						this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
						this.setPickingSword(true);
					}
				} else{
					this.currentState = PlayerState.COMBAT;
				}
				break;
				
			case JUMP:
				
				// cancels edge settings
				this.setForcedToStop(false);
				this.setOnTheEdge(false);
				
				if(!enemySaw || !wantCombat){
					if(finalDoorOpened){
						this.cleanYSpeed();
						this.setX(this.doorx);
						this.setY(this.doory);
						this.setCurrentAnimation("stairs", FRAME_DURATION);
					}
					else if(right_pressed || left_pressed){
						if(right_pressed && this.currentAnimation.equals("right")){
							this.setCurrentAnimation("simple jump_" + orientation, FRAME_DURATION);
						} else if(left_pressed && this.currentAnimation.equals("left")){
							this.setCurrentAnimation("simple jump_" + orientation, FRAME_DURATION);
						} else{
							this.setCurrentAnimation("scaling up start_" + orientation, FRAME_DURATION);
							this.startsClimbing = true;
						}
					} else{
						this.setCurrentAnimation("scaling up start_" + orientation, FRAME_DURATION);
						this.startsClimbing = true;
					}
				} else{
					this.currentState = PlayerState.COMBAT;
				}
				break;
				
			case MOVE:
				if(!enemySaw || !wantCombat){
					if(changed_position){
						changed_position = false;
						
						// cancels edge settings
						this.setForcedToStop(false);
						this.setOnTheEdge(false);
						
						this.setOrientation(newOrientation);
						this.setCurrentAnimation("turning_" + orientation, FRAME_DURATION);
					} else if(shift_pressed){
						if(canMakeStep){
							if (this.isOnTheEdge()) {

								// player has arrive at the edge
								this.fall();
							}
							else {
								
								// player walks normally
								this.setCurrentAnimation("walking a step_" + orientation, FRAME_DURATION);
								canMakeStep = false;
							}
						} else{
						
						}
					} else if(down_pressed){
						this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
					}
					else{
						if (this.isOnTheEdge()) {

							// player has arrive at the edge
							this.fall();
						}
						else {
							this.setCurrentAnimation("running start_" + orientation, FRAME_DURATION);
						}
					}
				} else{
					this.currentState = PlayerState.COMBAT;
				}
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				this.setCurrentAnimation("taking sword out_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "normal dead_left":
		case "normal dead_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				
				break;
				
			case DIED:
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "putting down sword_left":
		case "putting down sword_right":

			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.wantCombat = false;
					this.currentState = PlayerState.IDLE;
				}
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.wantCombat = false;
					this.currentState = PlayerState.IDLE;
				}
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				//this.setmovespeed(0);
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.wantCombat = false;
					this.currentState = PlayerState.IDLE;
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running collided_left":
		case "running collided_right":

			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.currentState = PlayerState.IDLE;
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running jump collided_left":
		case "running jump collided_right":

			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.fall();
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.fall();
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.fall();
				}
				break;
				
			case COLLIDED:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
					this.currentState = PlayerState.IDLE;
					this.fall();
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running jump start_left":
		case "running jump start_right":
			
			// if player doesnt have floor beneath him to make the jump, he falls
			if (!this.isCanLand()) {
				this.fall();
			}
			
			this.setLongLand(true);
			
			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLand()) {
						this.setCurrentAnimation("running jump_" + orientation, FRAME_DURATION);
						
						// resets some variables
						this.setCanLongLand(false);
						this.setLongLand(false);
					}
					else {
						this.fall();
					}
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLand()) {
						this.setCurrentAnimation("running jump_" + orientation, FRAME_DURATION);
						
						// resets some variables
						this.setCanLongLand(false);
						this.setLongLand(false);
					}
					else {
						this.fall();
					}
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLand()) {
						this.setCurrentAnimation("running jump_" + orientation, FRAME_DURATION);
						
						// resets some variables
						this.setCanLongLand(false);
						this.setLongLand(false);
					}
					else {
						this.fall();
					}
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running jump_left":
		case "running jump_right":
			
			this.jumping = true;
			
			if (this.isCanClimb() && shift_pressed) {
				
				// player reaches a corner in mid air
				this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
			}

			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLongLand()) {
						this.setCurrentAnimation("running jump landing_" + orientation, FRAME_DURATION);
					}
					else {
						System.out.println("WHAT IDLE");
						this.fall();
					}
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLongLand()) {
						this.setCurrentAnimation("running jump landing_" + orientation, FRAME_DURATION);
					}
					else {
						System.out.println("WHAT JUMP");
						this.fall();
					}
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLongLand()) {
						this.setCurrentAnimation("running jump landing_" + orientation, FRAME_DURATION);
					}
					else {
						System.out.println("WHAT MOVE");
						this.fall();
					}
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
		
		case "running jump landing_left":
		case "running jump landing_right":
			
			// resets can land condition
			this.setCanLand(false);
			this.setCanLongLand(false);
			
			// resets climb up and down conditions
			if (this.getCurrentAnimation().isOver(false)) {
				this.setCanClimb(false);
				this.setCornerToClimb(null);
				this.setCanClimbDown(false);
				this.setCornerToClimbDown(null);
			}
			
			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("running_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("running_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("running_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running start_left":
		case "running start_right":

			switch(currentState){
			case IDLE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					if(this.getOrientation().equals("left")){

					} else{

					}
					this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					if(this.getOrientation().equals("left")){
						if(left_pressed){
							this.setCurrentAnimation("running jump start_" + orientation, FRAME_DURATION);
						} else{
							this.setCurrentAnimation("running stop_" + orientation, FRAME_DURATION);
						}
					} else{
						if(right_pressed){
							this.setCurrentAnimation("running jump start_" + orientation, FRAME_DURATION);
						} else{
							this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
						}
					}
				}
				break;
				
			case MOVE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					if(changed_position){
						changed_position = false;
						this.setOrientation(newOrientation);
						this.setCurrentAnimation("turn running_" + orientation, FRAME_DURATION);
					} else{
						this.setCurrentAnimation("running_" + orientation, FRAME_DURATION);
					}
				} 
				break;
				
			case COLLIDED:
				if(this.getOrientation().equals("left")){

				} else{

				}
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running stop start_left":
		case "running stop start_right":

			switch(currentState){
			case IDLE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running stop_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running stop_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					if(changed_position){
						changed_position = false;
						this.setOrientation(newOrientation);
						this.setCurrentAnimation("turn running started_" + orientation, FRAME_DURATION);
					} else{
						this.currentState = PlayerState.IDLE;
						this.setCurrentAnimation("running stop_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case COLLIDED:
				if(this.getOrientation().equals("left")){

				} else{

				}
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running stop_left":
		case "running stop_right":

			switch(currentState){
			case IDLE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					if(changed_position){
						changed_position = false;
						this.setOrientation(newOrientation);
						this.setCurrentAnimation("turning_" + orientation, FRAME_DURATION);
						
					} else{
						if(this.getOrientation().equals("left")){

						} else{

						}
						this.setCurrentAnimation("running start_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case COLLIDED:
				if(this.getOrientation().equals("left")){

				} else{

				}
				
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "running_left":
		case "running_right":
			
			
//			if (currentAnimation.getId().equals("running_left")) {
//				this.setCurrentAnimation("turn running_right", FRAME_DURATION);
//			}
//			else if (currentAnimation.getId().equals("running_right")) {
//				this.setCurrentAnimation("turn running_left", FRAME_DURATION);
//			}
//			else {
				
			switch(currentState){
			case IDLE:
				this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
				break;
				
			case JUMP:
				if(this.getOrientation().equals("left")){
					if(left_pressed){
						this.setCurrentAnimation("running jump start_" + orientation, FRAME_DURATION);
					} else{
						this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
					}
				} else{
					if(right_pressed){
						this.setCurrentAnimation("running jump start_" + orientation, FRAME_DURATION);
					} else{
						this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
					}
				}
				
				break;
				
			case MOVE:
				if(this.getOrientation().equals("left")){
					
				} else{
					
				}
				if(changed_position){
					changed_position = false;
					this.setOrientation(newOrientation);
					this.setCurrentAnimation("turn running_" + orientation, FRAME_DURATION);
				} else if(down_pressed){
					this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				if(this.getOrientation().equals("left")){
			
				} else{
					
				}
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
//			}
			
		case "scaling down_left":
		case "scaling down_right":
			
			if (currentAnimation.isOver(false)) {
				this.setCanClimb(false);
				this.setCanClimbDown(false);
				this.setCornerToClimb(null);
			}
			
			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			default:
				
				break;
				
			case COLLIDED:
				System.out.println("COLISIONO EN POS TO RARA");
				break;
			}
			break;
			
		case "scaling fall_left":
		case "scaling fall_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "scaling up start_left":
		case "scaling up start_right":
			
//			System.out.println("START");

			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("scaling up_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(right_pressed || left_pressed){
					if(right_pressed && orientation.equals("right")){
						this.setCurrentAnimation("simple jump_" + orientation, FRAME_DURATION);
					} else if(left_pressed && orientation.equals("left")){
						this.setCurrentAnimation("simple jump_" + orientation, FRAME_DURATION);
					} else{
						if(this.currentAnimation.isOver(false)){
							this.setCurrentAnimation("scaling up_" + orientation, FRAME_DURATION);
						}
					}
				} else{
					if(this.currentAnimation.isOver(false)){
						this.setCurrentAnimation("scaling up_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case MOVE:
				if(right_pressed || left_pressed){
					if(right_pressed && orientation.equals("right")){
						this.setCurrentAnimation("simple jump_" + orientation, FRAME_DURATION);
					} else if(left_pressed && orientation.equals("left")){
						this.setCurrentAnimation("simple jump_" + orientation, FRAME_DURATION);
					} else{
						if(this.currentAnimation.isOver(false)){
							this.setCurrentAnimation("scaling up_" + orientation, FRAME_DURATION);
						}
					}
				} else{
					if(this.currentAnimation.isOver(false)){
						this.setCurrentAnimation("scaling up_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case COLLIDED:
				System.out.println("COLISIONO EN POS TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "scaling up_left":
		case "scaling up_right":
			
			// ends the initial climb jump
			this.startsClimbing = false;

			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false) && canClimb){
					this.setCurrentAnimation("scaling to hanging_" + orientation, FRAME_DURATION);
				}
				else if (this.currentAnimation.isOver(false) && !canClimb) {
					this.setCurrentAnimation("scaling down_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false) && canClimb){
					this.setCurrentAnimation("scaling to hanging_" + orientation, FRAME_DURATION);
				}
				else if (this.currentAnimation.isOver(false) && !canClimb) {
					this.setCurrentAnimation("scaling down_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false) && canClimb){
					this.setCurrentAnimation("scaling to hanging_" + orientation, FRAME_DURATION);
				}
				else if (this.currentAnimation.isOver(false) && !canClimb) {
					this.setCurrentAnimation("scaling down_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				System.out.println("COLISIONO EN UNA ANIMACION TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "scaling to hanging_left":
		case "scaling to hanging_right":
			
			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				System.out.println("COLISIONO EN UNA ANIMACION TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "simple jump_left":
		case "simple jump_right":

			this.jumping = true;
			this.startsClimbing = false;
			
			if (this.isCanClimb() && shift_pressed) {
				
				// player reaches a corner in mid air
				this.setCurrentAnimation("hanging idle_" + orientation, FRAME_DURATION);
			}

			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLongLand()) {
						this.setCurrentAnimation("simple jump landing_" + orientation, FRAME_DURATION);
					}
					else {
						this.setCurrentAnimation("simple jump fall_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLongLand()) {
						this.setCurrentAnimation("simple jump landing_" + orientation, FRAME_DURATION);
					}
					else {
						this.setCurrentAnimation("simple jump fall_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					if (this.isCanLongLand()) {
						this.setCurrentAnimation("simple jump landing_" + orientation, FRAME_DURATION);
					}
					else {
						this.setCurrentAnimation("simple jump fall_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "simple jump fall_left":
		case "simple jump fall_right":
			
			switch(currentState){
			case IDLE:
				if (this.currentAnimation.isOver(false)){
					this.fall();
				}
				break;
			case JUMP:
				if (this.currentAnimation.isOver(false)){
					this.fall();
				}
				break;
			case MOVE:
				if (this.currentAnimation.isOver(false)){
					this.fall();
				}
				break;
			case COLLIDED:
				this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
				break;
			default:
				break;
			}
			break;
			
		case "simple jump landing_left":
		case "simple jump landing_right":

			// resets can land condition
			this.setCanLand(false);
			
			// resets climb up and down conditions
			if (this.getCurrentAnimation().isOver(false)) {
				this.setCanClimb(false);
				this.setCornerToClimb(null);
				this.setCanClimbDown(false);
				this.setCornerToClimbDown(null);
			}
			
			switch(currentState){
			case IDLE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
			
		case "stairs":
			if(this.getCurrentAnimation().isOver(false)){
				finished = true;
				this.setCurrentAnimation("empty", FRAME_DURATION);
			}
			break;
			
		case "sword attack start_left":
		case "sword attack start_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("start attacking", this.currentAnimation.getCurrentFrame(), false);
				if(combatCanDefense && combatDefense){
					this.goingToCounter = true;
					this.combatCanDefense = false;
				}
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					if(!beenBlocked){
						this.goingToCounter = false;
						this.setCurrentAnimation("sword attack end_" + orientation, FRAME_DURATION);
						manageSword("end attacking",0,false);
					} else{
						beenBlocked = false;
						if(goingToCounter){
							this.goingToCounter = false;
							this.setCurrentAnimation("sword blocked and block_" + orientation, FRAME_DURATION);
							manageSword("defending after block",0,false);
						} else{
							System.out.println("He sido bloquiado");
							this.setCurrentAnimation("sword attack end blocked_" + orientation, FRAME_DURATION);
							manageSword("end attacking blocked",0,false);
						}
					}
				}
				
			default:
				
				break;
			}
			break;
			
		case "sword attack end blocked_left":
		case "sword attack end blocked_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("end attacking blocked", this.getCurrentAnimation().getCurrentFrame(), false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
					manageSword("idle", 0, false);
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword attack end_left":
		case "sword attack end_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("end attacking", this.getCurrentAnimation().getCurrentFrame(), false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
					manageSword("idle", 0, false);
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword attack up start_left":
		case "sword attack up start_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("start attacking up",this.getCurrentAnimation().getCurrentFrame(),false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					if(!beenBlocked){
						this.setCurrentAnimation("sword attack end_" + orientation, FRAME_DURATION);
						manageSword("end attacking",0,false);
					} else{
						beenBlocked = false;
						this.setCurrentAnimation("sword blocked_" + this.orientation, FRAME_DURATION);
						manageSword("blocked",0,false);
					}
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword blocked_left":
		case "sword blocked_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("blocked", this.getCurrentAnimation().getCurrentFrame(), false);
				if(this.combatCanDefense && combatDefense){
					this.goingToBlock = true;
					this.combatCanDefense = false;
				}
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					if(this.goingToBlock){
						this.goingToBlock = false;
						this.setCurrentAnimation("sword blocked and block_" + orientation, FRAME_DURATION);
						manageSword("defending after block",0,false);
					} else{
						this.setCurrentAnimation("sword attack end blocked_" + orientation, FRAME_DURATION);
						manageSword("end attacking blocked",0,false);
					}
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword blocked and block_left":
		case "sword blocked and block_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("defending after block",this.getCurrentAnimation().getCurrentFrame(),false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					this.setCurrentAnimation("sword defense start_" + orientation, FRAME_DURATION);
					manageSword("defending start",0,false);
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword defense start_left":
		case "sword defense start_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("defending start",this.getCurrentAnimation().getCurrentFrame(),false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					this.setCurrentAnimation("sword defense end_" + orientation, FRAME_DURATION);
					manageSword("defending end",0,false);
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword defense end_left":
		case "sword defense end_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("defending end",this.getCurrentAnimation().getCurrentFrame(),false);
				if(this.combatAttack){
					this.goingToAttack = true;
					this.combatCanAttack = false;
				}
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					if(this.goingToAttack){
						hasBlocked = false;
						this.goingToAttack = false;
						this.setCurrentAnimation("sword attack up start_" + orientation, FRAME_DURATION);
						manageSword("start attacking up",0,false);
					} else if(hasBlocked){
						this.hasBlocked = false;
						this.setCurrentAnimation("sword walking backwards_" + orientation, FRAME_DURATION);
						this.manageSword("moving backwards", 0, false);
					} else{
						this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
						manageSword("idle",0,false);
					}
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword walking backwards_left":
		case "sword walking backwards_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("moving backwards",this.getCurrentAnimation().getCurrentFrame(),false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
					manageSword("idle",0,false);
				}
				break;
				
			default:
				
				break;
			}
			break;
		
		case "sword hit_left":
		case "sword hit_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("hit",this.getCurrentAnimation().getCurrentFrame(),false);
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
					manageSword("idle",0,false);
					setCanShowSplash(true);
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		
			
		case "sword idle_left":
		case "sword idle_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("idle",0,false);
				//this.setmovespeed(0);
				if(this.combatCanMove && combatStepRight){
					this.combatCanMove = false;
					//this.setmovespeed(MOVE_SPEED);
					if(this.getOrientation().equals("right")){
						this.setCurrentAnimation("sword walking_" + orientation, FRAME_DURATION);
						manageSword("moving forward",0,false);
					} else{
						this.setCurrentAnimation("sword walking backwards_" + orientation, FRAME_DURATION);
						manageSword("moving backwards",0,false);
					}
				} else if(this.combatCanMove && combatStepLeft){
					this.combatCanMove = false;
					//this.setmovespeed(-MOVE_SPEED);
					if(this.getOrientation().equals("left")){
						this.setCurrentAnimation("sword walking_" + orientation, FRAME_DURATION);
						manageSword("moving forward",0,false);
					} else{
						this.setCurrentAnimation("sword walking backwards_" + orientation, FRAME_DURATION);
						manageSword("moving backwards",0,false);
					}
				} else if(this.combatCanDefense && combatDefense){
					this.combatCanDefense = false;
					this.setCurrentAnimation("sword defense start_" + orientation, FRAME_DURATION);
					manageSword("defending start",this.getCurrentAnimation().getCurrentFrame(),false);
				} else if(this.combatCanAttack && this.combatAttack){
					this.combatCanAttack = false;
					this.setCurrentAnimation("sword attack start_" + orientation, FRAME_DURATION);
					manageSword("start attacking", 0, false);
				} else if(down_pressed){
					this.setCurrentAnimation("putting down sword_" + orientation, FRAME_DURATION);
					this.sword = null;
				}
				
				break;
				
			default:
				
				break;
			}
			break;
			
		case "sword walking_left":
		case "sword walking_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:

				break;
				
			case COMBAT:
				manageSword("moving forward", this.currentAnimation.getCurrentFrame(), false);
				if(this.currentAnimation.isOver(false)){
					//this.setmovespeed(0);
					this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
					manageSword("idle", 0, false);
				}
				break;
				
			default:
				
				break;
			}
			break;
			
		case "taking sword out_left":
		case "taking sword out_right":

			switch(currentState){
			case IDLE:
				
				break;
				
			case JUMP:
				
				break;
				
			case MOVE:
				
				break;
				
			case COLLIDED:
				
				break;
				
			case COMBAT:
				//this.setmovespeed(0);
				if(this.currentAnimation.isOver(false)){
					this.setCurrentAnimation("sword idle_" + orientation, FRAME_DURATION);
					manageSword("idle", 0, true);
				}
				break;
				
			default:
				
				break;
			}
			break;

			
		case "turn running_left":
		case "turn running_right":
		case "turn running started_left":
		case "turn running started_right":

//			if (currentAnimation.isOver(false)) {
//				this.setCurrentAnimation("turning to running_" + orientation, FRAME_DURATION);
//			}
//			else {
				
			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running jump start_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("turning to running_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
//			}
			
		case "turning to running_left":
		case "turning to running_right":
			
//			if (currentAnimation.isOver(false)) {
//				this.setCurrentAnimation("running_" + orientation, FRAME_DURATION);
//			}
//			else {
				
			switch(currentState) {
			case IDLE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running stop start_" + orientation, FRAME_DURATION);
				}
				break;
			
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running jump start_" + orientation, FRAME_DURATION);
				}
				break;
			
			case MOVE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("running_" + orientation, FRAME_DURATION);
				}
				break;
			
			case COLLIDED:
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
			
			default:
				break;
			}
			break;
//			}
			
		case "turning_left":
		case "turning_right":

			switch(currentState){
			case IDLE:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("scaling up start_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(currentAnimation.isOver(false)){
					if(changed_position){
						changed_position = false;
						this.setOrientation(newOrientation);
						this.setCurrentAnimation("turning_" + orientation, FRAME_DURATION);
					} else if(shift_pressed){
						if(canMakeStep){
							if(this.getOrientation().equals("left")){

							} else{

							}
							this.setCurrentAnimation("walking a step_" + orientation, FRAME_DURATION);
							canMakeStep = false;
						} else{

						}
					} else{
						if(this.getOrientation().equals("left")){

						} else{

						}
						this.setCurrentAnimation("running start_" + orientation, FRAME_DURATION);
					}
				}
				break;
				
			case COLLIDED:
				System.out.println("COLISIONO EN UNA POS TO RARA");
				break;
				
			default:
				
				break;
			}
			break;
			
		case "walking a step_left":
		case "walking a step_right":

			switch(currentState){
			case IDLE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case JUMP:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case MOVE:
				if(this.getOrientation().equals("left")){

				} else{

				}
				if(currentAnimation.isOver(false)){
					this.setCurrentAnimation("idle_" + orientation, FRAME_DURATION);
				}
				break;
				
			case COLLIDED:
				if(this.getOrientation().equals("left")){

				} else{

				}
				this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
				break;
				
			default:
				
				break;
			}
			break;
					
		default:
			System.out.println("ANIMATION NOT RECOGNIZED");
			break;
		}
	}

	/**
	 * @return the currentState
	 */
	public PlayerState getCurrentState() {
		return currentState;
	}
	
	/**
	 * @return the fallDistance
	 */
	public int getFallDistance() {
		return fallDistance;
	}

	/**
	 * @param fallDistance the fallDistance to set
	 */
	public void setFallDistance(int fallDistance) {
		this.fallDistance = fallDistance;
	}
	
	public boolean isSafeFall() {
		return this.fallDistance <= SAFE_FALL_DISTANCE;
	}
	
	public boolean isRiskyFall() {
		return this.fallDistance <= RISKY_FALL_DISTANCE;
	}
	
	public boolean isDeadlyFall() {
		return this.fallDistance > RISKY_FALL_DISTANCE;
	}
	
	public void fall() {
		
		// corrects multiple condition values
		this.notJumping();
		this.setForcedToStop(false);
		this.setOnTheEdge(false);
		this.setCornerToClimb(null);
		this.setCornerToClimbDown(null);
		this.setFallDistance(0);
		this.setFallingSpeed(0);
		
		// lands variables resets
		this.setCanLongLand(false);
		this.setLongLand(false);
		
		this.setCurrentAnimation("falling_" + orientation, FRAME_DURATION);
		int fallOffset = 20;		
//		if (orientation.equals("left")) {
//			this.move(-fallOffset/2, 0);
//			System.out.println("Fall offset left applied");
//		}
//		else if (orientation.equals("right")) {
//			this.move(fallOffset*2/3, 0);
//			System.out.println("Fall offset right applied");
//		}
		this.falling = true;
	}
	
	public void safeLand() {
		this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
		
		// Only corrects player position if it is a short fall
//		if (!isFreeFall()) {
//			this.move(0, -20);
//		}
//		
//		else {
//			this.move(0, -1);
//		}
		
		this.enableBoundingBox();
	}
	
	public void riskyLand() {
		this.setRiskyLand(true);
		this.setCurrentAnimation("crouching down_" + orientation, FRAME_DURATION);
		this.enableBoundingBox();
		
		// player gets damaged by 1 hit point
		loader.getSound("kid hurt").play();
		this.hp = this.hp - 1;
		if (this.hp == 0) {
			this.currentState = PlayerState.DIED;
			this.sword = null;
			this.setCurrentAnimation("dieing_" + orientation, FRAME_DURATION);
		}
	}
	
	public void die() {
		
		// player dies from a long fall
		this.hp = 0;
		this.currentState = PlayerState.DIED;
		this.sword = null;
		this.setCurrentAnimation("dieing_" + orientation, FRAME_DURATION);
	}
	
	public void scaleDown() {
		this.setCurrentAnimation("scaling down_" + orientation, FRAME_DURATION);
	}
	
	/**
	 * Sets the player's current animation to collided
	 */
	public void collide(Entity wall) {
		this.setCurrentAnimation("running collided_" + orientation, FRAME_DURATION);
		this.enableBoundingBox();
	}
	
	public void collide_jump() {
		this.setCurrentAnimation("running jump collided_" + orientation, FRAME_DURATION);
		this.enableBoundingBox();
	}
	
	public boolean isColliding() {
		return currentAnimation.getId().contains("collided");
	}
	
	public boolean isHanged() {
		return hanged;
	}
	
	public void setHanged(boolean hanged) {
		this.hanged = hanged;
	}
	
	/**
	 * @return the canClimb
	 */
	public boolean isClimbing() {
		return (currentAnimation.getId().contains("scaling") ||
				currentAnimation.getId().contains("hanging") ||
				currentAnimation.getId().contains("clipping") ||
				currentAnimation.getId().contains("climbing") );
	}
	
	public boolean isHanging(){
		return (currentAnimation.getId().contains("hanging"));
	}
	
	public boolean isIdle() {
		return currentAnimation.getId().startsWith("idle");
	}

	/**
	 * @return the canClimb
	 */
	public boolean startsClimbing() {
		return startsClimbing;
	}
	
	public boolean isCanClimb() {
		return canClimb;
	}
	
	/**
	 * @param canClimb the canClimb to set
	 */
	public void setCanClimb(boolean canClimb) {
		this.canClimb = canClimb;
	}
	
	/**
	 * @return the canClimbDown
	 */
	public boolean isCanClimbDown() {
		return canClimbDown;
	}

	/**
	 * @param canClimbDown the canClimbDown to set
	 */
	public void setCanClimbDown(boolean canClimbDown) {
		this.canClimbDown = canClimbDown;
	}

	/**
	 * @param 
	 */
	public void setCornerPositionFixed(boolean cpf) {
		this.cornerPositionFixed = cpf;
	}
	
	/**
	 * @param 
	 */
	public boolean isCornerPositionFixed() {
		return cornerPositionFixed;
	}
	
	/**
	 * @return the cornerToClimb
	 */
	public Entity getCornerToClimb() {
		return cornerToClimb;
	}

	/**
	 * @param cornerToClimb the cornerToClimb to set
	 */
	public void setCornerToClimb(Entity cornerToClimb) {
		this.cornerToClimb = cornerToClimb;
	}

	/**
	 * @return the cornerToClimbDown
	 */
	public Entity getCornerToClimbDown() {
		return cornerToClimbDown;
	}

	/**
	 * @param cornerToClimbDown the cornerToClimbDown to set
	 */
	public void setCornerToClimbDown(Entity cornerToClimbDown) {
		this.cornerToClimbDown = cornerToClimbDown;
	}

	/**
	 * @return the cornerReached
	 */
	public boolean isCornerReached() {
		return cornerReached;
	}

	/**
	 * @param cornerReached the cornerReached to set
	 */
	public void setCornerReached(boolean cornerReached) {
		this.cornerReached = cornerReached;
	}

	public boolean isGettingUp() {
		return currentAnimation.getId().contains("crouching up");
	}
	
	/**
	 * 
	 * @return true if the player is executing one of its
	 * jump animations
	 */
	public boolean isJumping() {
		return currentAnimation.getId().contains("jump");
	}
	
	public boolean isClimbingLastFrame(){
		return (this.getCurrentAnimation().getId().startsWith("scaling up_") && this.getCurrentAnimation().getCurrentFrame() == 8);
	}
	
	/**
	 * 
	 * @return true if the player has just executed a jump
	 */
	public boolean wasJumping() {
		return jumping;
	}
	
	/**
	 * 
	 * sets the jumping value to false
	 */
	public boolean notJumping() {
		return jumping = false;
	}
	
	/**
	 * 
	 * @return true if the player is executing one of its
	 * fall animations
	 */
	public boolean isFalling() {
		return currentAnimation.getId().contains("fall");
	}
	
	/**
	 * 
	 * @return true if the player is not neither
	 * jumping nor falling
	 */
	public boolean isGrounded() {
		return (!isJumping() || isLanding()) &&
				!isFalling() && !isClimbing();
	}
	
	public boolean isLanding() {
		return this.getCurrentAnimation().getId().contains("landing");
	}
	
	@Override
	public void moveCharacter(){
		
		/* If character is blocked sideways, it cant move horizontally */
		if (leftBlocked || rightBlocked) {
			xSpeed = 0;
		}
		
		/* Applies gravity if falling */
		if ( isFalling() && this.getCurrentAnimation().isLastFrame() ) {
			fallingSpeed = fallingSpeed + gravity;
			int newySpeed = fallSpeed + fallingSpeed;
			
//			System.out.println("newySpeed: " + newySpeed);
			
			if (newySpeed > maxySpeed) {
				newySpeed = maxySpeed;
			}
			ySpeed = newySpeed;
			
			// Applies horizontal speed to the fall
			if ( !isStraightFall() && (!isFallCollided() && !isDeadlyFall()) ) {
				
				if ( isSafeFall() ) {
					
					if (this.getOrientation().equals("left")) {
						xFrameOffset = -fallxSpeed;
					}
					else if (this.getOrientation().equals("right")) {
						xFrameOffset = fallxSpeed;
					}
				}
				else if ( isRiskyFall() ) {
					
					if (this.getOrientation().equals("left")) {
						xFrameOffset = -fallxSpeed/2;
					}
					else if (this.getOrientation().equals("right")) {
						xFrameOffset = fallxSpeed/2;
					}
				}
			}
			else if ( isFallCollided() ) {
				xFrameOffset = 0;
			}
		}
		else if ( isGrounded() && !this.getCurrentAnimation().getId().equals("stairs")) {

			/* Character is on the ground */
			ySpeed = 0;
			yFrameOffset = 0;
			fallingSpeed = 0;
		}
		
		/* Moves the character and its bounding box if he is not at the edge*/
		if (!this.isBlocked()) {
			setX(x + xSpeed + xFrameOffset);
			setY(y + ySpeed + yFrameOffset);
			boundingBox.translate(xSpeed + xFrameOffset, ySpeed + yFrameOffset);
		}
		
		/* Play music */
		if(!sound.equals("")){
			loader.getSound(sound).play();;
		}
	}
	
	public void isEnemySaw(boolean isSaw){
		this.enemySaw = isSaw;
	}
	
	public void hasBlocked(){
		loader.getSound("sword vs sword").play();
		this.hasBlocked = true;
	}
	
	public void hasBeenBlocked(){
		this.beenBlocked = true;
	}
	
	public boolean isAttacking(){
		return (this.getCurrentAnimation().getId().startsWith("sword attack") || (this.goingToAttack));
	}
	
	public boolean isBeingBlocked(){
		return ((this.getCurrentAnimation().getId().startsWith("sword blocked and block") ||
				this.getCurrentAnimation().getId().startsWith("sword attack end blocked")) &&
				this.getCurrentAnimation().getCurrentFrame() == 0);
	}
	
	public boolean isBlocking(){
		
		return this.getCurrentAnimation().getId().startsWith("sword defense");
	}
	
	/**
	 * @return the forcedToStop
	 */
	public boolean isForcedToStop() {
		return forcedToStop;
	}

	/**
	 * @param forcedToStop the forcedToStop to set
	 */
	public void setForcedToStop(boolean forcedToStop) {
		this.forcedToStop = forcedToStop;
	}
	
	/**
	 * @return the onTheEdge
	 */
	public boolean isOnTheEdge() {
		return onTheEdge;
	}

	/**
	 * @param onTheEdge the onTheEdge to set
	 */
	public void setOnTheEdge(boolean onTheEdge) {
		this.onTheEdge = onTheEdge;
	}
	
	/**
	 * @return the canLand
	 */
	public boolean isCanLand() {
		return canLand;
	}

	/**
	 * @param canLand the canLand to set
	 */
	public void setCanLand(boolean canLand) {
		this.canLand = canLand;
	}

	/**
	 * @return the canLandScalingDown
	 */
	public boolean isCanLandScalingDown() {
		return canLandScalingDown;
	}

	/**
	 * @param canLandScalingDown the canLandScalingDown to set
	 */
	public void setCanLandScalingDown(boolean canLandScalingDown) {
		this.canLandScalingDown = canLandScalingDown;
	}

	/**
	 * @return the canLongLand
	 */
	public boolean isCanLongLand() {
		return canLongLand;
	}

	/**
	 * @param canLongLand the canLongLand to set
	 */
	public void setCanLongLand(boolean canLongLand) {
		this.canLongLand = canLongLand;
	}

	/**
	 * @return the canSimpleLand
	 */
	public boolean isCanSimpleLand() {
		return canSimpleLand;
	}

	/**
	 * @param canSimpleLand the canSimpleLand to set
	 */
	public void setCanSimpleLand(boolean canSimpleLand) {
		this.canSimpleLand = canSimpleLand;
	}

	/**
	 * @return the longLand
	 */
	public boolean isLongLand() {
		return longLand;
	}

	/**
	 * @param longLand the longLand to set
	 */
	public void setLongLand(boolean longLand) {
		this.longLand = longLand;
	}

	/**
	 * @return the simpleLand
	 */
	public boolean isSimpleLand() {
		return simpleLand;
	}

	/**
	 * @param simpleLand the simpleLand to set
	 */
	public void setSimpleLand(boolean simpleLand) {
		this.simpleLand = simpleLand;
	}

	/**
	 * @return the fallCollided
	 */
	public boolean isFallCollided() {
		return fallCollided;
	}

	/**
	 * @param fallCollided the fallCollided to set
	 */
	public void setFallCollided(boolean fallCollided) {
		this.fallCollided = fallCollided;
	}

	/**
	 * @return the straightFall
	 */
	public boolean isStraightFall() {
		return straightFall;
	}

	/**
	 * @param straightFall the straightFall to set
	 */
	public void setStraightFall(boolean straightFall) {
		this.straightFall = straightFall;
	}
	
	/**
	 * @return the canPickSword
	 */
	public boolean isCanPickSword() {
		return canPickSword;
	}
	
	/**
	 * @param canPickSword the canPickSword to set
	 */
	public void setCanPickSword(boolean canPickSword) {
		this.canPickSword = canPickSword;
	}
	
	/**
	 * @return the pickingSword
	 */
	public boolean isPickingSword() {
		return pickingSword;
	}

	/**
	 * @param pickingSword the pickingSword to set
	 */
	public void setPickingSword(boolean pickingSword) {
		this.pickingSword = pickingSword;
	}

	/**
	 * @return the canDrink
	 */
	public boolean isCanDrink() {
		return canDrink;
	}

	/**
	 * @param canDrink the canDrink to set
	 */
	public void setCanDrink(boolean canDrink) {
		this.canDrink = canDrink;
	}

	/**
	 * @return the drinkingPotion
	 */
	public boolean isDrinkingPotion() {
		return drinkingPotion;
	}

	/**
	 * @param drinkingPotion the drinkingPotion to set
	 */
	public void setDrinkingPotion(boolean drinkingPotion) {
		this.drinkingPotion = drinkingPotion;
	}

	/**
	 * @return the screaming
	 */
	public boolean isScreaming() {
		return screaming;
	}

	/**
	 * @param screaming the screaming to set
	 */
	public void setScreaming(boolean screaming) {
		this.screaming = screaming;
	}

	/**
	 * @return the landingFall
	 */
	public boolean isLandingFall() {
		return landingFall;
	}

	/**
	 * @param landingFall the landingFall to set
	 */
	public void setLandingFall(boolean landingFall) {
		this.landingFall = landingFall;
	}

	/**
	 * @return the riskyLand
	 */
	public boolean isRiskyLand() {
		return riskyLand;
	}

	/**
	 * @param riskyLand the riskyLand to set
	 */
	public void setRiskyLand(boolean riskyLand) {
		this.riskyLand = riskyLand;
	}

	/**
	 * @return the recovered
	 */
	public boolean isRecovered() {
		return recovered;
	}

	/**
	 * @param recovered the recovered to set
	 */
	public void setRecovered(boolean recovered) {
		this.recovered = recovered;
	}

	/**
	 * @return the canPotionSplash
	 */
	public boolean isCanPotionSplash() {
		return canPotionSplash;
	}

	/**
	 * @param canPotionSplash the canPotionSplash to set
	 */
	public void setCanPotionSplash(boolean canPotionSplash) {
		this.canPotionSplash = canPotionSplash;
	}

	/**
	 * @return the canSwordSplash
	 */
	public boolean isCanSwordSplash() {
		return canSwordSplash;
	}

	/**
	 * @param canSwordSplash the canSwordSplash to set
	 */
	public void setCanSwordSplash(boolean canSwordSplash) {
		this.canSwordSplash = canSwordSplash;
	}

	public boolean isBlocked() {
		return this.isWalkingAStep() && this.isOnTheEdge();
	}
	
	public boolean isWalkingAStep() {
		return this.getCurrentAnimation().getId().startsWith("walking a step");
	}
	
	public boolean isWalking(){
		return this.getCurrentAnimation().getId().startsWith("sword walking");
	}
	
	public boolean isRunning(String enemyDirection){
		if(enemyDirection.equals("left")){
			return this.currentState != PlayerState.COMBAT && 
					(this.getOrientation().equals("right") && this.xSpeed > 0);
		} else{
			return this.currentState != PlayerState.COMBAT &&  
					(this.getOrientation().equals("left") && this.xSpeed < 0);
		}
	}
	
	public void putSwordDown(){
		this.setCurrentAnimation("putting down sword_" + orientation, FRAME_DURATION);
		this.sword = null;
	}
	
	public boolean checkAttack(){
		boolean checkAttack = false;
		if(this.getCurrentAnimation().getId().startsWith("sword attack start")
				|| this.getCurrentAnimation().getId().startsWith("sword attack up start")){
			checkAttack = true;
		}
		
		return checkAttack;
	}
	
	public void beenHit(){
		loader.getSound("kid hurt").play();
		this.hp = this.hp - 1;
		if(this.currentState != PlayerState.COMBAT || this.hp == 0){
			this.hp = 0;
			this.currentState = PlayerState.DIED;
			this.sword = null;
			this.setCurrentAnimation("dieing_" + orientation, FRAME_DURATION);
		} else{
			this.setCurrentAnimation("sword hit_" + orientation, FRAME_DURATION);
		}
		this.beenBlocked = false;
		this.hasBlocked = false;
	}
	
	public boolean isHitting(){
		return this.getCurrentAnimation().getId().startsWith("sword attack end_") &&
				this.getCurrentAnimation().getCurrentFrame() == 1;
	}
	
	public boolean isDead(){
		return this.currentState == PlayerState.DIED;
	}
	
	public boolean isInCombat(){
		return this.currentState == PlayerState.COMBAT;
	}
	
	public void setSpiked(){
		System.out.println("spiked");
		this.hp = 0;
		this.currentState = PlayerState.DIED;
		this.setCurrentAnimation("dead spiked_" + orientation, FRAME_DURATION);
		this.cleanXSpeed();
	}
	
	public void setSword(){
		this.hasSword = true;
	}
	
	public boolean hasSword(){
		return this.hasSword;
	}
	
	public void finalDoorOpened(boolean state, int posx, int posy){
		this.doorx = posx;
		this.doory = posy;
		this.finalDoorOpened = state;
	}
	
	public boolean hasFinished(){
		return this.finished;
	}
	
	@Override
	public void manageSword(String animation, int currentFrame, boolean newSword){
		int x_offset = 0;
		int y_offset = -46;
		int[] x_offsets;
		int[] y_offsets;
		
		switch(animation){
		
		case "idle":
			if(this.getOrientation().equals("right")){
				x_offset = 56;
			} else{
				x_offset = -56;
			}
			if(newSword){
				this.sword = new SwordFighting(this.x,this.y,x_offset,y_offset,this.loader,"idle_" + orientation, "prince");
			} else{
				this.sword.setCurrentAnimation("idle_" + orientation, FRAME_DURATION, 0, this.x + x_offset,this.y + y_offset);
			}
			break;
			
		case "moving forward":
			x_offsets = new int[]{38,-58,42,-60};
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			this.sword.setCurrentAnimation("idle_" + orientation, FRAME_DURATION, 0, this.x + x_offset,this.y + y_offset);
			break;
			
		case "start attacking":
			x_offsets = new int[]{14,-40,0,0};
			y_offsets = new int[]{-54,0};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
			
			this.sword.setCurrentAnimation("start attacking_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "start attacking up":
			x_offsets = new int[]{2,-30,0,0};
			y_offsets = new int[]{-78,0};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
			
			this.sword.setCurrentAnimation("start attacking up_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "end attacking":
			x_offsets = new int[]{58,-86,56,-98,16,-58,0,-42,32,-44};
			y_offsets = new int[]{-44,-42,-26,-16,-36};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
				
			this.sword.setCurrentAnimation("end attacking_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "end attacking blocked":
			x_offsets = new int[]{16,-58,0,-42,32,-44};
			y_offsets = new int[]{-26,-16,-36};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
				
			this.sword.setCurrentAnimation("end attacking blocked_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "defending start":
			x_offsets = new int[]{28,-56};
			y_offsets = new int[]{-56};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
				
			this.sword.setCurrentAnimation("defending start_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "defending after block":
			x_offsets = new int[]{28,-70,22,-40};
			y_offsets = new int[]{-46,-36};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
				
			this.sword.setCurrentAnimation("defending after block_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "blocked":
			x_offsets = new int[]{42,-84};
			y_offsets = new int[]{-38};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
				
			this.sword.setCurrentAnimation("blocked_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "defending end":
			x_offsets = new int[]{26,-40,26,-40,26,-40};
			y_offsets = new int[]{-70,-70,-70};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
				
			this.sword.setCurrentAnimation("defending end_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "hit":
			x_offsets = new int[]{0,0,0,0,0,-16,0,-42,32,-44};
			y_offsets = new int[]{0,0,-32,-16,-36};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
			
			if(currentFrame == 0){
				setSplashVisible(true);
			} else if(currentFrame == 1){
				setSplashVisible(false);
				setCanShowSplash(false);
			}
				
			this.sword.setCurrentAnimation("hit_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "moving backwards":
			x_offsets = new int[]{22,-40,32,-44};
			y_offsets = new int[]{-36,-36};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
			
			this.sword.setCurrentAnimation("moving backwards_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			break;
			
		case "got sword":
			x_offsets = new int[]{14,-38,28,-40,28,-38,28,-42,56,-36,56,-40,36,-40,26,-40};
			y_offsets = new int[]{-64,-64,-62,-58,-56,-56,-50,-42};
			
			if(this.getOrientation().equals("right")){
				x_offset = x_offsets[2 * currentFrame];
			} else{
				x_offset = x_offsets[2 * currentFrame + 1];
			}
			y_offset = y_offsets[currentFrame];
			
			if(newSword){
				this.sword = new SwordFighting(this.x,this.y,x_offset,y_offset,this.loader,"got sword_" + orientation, "prince");
			} else{
				this.sword.setCurrentAnimation("got sword_" + orientation, FRAME_DURATION, currentFrame, this.x + x_offset,this.y + y_offset);
			}
			break;
		
		default:
			break;
		
		}
	}
}
