package core;

import java.util.ArrayList;
import java.util.List;

import core.entities.Entity;
import core.entities.Light;
import core.entities.Player;
import core.loaders.Loader;

public class SectorManager {
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> normalMapEntities = new ArrayList<Entity>();
	private List<Light> lights = new ArrayList<Light>();
	
	private int worldSectorWidth = 4;
	private int worldSectorHeight = 4;
	private int sectorSize = World.WORLD_SIZE / 4;
	
	private Sector[] sectors;
	private Sector currSector;
	private Sector preSector;
	
	private Player player;
	private Light sun;
	
	public SectorManager(Player player, Light sun, Loader loader) {
		this.player = player;
		this.sun = sun;
		sectors = new Sector[worldSectorWidth * worldSectorHeight];
		for (int x = 0; x < worldSectorWidth; x++) {
			for (int y = 0; y < worldSectorHeight; y++) {
				sectors[x + y * worldSectorWidth] = new Sector(x + y * worldSectorWidth, loader, x * sectorSize, y * sectorSize, sectorSize);
				entities.addAll(sectors[x + y * worldSectorWidth].getEntities());
				normalMapEntities.addAll(sectors[x + y * worldSectorWidth].getNormalEntities());
				lights.addAll(sectors[x + y * worldSectorWidth].getLights());
			}
		}
		currSector = sectors[0];
	}
	
	/*public void changeSector(int sectorID) {
		entities.remove(player);
		lights.remove(sun);
		entities.add(player);
		lights.add(sun);
	}*/
	
	public void update() {
		currSector = getSector(player.getPosition().x, player.getPosition().z);
		/*if (preSector != null) {
			if (!currSector.equals(preSector)) {
				changeSector(currSector.getID());
				preSector = currSector;
			}
		} else {
			changeSector(currSector.getID());
			preSector = currSector;
		}*/
	}
	
	public Sector getCurrSector() {
		return currSector;
	}
	
	public Sector getSector(float x, float z) {
		if (x < 0 || z < 0 || x > World.WORLD_SIZE || z > World.WORLD_SIZE) return null;
		int playerTerrainX = (int) Math.floor(x / sectorSize); // convert player coordinates to terrain coordinates
	    int playerTerrainZ = (int) Math.floor(z / sectorSize) ;
	    return sectors[playerTerrainX + playerTerrainZ * worldSectorWidth];
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public List<Entity> getNormalMapEntities() {
		return normalMapEntities;
	}
	
	public List<Light> getLights() {
		return lights;
	}

}
