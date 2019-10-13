package com.mygdx.game.Entities;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.Map.Map;

public class EntityStage extends Stage {
	private static final String TAG = EntityStage.class.getSimpleName();

    private ArrayList<Entity> _entities;

    public EntityStage(ArrayList<Entity> entities) {
        this._entities = entities;
        createActorsForEntities();
    }

	private void createActorsForEntities() {
        for (int x = 0; x < _entities.size(); x++) {
        	Entity entity = _entities.get(x);
            EntityActor actor = new EntityActor(entity);
            initializeActor(actor);
        }
    }
	
	private void initializeActor(EntityActor actor) {
		Entity entity = actor.getEntity();
        //DANGER : I assume every entity has a 32x32 size
        actor.setBounds(entity.getCurrentPosition().getRealScreenX(), entity.getCurrentPosition().getRealScreenY(), Map.TILE_WIDTH_PIXEL,Map.TILE_HEIGHT_PIXEL);
        addActor(actor);
        EventListener eventListener = new EntityClickListener(actor);
        actor.addListener(eventListener);
	}
}
