package dev.voltliz.client.util;

/** Frame-rate independent easing. Call {@link #tick()} once per frame. */
public final class Animator {
	private long lastFrame = System.nanoTime();
	private float delta = 1 / 60f;

	public void tick() {
		long now = System.nanoTime();
		delta = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
		lastFrame = now;
	}

	public float approach(float current, float target, float speed) {
		float factor = 1 - (float) Math.exp(-speed * delta);
		float next = current + (target - current) * factor;
		return Math.abs(target - next) < 0.001f ? target : next;
	}
}
