package dev.voltliz.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Projects world positions to GUI coordinates with the matrices of the last rendered frame,
 * captured by {@code LevelRendererMixin}.
 */
public final class Projection {
	private static final Matrix4f VIEW = new Matrix4f();
	private static final Matrix4f PROJECTION = new Matrix4f();
	private static final Vector4f VECTOR = new Vector4f();
	private static Vec3 camera = Vec3.ZERO;
	private static boolean ready;

	private Projection() {
	}

	public static void capture(Vec3 cameraPos, Matrix4f view, Matrix4f projection) {
		camera = cameraPos;
		VIEW.set(view);
		PROJECTION.set(projection);
		ready = true;
	}

	/**
	 * Writes the GUI position of a world point into {@code out} (x, y) and returns false when the
	 * point is behind the camera.
	 */
	public static boolean project(double x, double y, double z, float[] out) {
		if (!ready) {
			return false;
		}
		VECTOR.set((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z), 1);
		VIEW.transform(VECTOR);
		PROJECTION.transform(VECTOR);
		if (VECTOR.w <= 0.01f) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		float ndcX = VECTOR.x / VECTOR.w;
		float ndcY = VECTOR.y / VECTOR.w;
		out[0] = (ndcX + 1) / 2 * client.getWindow().getGuiScaledWidth();
		out[1] = (1 - ndcY) / 2 * client.getWindow().getGuiScaledHeight();
		return true;
	}
}
