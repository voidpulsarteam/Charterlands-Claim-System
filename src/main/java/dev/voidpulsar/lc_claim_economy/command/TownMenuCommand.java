package dev.voidpulsar.lc_claim_economy.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.network.OpenTownMenuPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TownMenuCommand {
    private TownMenuCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(LcClaimEconomy.COMMAND_ROOT)
                .then(Commands.literal("town")
                        .executes(context -> open(context.getSource()))));
    }

    private static int open(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can open the town menu."));
            return 0;
        }
        PacketDistributor.sendToPlayer(player, new OpenTownMenuPayload());
        return 1;
    }
}