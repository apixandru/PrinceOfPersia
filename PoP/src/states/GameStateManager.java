package states;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

import input.Key;
import input.KeyMapper;
import tests.Test;

public class GameStateManager {
	
	private ArrayList<State> gameStates;
	private int currentState;
	
	public static final int TESTSTATE = -1;
	public static final int MENUSTATE = 0;
	public static final int MAINGAMESTATE = 1;
	
	
	
	public GameStateManager(ConcurrentLinkedQueue<Key> keys){
		
		gameStates = new ArrayList<State>();
		
		currentState = MENUSTATE;
		
		Hashtable<String, Integer> keys_mapped = new Hashtable<String, Integer>();
		KeyMapper key_mapper = new KeyMapper(keys_mapped);
		key_mapper.initDefaultKeys();
		gameStates.add(new Test(this, keys, keys_mapped));
		gameStates.add(new MenuState(this,keys, keys_mapped));
	}
	
	public void setState(int state){
		currentState = state;
		gameStates.get(currentState).init();
	}
	
	public void update(long elapsedTime){
		gameStates.get(currentState).update(elapsedTime);
	}
	
	public void draw(Graphics2D g){
		gameStates.get(currentState).draw(g);
	}
}
