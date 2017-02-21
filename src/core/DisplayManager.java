package core;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

public class DisplayManager {
	// Test commit
	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	public static final int FPS = 120;
	
	public static void createDisplay() {
		ContextAttribs attribs = new ContextAttribs(3,2)
				.withForwardCompatible(true)
				.withProfileCore(true);
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.create(new PixelFormat(), attribs);
			Display.setTitle("Broken Universe");
		} catch (Exception e) { e.printStackTrace(); }
		
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
	}
	
	public static void updateDisplay() {
		Display.sync(FPS);
		Display.update();
	}
	
	public static void closeDisplay() {
		Display.destroy();
	}

}
