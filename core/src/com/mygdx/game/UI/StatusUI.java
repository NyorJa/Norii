package com.mygdx.game.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Screen.BattleScreen;

import Utility.Utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

public class StatusUI extends Window {
    private int levelVal;
    private int hpVal;
    private int mpVal;
    private int xpVal;
    private int iniVal;
	
    private WidgetGroup group;
    private WidgetGroup group2;
    private Image hpBarBackground;
    private Image hpBar;
    private Image xpBarBackground;
    private Image xpBar;
    
    private Label heroName;
    private Label hp;
    private Label mp;
    private Label xp;
    private Label levelValLabel;
    private Label iniValLabel;
    
    private Label hpLabel;
    private LabelStyle labelStyle;
    private Label mpLabel;
    private Label xpLabel;
    private Label levelLabel;
    private Label iniLabel;

    private Entity linkedEntity;
    
	private float statsUIOffsetX;
	private float statsUIOffsetY;
	
    private float tileWidthPixel;
    private float tileHeightPixel;
	
	private static final int WIDTH_TILES = 9;
	private static final int HEIGHT_TILES = 8; 
	private static final float BAR_WIDTH = 1.5f; 
	private static final float BAR_HEIGHT = 1; 
	
	private static final int TILE_TO_PIXEL_RATIO = 20;
	
	private static final int ALPHA = 80; 

    
    public StatusUI(Entity entity){
        super("", Utility.getStatusUISkin());
        this.debugAll();
        configureWindow(entity);      
        setFadeBackgroundEffect(entity);      
    	initPixelVariables();       
        initiateHeroStats();
        createElementsForUI();
        configureElements();
        addElementsToWindow();
    }

	private void configureWindow(Entity entity) {
		this.setVisible(false);
        this.linkedEntity = entity;
        this.setResizable(true);
	}

	private void setFadeBackgroundEffect(Entity entity) {
		Color newColor = this.getColor();
        newColor.a = ALPHA;
        entity.setStatusui(this);
	}

	private void initPixelVariables() {
		statsUIOffsetX = Gdx.graphics.getWidth() / (float) BattleScreen.VISIBLE_WIDTH;
    	statsUIOffsetY = Gdx.graphics.getHeight() / (float) BattleScreen.VISIBLE_HEIGHT;
        
    	tileWidthPixel = Gdx.graphics.getWidth() / (float) TILE_TO_PIXEL_RATIO;
    	tileHeightPixel = Gdx.graphics.getHeight() / (float) TILE_TO_PIXEL_RATIO;
	}
    
    private void initiateHeroStats() {
        levelVal = this.linkedEntity.getLevel();
        hpVal = this.linkedEntity.getHp();
        mpVal = this.linkedEntity.getMp();
        xpVal = this.linkedEntity.getXp();
        iniVal = this.linkedEntity.getBaseInitiative();
    }
    
    private void createElementsForUI() {
    	createFont();
        createGroups();     
        createLabels();
        
        createDynamicHpBar();
        createDynamicXpBar();
    }
    
	private void createFont() {
		BitmapFont font = Utility.getFreeTypeFontAsset("fonts/BLKCHCRY.ttf");
    	labelStyle = new LabelStyle();
    	labelStyle.font = font;
	}
    
	private void createGroups() {
		group = new WidgetGroup();
        group2 = new WidgetGroup();
	}
	
	
	private void createLabels() {
		heroName = new Label(linkedEntity.getName(),labelStyle);
        hpLabel = new Label(" hp:", labelStyle);
        hp = new Label(String.valueOf(hpVal), labelStyle);
        mpLabel = new Label(" mp:", labelStyle);
        mp = new Label(String.valueOf(mpVal), labelStyle);
        xpLabel = new Label(" xp:", labelStyle);
        xp = new Label(String.valueOf(xpVal), labelStyle);
        levelLabel = new Label(" lv:", labelStyle);
        levelValLabel = new Label(String.valueOf(levelVal), labelStyle);
        iniLabel = new Label(" ini:", labelStyle);
        iniValLabel = new Label(String.valueOf(iniVal), labelStyle);
	}

	private void createDynamicHpBar() {
        TextureAtlas skinAtlas = Utility.getUITextureAtlas();
        NinePatch hpBarBackgroundPatch = new NinePatch(skinAtlas.findRegion("default-round"), 5, 5, 4, 4);
        NinePatch hpBarPatch = new NinePatch(skinAtlas.findRegion("default-round-down"), 5, 5, 4, 4);
        hpBar = new Image(hpBarPatch);
        hpBarBackground = new Image(hpBarBackgroundPatch);
	}
	
	private void createDynamicXpBar() {
		TextureAtlas skinAtlas = Utility.getUITextureAtlas();
		NinePatch xpBarBackgroundPatch = new NinePatch(skinAtlas.findRegion("default-round"), 5, 5, 4, 4);
		NinePatch xpBarPatch = new NinePatch(skinAtlas.findRegion("default-round-down"), 5, 5, 4, 4);
		xpBarPatch.setColor(Color.BLACK);
		xpBar = new Image(xpBarPatch);
		xpBarBackground = new Image(xpBarBackgroundPatch);
	}
    
    private void configureElements() {
        hpBar.setWidth(BAR_WIDTH * tileWidthPixel);
        hpBarBackground.setWidth(BAR_WIDTH * tileWidthPixel);

        group.addActor(hpBarBackground);
        group.addActor(hpBar);
        group2.addActor(xpBar);
        group2.addActor(xpBarBackground);
        group.debug();
        hpBarBackground.debug();
        hpBar.debug();

        defaults().expand().fill();
    }
    
    private void addElementsToWindow() {
        this.pad(0, 10, 10, 10);
        
        this.add(heroName);
        this.row();

        this.add(hpLabel);
        this.add(hp);
        this.add(group).size(BAR_WIDTH * tileWidthPixel, BAR_HEIGHT * tileHeightPixel);
        this.row();

        this.add(mpLabel);
        this.add(mp).align(Align.left);
        this.row();

        this.add(levelLabel).align(Align.left);
        this.add(levelValLabel).align(Align.left);
        this.row();
        
        this.add(iniLabel).align(Align.left);
        this.add(iniValLabel).align(Align.left);
        this.row();
        
        this.add(xpLabel);
        this.add(xp);
        this.add(group2).size(BAR_WIDTH * tileWidthPixel, BAR_HEIGHT * tileHeightPixel).left().bottom().padBottom(2);
        this.row();

        this.pack();
    }
    
    public void update() {
		statsUIOffsetX = Gdx.graphics.getWidth() / (float) BattleScreen.VISIBLE_WIDTH;
    	statsUIOffsetY = Gdx.graphics.getHeight() / (float) BattleScreen.VISIBLE_HEIGHT;
    	
    	tileWidthPixel = Gdx.graphics.getWidth() / (float) TILE_TO_PIXEL_RATIO;
    	tileHeightPixel = Gdx.graphics.getHeight() / (float) TILE_TO_PIXEL_RATIO;
    	
        updateStats();
        updateLabels();
        updateSize();
        
        if(linkedEntity.getEntityactor().getIsHovering()) {
        	this.setVisible(true);
        }
        
        //we offset the position a little bit to make it look better
        this.setPosition((linkedEntity.getCurrentPosition().getCameraX()) + statsUIOffsetX, (linkedEntity.getCurrentPosition().getCameraY()) + statsUIOffsetY);

    }

	private void updateLabels() {
		hp.setText(String.valueOf(hpVal));
        mp.setText(String.valueOf(mpVal));
        xp.setText(String.valueOf(xpVal));
        levelValLabel.setText(String.valueOf(levelVal));
        iniValLabel.setText(String.valueOf(iniVal));
	}

	private void updateStats() {
		levelVal = linkedEntity.getLevel();
		
        hpVal = linkedEntity.getHp();
        mpVal = linkedEntity.getMp();
        xpVal = linkedEntity.getXp();
	}
	
	private void updateSize() {
		this.setSize(WIDTH_TILES * tileWidthPixel, HEIGHT_TILES * tileHeightPixel);
		float barWidth = BAR_WIDTH * tileWidthPixel;
		float barHeight = BAR_HEIGHT * tileHeightPixel;
		
		hpBar.setWidth(((float)linkedEntity.getHp() / (float)linkedEntity.getMaxHp()) * barWidth);
		hpBarBackground.setWidth(barWidth);
		
		hpBar.setHeight(barHeight);
		hpBarBackground.setHeight(barHeight);
		
		xpBar.setWidth(((float)linkedEntity.getXp() / (float)linkedEntity.getMaxXP()) * barWidth);
		xpBarBackground.setWidth(barWidth);
		
		xpBar.setHeight(barHeight);
		xpBarBackground.setHeight(barHeight);


    	for(Actor actor : this.getChildren()) {
    		if(actor.getClass() == Label.class) {
            	Label label = (Label) actor;
            	label.setFontScale(Gdx.graphics.getWidth() * 0.0015f, Gdx.graphics.getHeight() * 0.0015f);
    		}
    	}
	}
}

