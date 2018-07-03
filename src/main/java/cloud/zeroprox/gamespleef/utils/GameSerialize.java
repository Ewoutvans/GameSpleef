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

    public Location corner_floor_1;
    public Location corner_floor_2;
    public Location corner_area_1;
    public Location corner_area_2;

    public GameSerialize() {

    }
}
