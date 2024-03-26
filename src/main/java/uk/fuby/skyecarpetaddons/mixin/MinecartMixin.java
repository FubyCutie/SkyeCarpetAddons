package uk.fuby.skyecarpetaddons.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.fuby.skyecarpetaddons.Options;


@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartMixin extends Entity {

    public MinecartMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract Vec3d snapPositionToRail(double x, double y, double z);

    @Shadow
    protected void applySlowdown() {}

    @Shadow
    protected double getMaxSpeed() {return 0;}


    @Unique
    private int cartSpeedMultiplier(BlockPos pos, BlockState state) {
        if(Options.fastMinecarts.equals("off")) return 1;
        
        if(this.isTouchingWater()) return 1;
        if (state.isOf(Blocks.RAIL) || (state.isOf(Blocks.POWERED_RAIL) && state.get(PoweredRailBlock.POWERED))) {
            RailShape shape = state.get(((AbstractRailBlock)state.getBlock()).getShapeProperty());
            if (shape == RailShape.EAST_WEST || shape == RailShape.NORTH_SOUTH) {
                return Options.INSTANCE.getParsedFastMinecartBehaviour().getOrDefault(world.getBlockState(pos.down()).getBlock(), 1);
            }
        }
        return 1;
    }

    @Inject(at = @At("HEAD"), method = "moveOnRail", cancellable = true)
    protected void moveOnRailOverwrite(BlockPos pos, BlockState state, CallbackInfo ci) {
        if(cartSpeedMultiplier(pos, state) == 1) return;
        
        int i = 0;
        int steps = cartSpeedMultiplier(pos, state);
        while (steps > i) {
            int x = MathHelper.floor(this.getX());
            int y = MathHelper.floor(this.getY());
            int z = MathHelper.floor(this.getZ());
            BlockPos railPos = new BlockPos(x, y, z);
            BlockState railState = this.world.getBlockState(railPos);
            if (!(railState.getBlock() instanceof AbstractRailBlock)) break;
            fastCartMoveOnRail(railPos, railState);
            i++;
            steps = cartSpeedMultiplier(pos, state);
        }
        ci.cancel();

    }

    @Unique
    private void fastCartMoveOnRail(BlockPos pos, BlockState state) {
        this.onLanding();
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        Vec3d vec3d = this.snapPositionToRail(d, e, f);

        boolean bl = state.get(((AbstractRailBlock)state.getBlock()).getShapeProperty()) == RailShape.EAST_WEST;

        Vec3d velocity = this.getVelocity();
        double l = Math.min(2.0, velocity.horizontalLength());

        if(bl) {
            velocity = new Vec3d(velocity.x < 0.0 ? -l : l, velocity.y, 0);
            this.setPosition(d, e, 0.5 + pos.getZ());
        } else {
            velocity = new Vec3d(0, velocity.y, velocity.z < 0.0 ? -l : l);
            this.setPosition(0.5 + pos.getX(), e, f);
        }


        Entity entity = this.getFirstPassenger();
        if (entity instanceof PlayerEntity) {
            Vec3d vec3d3 = entity.getVelocity();
            double m = vec3d3.horizontalLengthSquared();
            double n = velocity.horizontalLengthSquared();
            if (m > 1.0E-4 && n < 0.01) {
                velocity = velocity.add(vec3d3.x * 0.1, 0.0, vec3d3.z * 0.1);
            }
        }
        this.setVelocity(velocity);

        double t = this.hasPassengers() ? 0.75 : 1.0;
        double u = this.getMaxSpeed();

        this.move(MovementType.SELF, new Vec3d(MathHelper.clamp(t * velocity.x, -u, u), 0.0, MathHelper.clamp(t * velocity.z, -u, u)));

        this.applySlowdown();


        Vec3d vec3d4 = this.snapPositionToRail(this.getX(), this.getY(), this.getZ());
        Vec3d vec3d5;
        double w;
        if (vec3d4 != null && vec3d != null) {
            double v = (vec3d.y - vec3d4.y) * 0.05;
            vec3d5 = this.getVelocity();
            w = vec3d5.horizontalLength();
            if (w > 0.0) {
                this.setVelocity(vec3d5.multiply((w + v) / w, 1.0, (w + v) / w));
            }

            this.setPosition(this.getX(), vec3d4.y, this.getZ());
        }

        int x = MathHelper.floor(this.getX());
        int y = MathHelper.floor(this.getZ());
        if (x != pos.getX() || y != pos.getZ()) {
            vec3d5 = this.getVelocity();
            w = vec3d5.horizontalLength();
            this.setVelocity(w * (double)(x - pos.getX()), vec3d5.y, w * (double)(y - pos.getZ()));
        }

        if (state.isOf(Blocks.POWERED_RAIL)) {
            vec3d5 = this.getVelocity();
            w = vec3d5.horizontalLength();
            if (w > 0.01) {
                this.setVelocity(vec3d5.add(vec3d5.x / w * 0.06, 0.0, vec3d5.z / w * 0.06));
            } else {
                double aa = vec3d5.x;
                double ab = vec3d5.z;
                if (bl) {
                    if (this.world.getBlockState(pos.west()).isSolidBlock(this.world, pos.west())) {
                        aa = 0.02;
                    } else if (this.world.getBlockState(pos.east()).isSolidBlock(this.world, pos.east())) {
                        aa = -0.02;
                    }
                } else {
                    if (this.world.getBlockState(pos.north()).isSolidBlock(this.world, pos.north())) {
                        ab = 0.02;
                    } else if (this.world.getBlockState(pos.south()).isSolidBlock(this.world, pos.south())) {
                        ab = -0.02;
                    }
                }

                this.setVelocity(aa, vec3d5.y, ab);
            }
        }
    }
}
