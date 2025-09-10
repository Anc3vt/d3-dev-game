package com.ancevt.devgame;

import com.ancevt.d3.engine.asset.OBJLoader;
import com.ancevt.d3.engine.asset.OBJModel;
import com.ancevt.d3.engine.asset.TextureLoader;
import com.ancevt.d3.engine.core.Application;
import com.ancevt.d3.engine.core.LaunchConfig;
import com.ancevt.d3.engine.core.Engine;
import com.ancevt.d3.engine.core.EngineContext;
import com.ancevt.d3.engine.scene.GameObject;
import com.ancevt.d3.engine.scene.Mesh;
import com.ancevt.d3.engine.scene.MyGameObject;

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

        // === создаём пол ===
        int groundTex = TextureLoader.loadTextureFromResources("texture/ground1.png");
        GameObject ground = createGround(200, groundTex); // плоскость 200x200
        ctx.getEngine().objects.add(ground);

        // === создаём замки ===
        int count = 30;
        float areaSize = 20.0f;

        for (int i = 0; i < count; i++) {
            float x = (float) (Math.random() * areaSize - areaSize / 2);
            float z = (float) (Math.random() * areaSize - areaSize / 2);
            GameObject castle = createCastle("castle.obj", x, (float) (Math.random() * 5), z);
            castle.setScale(1f, (float) (Math.random() + 0.5f), 1f);
            castle.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());

        }
    }

    public static GameObject createGround(float size, int textureId) {
        // Вершины квадрата (две треугольные плоскости)
        float[] vertices = {
                // posX, posY, posZ,   u, v,   nx, ny, nz
                -size, 0, -size, 0, 0, 0, 1, 0,
                size, 0, -size, 1, 0, 0, 1, 0,
                size, 0, size, 1, 1, 0, 1, 0,

                -size, 0, -size, 0, 0, 0, 1, 0,
                size, 0, size, 1, 1, 0, 1, 0,
                -size, 0, size, 0, 1, 0, 1, 0,
        };

        Mesh mesh = new Mesh(vertices, 8);
        return new GameObject(mesh, textureId);
    }

    private GameObject createCastle(String filename, float x, float y, float z) {
        OBJModel obj1 = OBJLoader.load("models/" + filename);
        int tex1 = (obj1.textureFile != null)
                ? TextureLoader.loadTextureFromResources("models/" + obj1.textureFile)
                : TextureLoader.loadTextureFromResources("texture/wall.png");

        GameObject go1 = new MyGameObject(obj1.mesh, tex1);
        go1.setPosition(x, y, z);
        ctx.getEngine().objects.add(go1);
        return go1;
    }

    @Override
    public void update() {

    }

    @Override
    public void shutdown() {

    }
}
