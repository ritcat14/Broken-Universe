package core.models;

import core.textures.ModelTexture;

public class TexturedModel {
	
	private RawModel rawModel;
	private ModelTexture texture;
	
	public TexturedModel(RawModel model, ModelTexture texture) {
		this.rawModel = model;
		this.texture = texture;
	}
	
	public RawModel getModel() {
		return rawModel;
	}
	
	public ModelTexture getTexture() {
		return texture;
	}

}
