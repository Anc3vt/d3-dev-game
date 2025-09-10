package com.ancevt.devgame;

import com.ancevt.d3.engine.asset.AssetManager;
import com.ancevt.d3.engine.asset.OBJModel;
import com.ancevt.d3.engine.asset.TextureLoader;
import com.ancevt.d3.engine.core.Application;
import com.ancevt.d3.engine.core.Engine;
import com.ancevt.d3.engine.core.EngineContext;
import com.ancevt.d3.engine.core.LaunchConfig;
import com.ancevt.d3.engine.render.ShaderProgram;
import com.ancevt.d3.engine.scene.*;
import com.ancevt.d3.engine.util.TextLoader;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;

public class DevGame implements Application {


    public static void main(String[] args) {
        new Engine(
                LaunchConfig.builder()
                        .width(2000)
                        .height(1000)
                        .title("D3 Dev")
                        .build()
        ).start(new DevGame());
    }

    private EngineContext ctx;

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;

        AssetManager assetManager = ctx.getAssetManager();

        String[] faces = {
                "skybox/right.png",
                "skybox/left.png",
                "skybox/top.png",
                "skybox/bottom.png",
                "skybox/front.png",
                "skybox/back.png"
        };
        int cubemapTex = TextureLoader.loadCubemap(faces);

        ShaderProgram skyboxShader = new ShaderProgram();
        skyboxShader.attachShader(TextLoader.load("shaders/skybox.vert"), GL_VERTEX_SHADER);
        skyboxShader.attachShader(TextLoader.load("shaders/skybox.frag"), GL_FRAGMENT_SHADER);
        skyboxShader.link();

        skyboxShader.use();
        int loc = glGetUniformLocation(skyboxShader.getId(), "skybox");
        glUniform1i(loc, 0);

        Skybox skybox = new Skybox(cubemapTex, skyboxShader);

        Engine.skybox = skybox;


        int cubeTex = assetManager.loadTexture("texture/wall.png", true);
        GameObjectNode cube = new GameObjectNode(MeshFactory.createTexturedCubeMesh(2.0f), cubeTex);
        cube.setPosition(0, 1, 0);
        cube.setCollidable(false); // куб твёрдый
        ctx.getEngine().root.addChild(cube);


        int groundTex = assetManager.loadTexture("texture/ground1.png", true);
        GameObjectNode ground = new GameObjectNode(createGround(200, groundTex, 1000).getMesh(), groundTex);
        ctx.getEngine().root.addChild(ground);

        int count = 20;
        float areaSize = 50.0f;

        for (int i = 0; i < count; i++) {
            float x = (float) (Math.random() * areaSize - areaSize / 2);
            float z = (float) (Math.random() * areaSize - areaSize / 2);

            GameObjectNode castle = createCastle("castle.obj", x, 0, z);
            castle.setScale(1f, (float) (Math.random() * 5f), 1f);
            castle.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());

            castle.setCollidable(true);

            ctx.getEngine().root.addChild(castle);
        }
    }


    public static GameObject createGround(float size, int textureId, float repeat) {
        float[] vertices = {
                -size, 0, -size, 0, 0, 0, 1, 0,
                size, 0, -size, repeat, 0, 0, 1, 0,
                size, 0, size, repeat, repeat, 0, 1, 0,

                -size, 0, -size, 0, 0, 0, 1, 0,
                size, 0, size, repeat, repeat, 0, 1, 0,
                -size, 0, size, 0, repeat, 0, 1, 0,
        };

        Mesh mesh = new Mesh(vertices, 8);
        return new GameObject(mesh, textureId);
    }


    private GameObjectNode createCastle(String filename, float x, float y, float z) {
        AssetManager assetManager = ctx.getAssetManager();

        OBJModel obj1 = assetManager.loadObj("models/" + filename);
        int tex1 = assetManager.loadTexture("texture/wall.png", true);
        GameObjectNode go1 = new GameObjectNode(obj1.mesh, tex1);
        go1.setPosition(x, y, z);

        go1.setTextureRepeat(5, 5);

        return go1;
    }

    @Override
    public void update() {

    }

    @Override
    public void shutdown() {

    }
}
