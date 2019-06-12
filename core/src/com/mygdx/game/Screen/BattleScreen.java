package com.mygdx.game.Screen;

import java.util.ArrayList;
import java.util.List;

import org.xguzm.pathfinding.grid.GridCell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Battle.BattleManager;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Entities.EntityObserver;
import com.mygdx.game.Entities.Owner;
import com.mygdx.game.Entities.Player;
import com.mygdx.game.Map.BattleMap;
import com.mygdx.game.Map.Map;
import com.mygdx.game.Map.MapManager;
import com.mygdx.game.Particles.ParticleMaker;
import com.mygdx.game.Particles.ParticleType;
import com.mygdx.game.Profile.ProfileManager;
import com.mygdx.game.UI.PlayerBattleHUD;

import Utility.TiledMapPosition;
import Utility.Utility;

public class BattleScreen extends GameScreen implements EntityObserver {
	private static final String TAG = BattleScreen.class.getSimpleName();

	private static class VIEWPORT {
		static float viewportWidth;
		static float viewportHeight;
		static float virtualWidth;
		static float virtualHeight;
		static float physicalWidth;
		static float physicalHeight;
		static float aspectRatio;
	}

	private ArrayList<Owner> _players;
	private OrthogonalTiledMapRenderer _mapRenderer = null;
	private OrthographicCamera _camera = null;
	private static MapManager _mapMgr;
	private BattleMap map;
	private BattleManager battlemanager;
	private Entity[] playerSortedUnits;
	private SpriteBatch spritebatch;
	private InputMultiplexer multiplexer;
	private OrthographicCamera _hudCamera;
	private static PlayerBattleHUD _playerBattleHUD;

	public BattleScreen(Object... params){
		initializeVariables();
		initializeHUD();
		initializeInput(); 
		initializeMap();
		initializeUnits(params);
		initializeObservers();
	}

	private void initializeVariables() {
		playerSortedUnits = Player.getInstance().getUnitsSortedByIni(); 
	}
	
	private void initializeHUD() {
		_hudCamera = new OrthographicCamera();
		_hudCamera.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight);
		_playerBattleHUD = new PlayerBattleHUD(_hudCamera,playerSortedUnits); //voorlopig alleen player units, moet alle units zijn
	}

	private void initializeInput() {
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(Player.getInstance().getEntityStage()); //need to add all units here not just player units
		battlemanager = new BattleManager(multiplexer,playerSortedUnits);
		multiplexer.addProcessor(_playerBattleHUD.getStage());
	}
	
	private void initializeMap() {
		_mapMgr = new MapManager();
		map = (BattleMap) _mapMgr.get_currentMap();
		map.setStage(battlemanager);
	}
	
	private void initializeUnits(Object... params) {
		int index = ScreenManager.ScreenParams.ARRAYLIST_OF_OWNERS.ordinal();
		if(params[index] != null) {
			_players = (ArrayList<Owner>) params[index];
		}else _players = new ArrayList<Owner>();
	}
	
	private void initializeObservers() {
		//HUD 
		ProfileManager.getInstance().addObserver(_playerBattleHUD);
		
		//UNIT 
		for(Owner player : _players) {
			for(Entity unit : player.getTeam()){
				unit.addObserver(this);
			}
		}
	}

	@Override
	public void show() {	
		_mapMgr.getCurrentTiledMap();
		
		//set multiplexer as active one
		multiplexer.addProcessor(map.getTiledMapStage());
		Gdx.input.setInputProcessor(multiplexer);
		
		//viewport setup
		setupViewport(map.getMapWidth(), map.getMapHeight());
		
		//get the current size
		_camera = new OrthographicCamera();
		_camera.setToOrtho(false, map.getMapWidth(), map.getMapHeight());
		
		_mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
		_mapRenderer.setView(_camera);
		Gdx.app.debug(TAG, "tiled map renderer batch info : " + _hudCamera.combined.getScaleX() + " , " + _mapRenderer.getBatch().getProjectionMatrix().getScaleY() + ")"); 
		
		spritebatch = new SpriteBatch();
		spritebatch.setProjectionMatrix(_hudCamera.combined);
		map.makeSpawnParticles();
		
		Gdx.app.debug(TAG, "UnitScale value is: " + _mapRenderer.getUnitScale());
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//act stages
		Player.getInstance().getEntityStage().act();
		_playerBattleHUD.getStage().act();
		map.getTiledMapStage().act();

		//Preferable to lock and center the _camera to the middle of the field
		_camera.position.set(map.getMapWidth()/2, map.getMapHeight()/2, 0f);
		_camera.update();
		
		_hudCamera.update();

		for (Owner owner : _players) {
		    owner.updateUnits(delta);
		}

		battlemanager.updateController(delta);

		_mapRenderer.setView(_camera);
		map.getTiledMapStage().getViewport().apply();
		_mapRenderer.render();
		_mapRenderer.getBatch().begin();
		
		//draw all units
		Player.getInstance().getEntityStage().getViewport().apply();
		for (Owner owner : _players) {
			ArrayList<Entity> units = owner.getTeam();
			for (Entity entity : units) {
				if(entity.isInBattle()) {
					_mapRenderer.getBatch().draw(entity.getFrame(), entity.getCurrentPosition().getTileX(), entity.getCurrentPosition().getTileY(), 1f,1f);
				}
			}	
		}
		
		_mapRenderer.getBatch().end();
		
		//draw grid
		renderGrid();
		
		//draw particles
		_camera.update();
		//spritebatch.setProjectionMatrix(_camera.combined);
		//spritebatch.setTransformMatrix(_camera.view);
		
		_playerBattleHUD.getStage().getViewport().apply();
		Player.getInstance().getEntityStage().getViewport().apply();
		spritebatch.setProjectionMatrix(Player.getInstance().getEntityStage().getViewport().getCamera().combined);
		spritebatch.begin();
		ParticleMaker.drawAllActiveParticles(spritebatch, delta);
		spritebatch.end();
		
		//render HUD
		_playerBattleHUD.getStage().getViewport().apply();
		_playerBattleHUD.render(delta);
			
	}

	@Override
	public void resize(int width, int height) {
		Player.getInstance().getEntityStage().getViewport().update(width, height, false);
		_playerBattleHUD.resize(width, height);
		map.getTiledMapStage().getViewport().update(width, height, false);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		//dispose all units
		for (Owner owner : _players) {
			owner.dispose();
		}
		battlemanager.dispose();
		Gdx.input.setInputProcessor(null);
		_mapRenderer.dispose();
	}

	private void setupViewport(int width, int height){
		//Make the viewport a percentage of the total display area
		VIEWPORT.virtualWidth = width;
		VIEWPORT.virtualHeight = height;

		//Current viewport dimensions
		VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
		VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;

		//pixel dimensions of display
		VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
		VIEWPORT.physicalHeight = Gdx.graphics.getHeight();

		//aspect ratio for current viewport
		VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);

		//update viewport if there could be skewing
		if( VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio){
			//Letterbox left and right
			VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth/VIEWPORT.physicalHeight);
			VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
		}else{
			//letterbox above and below
			VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
			VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight/VIEWPORT.physicalWidth);
		}

		Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")" );
		Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")" );
		Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")" );
	}

	public void renderGrid() {
		//create a visible grid
		for(int x = 0; x < map.getMapWidth(); x += 1)
			Utility.DrawDebugLine(new Vector2(x,0), new Vector2(x,map.getMapHeight()), _camera.combined);
		for(int y = 0; y < map.getMapHeight(); y += 1)
			Utility.DrawDebugLine(new Vector2(0,y), new Vector2(map.getMapWidth(),y), _camera.combined);
	}

	@Override
	public void onNotify(EntityCommand command,Entity unit) {
		switch(command){
		case IN_MOVEMENT:
			List<GridCell> path = map.getPathfinder().getCellsWithin(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY(), unit.getMp());
			for(GridCell cell : path) {
				TiledMapPosition positionToPutMoveParticle = new TiledMapPosition(cell.x,cell.y);
				ParticleMaker.addParticle(ParticleType.MOVE,positionToPutMoveParticle );
			}
			break;
		default:
			break;
		}	
	}
}

