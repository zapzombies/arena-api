package io.github.zap.arenaapi.nms.v1_16_R3.entity;

import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import io.github.zap.arenaapi.nms.v1_16_R3.pathfind.MobNavigator_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.pathfind.PathEntityWrapper_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.pathfind.PathPointWrapper_v1_16_R3;
import io.github.zap.commons.vectors.Vector3I;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class EntityBridge_v1_16_R3 implements EntityBridge {
    public static final EntityBridge_v1_16_R3 INSTANCE = new EntityBridge_v1_16_R3();

    private EntityBridge_v1_16_R3() { }

    @Override
    public int nextEntityID() {
        return net.minecraft.server.v1_16_R3.Entity.nextEntityId();
    }

    @Override
    public @NotNull UUID randomUUID() {
        return MathHelper.a(net.minecraft.server.v1_16_R3.Entity.SHARED_RANDOM);
    }

    @Override
    public int getEntityTypeID(@NotNull EntityType type) {
        return (EntityTypes.getByName(type.getKey().getKey())).map(IRegistry.ENTITY_TYPE::a).orElse(-1);
    }

    @Override
    public @NotNull PathEntityWrapper makePathEntity(@NotNull List<PathPointWrapper> pointWrappers,
                                                     @NotNull Vector3I destination, boolean reachesDestination) {
        List<PathPoint> points = new ArrayList<>();

        for(PathPointWrapper wrapper : pointWrappers) {
            PathPointWrapper_v1_16_R3 specific = (PathPointWrapper_v1_16_R3)wrapper;
            points.add(specific.pathPoint());
        }

        return new PathEntityWrapper_v1_16_R3(new PathEntity(points, new BlockPosition(destination.x(), destination.y(),
                destination.z()), reachesDestination));
    }

    @Override
    public @NotNull PathPointWrapper makePathPoint(@NotNull Vector3I blockLocation) {
        PathPoint pathPoint = new PathPoint(blockLocation.x(), blockLocation.y(), blockLocation.z());
        pathPoint.l = PathType.WALKABLE;
        return new PathPointWrapper_v1_16_R3(pathPoint);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull MobNavigator overrideNavigatorFor(@NotNull Mob mob) throws IllegalAccessException, NoSuchFieldException {
        Field entityNavigationField = EntityInsentient.class.getDeclaredField("navigation");
        entityNavigationField.setAccessible(true);

        EntityInsentient entityInsentient = ((CraftMob)mob).getHandle();
        NavigationAbstract navigation = (NavigationAbstract) entityNavigationField.get(entityInsentient);

        //entity was previously overridden
        if(navigation instanceof MobNavigator_v1_16_R3 mobNavigator) {
            return mobNavigator;
        }
        else {
            Field worldNavigatorsField = WorldServer.class.getDeclaredField("navigators");
            worldNavigatorsField.setAccessible(true);

            MobNavigator_v1_16_R3 customNavigator = new MobNavigator_v1_16_R3(entityInsentient,
                    entityInsentient.getWorld());

            Set<NavigationAbstract> navigators = (Set<NavigationAbstract>)worldNavigatorsField.get(entityInsentient.getWorld());
            navigators.remove(entityInsentient.getNavigation());

            entityNavigationField.set(entityInsentient, customNavigator);
            navigators.add(customNavigator);

            return customNavigator;
        }
    }

    public @Nullable MobNavigator getNavigator(@NotNull Mob mob) {
        //TODO: known issue with this is that mobs riding regular entities (players) will not work
        Entity vehicle = mob.getVehicle();
        if(vehicle instanceof Mob mobVehicle) {
            return getNavigator(mobVehicle);
        }
        else {
            NavigationAbstract navigationAbstract = ((CraftMob)mob).getHandle().getNavigation();

            if(navigationAbstract instanceof MobNavigator mobNavigator) {
                return mobNavigator;
            }

            return null;
        }
    }

    @Override
    public double distanceTo(@NotNull Entity entity, double x, double y, double z) {
        return ((CraftEntity) entity).getHandle().h(x, y, z);
    }

    @Override
    public @NotNull Random getRandomFor(@NotNull LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().getRandom();
    }

    @Override
    public boolean canSee(@NotNull Mob mob, @NotNull Entity target) {
        return ((CraftMob) mob).getHandle().getEntitySenses().a(((CraftEntity) target).getHandle());
    }

    @Override
    public void setLookDirection(@NotNull Mob mob, @NotNull Entity target, float maxYawChange, float maxPitchChange) {
        ((CraftMob) mob).getHandle().a(((CraftEntity) target).getHandle(), maxYawChange, maxPitchChange);
    }

    @Override
    public boolean hasAttribute(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute) {
        return ((CraftLivingEntity) livingEntity).getHandle().getAttributeMap()
                .b(CraftAttributeMap.toMinecraft(attribute));
    }

    @Override
    public void setAttributeFor(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute, double value) {
        EntityLiving nmsLivingEntity = ((CraftLivingEntity) livingEntity).getHandle();
        AttributeBase nmsAttribute = CraftAttributeMap.toMinecraft(attribute);

        AttributeMapBase attributeMap = nmsLivingEntity.getAttributeMap();
        AttributeModifiable modifiableAttribute = attributeMap.a(nmsAttribute);

        if (modifiableAttribute != null) {
            modifiableAttribute.setValue(value);
        }
        else {
            attributeMap.registerAttribute(nmsAttribute);
            //noinspection ConstantConditions
            attributeMap.a(nmsAttribute).setValue(value);
        }
    }

}
