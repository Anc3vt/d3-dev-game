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

        // === Skybox ===
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

        Engine.skybox = new Skybox(cubemapTex, skyboxShader);

        // === Текстуры ===
        int groundTex = assetManager.loadTexture("texture/ground1.png", true);
        int wallTex = assetManager.loadTexture("texture/wall.png", true);

        // === Генерация многоэтажного лабиринта ===
        generateMultiFloorMaze(
                ctx,
                6,   // ширина
                7,   // глубина
                4,    // этажи
                1.0f, // размер куба
                3.0f, // высота этажа
                groundTex,
                wallTex
        );
    }


    private void generateMultiFloorMaze(
            EngineContext ctx,
            int mazeWidth,
            int mazeDepth,
            int mazeLevels,
            float cubeSize,
            float levelHeight,
            int groundTex,
            int wallTex
    ) {
        boolean[][][] maze = new boolean[mazeWidth][mazeDepth][mazeLevels];

        for (int y = 0; y < mazeLevels; y++) {
            boolean[][] level = generateMaze(mazeWidth, mazeDepth);

            for (int x = 0; x < mazeWidth; x++) {
                for (int z = 0; z < mazeDepth; z++) {
                    maze[x][z][y] = level[x][z];
                }
            }

            // создаём "лестницу" (проход между уровнями)
            if (y < mazeLevels - 1) {
                int stairX = 1 + (int) (Math.random() * (mazeWidth - 2));
                int stairZ = 1 + (int) (Math.random() * (mazeDepth - 2));
                maze[stairX][stairZ][y] = false;
                maze[stairX][stairZ][y + 1] = false;
            }

            // === пол (платформы) ===
            for (int x = 0; x < mazeWidth; x++) {
                for (int z = 0; z < mazeDepth; z++) {

                    // иногда убираем часть плит на верхних уровнях
                    if (Math.random() < 0.15 && y > 0) continue;

                    GameObjectNode tile = new GameObjectNode(
                            MeshFactory.createTexturedCubeMesh(cubeSize),
                            groundTex
                    );

                    // случайное смещение по высоте (±0.25)
                    float yOffset = (float) (Math.random() * 0.5f - 0.25f);

                    tile.setPosition(
                            x * cubeSize - mazeWidth * cubeSize / 2,
                            y * levelHeight - (cubeSize * 0.1f) + yOffset,
                            z * cubeSize - mazeDepth * cubeSize / 2
                    );

                    // наклон плитки (±5 градусов)
                    float tiltX = (float) (Math.random() * 10f - 5f);
                    float tiltZ = (float) (Math.random() * 10f - 5f);
                    tile.getRotation().x = tiltX;
                    tile.getRotation().z = tiltZ;

                    // тонкая платформа
                    tile.setScale(1.0f, 0.2f, 1.0f);

                    tile.setCollidable(true);
                    ctx.getEngine().root.addChild(tile);
                }
            }
        }

        // === стены ===
        for (int y = 0; y < mazeLevels; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                for (int z = 0; z < mazeDepth; z++) {
                    if (maze[x][z][y]) {
                        GameObjectNode wall = new GameObjectNode(
                                MeshFactory.createTexturedCubeMesh(cubeSize),
                                wallTex
                        );

                        wall.setPosition(
                                x * cubeSize - mazeWidth * cubeSize / 2,
                                cubeSize / 2 + y * levelHeight,
                                z * cubeSize - mazeDepth * cubeSize / 2
                        );

                        float height = 2.0f + (float) (Math.random() * 1.5f);
                        wall.setScale(1.0f, height, 1.0f);

                        ctx.getEngine().root.addChild(wall);
                    }
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
