package core;

import java.io.File;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import core.entities.*;
import core.entities.world.Tree;
import core.fontMeshCreator.FontType;
import core.fontMeshCreator.GUIText;
import core.loaders.Loader;
import core.loaders.OBJLoader;
import core.masters.ParticleMaster;
import core.masters.PostProcessing;
import core.masters.TextMaster;
import core.modelData.Fbo;
import core.models.RawModel;
import core.models.TexturedModel;
import core.renderers.MasterRenderer;
import core.textures.ModelTexture;

public class Main {

	public static void main(String[] args) {
		DisplayManager.createDisplay();
		System.out.println(GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
		Loader loader = new Loader();
		TextMaster.init(loader);
		Tree.init(loader);
		
		//*******************OTHER SETUP***************

		RawModel bunnyModel = OBJLoader.loadObjModel("person", loader);
		TexturedModel stanfordBunny = new TexturedModel(bunnyModel, new ModelTexture(
				loader.loadTexture("playerTexture"), "playerTexture"));

		Player player = new Player(stanfordBunny, new Vector3f(0, 0, 0), 0, 0, 0, 0.6f);
		Camera camera = new Camera(player);
		
		MasterRenderer renderer = new MasterRenderer(loader, camera);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		FontType font = new FontType(loader.loadTexture("candara"), new File("res/candara.fnt"));
		GUIText text = new GUIText("Broken Universe", 3f, font, new Vector2f(0f, 0f), 1f, true);
		text.setColour(0, 0, 0);
		
		World world = new World(loader, renderer, camera, player);
		
		
		Fbo multisampleFbo = new Fbo(Display.getWidth(), Display.getHeight());
		Fbo outputFbo = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		PostProcessing.init(loader);
		
		//****************Game Loop Below*********************

		while (!Display.isCloseRequested()) {
			world.update(multisampleFbo, outputFbo);
			
			TextMaster.render();
			
			DisplayManager.updateDisplay();
		}

		//*********Clean Up Below**************
		
		PostProcessing.cleanUp();
		multisampleFbo.cleanUp();
		ParticleMaster.cleanUp();
		TextMaster.cleanUp();
		DisplayManager.closeDisplay();
	}


}
