package core.entities.world;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import core.entities.Entity;
import core.loaders.Loader;
import core.masters.ParticleSystem;
import core.models.TexturedModel;
import core.textures.ParticleTexture;

public class Tree extends Entity {

	public Tree(Loader loader, TexturedModel model, Vector3f position, float scale) {
		super(model, position, 0, random.nextFloat() * 360, 0, scale);
		usesParticles = random.nextBoolean();
	}
	
	public static void init(Loader loader) {
		system = new ParticleSystem(new ParticleTexture(loader.loadTexture("pine"), 1, false), 1, 5, 0.3f, 4, 0.8f);
		system.randomizeRotation();
		system.setDirection(new Vector3f(0, -1, 0), 0.1f);
		system.setLifeError(0.1f);
		system.setSpeedError(0.4f);
		system.setScaleError(0.8f);
	}

}
