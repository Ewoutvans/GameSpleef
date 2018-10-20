package cloud.zeroprox.gamespleef.commands;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.game.IGame;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class JoinCmd implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(GameSpleef.mM().NEED_TO_BE_PLAYER.apply().build());
        }
        Player player = (Player) src;
        if (GameSpleef.getGameManager().getPlayerGame(player).isPresent()) {
             throw new CommandException(GameSpleef.mM().YOU_ALREADY_PLAYING.apply().build());
        }
        String gameName = args.<String>getOne(Text.of("game")).orElse(GameSpleef.getGameManager().getDefaultName());
        Optional<IGame> game = GameSpleef.getGameManager().getGame(gameName);
        if (!game.isPresent()) {
            throw new CommandException(GameSpleef.mM().GAME_NAME_NOT_FOUND.apply(ImmutableMap.of("gamename", gameName)).build());
        }
        game.get().addPlayer(player);
        return CommandResult.success();
    }
}
