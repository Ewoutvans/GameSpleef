package cloud.zeroprox.gamespleef.commands.admin;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.game.IGame;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class KickCmd implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Player> player = args.getOne(Text.of("player"));
        if (!player.isPresent()) {
            throw new CommandException(Text.of("Player not found"));
        }
        Optional<IGame> gameOptional = GameSpleef.getGameManager().getPlayerGame(player.get());
        gameOptional.ifPresent(game -> game.leavePlayer(player.get(), true));
        src.sendMessage(Text.of("Player has been kicked and scores cleared."));
        return CommandResult.success();
    }
}
