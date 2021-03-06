package com.redomar.game.level;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.redomar.game.entities.Entity;
import com.redomar.game.entities.PlayerMP;
import com.redomar.game.gfx.Screen;
import com.redomar.game.level.tiles.Tile;

public class LevelHandler {

	private byte[] tiles;
	private int width;
	private int height;
	private List<Entity> entities = new ArrayList<Entity>();
	private String imagePath;
	private BufferedImage image;

	public LevelHandler(String imagePath) {

		if (imagePath != null) {
			this.imagePath = imagePath;
			this.loadLevelFromFile();
		} else {
			tiles = new byte[width * height];
			this.width = 64;
			this.height = 64;
			this.generateLevel();
		}
	}

	private void loadLevelFromFile() {
		try {
			this.image = ImageIO.read(Level.class.getResource(this.imagePath));
			this.width = image.getWidth();
			this.height = image.getHeight();
			tiles = new byte[width * height];
			this.loadTiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadTiles() {
		int[] tileColours = this.image.getRGB(0, 0, width, height, null, 0,
				width);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				tileCheck: for (Tile t : Tile.getTiles()) {
					if (t != null
							&& t.getLevelColour() == tileColours[x + y * width]) {
						this.tiles[x + y * width] = t.getId();
						break tileCheck;
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void saveLevelToFile() {
		try {
			ImageIO.write(image, "png",
					new File(Level.class.getResource(this.imagePath).getFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void alterTile(int x, int y, Tile newTile) {
		this.tiles[x + y * width] = newTile.getId();
		image.setRGB(x, y, newTile.getLevelColour());
	}

	private void generateLevel() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x * y % 10 < 7) {
					tiles[x + y * width] = Tile.getGrass().getId();
				} else {
					tiles[x + y * width] = Tile.getStone().getId();
				}
			}
		}
	}
	
	public synchronized List<Entity>getEntities(){
		return this.entities;
	}

	public void tick() {
		for (Entity e : getEntities()) {
			e.tick();
		}

		for (Tile t : Tile.getTiles()) {
			if (t == null) {
				break;
			}
			t.tick();
		}

	}

	public void renderTiles(Screen screen, int xOffset, int yOffset) {
		if (xOffset < 0) {
			xOffset = 0;
		}
		if (xOffset > ((width << 3) - screen.getWidth())) {
			xOffset = ((width << 3) - screen.getWidth());
		}
		if (yOffset < 0) {
			yOffset = 0;
		}
		if (yOffset > ((height << 3) - screen.getHeight())) {
			yOffset = ((height << 3) - screen.getHeight());
		}

		screen.setOffset(xOffset, yOffset);

		for (int y = (yOffset >> 3); y < (yOffset + screen.getHeight() >> 3) + 1; y++) {
			for (int x = (xOffset >> 3); x < (xOffset + screen.getWidth() >> 3) + 1; x++) {
				getTile(x, y).render(screen, this, x << 3, y << 3);
			}
		}
	}

	public void renderEntities(Screen screen) {
		for (Entity e : getEntities()) {
			e.render(screen);
		}
	}

	public Tile getTile(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) {
			return Tile.getVoid();
		}
		return Tile.getTiles()[tiles[x + y * width]];
	}

	public void addEntity(Entity entity) {
		this.getEntities().add(entity);
	}

	public void removeEntity(String username) {
		int index = 0;
		for(Entity e : getEntities()){
			if(e instanceof PlayerMP && ((PlayerMP)e).getUsername().equalsIgnoreCase(username)){
				break;
			}
			index++;
		}
		this.getEntities().remove(index);
	}
	
	private int getPlayerMPIndex(String username){
		int index = 0;
		for(Entity e : getEntities()){
			if(e instanceof PlayerMP && ((PlayerMP)e).getUsername().equalsIgnoreCase(username)){
				break;
			}
			index++;
		}
		return index;
	}
	
	public void movePlayer(String username, int x, int y, int numSteps, boolean isMoving, int movingDir){
		int index = getPlayerMPIndex(username);
		PlayerMP player = (PlayerMP)this.getEntities().get(index);
		player.x = x;
		player.y = y;
		player.setNumSteps(numSteps);
		player.setMoving(isMoving);
		player.setMovingDir(movingDir);
	}

}
