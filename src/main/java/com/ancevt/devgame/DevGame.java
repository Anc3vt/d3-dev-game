package com.ancevt.devgame;

import com.ancevt.onemore.engine.*;

public class DevGame implements Game {

    private static Engine engine;

    @Override
    public void init() {
        // === создаём пол ===
        int groundTex = TextureLoader.loadTextureFromResources("texture/wall.png");
        GameObject ground = createGround(200, groundTex); // плоскость 200x200
        engine.objects.add(ground);

        // === создаём замки ===
        int count = 100;
        float areaSize = 50.0f;

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

    private static GameObject createCastle(String filename, float x, float y, float z) {
        OBJModel obj1 = OBJLoader.load("models/" + filename);
        int tex1 = (obj1.textureFile != null)
                ? TextureLoader.loadTextureFromResources("models/" + obj1.textureFile)
                : TextureLoader.loadTextureFromResources("texture/wall.png");

        GameObject go1 = new MyGameObject(obj1.mesh, tex1);
        go1.setPosition(x, y, z);
        engine.objects.add(go1);
        return go1;
    }

    public static void main(String[] args) {
        DevGame gameApp = new DevGame();

        engine = new Engine();
        engine.run(gameApp);
    }
}
