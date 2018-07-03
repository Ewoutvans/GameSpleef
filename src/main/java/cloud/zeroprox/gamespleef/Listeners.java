package cloud.zeroprox.gamespleef;

import cloud.zeroprox.gamespleef.game.IGame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.util.Optional;

public class Listeners {

    @Listener
    public void onInteractBlockEvent(InteractBlockEvent.Primary.MainHand event, @First Player player) {
        if (player != null) {
            Optional<IGame> gameOptional = GameSpleef.getGameManager().getPlayerGame(player);
            if (gameOptional.isPresent()) {
                IGame iGame = gameOptional.get();
                if (event.getTargetBlock().getLocation().isPresent()) {
                    if (iGame.isInsideFloor(event.getTargetBlock().getLocation().get())) {
                        iGame.addBreakBlock(player, event.getTargetBlock());
                    }
                }
            }
        }
    }

    @Listener
    public void onMoveEntityEvent(MoveEntityEvent event, @First Player player) {
        if (player != null) {
            Optional<IGame> gameOptional = GameSpleef.getGameManager().getPlayerGame(player);
            if (gameOptional.isPresent()) {
                IGame iGame = gameOptional.get();
                iGame.checkPlayerFall(player);
            }
        }
    }
}
