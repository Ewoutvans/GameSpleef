package cloud.zeroprox.gamespleef.commands;

import cloud.zeroprox.gamespleef.GameSpleef;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class HelpCmd implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        PaginationList.builder()
                .title(GameSpleef.mM().SPLEEF_HELP_TITLE.apply().build())
                .padding(Text.of(TextColors.GOLD, "="))
                .contents(
                        Text.builder("/spleef join ").color(TextColors.GREEN).append(Text.of(TextColors.WHITE, "[area]")).onClick(TextActions.suggestCommand("/spleef join ")).build(),
                        Text.builder("/spleef leave").color(TextColors.GREEN).onClick(TextActions.runCommand("/spleef leave")).build(),
                        Text.builder("/spleef list").color(TextColors.GREEN).onClick(TextActions.runCommand("/spleef list")).build(),
                        Text.builder("/spleef admin").color(src.hasPermission("gamespleef.admin") ? TextColors.GREEN : TextColors.RED).onClick(TextActions.runCommand("/spleef admin")).build()
                )
                .build()
                .sendTo(src);
        return CommandResult.success();
    }
}
