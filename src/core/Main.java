package core;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import core.entities.*;
import core.loaders.Loader;
import core.loaders.OBJLoader;
import core.models.*;
import core.renderers.Renderer;
import core.shaders.StaticShader;
import core.textures.ModelTexture;

public class Main {
	
	public static void main(String[] args) {
		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);
		
		RawModel model = OBJLoader.loadObjModel("stall", loader);
		
		TexturedModel texturedModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("stallTexture")));
		
		Entity entity = new Entity(texturedModel, new Vector3f(-1,0,0),0,0,0,1);
		
		Light light = new Light(new Vector3f(0, 0, -20), new Vector3f(1, 1, 1));
		
		Camera camera = new Camera();
		
		while(!Display.isCloseRequested()) {
			camera.move();
			renderer.prepare();
			shader.start();
			shader.loadLight(light);
			shader.loadViewMatrix(camera);
			renderer.render(entity, shader);
			shader.stop();
			DisplayManager.updateDisplay();
		}
		loader.cleanUp();
		shader.cleanUp();
		DisplayManager.closeDisplay();
	}

}
