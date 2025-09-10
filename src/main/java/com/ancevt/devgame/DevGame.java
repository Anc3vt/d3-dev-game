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
import org.w3c.dom.ls.LSOutput;

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


//        int cubeTex = assetManager.loadTexture("texture/wall.png", true);
//        GameObjectNode cube = new GameObjectNode(MeshFactory.createTexturedCubeMesh(2.0f), cubeTex);
//        cube.setPosition(0, 1, 0);
//        cube.setCollidable(false); // куб твёрдый
//        ctx.getEngine().root.addChild(cube);


        int groundTex = assetManager.loadTexture("texture/ground1.png", true);
        GameObjectNode ground = new GameObjectNode(createGround(200, groundTex, 1000).getMesh(), groundTex);
        ctx.getEngine().root.addChild(ground);

//        int count = 20;
//        float areaSize = 50.0f;
//
//        for (int i = 0; i < count; i++) {
//            float x = (float) (Math.random() * areaSize - areaSize / 2);
//            float z = (float) (Math.random() * areaSize - areaSize / 2);
//
//            GameObjectNode castle = createCastle("castle.obj", x, 0, z);
//            castle.setScale(1f, (float) (Math.random() * 5f), 1f);
//            castle.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());
//
//            castle.setCollidable(true);
//
//            ctx.getEngine().root.addChild(castle);
//        }


        int mazeWidth = 30;
        int mazeHeight = 30;
        float cubeSize = 1.0f;

        int wallTex = assetManager.loadTexture("texture/wall.png", true);

        boolean[][] maze = generateMaze(mazeWidth, mazeHeight);

        for (int x = 0; x < mazeWidth; x++) {
            for (int z = 0; z < mazeHeight; z++) {
                if (maze[x][z]) { // стена
                    GameObjectNode wall = new GameObjectNode(
                            MeshFactory.createTexturedCubeMesh(cubeSize),
                            wallTex
                    );
                    wall.setPosition(
                            x * cubeSize - mazeWidth * cubeSize / 2,
                            cubeSize / 2,
                            z * cubeSize - mazeHeight * cubeSize / 2
                    );

                    // случайная высота
                    float height = 1.0f + (float) (Math.random() * 2.0f);
                    wall.setScale(1.0f, height, 1.0f);

                    // случайный цвет
                    //wall.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());

                    ctx.getEngine().root.addChild(wall);
                }
            }
        }
    }

    private boolean[][] generateMaze(int width, int height) {
        boolean[][] maze = new boolean[width][height];

        // Заполнить всё стенами
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                maze[x][z] = true; // стена
            }
        }

        // Начало в (1,1)
        carve(1, 1, maze, width, height);

        return maze;
    }

    private void carve(int x, int z, boolean[][] maze, int width, int height) {
        int[] dx = {2, -2, 0, 0};
        int[] dz = {0, 0, 2, -2};

        Integer[] dirs = {0, 1, 2, 3};
        java.util.Collections.shuffle(java.util.Arrays.asList(dirs));

        maze[x][z] = false; // пустота

        for (int dir : dirs) {
            int nx = x + dx[dir];
            int nz = z + dz[dir];

            if (nx > 0 && nz > 0 && nx < width - 1 && nz < height - 1) {
                if (maze[nx][nz]) {
                    maze[x + dx[dir] / 2][z + dz[dir] / 2] = false; // пробить стену
                    carve(nx, nz, maze, width, height);
                }
            }
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
        ctx.getEngine().mainLight.getPosition().y += 0.001f;
    }

    @Override
    public void shutdown() {

    }
}
