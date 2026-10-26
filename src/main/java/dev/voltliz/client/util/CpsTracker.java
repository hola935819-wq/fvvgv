package dev.voltliz.client.util;

import java.util.ArrayDeque;
import java.util.Deque;

/** Counts mouse clicks in the last second. Fed by {@code MouseHandlerMixin}. */
public final class CpsTracker {
	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();

	private CpsTracker() {
	}

	public static synchronized void onClick(int button) {
		if (button == 0) {
			LEFT.addLast(System.currentTimeMillis());
		} else if (button == 1) {
			RIGHT.addLast(System.currentTimeMillis());
		}
	}

	public static synchronized int getLeft() {
		return count(LEFT);
	}

	public static synchronized int getRight() {
		return count(RIGHT);
	}

	private static int count(Deque<Long> clicks) {
		long cutoff = System.currentTimeMillis() - 1000;
		while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) {
			clicks.removeFirst();
		}
		return clicks.size();
	}
}
