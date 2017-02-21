package core.entities;

public class WaterTile {
	
	public final float TILE_SIZE;
	
	private float height;
	private float x,z;
	
	public WaterTile(float SIZE, float centerX, float centerZ, float height) {
		this.TILE_SIZE = SIZE;
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
