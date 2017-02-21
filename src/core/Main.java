package core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import core.entities.*;
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
import core.masters.TextMaster;
import core.modelData.Fbo;
import core.modelData.WaterFrameBuffers;
import core.models.RawModel;
import core.models.TexturedModel;
import core.renderers.GuiRenderer;
import core.renderers.MasterRenderer;
import core.renderers.WaterRenderer;
import core.shaders.WaterShader;
import core.terrains.Terrain;
import core.textures.GuiTexture;
import core.textures.ModelTexture;
import core.textures.ParticleTexture;
import core.textures.TerrainTexture;
import core.textures.TerrainTexturePack;
import core.toolbox.MousePicker;

public class Main {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		TextMaster.init(loader);
		Tree.init(loader);
		
		//*******************OTHER SETUP***************

		RawModel bunnyModel = OBJLoader.loadObjModel("person", loader);
		TexturedModel stanfordBunny = new TexturedModel(bunnyModel, new ModelTexture(
				loader.loadTexture("playerTexture")));

		Player player = new Player(stanfordBunny, new Vector3f(75, 5, -75), 0, 100, 0, 0.6f);
		Camera camera = new Camera(player);
		
		MasterRenderer renderer = new MasterRenderer(loader, camera);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		FontType font = new FontType(loader.loadTexture("candara"), new File("res/candara.fnt"));
		GUIText text = new GUIText("Broken Universe", 3f, font, new Vector2f(0f, 0f), 1f, true);
		text.setColour(0, 0, 0);
		
		World world = new World(loader, renderer, camera, player);
		
		
		List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		//GuiTexture shadowMap = new GuiTexture(renderer.getShadowMapTexture(), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f));
		//guiTextures.add(shadowMap);
		
		Fbo multisampleFbo = new Fbo(Display.getWidth(), Display.getHeight());
		Fbo outputFbo = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		PostProcessing.init(loader);
		
		//****************Game Loop Below*********************

		while (!Display.isCloseRequested()) {
			world.update(multisampleFbo, outputFbo);
			
			guiRenderer.render(guiTextures);
			TextMaster.render();
			
			DisplayManager.updateDisplay();
		}

		//*********Clean Up Below**************
		
		PostProcessing.cleanUp();
		multisampleFbo.cleanUp();
		ParticleMaster.cleanUp();
		TextMaster.cleanUp();
		guiRenderer.cleanUp();
		DisplayManager.closeDisplay();

	}


}
