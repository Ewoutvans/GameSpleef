package cloud.zeroprox.gamespleef.utils;

import cloud.zeroprox.gamespleef.GameSpleef;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

@ConfigSerializable
public class GameSerialize {

    @Setting("name")
    public String name;

    @Setting("lobby")
    public Transform<World> lobby;

    @Setting("spawn")
    public Transform<World> spawn;

    @Setting("area")
    public AABBSerialize area;

    @Setting("floors")
    public List<AABBSerialize> floors;

    @Setting("gametype")
    public GameSpleef.GameType gameType;

    @Setting(value = "saveinv", comment = "experimental inventory save option DO NOT USE ON MODDED SERVER")
    public boolean saveInv;

    @Setting(value = "playerLimit", comment = "maximum player to join this arena")
    public int playerLimit;

    @Setting(value = "campRadius", comment = "radius to check every interval, if the player has not moved in that radius it will count as camping")
    public int campRadius;

    @Setting(value = "campInterval", comment = "how many seconds interval to check player are camping")
    public int campInterval;

    @Setting(value = "campPlayers", comment = "when player count is lower than campPlayers anticamping start checking")
    public int campPlayers;

    @Setting(value = "winningCommand", comment = "Runs this command from console when someone wins. %winner% is replaced with the name of the winner, %game% is replace with the gamename")
    public List<String> winningCommand;

    @Setting(value = "winningMinPlayers", comment = "Only run command when this many players where in the game (prevents users farming with 2 accounts)")
    public int winningMinPlayers;

    @Setting(value = "winningCooldown", comment = "Cooldown for the player to execute the command (does not save to disk) in minutes, user gets cooldown global, time is per game")
    public int winningCooldown;

    public Location corner_floor_1;
    public Location corner_floor_2;
    public Location corner_area_1;
    public Location corner_area_2;

    public GameSerialize() {

    }
}
