package io.github.zap.arenaapi.nms.v1_16_R3.pathfind;

import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class MobNavigator_v1_16_R3 extends Navigation implements MobNavigator {
    private PathEntityWrapper_v1_16_R3 currentPath;
    private boolean isStopped = false;
    private double lastSpeed;

    public MobNavigator_v1_16_R3(EntityInsentient entityinsentient, World world) {
        super(entityinsentient, world);
    }

    @Override
    public void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper, double speed) {
        PathEntity newPath = (currentPath = ((PathEntityWrapper_v1_16_R3)pathEntityWrapper)).pathEntity();
        lastSpeed = speed;

        if(!isStopped) {
            Vec3D currentPos = getEntity().getPositionVector();

            int entityX = NumberConversions.floor(currentPos.x);
            int entityY = NumberConversions.floor(currentPos.y);
            int entityZ = NumberConversions.floor(currentPos.z);

            for(int i = 0; i < newPath.e(); i++) {
                PathPoint sample = newPath.a(i);

                if(sample.getX() == entityX && sample.getY() == entityY && sample.getZ() == entityZ) {
                    newPath.c(i);
                    a(newPath, speed);
                    return;
                }
            }

            a((PathEntity)null, speed);
        }
    }

    @Override
    public @Nullable PathEntityWrapper currentPath() {
        return currentPath;
    }

    @Override
    public boolean isIdle() {
        return m();
    }

    @Override
    public void pauseNavigating() {
        isStopped = true;
        stopPathfinding();
    }

    @Override
    public void resumeNavigating() {
        isStopped = false;

        if(currentPath != null) {
            a(currentPath.pathEntity(), lastSpeed);
        }
    }

    @Override
    public boolean a(@javax.annotation.Nullable PathEntity pathentity, double d0) {
        if (pathentity == null) {
            this.c = null;
            return false;
        } else {
            this.c = pathentity;

            if (this.m()) {
                return false;
            } else {
                this.D_();
                if (this.c.e() <= 0) {
                    return false;
                } else {
                    this.d = d0;
                    Vec3D vec3d = this.b();
                    this.f = this.e;
                    this.g = vec3d;
                    return true;
                }
            }
        }
    }

    @Override
    protected boolean a() {
        return true;
    }

    @Override
    public PathEntity a(BlockPosition blockposition, int i) {
        return c;
    }

    @Override
    public PathEntity a(Entity entity, int i) {
        return c;
    }

    @Override
    protected boolean a(Vec3D vec3d, Vec3D vec3d1, int i, int j, int k) {
        return true;
    }

    @Override
    protected boolean a(PathType pathtype) {
        return true;
    }

    @Override
    public void a(boolean flag) { }

    @Override
    public boolean f() {
        return true;
    }

    @Override
    public void c(boolean flag) { }

    @Override
    public Pathfinder getPathfinder() {
        return null;
    }

    @Override
    public void g() { }

    @Override
    public void a(float f) { }

    @Override
    public void a(double d0) {
        super.a(d0);
    }

    @Nullable
    @Override
    public PathEntity a(Stream<BlockPosition> stream, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(Set<BlockPosition> set, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(BlockPosition blockposition, Entity target, int i) {
        return c;
    }

    @Nullable
    @Override
    protected PathEntity a(Set<BlockPosition> set, int i, boolean flag, int j) {
        return c;
    }

    @Nullable
    @Override
    protected PathEntity a(Set<BlockPosition> set, Entity target, int i, boolean flag, int j) {
        return c;
    }

    @Override
    public boolean a(double d0, double d1, double d2, double d3) {
        return false;
    }

    @Override
    public boolean a(Entity entity, double d0) {
        return false;
    }

    @Override
    public boolean setDestination(@Nullable PathEntity pathentity, double speed) {
        return a(pathentity, speed);
    }

    @Nullable
    @Override
    public PathEntity getPathEntity() {
        return c;
    }

    @Nullable
    @Override
    public PathEntity k() {
        return c;
    }

    @Override
    public void o() { }

    @Override
    protected boolean p() { return false; }

    @Override
    public boolean a(BlockPosition blockposition) {
        return true;
    }

    @Override
    public PathfinderAbstract q() {
        return null;
    }

    @Override
    public void d(boolean flag) { }

    @Override
    public boolean r() {
        return false;
    }

    @Override
    protected void D_() { }
}
