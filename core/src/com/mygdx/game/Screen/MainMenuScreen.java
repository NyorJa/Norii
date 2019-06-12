package com.mygdx.game.Screen;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Audio.AudioObserver;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Entities.EntityFilePath;

import Utility.Utility;

import com.mygdx.game.Entities.Owner;
import com.mygdx.game.Entities.Player;

public class MainMenuScreen extends GameScreen {
	private static final String TAG = MainMenuScreen.class.getSimpleName();
	
	public final int FRAME_WIDTH = 2000;
	public final int FRAME_HEIGHT = 1125;
	private static String _defaultBackgroundPath = "sprites/gui/forest50.png";
	private static final int bganimationframes = 50;
	private static final int FRAME_COLS = 5, FRAME_ROWS = 10;

	private Stage _stage;
	private Table mainMenuTableOfButtons;
	private TextButton newGameButton;
	private TextButton exitButton;
	private ArrayList<Owner> fighters;
	private ArrayList<Entity> monsters;
	private Animation<TextureRegion> bganimation;
	private Array<TextureRegion> _bgframes;
	private SpriteBatch backgroundbatch;
	
	protected float _frameTime = 0f;
	protected Sprite _frameSprite = null;
	protected TextureRegion _currentFrame = null;
	private TextureRegion[] bgFrames;

	public MainMenuScreen(Object... params){
		
		initializeClassVariables();
		
		createBackground();
		createButtons();
		createLayout();
		
		addListeners();
		
		//load music
		notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_TITLE);
	}
	
	private void initializeClassVariables() {
		_stage = new Stage();
		mainMenuTableOfButtons = new Table();
		mainMenuTableOfButtons.setDebug(false);
		mainMenuTableOfButtons.setFillParent(true);
	}
	
	private void createBackground(){
		backgroundbatch = new SpriteBatch();
		Utility.loadTextureAsset(_defaultBackgroundPath);
		loadBackgroundSprites();
		initializeBgAnimation();
	}
	
	private void createButtons() {
		new Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("bludbourne_title"));
		newGameButton = new TextButton("New Game", Utility.STATUSUI_SKIN);
		exitButton = new TextButton("Exit",Utility.STATUSUI_SKIN);
	}
	
	private void createLayout() {
		mainMenuTableOfButtons.row();
		mainMenuTableOfButtons.add(newGameButton).spaceBottom(10).padTop(50).row();
		mainMenuTableOfButtons.add(exitButton).spaceBottom(10).row();

		_stage.addActor(mainMenuTableOfButtons);
	}
	
	private void addListeners() {
		//Listeners
		newGameButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				fighters = new ArrayList<Owner>();
				monsters = new ArrayList<Entity>();
				monsters.add(new Entity("Fallia",EntityFilePath.COMMANDER));
				monsters.add(new Entity("Julian",EntityFilePath.DEMON));
				monsters.add(new Entity("Jelte",EntityFilePath.ICARUS));
				monsters.add(new Entity("Shaman",EntityFilePath.SHAMAN));
				Player.getInstance().setTeam(monsters);
				fighters.add(Player.getInstance());
				ScreenManager.getInstance().showScreen( ScreenEnum.BATTLE,fighters); 
				return true;
			}
								  }
		);

		exitButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.exit();
				return true;
			}
		});
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//update timer for background animation
		 updatebg(delta);
		 
		 //animate background
		 _currentFrame = bganimation.getKeyFrame(_frameTime, true);
		 backgroundbatch.begin();
		 backgroundbatch.draw(_currentFrame, 0, 0,_stage.getViewport().getWorldWidth(),_stage.getViewport().getWorldHeight());
		 backgroundbatch.end();
		 
		_stage.act(delta);
		_stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		_stage.getViewport().setScreenSize(width, height);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(_stage);
		notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		_stage.dispose();
		Utility.unloadAsset(_defaultBackgroundPath);
		backgroundbatch.dispose();
	}
	
	private void loadBackgroundSprites()
	{
		Texture texture = Utility.getTextureAsset(_defaultBackgroundPath);
		TextureRegion[][] textureFrames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
		
		// Place the regions into a 1D array in the correct order, starting from the top 
		// left, going across first. The Animation constructor requires a 1D array.
		bgFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		int index = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				bgFrames[index++] = textureFrames[i][j];
			}
		}
	}
	
	private void initializeBgAnimation() {
		bganimation = new Animation<TextureRegion>(0.050f, bgFrames);
	}
	
	public void updatebg(float delta){
		_frameTime = (_frameTime + delta)%70; //Want to avoid overflow
	}
	
}




