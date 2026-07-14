package dev.voidpulsar.lc_claim_economy.client.gui;

import dev.voidpulsar.lc_claim_economy.client.TownMenuState;
import dev.voidpulsar.lc_claim_economy.network.TownMenuActionPayload;
import dev.voidpulsar.lc_claim_economy.network.TownMenuEntry;
import dev.voidpulsar.lc_claim_economy.town.TownBankTransaction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TownBankScreen extends Screen {
    private static final int PAD = 16;

    private EditBox amountBox;

    public TownBankScreen() {
        super(Component.translatable("gui.lc_claim_economy.town_bank.title"));
    }

    @Override
    protected void init() {
        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (currentTown == null) {
            addRenderableWidget(Button.builder(Component.literal("No town selected"), button -> {})
                    .bounds(PAD, 40, 200, 20)
                    .build());
            return;
        }

        amountBox = new EditBox(font, PAD, 44, Math.max(160, width - PAD * 2 - 180), 20, Component.empty());
        amountBox.setMaxLength(12);
        amountBox.setHint(Component.literal("Copper amount"));
        addRenderableWidget(amountBox);

            if (currentTown.canDepositTreasury()) {
                addRenderableWidget(Button.builder(Component.literal("Deposit from player"), button -> send(TownMenuActionPayload.Action.DEPOSIT_TREASURY))
                    .bounds(width - PAD - 160, 44, 160, 20)
                    .build());
            }
            if (currentTown.canWithdrawTreasury()) {
                addRenderableWidget(Button.builder(Component.literal("Withdraw to player"), button -> send(TownMenuActionPayload.Action.WITHDRAW_TREASURY))
                    .bounds(width - PAD - 160, 68, 160, 20)
                    .build());
            }

        addRenderableWidget(Button.builder(Component.literal("Back"), button -> minecraft.setScreen(new TownMenuScreen()))
                .bounds(PAD, height - 32, 80, 20)
                .build());
    }

    private void send(TownMenuActionPayload.Action action) {
        if (amountBox == null || TownMenuState.playerTownId() == null) {
            return;
        }
        String amount = amountBox.getValue().trim();
        if (amount.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(new TownMenuActionPayload(action, TownMenuState.playerTownId(), amount, "", ""));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (currentTown == null) {
            graphics.drawString(font, Component.literal("Open the town menu and join or create a town first."), PAD, 22, 0xA0A0A0);
            super.render(graphics, mouseX, mouseY, delta);
            return;
        }

        graphics.drawString(font, Component.literal("Town: " + currentTown.name()), PAD, 24, 0xFFFFFF);
        graphics.drawString(font, Component.literal("Treasury balance: " + currentTown.treasuryCopper() + " copper"), PAD, 36, 0xA0A0A0);
        graphics.drawString(font, Component.literal("Use the amount box to move copper between your bank and the town treasury."), PAD, 88, 0xA0A0A0);

        int ledgerY = 108;
        graphics.drawString(font, Component.literal("Recent transactions"), PAD, ledgerY, 0xFFFFFF);
        ledgerY += 12;
        if (currentTown.bankLedger().isEmpty()) {
            graphics.drawString(font, Component.literal("No ledger entries yet."), PAD, ledgerY, 0xA0A0A0);
        } else {
            for (TownBankTransaction transaction : currentTown.bankLedger()) {
                String sign = transaction.amountCopper() >= 0 ? "+" : "";
                graphics.drawString(font, Component.literal(transaction.action() + " by " + transaction.actorName() + " " + sign + transaction.amountCopper() + " copper"), PAD, ledgerY, 0xA0A0A0);
                ledgerY += 12;
            }
        }
        super.render(graphics, mouseX, mouseY, delta);
    }
}