package dev.voltliz.client.gui;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.gui.component.ModuleCard;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.module.client.ClickGuiModule;
import dev.voltliz.client.render.Icons;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** The Voltliz menu: a centered window with a category sidebar, a search box and a grid of module cards. */
public class ClickGuiScreen extends Screen {
	private static final int SIDEBAR_WIDTH = 118;
	private static final int RADIUS = 12;
	private static final int GAP = 7;
	private static final int CONTENT_TOP = 46;
	private static final int ITEM_HEIGHT = 24;
	private static final int SEARCH_WIDTH = 132;

	private final Animator animator = new Animator();
	private final Map<Category, List<ModuleCard>> cards = new EnumMap<>(Category.class);
	private final List<ModuleCard> allCards = new ArrayList<>();
	private final float[] categoryHover = new float[Category.values().length];
	private final float[] categorySelect = new float[Category.values().length];

	private int windowX;
	private int windowY;
	private int windowWidth;
	private int windowHeight;
	private float scroll;
	private float scrollTarget;
	private float hudButtonHover;
	private float openProgress;
	private String search = "";

	public ClickGuiScreen() {
		super(Component.literal(VoltlizClient.NAME));
		for (Category category : Category.values()) {
			List<ModuleCard> list = new ArrayList<>();
			for (Module module : VoltlizClient.get().getModuleManager().getModules(category)) {
				list.add(new ModuleCard(module, animator));
			}
			cards.put(category, list);
			allCards.addAll(list);
		}
		categorySelect[GuiState.selectedCategory.ordinal()] = 1;
	}

	@Override
	protected void init() {
		windowWidth = Math.min(480, width - 24);
		windowHeight = Math.min(300, height - 24);
		windowX = (width - windowWidth) / 2;
		windowY = (height - windowHeight) / 2;
	}

	private List<ModuleCard> visibleCards() {
		if (search.isEmpty()) {
			return cards.get(GuiState.selectedCategory);
		}
		String query = search.toLowerCase(Locale.ROOT);
		return allCards.stream()
				.filter(card -> card.getModule().getName().toLowerCase(Locale.ROOT).contains(query)
						|| card.getModule().getDescription().toLowerCase(Locale.ROOT).contains(query))
				.toList();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		ClickGuiModule gui = Theme.gui();
		if (gui == null || gui.dimBackground.get()) {
			int alpha = Math.round(openProgress * 0xB0);
			graphics.fillGradient(0, 0, width, height, ColorUtil.withAlpha(0x04050A, alpha * 2 / 3), ColorUtil.withAlpha(0x04050A, alpha));
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		animator.tick();
		openProgress = animator.approach(openProgress, 1, 9);

		// Fade in and grow slightly from the center.
		float scale = 0.94f + 0.06f * openProgress;
		float centerX = width / 2f;
		float centerY = height / 2f;
		int localX = Math.round(centerX + (mouseX - centerX) / scale);
		int localY = Math.round(centerY + (mouseY - centerY) / scale);
		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().scale(scale, scale);
		graphics.pose().translate(-centerX, -centerY);
		Render2D.setAlpha(openProgress);

		Render2D.shadow(graphics, windowX, windowY, windowWidth, windowHeight, RADIUS, 14, 0xB0);
		Render2D.shadow(graphics, windowX, windowY, windowWidth, windowHeight, RADIUS, 10, 0x18, Theme.accent());
		Render2D.verticalGradient(graphics, windowX, windowY, windowWidth, windowHeight, RADIUS, Theme.WINDOW_TOP, Theme.WINDOW_BOTTOM);
		Render2D.roundedOutline(graphics, windowX, windowY, windowWidth, windowHeight, RADIUS, 0x16FFFFFF);

		renderSidebar(graphics, localX, localY);
		renderHeader(graphics, localX, localY);
		renderCards(graphics, localX, localY);

		Render2D.setAlpha(1);
		graphics.pose().popMatrix();
	}

	private void renderSidebar(GuiGraphics graphics, int mouseX, int mouseY) {
		Render2D.roundedRect(graphics, windowX, windowY, SIDEBAR_WIDTH, windowHeight, RADIUS, Theme.SIDEBAR);
		Render2D.rect(graphics, windowX + SIDEBAR_WIDTH - RADIUS, windowY, RADIUS, windowHeight, Theme.SIDEBAR);
		Render2D.rect(graphics, windowX + SIDEBAR_WIDTH, windowY + 1, 1, windowHeight - 2, Theme.SEPARATOR);

		// Logo and wordmark.
		int logoX = windowX + 12;
		int logoY = windowY + 12;
		Render2D.shadow(graphics, logoX, logoY, 24, 24, 6, 6, 0x50, Theme.accent());
		Icons.logo(graphics, logoX, logoY, 24);
		VText.drawGradient(graphics, "Voltliz", logoX + 31, logoY + 2, Theme.TEXT, Theme.accent(), true);
		VText.drawScaled(graphics, "C L I E N T", logoX + 31, logoY + 14, Theme.TEXT_DIM, false, 0.7f);

		VText.drawScaled(graphics, "MENU", windowX + 14, windowY + 50, ColorUtil.withAlpha(Theme.TEXT_DIM, 0xA0), true, 0.7f);
		int itemY = windowY + 60;
		for (Category category : Category.values()) {
			int index = category.ordinal();
			boolean selected = search.isEmpty() && GuiState.selectedCategory == category;
			boolean hovered = RenderUtil.isInside(mouseX, mouseY, windowX + 8, itemY, SIDEBAR_WIDTH - 16, ITEM_HEIGHT - 2);
			categoryHover[index] = animator.approach(categoryHover[index], hovered ? 1 : 0, 18);
			categorySelect[index] = animator.approach(categorySelect[index], selected ? 1 : 0, 14);

			float select = categorySelect[index];
			int itemX = windowX + 8;
			int itemWidth = SIDEBAR_WIDTH - 16;
			Render2D.roundedRect(graphics, itemX, itemY, itemWidth, ITEM_HEIGHT - 2, 7, ColorUtil.withAlpha(0xFFFFFF, Math.round(categoryHover[index] * 0x0A)));
			if (select > 0.01f) {
				Render2D.horizontalGradient(graphics, itemX, itemY, itemWidth, ITEM_HEIGHT - 2, 7,
						ColorUtil.withAlpha(Theme.accent(), Math.round(select * 0x50)), ColorUtil.withAlpha(Theme.accent2(), Math.round(select * 0x10)));
				Render2D.roundedRect(graphics, itemX - 4, itemY + 6, 3, ITEM_HEIGHT - 14, 1.5f, ColorUtil.withAlpha(Theme.accent(), Math.round(select * 255)));
			}

			int iconColor = ColorUtil.lerp(Theme.TEXT_DIM, Theme.accent(), select);
			Icons.category(graphics, category, itemX + 8, itemY + 6, 10, iconColor);
			int textColor = ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(select, categoryHover[index] * 0.6f));
			VText.draw(graphics, category.getDisplayName(), itemX + 24, itemY + 7, textColor, selected);

			int enabled = (int) cards.get(category).stream().filter(card -> card.getModule().isEnabled() && card.getModule().isToggleable()).count();
			if (enabled > 0) {
				String count = String.valueOf(enabled);
				int pillWidth = Math.max(13, VText.width(count) + 8);
				Render2D.roundedRect(graphics, itemX + itemWidth - pillWidth - 5, itemY + 5, pillWidth, 12, 6, ColorUtil.lerp(Theme.FIELD, Theme.accent(), select * 0.85f));
				VText.drawCentered(graphics, count, itemX + itemWidth - pillWidth / 2f - 5, itemY + 7, Theme.TEXT, false);
			}
			itemY += ITEM_HEIGHT;
		}

		// Profile row.
		int profileY = windowY + windowHeight - 62;
		String name = minecraft != null ? minecraft.getUser().getName() : "Player";
		Render2D.horizontalGradient(graphics, windowX + 12, profileY, 20, 20, 10, Theme.accent(), Theme.accent2());
		VText.drawCentered(graphics, name.substring(0, 1).toUpperCase(Locale.ROOT), windowX + 22, profileY + 6, 0xFFFFFFFF, true);
		VText.draw(graphics, VText.trim(name, SIDEBAR_WIDTH - 50, true), windowX + 38, profileY + 1, Theme.TEXT, true);
		VText.drawScaled(graphics, "v" + VoltlizClient.version(), windowX + 38, profileY + 12, Theme.TEXT_DIM, false, 0.75f);

		// "Edit HUD" button pinned to the bottom of the sidebar.
		int buttonX = windowX + 10;
		int buttonY = windowY + windowHeight - 32;
		int buttonWidth = SIDEBAR_WIDTH - 20;
		boolean hovered = RenderUtil.isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, 22);
		hudButtonHover = animator.approach(hudButtonHover, hovered ? 1 : 0, 18);
		Render2D.shadow(graphics, buttonX, buttonY, buttonWidth, 22, 8, 6, Math.round(0x40 + hudButtonHover * 0x50), Theme.accent());
		int lighten = Math.round(hudButtonHover * 0x30);
		Render2D.horizontalGradient(graphics, buttonX, buttonY, buttonWidth, 22, 8,
				ColorUtil.lerp(Theme.accent(), 0xFFFFFFFF, lighten / 255f), ColorUtil.lerp(Theme.accent2(), 0xFFFFFFFF, lighten / 255f));
		int labelWidth = VText.width("Edit HUD", true) + 14;
		float labelX = buttonX + (buttonWidth - labelWidth) / 2f;
		Icons.layout(graphics, labelX, buttonY + 7, 8, 0xFFFFFFFF);
		VText.draw(graphics, "Edit HUD", labelX + 14, buttonY + 7, 0xFFFFFFFF, true);
	}

	private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY) {
		int contentX = windowX + SIDEBAR_WIDTH + 14;
		List<ModuleCard> list = visibleCards();
		String title = search.isEmpty() ? GuiState.selectedCategory.getDisplayName() : "Search";
		VText.drawScaled(graphics, title, contentX, windowY + 14, Theme.TEXT, true, 1.35f);

		long enabled = list.stream().filter(card -> card.getModule().isEnabled() && card.getModule().isToggleable()).count();
		String subtitle = list.size() + (list.size() == 1 ? " module" : " modules") + "  ·  " + enabled + " enabled";
		VText.draw(graphics, subtitle, contentX, windowY + 29, Theme.TEXT_DIM);

		// Search box: always focused, typing filters every category.
		int boxX = windowX + windowWidth - SEARCH_WIDTH - 14;
		int boxY = windowY + 14;
		boolean active = !search.isEmpty();
		Render2D.roundedRect(graphics, boxX, boxY, SEARCH_WIDTH, 20, 10, Theme.FIELD);
		if (active) {
			Render2D.roundedOutline(graphics, boxX, boxY, SEARCH_WIDTH, 20, 10, ColorUtil.withAlpha(Theme.accent(), 0xA0));
		}
		Icons.search(graphics, boxX + 9, boxY + 6, 9, active ? Theme.accent() : Theme.TEXT_DIM);
		if (active) {
			String shown = search;
			while (shown.length() > 1 && VText.width(shown) > SEARCH_WIDTH - 36) {
				shown = shown.substring(1);
			}
			VText.draw(graphics, shown, boxX + 24, boxY + 6, Theme.TEXT);
			if ((System.currentTimeMillis() / 500) % 2 == 0) {
				Render2D.rect(graphics, boxX + 25 + VText.width(shown), boxY + 5, 1, 10, Theme.accent());
			}
		} else {
			VText.draw(graphics, "Type to search...", boxX + 24, boxY + 6, ColorUtil.withAlpha(Theme.TEXT_DIM, 0xB0));
		}
	}

	private void renderCards(GuiGraphics graphics, int mouseX, int mouseY) {
		List<ModuleCard> list = visibleCards();
		int contentX = windowX + SIDEBAR_WIDTH + 14;
		int contentWidth = windowWidth - SIDEBAR_WIDTH - 28;
		int top = windowY + CONTENT_TOP;
		int bottom = windowY + windowHeight - 10;
		int columnWidth = (contentWidth - GAP) / 2;

		for (ModuleCard card : list) {
			card.tick(mouseX, mouseY);
		}

		// Masonry layout: each card goes into the shorter column.
		int[] columnHeights = new int[2];
		for (ModuleCard card : list) {
			int column = columnHeights[0] <= columnHeights[1] ? 0 : 1;
			card.x = contentX + column * (columnWidth + GAP);
			card.y = top + 4 + columnHeights[column] - Math.round(scroll);
			card.width = columnWidth;
			columnHeights[column] += card.getHeight() + GAP;
		}

		int contentHeight = Math.max(columnHeights[0], columnHeights[1]) + 4;
		int maxScroll = Math.max(0, contentHeight - (bottom - top));
		scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
		scroll = animator.approach(scroll, scrollTarget, 16);

		if (list.isEmpty()) {
			VText.drawCentered(graphics, "No modules match \"" + search + "\"", contentX + contentWidth / 2f, top + 40, Theme.TEXT_DIM, false);
		}

		graphics.enableScissor(windowX + SIDEBAR_WIDTH + 1, top, windowX + windowWidth - 1, bottom);
		for (ModuleCard card : list) {
			if (card.y + card.getHeight() >= top - 8 && card.y <= bottom + 8) {
				card.render(graphics, mouseX, mouseY);
			}
		}
		graphics.disableScissor();

		if (maxScroll > 0) {
			float visible = (bottom - top) / (float) contentHeight;
			float barHeight = Math.max(18, (bottom - top) * visible);
			float barY = top + (bottom - top - barHeight) * (scroll / maxScroll);
			Render2D.roundedRect(graphics, windowX + windowWidth - 6, barY, 3, barHeight, 1.5f, 0x38FFFFFF);
		}
	}

	private int[] toLocal(double mouseX, double mouseY) {
		float scale = 0.94f + 0.06f * openProgress;
		return new int[] {(int) Math.round(width / 2f + (mouseX - width / 2f) / scale), (int) Math.round(height / 2f + (mouseY - height / 2f) / scale)};
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int[] local = toLocal(event.x(), event.y());
		double mouseX = local[0];
		double mouseY = local[1];
		int button = event.button();

		if (button == 0 && RenderUtil.isInside(mouseX, mouseY, windowX + 10, windowY + windowHeight - 32, SIDEBAR_WIDTH - 20, 22)) {
			minecraft.setScreen(new HudEditorScreen(this));
			return true;
		}

		int itemY = windowY + 60;
		for (Category category : Category.values()) {
			if (RenderUtil.isInside(mouseX, mouseY, windowX + 8, itemY, SIDEBAR_WIDTH - 16, ITEM_HEIGHT - 2)) {
				if (GuiState.selectedCategory != category || !search.isEmpty()) {
					GuiState.selectedCategory = category;
					search = "";
					scroll = 0;
					scrollTarget = 0;
				}
				return true;
			}
			itemY += ITEM_HEIGHT;
		}

		if (mouseY >= windowY + CONTENT_TOP && mouseY <= windowY + windowHeight - 10) {
			for (ModuleCard card : visibleCards()) {
				if (card.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		int[] local = toLocal(event.x(), event.y());
		for (ModuleCard card : allCards) {
			card.mouseReleased(local[0], local[1], event.button());
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scrollTarget -= (float) (verticalAmount * 28);
		return true;
	}

	private boolean isListening() {
		return allCards.stream().anyMatch(ModuleCard::isListening);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		for (ModuleCard card : visibleCards()) {
			if (card.keyPressed(event.key())) {
				return true;
			}
		}
		if (!search.isEmpty() && event.key() == GLFW.GLFW_KEY_BACKSPACE) {
			search = search.substring(0, search.length() - 1);
			scrollTarget = 0;
			return true;
		}
		if (!search.isEmpty() && event.key() == GLFW.GLFW_KEY_ESCAPE) {
			search = "";
			return true;
		}
		if (VoltlizClient.get().isClickGuiKey(event.key())) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		int codepoint = event.codepoint();
		if (isListening() || !Character.isLetterOrDigit(codepoint) && codepoint != ' ') {
			return false;
		}
		if (search.length() < 32 && !(search.isEmpty() && codepoint == ' ')) {
			search += Character.toString(codepoint);
			scroll = 0;
			scrollTarget = 0;
		}
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		VoltlizClient.get().saveConfig();
	}
}
