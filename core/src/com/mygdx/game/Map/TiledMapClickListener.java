package com.mygdx.game.Map;

import java.util.List;

import org.xguzm.pathfinding.grid.GridCell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.Battle.BattleState;
import com.mygdx.game.Entities.Entity;

public class TiledMapClickListener extends ClickListener {
	private static final String TAG = TiledMapClickListener.class.getSimpleName();

    private TiledMapActor actor;

    public TiledMapClickListener(TiledMapActor actor) {
        this.actor = actor;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
    	
    	switch(actor.getBattlemanager().getBattleState()) {
    	  case UNIT_PLACEMENT:
    	    Gdx.app.debug(TAG, "battlestate = UNIT PLACEMENT");
    	    deployUnit();
    	    break;
    	  case MOVEMENT_PHASE:
    		  Gdx.app.debug(TAG, "battlestate = MOVEMENT PHASE");
    	    possibleMove();
    	    break;
    	  default:
    	    // code block
    	}    
    }
    
    private void deployUnit(){
        if(actor.getIsFreeSpawn()) {
        	actor.getBattlemanager().deployUnit(actor.getX() * Map.UNIT_SCALE,actor.getY() * Map.UNIT_SCALE);
        	actor.setIsFreeSpawn(false);
        }
    }
    
    private void possibleMove() {
    	Entity currentUnit = actor.getBattlemanager().getActiveUnit();
    	List<GridCell> path = actor.getBattlemanager().getPathfinder().getCellsWithin((int)currentUnit.getCurrentPosition().x, (int)currentUnit.getCurrentPosition().y, currentUnit.getMp());
    	//test if spot is inside path, then move unit there
    	for(int i = 0;i<path.size();i++) {
    		if((path.get(i).x == currentUnit.getCurrentPosition().x) && (path.get(i).y == currentUnit.getCurrentPosition().y)) {
    			currentUnit.setCurrentPosition((int)actor.getX() * Map.UNIT_SCALE, (int)actor.getY() * Map.UNIT_SCALE);
    			currentUnit.setInActionPhase(true);
    			currentUnit.setInMovementPhase(false);
    			
    			//set battlestate to action (a button should be made to revert this)
    			actor.getBattlemanager().setBattleState(BattleState.ACTION_PHASE);
    		}
    	}
    }
}
