package cloud.zeroprox.gamespleef.stats;

import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStats {

    private UUID owner;
    private List<UUID> knockouts;
    private long started, left;
    private int blocksBroken;

    public PlayerStats(UUID owner) {
        this.owner = owner;
        this.knockouts = new ArrayList<>();
        this.started = System.currentTimeMillis();
        this.left = -1;
        this.blocksBroken = 0;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getKnockouts() {
        return knockouts;
    }

    public long getStarted() {
        return started;
    }

    public long getLeft() {
        return left;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void addBlocksBroken(int amount) {
        this.blocksBroken = this.blocksBroken + amount;
    }

    public void addKnockout(Player player) {
        this.knockouts.add(player.getUniqueId());
    }
}
