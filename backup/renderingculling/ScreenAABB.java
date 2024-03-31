package rogo.renderingculling;

import net.minecraft.world.phys.Vec3;

import java.util.Queue;

public record ScreenAABB(Vec3 closest, Queue<Vec3> aabb) {
}
