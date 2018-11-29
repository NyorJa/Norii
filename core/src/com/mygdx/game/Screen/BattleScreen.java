package com.mygdx.game.Screen;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Utility;
import com.mygdx.game.Battle.BattleManager;
import com.mygdx.game.Battle.BattleState;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Entities.Owner;
import com.mygdx.game.Entities.Player;
import com.mygdx.game.Map.BattleMap;
import com.mygdx.game.Map.Map;
import com.mygdx.game.Map.MapManager;
import com.mygdx.game.Profile.ProfileManager;
import com.mygdx.game.UI.PlayerBattleHUD;

public class BattleScreen implements Screen {
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
	private ArrayList<Vector2> spawnPoints;
	
	
	private InputMultiplexer multiplexer;
	
	//HUD stuff
	private OrthographicCamera _hudCamera;
	private static PlayerBattleHUD _playerBattleHUD;

	public BattleScreen(Object... params){
		//initialize player
		playerSortedUnits = Player.getInstance().getUnitsSortedByIni();
		
		//init HUD
		_hudCamera = new OrthographicCamera();
		_hudCamera.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight);
		_playerBattleHUD = new PlayerBattleHUD(_hudCamera,playerSortedUnits); //voorlopig alleen player units, moet alle units zijn
		

		multiplexer = new InputMultiplexer();
		
		//add 3 inputlisteners
		multiplexer.addProcessor(Player.getInstance().getEntityStage()); //need to add all units here not just player units
		battlemanager = new BattleManager(multiplexer,playerSortedUnits);
		multiplexer.addProcessor(_playerBattleHUD.getStage()); 
		
		
		_mapMgr = new MapManager();
		map = (BattleMap) _mapMgr.get_currentMap();
		map.setStage(battlemanager);
		

		//if owners are supplied, initialize them
		int index = ScreenManager.ScreenParams.ARRAYLIST_OF_OWNERS.ordinal();
		if(params[index] != null) {
			_players = (ArrayList<Owner>) params[index];
		}else _players = new ArrayList<Owner>();
		
		//add HUD as observer
		ProfileManager.getInstance().addObserver(_playerBattleHUD);
	}

	@Override
	public void show() {
		_mapMgr.getCurrentTiledMap();
		
		//fill spawn points
		spawnPoints = map.getSpawnPositionsFromScaledUnits();
		battlemanager.setSpawnPoints(spawnPoints);
		
		//init units
		for (Owner owner : _players) {
		    owner.initUnits();
		}
		
		//set multiplexer as active one
		multiplexer.addProcessor(map.getTiledMapStage());
		Gdx.input.setInputProcessor(multiplexer);
		
		//_camera setup
		setupViewport(map.getMapWidth(), map.getMapHeight());
		
		//get the current size
		_camera = new OrthographicCamera();
		_camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

		_mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
		_mapRenderer.setView(_camera);

		Gdx.app.debug(TAG, "UnitScale value is: " + _mapRenderer.getUnitScale());
	}

	@Override
	public void hide() {
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//act stages
		Player.getInstance().getEntityStage().act();
		_playerBattleHUD.getStage().act();
		map.getTiledMapStage().act();

		//Preferable to lock and center the _camera to the middle of the field
		_camera.position.set(map.getMapWidth()/2, map.getMapHeight()/2, 0f);
		_camera.update();

		for (Owner owner : _players) {
		    owner.updateUnits(delta);
		}

		battlemanager.updateController(delta);

		//_mapRenderer.getBatch().enableBlending();
		//_mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		_mapRenderer.setView(_camera);
		
		_mapRenderer.render();


		_mapRenderer.getBatch().begin();
		
		//draw all units
		for (Owner owner : _players) {
			ArrayList<Entity> units = owner.getTeam();
			for (Entity entity : units) {
				if(entity.isInBattle())
				_mapRenderer.getBatch().draw(entity.getFrame(), entity.getFrameSprite().getX(), entity.getFrameSprite().getY(), 1f,1f);
			}	
		}
		
		_mapRenderer.getBatch().end();
		
		//draw grid
		renderGrid();
		
		//highlight tiles if necesary
		if(battlemanager.getBattleState() == BattleState.UNIT_PLACEMENT) highlightTiles();
		
		//render HUD
		_playerBattleHUD.render(delta);
			
	}

	@Override
	public void resize(int width, int height) {
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
	
	public void highlightTiles() {
		for(Vector2 vector : spawnPoints) {
			Utility.FillSquare(vector.x, vector.y, Color.GOLD, _camera.combined);
		}
	}


}

