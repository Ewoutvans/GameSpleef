package cloud.zeroprox.gamespleef.game;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.stats.PlayerStats;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GameClassic implements IGame {

    private String name;
    private GameSpleef.Mode mode;
    private UUID world;
    private AABB area;
    private List<AABB> floors;
    private Transform<World> spawn, lobby;
    private int limit, lowestY;
    private HashMap<BlockSnapshot, UUID> brokenBlocks;
    private Task task;
    Map<UUID, PlayerStats> activePlayers = new HashMap<>();
    Map<UUID, PlayerStats> inactivePlayers = new HashMap<>();

    public GameClassic(String name, AABB area, List<AABB> floors, Transform<World> spawn, Transform<World> lobby, int limit) {
        this.name = name;
        this.area = area;
        this.floors = floors;
        this.spawn = spawn;
        this.lobby = lobby;
        this.limit = limit;
        this.world = lobby.getExtent().getUniqueId();
        this.lowestY = 255;
        for (AABB floor : floors) {
            if (floor.getMin().getFloorY() <= lowestY) {
                lowestY = floor.getMin().getFloorY();
            }
        }
        this.brokenBlocks = new HashMap<>();
        this.mode = GameSpleef.Mode.READY;
    }

    @Override
    public GameSpleef.Mode getMode() {
        return this.mode;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<UUID> getAllPlayers() {
        return this.activePlayers.keySet();
    }

    @Override
    public Transform<World> getSpawn() {
        return this.spawn;
    }

    @Override
    public Transform<World> getLobby() {
        return this.lobby;
    }

    @Override
    public boolean isInsideArea(Location<World> location) {
        return this.area.contains(location.getX(), location.getY(), location.getZ()) && world.equals(location.getExtent().getUniqueId());
    }

    @Override
    public boolean isInsideFloor(Location<World> location) {
        return world.equals(location.getExtent().getUniqueId()) && this.floors.stream().anyMatch(floor -> floor.contains(location.getX(), location.getY(), location.getZ()));
    }

    @Override
    public Optional<PlayerStats> getPlayerStats(Player player) {
        return this.getPlayerStats(player.getUniqueId());
    }

    @Override
    public Optional<PlayerStats> getPlayerStats(UUID uuid) {
        if (this.activePlayers.containsKey(uuid)) {
            return Optional.of(this.activePlayers.get(uuid));
        }
        if (this.inactivePlayers.containsKey(uuid)) {
            return Optional.of(this.inactivePlayers.get(uuid));
        }
        return Optional.empty();
    }

    @Override
    public void showStats(Player player) {

    }

    @Override
    public void addPlayer(Player player) {
        if (this.activePlayers.size() >= this.limit) {
            player.sendMessage(Text.of(TextColors.RED, "You can't join, the game is at a limit."));
            return;
        }
        if (this.mode == GameSpleef.Mode.DISABLED) {
            player.sendMessage(Text.of(TextColors.RED, "You can't join the game is not ready."));
            return;
        }
        if (this.mode == GameSpleef.Mode.PLAYING) {
            player.sendMessage(Text.of(TextColors.RED, "You can't join the game is already started."));
            return;
        }
        player.health().set(20D);
        player.maxHealth().set(20D);
        player.foodLevel().set(20);
        player.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
        player.offer(Keys.CAN_FLY, false);
        player.offer(Keys.FIRE_TICKS, 0);
        player.offer(Keys.POTION_EFFECTS, Arrays.asList(
                PotionEffect.builder().amplifier(5).duration(20 * 60 * 60 * 60).particles(false).potionType(PotionEffectTypes.RESISTANCE).build(),
                PotionEffect.builder().amplifier(1).duration(20 * 60 * 60 * 60).particles(false).potionType(PotionEffectTypes.WATER_BREATHING).build(),
                PotionEffect.builder().amplifier(1).duration(20 * 60 * 60 * 60).particles(false).potionType(PotionEffectTypes.NIGHT_VISION).build()));
        player.sendMessage(Text.of(TextColors.GREEN, "You have joined the game"));
        ((PlayerInventory) player.getInventory()).getHotbar().setSelectedSlotIndex(0);

        this.activePlayers.put(player.getUniqueId(), new PlayerStats(player.getUniqueId()));

        player.setTransform(this.getSpawn());

        if (this.activePlayers.size() >= 1) {
            if (this.mode == GameSpleef.Mode.READY) {
                this.mode = GameSpleef.Mode.COUNTDOWN;
                task = Task.builder().execute(new StartingTimerTask()).interval(1, TimeUnit.SECONDS).name("Game timer").submit(GameSpleef.getInstance());
            }
        }
    }

    @Override
    public void leavePlayer(Player player, boolean resetStats) {
        if (this.activePlayers.size() <= 1) {
            resetGame();
        }
        this.activePlayers.remove(player.getUniqueId());
        player.getInventory().clear();
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
        player.offer(Keys.POTION_EFFECTS, new ArrayList<>());
        player.setScoreboard(null);
        player.setTransform(this.getLobby());
    }

    @Override
    public boolean containsPlayer(Player player) {
        return this.activePlayers.containsKey(player.getUniqueId());
    }

    @Override
    public void addBreakBlock(Player player, BlockSnapshot targetBlock) {
        if (this.mode != GameSpleef.Mode.PLAYING) return;
        if (targetBlock.getState().equals(BlockTypes.AIR.getDefaultState())) return;
        PlayerStats playerStats = this.getPlayerStats(player).get();
        playerStats.addBlocksBroken(1);

        targetBlock.getLocation().get().getExtent().setBlockType(targetBlock.getLocation().get().getBlockPosition(), BlockTypes.AIR);
        brokenBlocks.put(targetBlock, player.getUniqueId());
    }

    @Override
    public void toggleStatus() {
        this.mode = this.mode.equals(GameSpleef.Mode.READY) ? GameSpleef.Mode.DISABLED : GameSpleef.Mode.READY;
    }

    @Override
    public void startGame() {
        this.mode = GameSpleef.Mode.PLAYING;
        this.inactivePlayers = new HashMap<>();
        for (UUID uuid : this.activePlayers.keySet()) {
            Sponge.getServer().getPlayer(uuid).get().offer(Keys.GAME_MODE, GameModes.SURVIVAL);
        }
    }

    @Override
    public void checkPlayerFall(Player player) {
        if (player.getLocation().getBlockY() > this.lowestY) {
            return;
        }
        if (this.mode == GameSpleef.Mode.PLAYING) {
            this.killPlayer(player);
        } else {
            this.activePlayers.remove(player.getUniqueId());
            player.setTransform(this.getLobby());
            this.activePlayers.put(player.getUniqueId(), new PlayerStats(player.getUniqueId()));
        }
    }

    @Override
    public void killPlayer(Player player) {
        for (BlockSnapshot bs : this.brokenBlocks.keySet()) {
            if (bs.getLocation().isPresent()) {
                if (bs.getLocation().get().getBlockPosition().getX() == player.getLocation().getBlockPosition().getX()
                        && bs.getLocation().get().getBlockPosition().getZ() == player.getLocation().getBlockPosition().getZ()
                        && bs.getLocation().get().getBlockPosition().getY() == this.lowestY) {
                    Player killer = Sponge.getServer().getPlayer(this.brokenBlocks.get(bs)).get();
                    getPlayerStats(killer).get().addKnockout(player);
                    player.sendMessage(
                            Text.of(TextColors.GRAY, "[", TextColors.RED, "SPLEEF", TextColors.GRAY, "] You have been knocked out by ", TextColors.RED, killer.getName(), TextColors.GRAY, ".")
                    );
                }
            }
        }
        this.inactivePlayers.put(player.getUniqueId(), this.activePlayers.get(player.getUniqueId()));
        this.activePlayers.remove(player.getUniqueId());
        player.getInventory().clear();
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
        player.offer(Keys.POTION_EFFECTS, new ArrayList<>());
        player.offer(Keys.FOOD_LEVEL, 20);
        player.offer(Keys.HEALTH, player.get(Keys.MAX_HEALTH).get());
        player.setScoreboard(null);
        player.setTransform(this.getLobby());
        if (this.activePlayers.size() <= 1) {
            Sponge.getServer().getBroadcastChannel().send(
                    Text.of(TextColors.GRAY, "[", TextColors.GOLD, "SPLEEF", TextColors.GRAY, "] ", TextColors.GOLD, Sponge.getServer().getPlayer(this.activePlayers.keySet().iterator().next()).get().getName(), " won spleef on arena ", this.name)
            );
            resetGame();
        }
    }

    @Override
    public void resetGame() {


        for (UUID playerUid : this.activePlayers.keySet()) {
            Player player = Sponge.getServer().getPlayer(playerUid).get();
            player.getInventory().clear();
            player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
            player.offer(Keys.POTION_EFFECTS, new ArrayList<>());
            player.offer(Keys.FOOD_LEVEL, 20);
            player.offer(Keys.HEALTH, player.get(Keys.MAX_HEALTH).get());
            player.setScoreboard(null);
            player.setTransform(this.getLobby());
            this.inactivePlayers.put(playerUid, this.activePlayers.get(playerUid));
        }



        for (BlockSnapshot bs : this.brokenBlocks.keySet()) {
            bs.restore(true, BlockChangeFlags.NONE);
        }
        this.brokenBlocks.clear();
        this.mode = GameSpleef.Mode.READY;

        UUID max_kills_player = null;
        double max_kills = 0;

        for (UUID playerUid : this.inactivePlayers.keySet()) {
            double k = this.inactivePlayers.get(playerUid).getKnockouts().size();
            Sponge.getServer().getPlayer(playerUid).get().sendMessage(
                    Text.of(TextColors.GRAY, "[", TextColors.RED, "SPLEEF", TextColors.GRAY, "] You knocked out ", TextColors.RED, (int) k, TextColors.GRAY, " (", (int)(k/this.inactivePlayers.size() * 100.0), "%) players")
            );
            if (k > max_kills) {
                max_kills = k;
                max_kills_player = playerUid;
            }
        }

        UUID max_breaks_player = null;
        double max_breaks = 0;
        int totalBlocks = 0;
        for (AABB aabb : this.floors)
            totalBlocks += (aabb.getSize().getFloorX() * aabb.getSize().getFloorZ());

        for (UUID playerUid : this.inactivePlayers.keySet()) {
            double i = this.inactivePlayers.get(playerUid).getBlocksBroken();
            Sponge.getServer().getPlayer(playerUid).get().sendMessage(
                    Text.of(TextColors.GRAY, "[", TextColors.RED, "SPLEEF", TextColors.GRAY, "] You broke ", TextColors.RED, (int) i, TextColors.GRAY, " blocks (", (int)(i/totalBlocks * 100.0), "%)")
            );
            if (i > max_breaks) {
                max_breaks = i;
                max_breaks_player = playerUid;
            }
        }

        for (UUID playerUid : this.inactivePlayers.keySet()) {
            Sponge.getServer().getPlayer(playerUid).get().sendMessage(
                    Text.of(TextColors.GRAY, "-------------------------------------------------")
            );
            Sponge.getServer().getPlayer(playerUid).get().sendMessage(
                    Text.of(TextColors.GRAY, "Most knockouts by ", TextColors.RED, Sponge.getServer().getPlayer(max_kills_player).get().getName(), TextColors.GRAY, " (", (int)max_kills, ", ", (int)(max_kills/this.inactivePlayers.size()*100.0), "%)")
            );
            Sponge.getServer().getPlayer(playerUid).get().sendMessage(
                    Text.of(TextColors.GRAY, "Most blocks broke by ", TextColors.RED, Sponge.getServer().getPlayer(max_breaks_player).get().getName(), TextColors.GRAY, " (", (int)max_breaks, ", ", (int)(max_breaks/totalBlocks*100.0), "%)")
            );
            Sponge.getServer().getPlayer(playerUid).get().sendMessage(
                    Text.of(TextColors.GRAY, "-------------------------------------------------")
            );
        }

        this.mode = GameSpleef.Mode.READY;
        this.inactivePlayers.clear();
        this.activePlayers.clear();

    }

    private class StartingTimerTask implements Consumer<Task> {

        private int seconds = 16;

        @Override
        public void accept(Task task) {
            seconds--;
            if (seconds % 5 == 0 || seconds < 6) {
                for (UUID playerUid : activePlayers.keySet()) {
                    Sponge.getServer().getPlayer(playerUid).ifPresent(player -> player.sendMessage(
                            Text.of(TextColors.GRAY, "[", TextColors.RED, "SPLEEF", TextColors.GRAY, "] Game starting in ", TextColors.RED, seconds, TextColors.GRAY, " seconds.")
                    ));
                }
            }
            if (seconds <= 0) {
                startGame();
                task.cancel();
            }
        }
    }
}
