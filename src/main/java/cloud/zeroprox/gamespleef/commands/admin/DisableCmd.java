package cloud.zeroprox.gamespleef.commands.admin;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.game.IGame;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class DisableCmd implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<String> gameName = args.getOne(Text.of("game"));
        if (!gameName.isPresent()) {
            throw new CommandException(Text.of("Game not found"));
        }
        Optional<IGame> gameOptional = GameSpleef.getGameManager().getGame(gameName.get());
        gameOptional.ifPresent(game -> game.toggleStatus());
        src.sendMessage(Text.of("Game status toggled."));
        return CommandResult.success();
    }
}
