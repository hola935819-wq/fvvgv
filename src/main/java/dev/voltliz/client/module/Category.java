package dev.voltliz.client.module;

public enum Category {
	HUD("HUD"),
	PLAYER("Player"),
	CLIENT("Client");

	private final String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
