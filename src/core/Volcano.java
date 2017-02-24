package core;

import core.loaders.Loader;
import core.terrains.Terrain;
import core.textures.TerrainTexture;
import core.textures.TerrainTexturePack;

public class Volcano extends Terrain {

	public Volcano(int SIZE, int gridX, int gridZ, Loader loader, TerrainTexturePack texturePack,
			TerrainTexture blendMap) {
		super(SIZE, gridX, gridZ, loader, texturePack, blendMap, "heightmap1");
	}
	
}
