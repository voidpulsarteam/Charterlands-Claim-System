package dev.voidpulsar.lc_claim_economy.client.gui;

import dev.voidpulsar.lc_claim_economy.client.TownMenuState;
import dev.voidpulsar.lc_claim_economy.network.TownMenuActionPayload;
import dev.voidpulsar.lc_claim_economy.network.TownMenuEntry;
import dev.voidpulsar.lc_claim_economy.network.TownResidentEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TownMenuScreen extends Screen {
    private static final int PAD = 16;
    private static final int ROW_HEIGHT = 24;

    private EditBox townNameBox;
    private EditBox residentNameBox;

    public TownMenuScreen() {
        super(Component.translatable("gui.lc_claim_economy.town_menu.title"));
    }

    @Override
    protected void init() {
        townNameBox = new EditBox(font, PAD, 30, Math.max(180, width - PAD * 2 - 90), 20, Component.empty());
        townNameBox.setMaxLength(32);
        townNameBox.setHint(Component.translatable("gui.lc_claim_economy.town_menu.create_hint"));
        addRenderableWidget(townNameBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.create"), button -> {
            String name = townNameBox.getValue().trim();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new TownMenuActionPayload(TownMenuActionPayload.Action.CREATE_TOWN, null, name, "", ""));
            }
        }).bounds(width - PAD - 80, 30, 80, 20).build());

        if (TownMenuState.playerTownId() != null) {
            residentNameBox = new EditBox(font, PAD, 88, Math.max(180, width - PAD * 2 - 160), 20, Component.empty());
            residentNameBox.setMaxLength(48);
            residentNameBox.setHint(Component.translatable("gui.lc_claim_economy.town_menu.invite_hint"));
            addRenderableWidget(residentNameBox);

            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.invite"), button -> {
                String value = residentNameBox.getValue().trim();
                if (!value.isEmpty()) {
                    PacketDistributor.sendToServer(new TownMenuActionPayload(
                            TownMenuActionPayload.Action.INVITE_PLAYER,
                            TownMenuState.playerTownId(),
                            "",
                            value,
                            ""
                    ));
                }
                }).bounds(width - PAD - 140, 88, 140, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Open bank"), button -> minecraft.setScreen(new TownBankScreen()))
                    .bounds(width - PAD - 140, 112, 140, 20)
                    .build());
        }

            int y = TownMenuState.playerTownId() != null ? 148 : 64;
        for (TownMenuEntry town : TownMenuState.towns()) {
            addTownRow(town, y);
            y += ROW_HEIGHT + 10;
        }

        if (TownMenuState.playerTownId() != null) {
            TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
            if (currentTown != null) {
                addRenderableWidget(Button.builder(Component.literal("Treasury: " + currentTown.treasuryCopper() + " copper"), button -> {})
                    .bounds(PAD, 136, Math.max(220, width - PAD * 2), 20)
                        .build());
            }

                int residentStartY = 160;
            addRenderableWidget(Button.builder(Component.literal("Residents"), button -> {})
                    .bounds(PAD, residentStartY - 16, 140, 20)
                    .build());

            int rowY = residentStartY;
            for (TownResidentEntry resident : TownMenuState.residents()) {
                addResidentRow(resident, rowY);
                rowY += 12;
            }
        }
    }

    private void addTownRow(TownMenuEntry town, int y) {
        addRenderableWidget(Button.builder(
                Component.literal(town.name() + " • " + town.mayorName()),
                button -> {}
        ).bounds(PAD, y, Math.max(220, width - PAD * 2), 20).build());

        boolean currentTownOwnsPlot = town.townId().equals(TownMenuState.currentPlotTownId());
        boolean wilderness = TownMenuState.currentPlotTownId() == null;

        addRenderableWidget(Button.builder(
                Component.literal(town.publicAccess() ? "Public" : "Private"),
                button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                        TownMenuActionPayload.Action.TOGGLE_PUBLIC_ACCESS,
                        town.townId(),
                        "",
                        "",
                        ""
                ))
        ).bounds(width - PAD - 246, y + 24, 70, 20).build());

        String plotActionLabel = currentTownOwnsPlot ? "Unclaim" : (wilderness ? "Claim" : "Taken by " + TownMenuState.currentPlotTownName());
        addRenderableWidget(Button.builder(
                Component.literal(plotActionLabel),
                button -> {
                    if (currentTownOwnsPlot) {
                        PacketDistributor.sendToServer(new TownMenuActionPayload(
                                TownMenuActionPayload.Action.UNCLAIM_CURRENT_PLOT,
                                town.townId(),
                                "",
                                "",
                                ""
                        ));
                    } else if (wilderness) {
                        PacketDistributor.sendToServer(new TownMenuActionPayload(
                                TownMenuActionPayload.Action.CLAIM_CURRENT_PLOT,
                                town.townId(),
                                "",
                                "",
                                ""
                        ));
                    }
                }
        ).bounds(width - PAD - 168, y + 24, 136, 20).build());

        if (town.invited() && !town.playerTown()) {
            addRenderableWidget(Button.builder(Component.literal("Join"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.ACCEPT_INVITE,
                    town.townId(),
                    "",
                    "",
                    ""
            ))).bounds(width - PAD - 82, y, 38, 20).build());
            addRenderableWidget(Button.builder(Component.literal("No"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.DECLINE_INVITE,
                    town.townId(),
                    "",
                    "",
                    ""
            ))).bounds(width - PAD - 40, y, 24, 20).build());
        }
    }

    private void addResidentRow(TownResidentEntry resident, int y) {
        addRenderableWidget(Button.builder(
                Component.literal(resident.playerName() + " • " + resident.rank()),
                button -> {}
        ).bounds(PAD, y, Math.max(220, width - PAD * 2), 20).build());

        if (!"MAYOR".equals(resident.rank())) {
            addRenderableWidget(Button.builder(
                    Component.literal(resident.trustedOnCurrentPlot() ? "Untrust" : "Trust"),
                    button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                            resident.trustedOnCurrentPlot()
                                    ? TownMenuActionPayload.Action.UNTRUST_CURRENT_PLOT
                                    : TownMenuActionPayload.Action.TRUST_CURRENT_PLOT,
                            TownMenuState.playerTownId(),
                            "",
                            resident.playerId().toString(),
                            ""
                    ))
            ).bounds(width - PAD - 214, y, 44, 20).build());

            addRenderableWidget(Button.builder(
                Component.literal(resident.deniedOnCurrentPlot() ? "Undeny" : "Deny"),
                button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    resident.deniedOnCurrentPlot()
                        ? TownMenuActionPayload.Action.UNDENY_CURRENT_PLOT
                        : TownMenuActionPayload.Action.DENY_CURRENT_PLOT,
                    TownMenuState.playerTownId(),
                    "",
                    resident.playerId().toString(),
                    ""
                ))
            ).bounds(width - PAD - 166, y, 44, 20).build());

            addRenderableWidget(Button.builder(Component.literal("+"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.SET_RESIDENT_RANK,
                    TownMenuState.playerTownId(),
                    "",
                resident.playerId().toString(),
                "ASSISTANT"
            ))).bounds(width - PAD - 118, y, 20, 20).build());

            addRenderableWidget(Button.builder(Component.literal("-"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.SET_RESIDENT_RANK,
                    TownMenuState.playerTownId(),
                    "",
                resident.playerId().toString(),
                "RESIDENT"
            ))).bounds(width - PAD - 94, y, 20, 20).build());

            addRenderableWidget(Button.builder(Component.literal("X"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.REMOVE_RESIDENT,
                    TownMenuState.playerTownId(),
                    "",
                    resident.playerId().toString(),
                    ""
            ))).bounds(width - PAD - 70, y, 20, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        graphics.drawString(font, Component.literal("Current chunk: " + TownMenuState.currentChunkKey()), PAD, 52, 0xA0A0A0);
        graphics.drawString(font, Component.literal("Current plot: " + TownMenuState.currentPlotTownName()), PAD, 64, 0xA0A0A0);
        if (TownMenuState.playerTownId() == null && TownMenuState.canCreateTown()) {
            graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_menu.new_player_prompt"), PAD, 76, 0xA0A0A0);
        }
        super.render(graphics, mouseX, mouseY, delta);
    }
}
