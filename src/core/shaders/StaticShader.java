package core.shaders;

import org.lwjgl.util.vector.Matrix4f;

import core.entities.Camera;
import core.entities.Light;
import core.toolbox.Maths;

public class StaticShader extends ShaderProgram {
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_lightPosition;
	private int location_lightColour;

	public StaticShader() {
		super("src/core/shaders/vertexShader.txt", "src/core/shaders/fragmentShader.txt");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
		super.bindAttribute(2, "normal");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_lightPosition = super.getUniformLocation("lightPosition");
		location_lightColour = super.getUniformLocation("lightColour");
	}
	
	public void loadLight(Light light) {
		super.loadVector(location_lightPosition, Maths.vector3fToFloat(light.getPosition()));
		super.loadVector(location_lightColour, Maths.vector3fToFloat(light.getColour()));
	}
	
	public void loadTransformationMatrix(Matrix4f matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix) {
		super.loadMatrix(location_projectionMatrix, matrix);
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}

}
