package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import core.entities.Entity;
import core.entities.Light;
import core.entities.Player;
import core.entities.world.Tree;
import core.loaders.Loader;
import core.loaders.OBJFileLoader;
import core.models.RawModel;
import core.models.TexturedModel;
import core.terrains.Terrain;
import core.textures.ModelTexture;

public class Sector {
	
	private float x, y;
	private final int sectorID;
	private boolean active = false;
	
	private List<Entity> entities;
	private List<Entity> normalEntities;
	private List<Light> lights;
	
	protected Loader loader;
	
	private float size;
	
	public Sector(int sectorID, Loader loader, float x, float y, float size) {
		this.size = size;
		this.sectorID = sectorID;
		this.x = x;
		this.y = y;
		this.loader = loader;
		entities = new ArrayList<Entity>();
		normalEntities = new ArrayList<Entity>();
		lights = new ArrayList<Light>();
		init();
	}
	
	public int getID() {
		return sectorID;
	}
	
	public void init() {
		String file = "sector" + sectorID + ".sec";
		BufferedReader reader = null;
		String line ="";
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine())!=null) {
				String[] parts;
				if (line.startsWith("E:") || line.startsWith("NE:")) {
					parts = line.split(":");
					String[] data = parts[1].split(",");
					Vector3f position = new Vector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
					float rotX = Float.parseFloat(data[3]);
					float rotY = Float.parseFloat(data[4]);
					float rotZ = Float.parseFloat(data[5]);
					float scale = Float.parseFloat(data[6]);
					boolean removed = Boolean.parseBoolean(data[7]);
					int textureIndex = Integer.parseInt(data[8]);
					boolean usesParticles = Boolean.parseBoolean(data[9]);
					
					data = parts[2].split(",");
					float shineDamper = Float.parseFloat(data[0]);
					float reflectivity = Float.parseFloat(data[1]);
					boolean hasTransparency = Boolean.parseBoolean(data[2]);
					boolean useFakeLighting = Boolean.parseBoolean(data[3]);
					int numberOfRows = Integer.parseInt(data[4]);
					String textureID = data[5];
					
					ModelTexture mt = new ModelTexture(loader.loadTexture(textureID), textureID);
					mt.setShineDamper(shineDamper);
					mt.setReflectivity(reflectivity);
					mt.setHasTransparency(hasTransparency);
					mt.setUseFakeLighting(useFakeLighting);
					mt.setNumberOfRows(numberOfRows);
					
					TexturedModel tm  = new TexturedModel(OBJFileLoader.loadOBJ(textureID, loader), mt);
					if (line.startsWith("E:")) {
						Entity e = new Entity(tm, position, rotX, rotY, rotZ, scale);
						e.setRemoved(removed);
						e.setUsesParticles(usesParticles);
						entities.add(e);
					} else {
						Entity e = new Entity(tm, textureIndex, position, rotX, rotY, rotZ, scale);
						e.setRemoved(removed);
						e.setUsesParticles(usesParticles);
						normalEntities.add(e);
					}
				} else if (line.startsWith("L:")) {
					
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void generate() {
		Terrain terrain = World.terrain;
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"), "fern");
		fernTextureAtlas.setNumberOfRows(2);

		TexturedModel fern = new TexturedModel(OBJFileLoader.loadOBJ("fern", loader),
				fernTextureAtlas);

		TexturedModel bobble = new TexturedModel(OBJFileLoader.loadOBJ("pine", loader),
				new ModelTexture(loader.loadTexture("pine"), "pine"));
		bobble.getTexture().setHasTransparency(true);
		
		Random random = new Random(5666778);
		float waterHeight = World.water.getHeight();
		for (int i = 0; i < size/8; i++) {
			if (i % 3 == 0) {
				float x = this.x + (random.nextFloat() * size);
				float z = this.y + (random.nextFloat() * size);
				if ((x > 50 && x < 100) || (z < -50 && z > -100)) {
				} else {
					float y = terrain.getHeightOfTerrain(x, z);
					if (y < waterHeight) continue;
					entities.add(new Entity(fern, 3, new Vector3f(x, y, z), 0,
							random.nextFloat() * 360, 0, 0.9f));
				}
			}
			if (i % 2 == 0) {

				float x = this.x + (random.nextFloat() * size);
				float z = this.y + (random.nextFloat() * size);
				if ((x > 50 && x < 100) || (z < -50 && z > -100)) {
				} else {
					float y = terrain.getHeightOfTerrain(x, z);
					if (y < waterHeight) continue;
					entities.add(new Tree(loader, bobble,new Vector3f(x, y, z), random.nextFloat() * 0.6f + 0.8f));
				}
			}
		}
		save(entities, normalEntities, lights);
	}
	
	public void save(List<Entity> entities, List<Entity> normalEntities, List<Light> lights) {
		String file = "sector" + sectorID + ".sec";
		try {
			PrintWriter writer = new PrintWriter(new File(file));
			for (Entity e : entities) {
				writer.println("E:" + e.getData());
			}
			for (Entity e : normalEntities) {
				writer.println("NE:" + e.getData());
			}
			for (Light l : lights) {
				writer.println("L:" + l.getData());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (!active) {
			active = true;
			init();
		}
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
