package dev.voltliz.client.gui;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.gui.component.ModuleCard;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.module.client.ClickGuiModule;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** The Voltliz menu: a centered window with a category sidebar and a two column grid of module cards. */
public class ClickGuiScreen extends Screen {
	private static final int SIDEBAR_WIDTH = 104;
	private static final int GAP = 6;
	private static final int CONTENT_TOP = 40;

	private final Animator animator = new Animator();
	private final Map<Category, List<ModuleCard>> cards = new EnumMap<>(Category.class);
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

	public ClickGuiScreen() {
		super(Component.literal(VoltlizClient.NAME));
		for (Category category : Category.values()) {
			List<ModuleCard> list = new ArrayList<>();
			for (Module module : VoltlizClient.get().getModuleManager().getModules(category)) {
				list.add(new ModuleCard(module, animator));
			}
			cards.put(category, list);
		}
		categorySelect[GuiState.selectedCategory.ordinal()] = 1;
	}

	@Override
	protected void init() {
		windowWidth = Math.min(440, width - 24);
		windowHeight = Math.min(280, height - 24);
		windowX = (width - windowWidth) / 2;
		windowY = (height - windowHeight) / 2;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		ClickGuiModule gui = Theme.gui();
		if (gui == null || gui.dimBackground.get()) {
			int alpha = Math.round(openProgress * 0xA0);
			graphics.fillGradient(0, 0, width, height, ColorUtil.withAlpha(0x05060A, alpha * 3 / 4), ColorUtil.withAlpha(0x05060A, alpha));
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		animator.tick();
		openProgress = animator.approach(openProgress, 1, 10);

		// Slide up slightly while opening.
		int offset = Math.round((1 - openProgress) * 12);
		mouseY -= offset;
		graphics.pose().pushMatrix();
		graphics.pose().translate(0, offset);

		Render2D.shadow(graphics, windowX, windowY, windowWidth, windowHeight, 10, 10, 0x90);
		Render2D.roundedRect(graphics, windowX, windowY, windowWidth, windowHeight, 10, Theme.WINDOW);

		renderSidebar(graphics, mouseX, mouseY);
		renderContent(graphics, mouseX, mouseY);

		graphics.pose().popMatrix();
	}

	private void renderSidebar(GuiGraphics graphics, int mouseX, int mouseY) {
		Render2D.roundedRect(graphics, windowX, windowY, SIDEBAR_WIDTH, windowHeight, 10, Theme.SIDEBAR);
		Render2D.rect(graphics, windowX + SIDEBAR_WIDTH - 10, windowY, 10, windowHeight, Theme.SIDEBAR);
		Render2D.rect(graphics, windowX + SIDEBAR_WIDTH, windowY, 1, windowHeight, Theme.SEPARATOR);

		// Logo: a glowing accent dot and the client name.
		int logoX = windowX + 14;
		int logoY = windowY + 14;
		Render2D.circle(graphics, logoX + 4, logoY + 6, 6, ColorUtil.withAlpha(Theme.accent(), 0x40));
		Render2D.circle(graphics, logoX + 4, logoY + 6, 3.5f, Theme.accent());
		VText.drawScaled(graphics, "Voltliz", logoX + 14, logoY, Theme.TEXT, true, 1.35f);
		VText.draw(graphics, "CLIENT", logoX + 15, logoY + 14, Theme.accent());

		int itemY = windowY + 48;
		for (Category category : Category.values()) {
			int index = category.ordinal();
			boolean selected = GuiState.selectedCategory == category;
			boolean hovered = RenderUtil.isInside(mouseX, mouseY, windowX + 8, itemY, SIDEBAR_WIDTH - 16, 20);
			categoryHover[index] = animator.approach(categoryHover[index], hovered ? 1 : 0, 18);
			categorySelect[index] = animator.approach(categorySelect[index], selected ? 1 : 0, 14);

			float select = categorySelect[index];
			int background = ColorUtil.withAlpha(0xFFFFFF, Math.round(categoryHover[index] * 0x0C));
			Render2D.roundedRect(graphics, windowX + 8, itemY, SIDEBAR_WIDTH - 16, 20, 6, background);
			if (select > 0.01f) {
				Render2D.roundedRect(graphics, windowX + 8, itemY, SIDEBAR_WIDTH - 16, 20, 6, ColorUtil.withAlpha(Theme.accent(), Math.round(select * 0x30)));
				Render2D.roundedRect(graphics, windowX + 8, itemY + 5, 3, 10, 1.5f, ColorUtil.withAlpha(Theme.accent(), Math.round(select * 0xFF)));
			}
			int textColor = ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(select, categoryHover[index] * 0.6f));
			VText.draw(graphics, category.getDisplayName(), windowX + 18 + select * 2, itemY + 6, textColor, selected);

			String count = String.valueOf(cards.get(category).size());
			VText.draw(graphics, count, windowX + SIDEBAR_WIDTH - 16 - VText.width(count), itemY + 6, Theme.TEXT_DIM);
			itemY += 24;
		}

		// "Edit HUD" button pinned to the bottom of the sidebar.
		int buttonX = windowX + 10;
		int buttonY = windowY + windowHeight - 32;
		int buttonWidth = SIDEBAR_WIDTH - 20;
		boolean hovered = RenderUtil.isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, 20);
		hudButtonHover = animator.approach(hudButtonHover, hovered ? 1 : 0, 18);
		Render2D.shadow(graphics, buttonX, buttonY, buttonWidth, 20, 6, 4, Math.round(0x40 + hudButtonHover * 0x40));
		Render2D.roundedRect(graphics, buttonX, buttonY, buttonWidth, 20, 6, ColorUtil.lerp(Theme.accent(), 0xFFFFFFFF, hudButtonHover * 0.15f));
		VText.drawCentered(graphics, "Edit HUD", buttonX + buttonWidth / 2f, buttonY + 6, 0xFFFFFFFF, true);
	}

	private void renderContent(GuiGraphics graphics, int mouseX, int mouseY) {
		Category category = GuiState.selectedCategory;
		List<ModuleCard> list = cards.get(category);
		int contentX = windowX + SIDEBAR_WIDTH + 12;
		int contentWidth = windowWidth - SIDEBAR_WIDTH - 24;

		VText.drawScaled(graphics, category.getDisplayName(), contentX, windowY + 13, Theme.TEXT, true, 1.25f);
		String subtitle = list.size() + (list.size() == 1 ? " module" : " modules");
		VText.draw(graphics, subtitle, contentX + VText.width(category.getDisplayName(), true) * 1.25f + 8, windowY + 16, Theme.TEXT_DIM);

		String version = "v" + VoltlizClient.version();
		VText.draw(graphics, version, windowX + windowWidth - 12 - VText.width(version), windowY + 16, Theme.TEXT_DIM);

		int top = windowY + CONTENT_TOP;
		int bottom = windowY + windowHeight - 8;
		int columnWidth = (contentWidth - GAP) / 2;

		for (ModuleCard card : list) {
			card.tick(mouseX, mouseY);
		}

		// Masonry layout: each card goes into the shorter column.
		int[] columnHeights = new int[2];
		for (ModuleCard card : list) {
			int column = columnHeights[0] <= columnHeights[1] ? 0 : 1;
			card.x = contentX + column * (columnWidth + GAP);
			card.y = top + columnHeights[column] - Math.round(scroll);
			card.width = columnWidth;
			columnHeights[column] += card.getHeight() + GAP;
		}

		int maxScroll = Math.max(0, Math.max(columnHeights[0], columnHeights[1]) - GAP - (bottom - top));
		scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
		scroll = animator.approach(scroll, scrollTarget, 16);

		graphics.enableScissor(windowX + SIDEBAR_WIDTH + 1, top - 2, windowX + windowWidth, bottom);
		for (ModuleCard card : list) {
			if (card.y + card.getHeight() >= top && card.y <= bottom) {
				card.render(graphics, mouseX, mouseY);
			}
		}
		graphics.disableScissor();

		// Fade the content into the window at the top and bottom edges.
		graphics.fillGradient(windowX + SIDEBAR_WIDTH + 1, top - 2, windowX + windowWidth - 4, top + 4, Theme.WINDOW, ColorUtil.withAlpha(Theme.WINDOW, 0));
		graphics.fillGradient(windowX + SIDEBAR_WIDTH + 1, bottom - 8, windowX + windowWidth - 4, bottom, ColorUtil.withAlpha(Theme.WINDOW, 0), Theme.WINDOW);

		if (maxScroll > 0) {
			float visible = (bottom - top) / (float) (maxScroll + bottom - top);
			float barHeight = Math.max(16, (bottom - top) * visible);
			float barY = top + (bottom - top - barHeight) * (scroll / maxScroll);
			Render2D.roundedRect(graphics, windowX + windowWidth - 6, barY, 2, barHeight, 1, 0x40FFFFFF);
		}
	}

	private int openOffset() {
		return Math.round((1 - openProgress) * 12);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y() - openOffset();
		int button = event.button();

		if (RenderUtil.isInside(mouseX, mouseY, windowX + 10, windowY + windowHeight - 32, SIDEBAR_WIDTH - 20, 20) && button == 0) {
			minecraft.setScreen(new HudEditorScreen(this));
			return true;
		}

		int itemY = windowY + 48;
		for (Category category : Category.values()) {
			if (RenderUtil.isInside(mouseX, mouseY, windowX + 8, itemY, SIDEBAR_WIDTH - 16, 20)) {
				if (GuiState.selectedCategory != category) {
					GuiState.selectedCategory = category;
					scroll = 0;
					scrollTarget = 0;
				}
				return true;
			}
			itemY += 24;
		}

		int top = windowY + CONTENT_TOP;
		int bottom = windowY + windowHeight - 8;
		if (mouseY >= top && mouseY <= bottom) {
			for (ModuleCard card : cards.get(GuiState.selectedCategory)) {
				if (card.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		for (ModuleCard card : cards.get(GuiState.selectedCategory)) {
			card.mouseReleased(event.x(), event.y() - openOffset(), event.button());
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scrollTarget -= (float) (verticalAmount * 24);
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		for (ModuleCard card : cards.get(GuiState.selectedCategory)) {
			if (card.keyPressed(event.key())) {
				return true;
			}
		}
		if (VoltlizClient.get().isClickGuiKey(event.key())) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
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
