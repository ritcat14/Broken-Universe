package core;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import core.entities.Camera;
import core.entities.Entity;
import core.entities.Light;
import core.entities.Player;
import core.entities.WaterTile;
import core.entities.world.Tree;
import core.loaders.Loader;
import core.loaders.OBJFileLoader;
import core.masters.ParticleMaster;
import core.masters.PostProcessing;
import core.modelData.Fbo;
import core.modelData.FrameBuffers;
import core.models.TexturedModel;
import core.renderers.GuiRenderer;
import core.renderers.MasterRenderer;
import core.renderers.WaterRenderer;
import core.shaders.WaterShader;
import core.terrains.Terrain;
import core.textures.GuiTexture;
import core.textures.ModelTexture;
import core.textures.TerrainTexture;
import core.textures.TerrainTexturePack;
import core.toolbox.FileLoader;

public class World {

	public final static int WORLD_SIZE = 4096;
	
	public FileLoader fileLoader;
	public boolean loaded = false;
	
	private GuiRenderer guiRenderer;
	private GuiTexture playerMarker, miniMap;
	
	private ArrayList<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<Entity> normalMapEntities = new ArrayList<Entity>();
	private ArrayList<Light> lights = new ArrayList<Light>();
	
	private ArrayList<Entity> entitiesToRender = new ArrayList<Entity>();
	private ArrayList<Entity> normalMapEntitiesToRender = new ArrayList<Entity>();
	private ArrayList<Light> lightsToRender = new ArrayList<Light>();
	
	private Terrain terrain;
	
	private WaterTile water;
	private WaterRenderer waterRenderer;
	private WaterShader waterShader;
	
	private Loader loader;
	private Player player;
	private Camera camera;
	private MasterRenderer renderer;
	private FrameBuffers buffers;
	private Light sun;
	
	public World(Loader loader, MasterRenderer renderer, Camera camera, Player player) {
		this.loader = loader;
		this.player = player;
		this.renderer = renderer;
		this.camera = camera;
		fileLoader = new FileLoader();
		init();
	}
	
	private void init() {
		buffers = new FrameBuffers();
		
		// *********TERRAIN TEXTURE STUFF**********
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture,
				gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		terrain = new Terrain(WORLD_SIZE, 0, 0, loader, texturePack, blendMap, "heightMap");
		waterShader = new WaterShader();
		waterShader.start();
		waterShader.loadSkyColour(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
		waterShader.stop();
		waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		water = new WaterTile(WORLD_SIZE, WORLD_SIZE, WORLD_SIZE/2, WORLD_SIZE/2, -25);
		
		//generate();

		sun = new Light(new Vector3f(1000000, 1500000, -1000000), new Vector3f(1.3f, 1.3f, 1.3f));
		
		guiRenderer = new GuiRenderer(loader);
		
		Vector2f size = getScaledVector(0.15f);
		miniMap = new GuiTexture(loader.loadTexture("miniMap"), new Vector2f(size.x - 1, 1 - size.y), size);
		guiTextures.add(miniMap);
		
		size = getScaledVector(0.004f);
		playerMarker = new GuiTexture(loader.loadTexture("playerMarker"), new Vector2f(0, 0), size);
		guiTextures.add(playerMarker);

		entitiesToRender.add(player);
		lightsToRender.add(sun);
	}
	
	private void loadData() {
		fileLoader.readData("sector.sec");
		String[] fileData = fileLoader.getData();
		int max = fileLoader.getLineNum() - 1;
		try {
			for (String line : fileData) {
				String[] parts = line.split(":");
				if (line.startsWith("E:") || line.startsWith("NE:")) {
					String[] data = parts[2].split(",");
					Vector3f position = new Vector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
					float rotX = Float.parseFloat(data[3]);
					float rotY = Float.parseFloat(data[4]);
					float rotZ = Float.parseFloat(data[5]);
					float scale = Float.parseFloat(data[6]);
					boolean removed = Boolean.parseBoolean(data[7]);
					int textureIndex = Integer.parseInt(data[8]);
					boolean usesParticles = Boolean.parseBoolean(data[9]);
					
					data = parts[3].split(",");
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
						System.out.println(entities.size() + "/" + max);
					} else {
						Entity e = new Entity(tm, textureIndex, position, rotX, rotY, rotZ, scale);
						e.setRemoved(removed);
						e.setUsesParticles(usesParticles);
						normalMapEntities.add(e);
					}
				} else if (line.startsWith("L:")) {
					
				} else if (line.startsWith("P:")) {
					String[] data = parts[2].split(",");
					Vector3f position = new Vector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
					float rotX = Float.parseFloat(data[3]);
					float rotY = Float.parseFloat(data[4]);
					float rotZ = Float.parseFloat(data[5]);
					float scale = Float.parseFloat(data[6]);
					player.setPosition(position);
					player.setRotX(rotX);
					player.setRotY(rotY);
					player.setRotZ(rotZ);
					player.setScale(scale);
				}
			}
		} catch (Exception e) {
			generate();
		}
	}
	
	private void generate() {
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"), "fern");
		fernTextureAtlas.setNumberOfRows(2);

		TexturedModel fern = new TexturedModel(OBJFileLoader.loadOBJ("fern", loader),
				fernTextureAtlas);

		TexturedModel bobble = new TexturedModel(OBJFileLoader.loadOBJ("pine", loader),
				new ModelTexture(loader.loadTexture("pine"), "pine"));
		bobble.getTexture().setHasTransparency(true);
		
		Random random = new Random(5666778);
		float waterHeight = water.getHeight();
		for (int i = 0; i < WORLD_SIZE; i++) {
			if (i % 3 == 0) {
				float x = (random.nextFloat() * WORLD_SIZE);
				float z = (random.nextFloat() * WORLD_SIZE);
				if ((x > 50 && x < 100) || (z < -50 && z > -100)) {
				} else {
					float y = terrain.getHeightOfTerrain(x, z);
					if (y < waterHeight) continue;
					entities.add(new Entity(fern, 3, new Vector3f(x, y, z), 0,
							random.nextFloat() * 360, 0, 0.9f));
				}
			}
			if (i % 2 == 0) {

				float x = (random.nextFloat() * WORLD_SIZE);
				float z = (random.nextFloat() * WORLD_SIZE);
				if ((x > 50 && x < 100) || (z < -50 && z > -100)) {
				} else {
					float y = terrain.getHeightOfTerrain(x, z);
					if (y < waterHeight) continue;
					entities.add(new Tree(loader, bobble,new Vector3f(x, y, z), random.nextFloat() * 0.6f + 0.8f));
				}
			}
		}
	}
	
	public void save() {
		String[] data = new String[entities.size() + normalMapEntities.size() + lights.size() + 1];
		int index = 0;
		for (Entity e : entities) {
			data[index] = "E:" + index + ":" + e.getData();
			index++;
		}
		for (Entity e : normalMapEntities) {
			data[index] = "NE:" + e.getData();
			index++;
		}
		for (Light l : lights) {
			data[index] = l.getData();
			index++;
		}
		data[index] = "P:" + player.getData();
		fileLoader.writeData("sector.sec", data);
	}
	
	public void update(Fbo fbo, Fbo outputFbo) {
		player.move(this);
		camera.move();
		
		float xPercent = ((player.getPosition().x / WORLD_SIZE) * 100);
		float zPercent = ((player.getPosition().z / WORLD_SIZE) * 100);
		xPercent = (miniMap.getScale().x / 100) * xPercent * 2;
		zPercent = (miniMap.getScale().y / 100) * zPercent * 2;
		playerMarker.setPosition(new Vector2f(xPercent-1, 1-zPercent));
		ArrayList<Entity> entitiesToRemove = new ArrayList<Entity>();
		renderer.renderShadowMap(entities, sun);
		for (Entity e : entities) {
			if (e instanceof Player) continue;
			if (e.isRemoved()) {
				entitiesToRemove.add(e);
				continue;
			}
			boolean inBounds = false;
			int boundaries = 500;
			int x0 = (int) (player.getPosition().x - boundaries);
			int x1 = (int) (player.getPosition().x + boundaries);
			int z0 = (int) (player.getPosition().z - boundaries);
			int z1 = (int) (player.getPosition().z + boundaries);
			int eX = (int) e.getPosition().x;
			int eZ = (int) e.getPosition().z;
			
			inBounds = (eX > x0 && eX < x1 && eZ > z0 && eZ < z1);
			
			if (inBounds) {
				if (!entitiesToRender.contains(e)) entitiesToRender.add(e);
			} else {
				if (entitiesToRender.contains(e)) entitiesToRender.remove(e);
			}
		}
		entities.removeAll(entitiesToRemove);
		for (Entity e : entitiesToRender) {
			if (e.isUsesParticles()) {
				Vector3f position = e.getPosition();
				position = new Vector3f(position.x, position.y  + 10, position.z);
				Entity.system.generateParticles(position, (e instanceof Tree));
			}
		}
		
		ParticleMaster.update(camera, terrain);
		
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		
		buffers.bindReflectionFrameBuffer();
		float distance = 2 * (camera.getPosition().y - water.getHeight());
		camera.getPosition().y -= distance;
		camera.invertPitch();
		renderer.renderScene(entitiesToRender, normalMapEntitiesToRender, terrain, lightsToRender, camera, new Vector4f(0, 1, 0, -water.getHeight()+1));
		camera.getPosition().y += distance;
		camera.invertPitch();
		
		//render refraction texture
		buffers.bindRefractionFrameBuffer();
		renderer.renderScene(entitiesToRender, normalMapEntitiesToRender, terrain, lightsToRender, camera, new Vector4f(0, -1, 0, water.getHeight()));
		
		//render to screen
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		buffers.unbindCurrentFrameBuffer();	
		
		fbo.bindFrameBuffer();
		renderer.renderScene(entitiesToRender, normalMapEntitiesToRender, terrain, lightsToRender, camera, new Vector4f(0, -1, 0, 100000));	
		waterRenderer.render(water, camera, sun);
		ParticleMaster.renderParticles(camera);
		fbo.unbindFrameBuffer();
		fbo.resolveToFbo(outputFbo);
		PostProcessing.doPostProcessing(outputFbo.getColourTexture());
		
		guiRenderer.render(guiTextures);
		

		if (!loaded) {
			loadData();
			loaded = true;
		}
	}
	
	private Vector2f getScaledVector(float width) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		int aspectX = tk.getScreenSize().width / 100;
		int aspectY = tk.getScreenSize().height / 100;
		float height = (width / aspectY) * aspectX;
		return new Vector2f(width, height);
	}
	
	public void cleanUp() {
		buffers.cleanUp();
		waterShader.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		guiRenderer.cleanUp();
	}
	
	public Terrain getTerrain() {
		return terrain;
	}

}
