package core.entities;

public class LavaTile {
	
	public final float TILE_WIDTH;
	public final float TILE_HEIGHT;
	
	private float height;
	private float x,z;
	
	public LavaTile(float WIDTH, float HEIGHT, float centerX, float centerZ, float height) {
		this.TILE_WIDTH = WIDTH;
		this.TILE_HEIGHT = HEIGHT;
		this.x = centerX;
		this.z = centerZ;
		this.height = height;
	}

	public float getHeight() {
		return height;
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}



}
