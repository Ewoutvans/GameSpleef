package cloud.zeroprox.gamespleef.commands;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.game.IGame;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class LeaveCmd implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "You need to be a player to join a game"));
        }
        Player player = (Player) src;

        Optional<IGame> game = GameSpleef.getGameManager().getPlayerGame(player);
        if (!game.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "You are not in a game"));
        }

        game.get().leavePlayer(player, args.hasAny("f"));

        return CommandResult.success();
    }
}
