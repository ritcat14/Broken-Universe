package core;

import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import core.entities.Camera;
import core.entities.Entity;
import core.entities.LavaTile;
import core.entities.Light;
import core.entities.Player;
import core.entities.WaterTile;
import core.entities.world.Tree;
import core.fontMeshCreator.FontType;
import core.fontMeshCreator.GUIText;
import core.loaders.Loader;
import core.loaders.NormalMappedObjLoader;
import core.loaders.OBJFileLoader;
import core.loaders.OBJLoader;
import core.masters.ParticleMaster;
import core.masters.ParticleSystem;
import core.masters.PostProcessing;
import core.modelData.Fbo;
import core.modelData.FrameBuffers;
import core.models.TexturedModel;
import core.renderers.GuiRenderer;
import core.renderers.LavaRenderer;
import core.renderers.MasterRenderer;
import core.renderers.WaterRenderer;
import core.shaders.LavaShader;
import core.shaders.WaterShader;
import core.terrains.Terrain;
import core.textures.GuiTexture;
import core.textures.ModelTexture;
import core.textures.ParticleTexture;
import core.textures.TerrainTexture;
import core.textures.TerrainTexturePack;
import core.toolbox.MousePicker;

public class World {

	private final int WORLD_SIZE = 4096;
	
	private GUIText text;
	private FontType font;
	private GuiRenderer guiRenderer;
	private GuiTexture playerMarker, miniMap;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> normalMapEntities = new ArrayList<Entity>();
	private List<Light> lights = new ArrayList<Light>();
	private List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
	
	private Terrain terrain;
	
	protected WaterTile water;
	protected WaterRenderer waterRenderer;
	protected WaterShader waterShader;
	
	private Loader loader;
	private Player player;
	private Camera camera;
	private MasterRenderer renderer;
	private FrameBuffers buffers;
	private Light sun;
	
	private int worldSectorWidth = 4;
	private int worldSectorHeight = 4;
	private int sectorSize = WORLD_SIZE / 4;
	
	private Sector[] sectors;
	private Sector currSector;
	private Sector preSector;
	
	public World(Loader loader, MasterRenderer renderer, Camera camera, Player player) {
		this.loader = loader;
		this.player = player;
		this.renderer = renderer;
		this.camera = camera;
		init();
	}
	
	private void init() {
		entities.add(player);
		
		buffers = new FrameBuffers();
		sectors = new Sector[worldSectorWidth * worldSectorHeight];
		for (int x = 0; x < worldSectorWidth; x++) {
			for (int y = 0; y < worldSectorHeight; y++) {
				sectors[x + y * worldSectorWidth] = new Sector(x + y * worldSectorWidth, loader, x * sectorSize, y * sectorSize);
			}
		}
		
		currSector = sectors[0];
		
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
		water = new WaterTile(WORLD_SIZE, WORLD_SIZE, WORLD_SIZE/2, WORLD_SIZE/2, -15);

		sun = new Light(new Vector3f(1000000, 1500000, -1000000), new Vector3f(1.3f, 1.3f, 1.3f));
		lights.add(sun);

		font = new FontType(loader.loadTexture("candara"), new File("res/candara.fnt"));
		text = new GUIText("Sector: " + currSector.getID(), 1f, font, new Vector2f(0f, 0f), 1f, false);
		text.setColour(0, 0, 0);
		float y = terrain.getHeightOfTerrain(sectorSize, sectorSize);
		player.setPosition(new Vector3f(sectorSize, y, sectorSize));
		
		guiRenderer = new GuiRenderer(loader);
		
		Vector2f size = getScaledVector(0.125f);
		miniMap = new GuiTexture(loader.loadTexture("miniMap"), new Vector2f(size.x - 1, 1 - size.y), size);
		guiTextures.add(miniMap);
		
		size = getScaledVector(0.004f);
		playerMarker = new GuiTexture(loader.loadTexture("playerMarker"), new Vector2f(0, 0), size);
		guiTextures.add(playerMarker);
	}
	
	public void update(Fbo fbo, Fbo outputFbo) {
		currSector = getSector();
		if (preSector != null) {
			if (!currSector.equals(preSector)) {
				entities.addAll(currSector.getEntities());
				lights.addAll(currSector.getLights());
				normalMapEntities.addAll(currSector.getNormalEntities());
				text.setText("Sector: " + currSector.getID());
			}
		}
		preSector = currSector;

		float xPercent = ((player.getPosition().x / WORLD_SIZE) * miniMap.getScale().x);
		float zPercent = ((player.getPosition().z / WORLD_SIZE) * miniMap.getScale().y);
		playerMarker.setPosition(new Vector2f(- (1 - xPercent), zPercent));
		System.out.println(playerMarker.getPosition().x + " : " + playerMarker.getPosition().y);
		
		player.move(this);
		camera.move();
		
		renderer.renderShadowMap(entities, sun);
		
		for (Entity e : entities) {
			boolean inBounds = false;
			int boundaries = 500;
			int x0 = (int) (player.getPosition().x - boundaries);
			int x1 = (int) (player.getPosition().x + boundaries);
			int z0 = (int) (player.getPosition().z - boundaries);
			int z1 = (int) (player.getPosition().z + boundaries);
			int eX = (int) e.getPosition().x;
			int eZ = (int) e.getPosition().z;
			
			inBounds = (eX > x0 && eX < x1 && eZ > z0 && eZ < z1);
			
			if (!inBounds) {
				e.remove();
				continue;
			} else {
				if (e.isRemoved()) e.setRemoved(false);
			}
			if (e.isUsesParticles() && !e.isRemoved()) {
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
		renderer.renderScene(entities, normalMapEntities, terrain, lights, camera, new Vector4f(0, 1, 0, -water.getHeight()+1));
		camera.getPosition().y += distance;
		camera.invertPitch();
		
		//render refraction texture
		buffers.bindRefractionFrameBuffer();
		renderer.renderScene(entities, normalMapEntities, terrain, lights, camera, new Vector4f(0, -1, 0, water.getHeight()));
		
		//render to screen
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		buffers.unbindCurrentFrameBuffer();	
		
		fbo.bindFrameBuffer();
		renderer.renderScene(entities, normalMapEntities, terrain, lights, camera, new Vector4f(0, -1, 0, 100000));	
		waterRenderer.render(water, camera, sun);
		ParticleMaster.renderParticles(camera);
		fbo.unbindFrameBuffer();
		fbo.resolveToFbo(outputFbo);
		PostProcessing.doPostProcessing(outputFbo.getColourTexture());
		
		guiRenderer.render(guiTextures);
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
	
	public void add(Entity e) {
		entities.add(e);
	}
	
	public Terrain getTerrain() {
		return terrain;
	}
	
	private Sector getSector() {
		int playerTerrainX = (int) Math.floor(player.getPosition().x / sectorSize); // convert player coordinates to terrain coordinates
	    int playerTerrainZ = (int) Math.floor(player.getPosition().z / sectorSize) ;
	    return sectors[playerTerrainX + playerTerrainZ * worldSectorWidth];
	}

}
