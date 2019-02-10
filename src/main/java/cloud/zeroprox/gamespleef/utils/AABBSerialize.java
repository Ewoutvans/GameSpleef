package cloud.zeroprox.gamespleef.utils;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.util.AABB;

@ConfigSerializable
public class AABBSerialize {

    @Setting("x1")
    int x1;

    @Setting("y1")
    int y1;

    @Setting("z1")
    int z1;

    @Setting("x2")
    int x2;

    @Setting("y2")
    int y2;

    @Setting("z2")
    int z2;

    public AABBSerialize() {

    }

    public AABBSerialize(int x, int y, int z, int x2, int y2, int z2) {
        this.x1 = x;
        this.y1 = y;
        this.z1 = z;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public AABB toAABB() {
        return new AABB(x1, y1, z1, x2, y2, z2);
    }
}
