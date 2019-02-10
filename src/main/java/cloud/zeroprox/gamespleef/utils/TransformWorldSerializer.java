package cloud.zeroprox.gamespleef.utils;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;

import java.util.UUID;

public class TransformWorldSerializer implements TypeSerializer<Transform<World>> {
    @Override
    public Transform<World> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        World world = Sponge.getServer().getWorld(value.getNode("world").getValue(TypeToken.of(UUID.class))).get();
        Vector3d position = value.getNode("position").getValue(TypeToken.of(Vector3d.class));
        Vector3d rotation = value.getNode("rotation").getValue(TypeToken.of(Vector3d.class));
        return new Transform<World>(world, position, rotation);
    }

    @Override
    public void serialize(TypeToken<?> type, Transform<World> obj, ConfigurationNode value) throws ObjectMappingException {
        value.getNode("world").setValue(TypeToken.of(UUID.class), obj.getExtent().getUniqueId());
        value.getNode("position").setValue(TypeToken.of(Vector3d.class), obj.getPosition());
        value.getNode("rotation").setValue(TypeToken.of(Vector3d.class), obj.getRotation());
    }
}
