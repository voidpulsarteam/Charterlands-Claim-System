package dev.voidpulsar.lc_claim_economy.client.gui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.PanelScrollBar;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.voidpulsar.lc_claim_economy.client.ClaimMapState;
import dev.voidpulsar.lc_claim_economy.client.ClientClaimPrices;
import dev.voidpulsar.lc_claim_economy.network.ClaimChunkFromMapPayload;
import dev.voidpulsar.lc_claim_economy.network.ClaimMapEntry;
import dev.voidpulsar.lc_claim_economy.network.RequestClaimMapPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ClaimMapScreen extends BaseScreen {
    private static final int HEADER_HEIGHT = 22;
    private static final int HEADER_BUTTON_SIZE = 16;
    private static final int CONTENT_PAD = 8;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int BOARD_RADIUS = 6;
    private static final int CELL_SIZE = 14;
    private static final int BOARD_SIZE = BOARD_RADIUS * 2 + 1;

    private SimpleButton backButton;
    private SimpleButton refreshButton;
    private BoardPanel boardPanel;
    private PanelScrollBar scrollBar;

    public ClaimMapScreen() {
    }

    public static void refreshIfOpen() {
        ClaimMapScreen screen = ClientUtils.getCurrentGuiAs(ClaimMapScreen.class);
        if (screen != null) {
            screen.rebuild();
        }
    }

    @Override
    public boolean onInit() {
        setWidth(Math.min(getScreen().getGuiScaledWidth() - 20, 440));
        setHeight(Math.min(getScreen().getGuiScaledHeight() - 20, 320));
        PacketDistributor.sendToServer(new RequestClaimMapPayload());
        return true;
    }

    @Override
    public void addWidgets() {
        backButton = new SimpleButton(this, Component.translatable("gui.back"), Icons.BACK, (button, mouseButton) -> openGuiParent());
        add(backButton);

        refreshButton = new SimpleButton(this, Component.translatable("gui.lc_claim_economy.claim_map.refresh"), Icons.REFRESH, (button, mouseButton) ->
                PacketDistributor.sendToServer(new RequestClaimMapPayload()));
        add(refreshButton);

        boardPanel = new BoardPanel(this);
        boardPanel.setOnlyRenderWidgetsInside(true);
        boardPanel.setOnlyInteractWithWidgetsInside(true);
        add(boardPanel);

        scrollBar = new PanelScrollBar(this, boardPanel);
        add(scrollBar);
    }

    @Override
    public void alignWidgets() {
        backButton.setPosAndSize(5, 5, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE);
        refreshButton.setPosAndSize(width - 5 - HEADER_BUTTON_SIZE, 5, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE);

        int contentTop = HEADER_HEIGHT + 6;
        int contentHeight = height - contentTop - CONTENT_PAD;
        int contentWidth = width - CONTENT_PAD * 2 - SCROLLBAR_WIDTH - 2;

        boardPanel.setPosAndSize(CONTENT_PAD, contentTop, contentWidth, contentHeight);
        boardPanel.alignWidgets();
        scrollBar.setPosAndSize(CONTENT_PAD + contentWidth + 2, contentTop, SCROLLBAR_WIDTH, contentHeight);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawBackground(graphics, theme, x, y, w, h);
        NordColors.POLAR_NIGHT_0.draw(graphics, x + 4, y + HEADER_HEIGHT + 2, w - 8, h - HEADER_HEIGHT - 6);
        NordColors.POLAR_NIGHT_2.draw(graphics, x + 4, y + HEADER_HEIGHT + 2, w - 8, 1);
    }

    @Override
    public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawForeground(graphics, theme, x, y, w, h);
        theme.drawString(
                graphics,
                Component.translatable("gui.lc_claim_economy.claim_map.title"),
                x + w / 2,
                y + 7,
                NordColors.SNOW_STORM_0,
                Theme.CENTERED
        );
    }

    private void openGuiParent() {
        closeGui();
    }

    private void rebuild() {
        if (boardPanel != null) {
            boardPanel.refreshWidgets();
            boardPanel.alignWidgets();
        }
        alignWidgets();
    }

    private final class BoardPanel extends Panel {
        private final List<ClaimCellButton> cells = new ArrayList<>();

        BoardPanel(BaseScreen screen) {
            super(screen);
        }

        @Override
        public void addWidgets() {
            widgets.clear();
            cells.clear();

            for (int dz = -BOARD_RADIUS; dz <= BOARD_RADIUS; dz++) {
                for (int dx = -BOARD_RADIUS; dx <= BOARD_RADIUS; dx++) {
                    ClaimCellButton cell = new ClaimCellButton(this, ClaimMapState.centerChunkX() + dx, ClaimMapState.centerChunkZ() + dz);
                    cells.add(cell);
                    add(cell);
                }
            }
        }

        @Override
        public void alignWidgets() {
            int boardWidth = BOARD_SIZE * CELL_SIZE;
            int boardHeight = BOARD_SIZE * CELL_SIZE;
            int startX = 12;
            int startY = 18;

            for (ClaimCellButton cell : cells) {
                int dx = cell.chunkX - ClaimMapState.centerChunkX();
                int dz = cell.chunkZ - ClaimMapState.centerChunkZ();
                cell.setPosAndSize(startX + (dx + BOARD_RADIUS) * CELL_SIZE, startY + (dz + BOARD_RADIUS) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            if (!ClientClaimPrices.isSynced()) {
                add(new MessageRow(this, Component.translatable("gui.lc_claim_economy.claim_map.loading").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            }
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            NordColors.POLAR_NIGHT_1.draw(graphics, x, y, w, h);
            NordColors.POLAR_NIGHT_3.draw(graphics, x, y, w, 1);

            int boardWidth = BOARD_SIZE * CELL_SIZE;
            int boardHeight = BOARD_SIZE * CELL_SIZE;
            int boardX = x + 12;
            int boardY = y + 18;

            graphics.drawString(theme.getFont(), Component.translatable("gui.lc_claim_economy.claim_map.dimension", ClaimMapState.dimensionId()), x + 12, y + 4, 0xD0D0D0, false);
            drawLegend(graphics, theme, x + boardWidth + 32, y + 18);
            graphics.drawCenteredString(theme.getFont(), Component.translatable("gui.lc_claim_economy.claim_map.hover_hint"), x + w / 2, y + h - 22, 0xA0A0A0);

            if (!ClientClaimPrices.isSynced()) {
                graphics.drawCenteredString(theme.getFont(), Component.translatable("gui.lc_claim_economy.claim_map.loading"), x + w / 2, y + h - 34, 0xA0A0A0);
            } else {
                graphics.drawCenteredString(theme.getFont(), Component.translatable("gui.lc_claim_economy.claim_map.cost", ClientClaimPrices.currentEffectiveClaimPrice()), x + w / 2, y + h - 34, 0xA0A0A0);
            }
        }

        private void drawLegend(GuiGraphics graphics, Theme theme, int x, int y) {
            theme.drawString(graphics, Component.translatable("gui.lc_claim_economy.claim_map.legend"), x, y, NordColors.SNOW_STORM_0, 0);
            graphics.fill(x, y + 14, x + 6, y + 20, 0xFF40658A);
            theme.drawString(graphics, Component.translatable("gui.lc_claim_economy.claim_map.legend_claimed_build"), x + 10, y + 13, NordColors.SNOW_STORM_1, 0);
            graphics.fill(x, y + 28, x + 6, y + 34, 0xFF3E6D50);
            theme.drawString(graphics, Component.translatable("gui.lc_claim_economy.claim_map.legend_claimed_land"), x + 10, y + 27, NordColors.SNOW_STORM_1, 0);
            graphics.fill(x, y + 42, x + 6, y + 48, 0xFF2B2F36);
            theme.drawString(graphics, Component.translatable("gui.lc_claim_economy.claim_map.legend_wilderness"), x + 10, y + 41, NordColors.SNOW_STORM_1, 0);
        }
    }

    private final class ClaimCellButton extends Button {
        private final int chunkX;
        private final int chunkZ;

        ClaimCellButton(Panel panel, int chunkX, int chunkZ) {
            super(panel, Component.empty(), Color4I.empty());
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            if (!mouseButton.isLeft()) {
                return;
            }

            Optional<ClaimMapEntry> cellOpt = ClaimMapState.entryAt(chunkX, chunkZ);
            if (cellOpt.isPresent() && cellOpt.get().claimed()) {
                return;
            }

            String chunkKey = ClaimMapState.dimensionId() + "#" + chunkX + "#" + chunkZ;
            PacketDistributor.sendToServer(new ClaimChunkFromMapPayload(chunkKey));
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            Optional<ClaimMapEntry> cellOpt = ClaimMapState.entryAt(chunkX, chunkZ);
            if (cellOpt.isEmpty()) {
                list.add(Component.literal("Chunk " + chunkX + ", " + chunkZ).withStyle(ChatFormatting.GRAY));
                return;
            }

            ClaimMapEntry cell = cellOpt.get();
            if (cell.claimed()) {
                list.add(Component.literal("Chunk " + chunkX + ", " + chunkZ).withStyle(ChatFormatting.AQUA));
                list.add(Component.literal((cell.land() ? "Land" : "Build") + " claim by " + (cell.ownerName().isBlank() ? "unknown" : cell.ownerName())).withStyle(ChatFormatting.GREEN));
                list.add(Component.translatable("gui.lc_claim_economy.claim_map.claimed_hint").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                list.add(Component.literal("Chunk " + chunkX + ", " + chunkZ).withStyle(ChatFormatting.GREEN));
                list.add(Component.translatable("gui.lc_claim_economy.claim_map.claim_hint").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            Optional<ClaimMapEntry> cellOpt = ClaimMapState.entryAt(chunkX, chunkZ);
            ClaimMapEntry cell = cellOpt.orElse(null);

            int color = 0xFF2B2F36;
            String label = "";
            if (cell != null && cell.claimed()) {
                color = cell.land() ? 0xFF3E6D50 : 0xFF40658A;
                label = cell.ownerName().isBlank() ? "?" : cell.ownerName().substring(0, Math.min(2, cell.ownerName().length())).toUpperCase();
            }

            graphics.fill(x, y, x + w, y + h, color);
            graphics.fill(x, y, x + w, y + 1, 0xFF111827);
            graphics.fill(x, y + h - 1, x + w, y + h, 0xFF111827);
            graphics.fill(x, y, x + 1, y + h, 0xFF111827);
            graphics.fill(x + w - 1, y, x + w, y + h, 0xFF111827);

            if (chunkX == ClaimMapState.centerChunkX() && chunkZ == ClaimMapState.centerChunkZ()) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x3300FFD5);
            }

            if (!label.isEmpty()) {
                theme.drawString(graphics, Component.literal(label), x + w / 2, y + 4, NordColors.SNOW_STORM_0, Theme.CENTERED);
            }
        }
    }

    private static final class MessageRow extends Button {
        MessageRow(Panel panel, Component title) {
            super(panel, title, Color4I.empty());
        }

        @Override
        public void onClicked(MouseButton button) {
        }
    }
}