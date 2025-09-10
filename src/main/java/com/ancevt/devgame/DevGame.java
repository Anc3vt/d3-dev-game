package com.ancevt.devgame;

import com.ancevt.d3.engine.asset.OBJLoader;
import com.ancevt.d3.engine.asset.OBJModel;
import com.ancevt.d3.engine.asset.TextureLoader;
import com.ancevt.d3.engine.core.Application;
import com.ancevt.d3.engine.core.Engine;
import com.ancevt.d3.engine.core.EngineContext;
import com.ancevt.d3.engine.core.LaunchConfig;
import com.ancevt.d3.engine.scene.GameObject;
import com.ancevt.d3.engine.scene.GameObjectNode;
import com.ancevt.d3.engine.scene.Mesh;

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

        int groundTex = TextureLoader.loadTextureFromResources("texture/ground1.png", true);
        GameObjectNode ground = new GameObjectNode(createGround(200, groundTex, 1000).getMesh(), groundTex);
        ctx.getEngine().root.addChild(ground);

        int count = 30;
        float areaSize = 10.0f;

        for (int i = 0; i < count; i++) {
            float x = (float) (Math.random() * areaSize - areaSize / 2);
            float z = (float) (Math.random() * areaSize - areaSize / 2);

            GameObjectNode castle = createCastle("castle.obj", x, 0, z);
            castle.setScale(1f, (float) (Math.random() * 5f), 1f);
            //castle.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());

            ctx.getEngine().root.addChild(castle);
        }
    }


    public static GameObject createGround(float size, int textureId, float repeat) {
        float[] vertices = {
                -size, 0, -size, 0,      0,       0, 1, 0,
                size, 0, -size, repeat, 0,       0, 1, 0,
                size, 0,  size, repeat, repeat,  0, 1, 0,

                -size, 0, -size, 0,      0,       0, 1, 0,
                size, 0,  size, repeat, repeat,  0, 1, 0,
                -size, 0,  size, 0,      repeat,  0, 1, 0,
        };

        Mesh mesh = new Mesh(vertices, 8);
        return new GameObject(mesh, textureId);
    }


    private GameObjectNode createCastle(String filename, float x, float y, float z) {
        OBJModel obj1 = OBJLoader.load("models/" + filename);
        int tex1 = TextureLoader.loadTextureFromResources("texture/wall.png", true);
        GameObjectNode go1 = new GameObjectNode(obj1.mesh, tex1);
        go1.setPosition(x, y, z);

        // 👇 натягиваем текстуру 5х5 раз
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
