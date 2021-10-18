package io.github.zap.arenaapi.nms.v1_16_R3.pathfind;

import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.commons.vectors.Vectors;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Color;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class MobNavigator_v1_16_R3 extends Navigation implements MobNavigator {
    private final double SQRT_2 = Math.sqrt(2);
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

                    for(PathPoint point : newPath.getPoints()) {
                        super.b.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE,
                                point.getX() + 0.5, point.getY() + 0.5, point.getZ() + 0.5, 1, 0, 0,
                                0, new org.bukkit.Particle.DustOptions(Color.GREEN, 2));
                    }

                    return;
                }
            }


            newPath.c(0);
            a(newPath, speed);
        }
    }

    @Override
    public @Nullable PathEntityWrapper currentPath() {
        return currentPath;
    }

    @Override
    public boolean hasActivePath() {
        return !m();
    }

    @Override
    public boolean isOnPath() {
        PathPoint currentPoint;
        if(currentPath != null && (currentPoint = currentPath.pathEntity().i()) != null) {
            Vec3D currentPos = getEntity().getPositionVector();

            int entityX = NumberConversions.floor(currentPos.x);
            int entityY = NumberConversions.floor(currentPos.y);
            int entityZ = NumberConversions.floor(currentPos.z);

            return currentPoint.getX() == entityX && currentPoint.getY() == entityY && currentPoint.getZ() == entityZ;
        }

        return false;
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
    protected void l() {
        Vec3D agentPos = this.b();
        this.l = this.a.getWidth() > 0.75F ? this.a.getWidth() / 2.0F : 0.75F - this.a.getWidth() / 2.0F;
        BlockPosition blockposition = this.c.g();
        double d0 = Math.abs(this.a.locX() - ((double)blockposition.getX() + 0.5D));
        double d1 = Math.abs(this.a.locY() - (double)blockposition.getY());
        double d2 = Math.abs(this.a.locZ() - ((double)blockposition.getZ() + 0.5D));
        boolean flag = d0 < (double)this.l && d2 < (double)this.l && d1 < 1.0D;

        //|| this.a.b(this.c.h().l) && this.canAdvance(agentPos)
        if (flag || this.canAdvance(agentPos)) {
            System.out.println("Continuing (flag = " + flag + ")");
            this.c.a();
        }

        this.a(agentPos);
    }

    private boolean canAdvance(Vec3D agentPos) {
        if (this.c.f() + 1 >= this.c.e()) {
            return false;
        } else {
            Vec3D currentCenterPos = Vec3D.c(this.c.g());

            if (!agentPos.a(currentCenterPos, 2)) { //if agentPos is not within 2 blocks of the target node
                return false;
            } else {
                Vec3D nextCenterPos = Vec3D.c(this.c.d(this.c.f() + 1));

                //whoa look more code whose only purpose is to fix mojang's incompetence
                if(agentPos.a(nextCenterPos, SQRT_2)) {
                    Vec3D nextMinusCurrent = nextCenterPos.d(currentCenterPos);
                    Vec3D agentMinusCurrent = agentPos.d(currentCenterPos);
                    return nextMinusCurrent.b(agentMinusCurrent) > 0.0D; //acute angle test
                }
                else {
                    return false;
                }
            }
        }
    }

    @Override
    public void c() {
        ++this.e;

        if (!this.m()) {
            System.out.println(this.c.f());

            Vec3D vec3d;
            if (this.a()) { //if entity valid for pathfinding...
                this.l();
            } else if (this.c != null && !this.c.c()) {
                vec3d = this.b();
                Vec3D vec3d1 = this.c.a(this.a);
                if (vec3d.y > vec3d1.y && !this.a.isOnGround() && MathHelper.floor(vec3d.x) ==
                        MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                    this.c.a();
                }
            }

            if (!this.m()) {
                vec3d = this.c.a(this.a);

                BlockPosition blockposition = new BlockPosition(vec3d);
                this.a.getControllerMove().a(vec3d.x, this.b.getType(blockposition.down()).isAir() ? vec3d.y :
                        PathfinderNormal.a(this.b, blockposition), vec3d.z, this.d);
            }
        }
    }
}
