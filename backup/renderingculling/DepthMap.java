package rogo.renderingculling;

import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;

public class DepthMap {
    private final int width;
    private final int height;
    private float[][] depthData;

    public DepthMap(float scale) {
        this.width = (int) (Minecraft.getInstance().getWindow().getWidth() * scale);
        this.height = (int) (Minecraft.getInstance().getWindow().getHeight() * scale);
        getTextureData(CullingHandler.DEPTH_TEXTURE);
    }

    public DepthMap(DepthMap sampler) {
        this.width = sampler.width;
        this.height = sampler.height;
        getTextureData(sampler);
    }

    public int getPosIndex(BlockPos pos) {
        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
        int spacePartitionSize = 2 * renderDistance + 1;

        return (pos.getX() + renderDistance) * spacePartitionSize * spacePartitionSize +
                (pos.getY() + renderDistance) * spacePartitionSize +
                (pos.getZ() + renderDistance);
    }

    public static Vec2i getScreenPosFromIndex(int idx) {
        Window window = Minecraft.getInstance().getWindow();
        int y = idx / window.getWidth();
        int x = idx - (y*window.getWidth());
        return new Vec2i(x, y);
    }

    public boolean isChunkVisible(ScreenAABB entityVertices, Matrix4f view, float offset) {
        int width = this.width;
        int height = this.height;

        double entityDepth = entityVertices.closest().distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())-offset*offset;

        int maxX = -1;
        int maxY = -1;
        int minX = width;
        int minY = height;

        for (Vec3 vertex : entityVertices.aabb()) {
            Vec3i screenCoords = worldToScreenSpace(vertex, view);
            if (screenCoords.getX() >= 0 && screenCoords.getX() < width && screenCoords.getY() >= 0 && screenCoords.getY() < height) {
                if(screenCoords.getX() > maxX)
                    maxX = screenCoords.getX();
                if(screenCoords.getY() > maxY)
                    maxY = screenCoords.getY();
                if(screenCoords.getX() < minX)
                    minX = screenCoords.getX();
                if(screenCoords.getY() < minY)
                    minY = screenCoords.getY();
            }
        }

        boolean visible = false;
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                if(x < depthData.length && y < depthData[x].length) {
                    float currentDepth = depthData[x][y];
                    if(entityDepth < currentDepth*currentDepth) {
                        return true;
                    }
                }
            }
        }
        return visible;
    }

    public boolean isAABBVisible(ScreenAABB entityVertices, Matrix4f view, float offset) {
        int width = this.width;
        int height = this.height;

        float entityDepth = calculateVertexDepth(new Vector3f(entityVertices.closest())) - offset;
        float overwriteDepth = 0;
        boolean updateDepth = Config.UPDATE_DEPTH.get();
        if(updateDepth) {
            Vec3 center = Vec3.ZERO;
            for(Vec3 vec3 : entityVertices.aabb()) {
                center = center.add((float) vec3.x, (float) vec3.y, (float) vec3.z);
            }
            center = center.scale(1f/entityVertices.aabb().size());
            overwriteDepth = calculateVertexDepth(new Vector3f(center));
        }

        int maxX = -1;
        int maxY = -1;
        int minX = width;
        int minY = height;

        for (Vec3 vertex : entityVertices.aabb()) {
            Vec3i screenCoords = worldToScreenSpace(vertex, view);
            if (screenCoords.getX() >= 0 && screenCoords.getX() < width && screenCoords.getY() >= 0 && screenCoords.getY() < height) {
                if(screenCoords.getX() > maxX)
                    maxX = screenCoords.getX();
                if(screenCoords.getY() > maxY)
                    maxY = screenCoords.getY();
                if(screenCoords.getX() < minX)
                    minX = screenCoords.getX();
                if(screenCoords.getY() < minY)
                    minY = screenCoords.getY();
            }
        }

        boolean visible = false;
        for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
                float currentDepth = depthData[x][y];
                if(entityDepth < currentDepth) {
                    if(updateDepth) {
                        visible = true;
                        depthData[x][y] = overwriteDepth;
                    } else
                        return true;
                }
            }
        }
        return visible;
    }

    private float calculateVertexDepth(Vector3f vertex) {
        Vector4f cameraSpace = new Vector4f(vertex);
        cameraSpace.transform(CullingHandler.VIEW_MATRIX.last().pose());
        return -cameraSpace.z();
    }


    private static float convertToLinearDepth(float depth) {
        float near = 0.1f;
        float far = 1000.0f;

        float z = depth * 2.0f - 1.0f;
        return (near * far) / (far + near - z * (far - near));
    }


    private void getTextureData(int textureId) {
        glBindTexture(GL_TEXTURE_2D, textureId);
        depthData = new float[width][height];
        FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, buffer);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                depthData[x][y] = convertToLinearDepth(buffer.get());
            }
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void getTextureData(DepthMap sampler) {
        depthData = new float[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                depthData[x][y] = sampler.depthData[x][y];
            }
        }
    }

    public Vec3i worldToScreenSpace(Vec3 worldPosition, Matrix4f view) {
        Vector4f cameraSpace = new Vector4f((float) worldPosition.x, (float) worldPosition.y, (float) worldPosition.z, 1);
        cameraSpace.transform(view);
        cameraSpace.transform(CullingHandler.PROJECTION_MATRIX);
        Vec3 ndc = new Vec3(cameraSpace.x() / cameraSpace.w(), cameraSpace.y() / cameraSpace.w(), cameraSpace.z() / cameraSpace.w());

        int screenX = (int) ((ndc.x + 1.0f) * 0.5f * width);
        int screenY = (int) ((ndc.y + 1.0f) * 0.5f * height);

        return new Vec3i(screenX, screenY, 1);
    }
}
