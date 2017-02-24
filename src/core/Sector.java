package core;

import java.util.ArrayList;
import java.util.List;

import core.entities.Entity;
import core.entities.Light;
import core.loaders.Loader;

public class Sector {
	
	private float x, y;
	private final int sectorID;
	
	private List<Entity> entities;
	private List<Entity> normalEntities;
	private List<Light> lights;
	
	protected Loader loader;
	
	public Sector(int sectorID, Loader loader, float x, float y) {
		this.sectorID = sectorID;
		this.x = x;
		this.y = y;
		this.loader = loader;
		entities = normalEntities = new ArrayList<Entity>();
		lights = new ArrayList<Light>();
		init();
	}
	
	public int getID() {
		return sectorID;
	}
	
	private void init() {
		String file = "sector" + sectorID + ".sec";
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public List<Entity> getNormalEntities() {
		return normalEntities;
	}
	
	public List<Light> getLights() {
		return lights;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

}
