package cloud.zeroprox.gamespleef.game;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.stats.PlayerStats;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IGame {

    GameSpleef.Mode getMode();

    String getName();

    Collection<UUID> getAllPlayers();

    Transform<World> getSpawn();

    Transform<World> getLobby();

    boolean isInsideArea(Location<World> location);

    boolean isInsideFloor(Location<World> location);

    Optional<PlayerStats> getPlayerStats(Player player);

    Optional<PlayerStats> getPlayerStats(UUID uuid);

    void showStats(Player player);

    void addPlayer(Player player);

    void leavePlayer(Player player, boolean resetStats);

    boolean containsPlayer(Player player);

    boolean addBreakBlock(Player player, BlockSnapshot targetBlock);

    void toggleStatus();

    void startGame();

    void checkPlayerFall(Player player);

    void killPlayer(Player player);

    void resetGame();

    void winPlayer(Player player);

    boolean checkPlayerMoved(Optional<Player> player);
}
