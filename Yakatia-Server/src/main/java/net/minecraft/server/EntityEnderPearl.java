package net.minecraft.server;

import dev.cobblesword.nachospigot.commons.Constants;
import me.elier.nachospigot.config.NachoConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.github.paperspigot.PaperSpigotConfig;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EntityEnderPearl extends EntityProjectile {

    private EntityLiving shooter;

    public EntityEnderPearl(World world) {
        super(world);
        this.loadChunks = world.paperSpigotConfig.loadUnloadedEnderPearls;
    }

    public EntityEnderPearl(World world, EntityLiving shooter) {
        super(world, shooter);
        this.shooter = shooter;
        this.loadChunks = world.paperSpigotConfig.loadUnloadedEnderPearls;
    }

    @Override
    protected void a(MovingObjectPosition movingObjectPosition) {
        EntityLiving shooter = this.getShooter();

        if (movingObjectPosition.entity != null) {
            if (movingObjectPosition.entity.equals(this.shooter)) {
                return;
            }

            movingObjectPosition.entity.damageEntity(DamageSource.projectile(this, shooter), 0.0F);
        }

        // PaperSpigot start - Remove entities in unloaded chunks
        if (this.inUnloadedChunk && world.paperSpigotConfig.removeUnloadedEnderPearls) {
            this.die();
            return;
        }
        // PaperSpigot end

        // FlamePaper start - Handle pearl collision with blocks
        BlockPosition blockPosition = movingObjectPosition.a();
        if (blockPosition != null) {
            IBlockData blockData = world.getType(blockPosition);
            Block block = (Block) blockData.getBlock();
            boolean collides = shouldPearlPassThrough(block, blockData);

            if (collides) {
                return;
            }
        }
        // FlamePaper end

        for (int i = 0; i < 32; ++i) {
            this.world.addParticle(EnumParticle.PORTAL, this.locX, this.locY + this.random.nextDouble() * 2.0D, this.locZ, this.random.nextGaussian(), 0.0D, this.random.nextGaussian(), Constants.EMPTY_ARRAY);
        }

        if (!this.world.isClientSide) {
            handleTeleport();
            this.die();
        }
    }

    private boolean shouldPearlPassThrough(Block block, IBlockData blockData) {
        return PaperSpigotConfig.pearlPassthroughTripwire && block == Blocks.TRIPWIRE
                || PaperSpigotConfig.pearlPassthroughCobweb && block == Blocks.WEB
                || PaperSpigotConfig.pearlPassthroughBed && block == Blocks.BED
                || PaperSpigotConfig.pearlPassthroughFenceGate && (block == Blocks.FENCE_GATE || block == Blocks.SPRUCE_FENCE_GATE || block == Blocks.BIRCH_FENCE_GATE || block == Blocks.JUNGLE_FENCE_GATE || block == Blocks.DARK_OAK_FENCE_GATE || block == Blocks.ACACIA_FENCE_GATE) && ((Boolean) blockData.get(BlockFenceGate.OPEN))
                || PaperSpigotConfig.pearlPassthroughSlab && (block == Blocks.STONE_SLAB || block == Blocks.WOODEN_SLAB || block == Blocks.STONE_SLAB2);
    }

    private void handleTeleport() {
        if (this.shooter instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) this.shooter;

            if (player.playerConnection.a().isConnected() && player.world == this.world && !player.isSleeping()) {
                CraftPlayer bukkitPlayer = player.getBukkitEntity();
                Location location = getBukkitEntity().getLocation();
                location.setPitch(bukkitPlayer.getLocation().getPitch());
                location.setYaw(bukkitPlayer.getLocation().getYaw());

                if (NachoConfig.antiEnderPearlGlitch) {
                    adjustLocationForAntiGlitch(location, bukkitPlayer.getLocation());
                }

                PlayerTeleportEvent teleportEvent = new PlayerTeleportEvent(bukkitPlayer, bukkitPlayer.getLocation(), location, TeleportCause.ENDER_PEARL);
                Bukkit.getPluginManager().callEvent(teleportEvent);

                if (!teleportEvent.isCancelled() && !player.playerConnection.isDisconnected()) {
                    if (this.random.nextFloat() < 0.05F && this.world.getGameRules().getBoolean("doMobSpawning") && world.nachoSpigotConfig.endermiteSpawning) {
                        spawnEndermite();
                    }

                    if (player.au()) {
                        player.mount((Entity) null);
                    }

                    player.playerConnection.teleport(teleportEvent.getTo());
                    player.fallDistance = 0.0F;
                    CraftEventFactory.entityDamage = this;
                    player.damageEntity(DamageSource.FALL, 5.0F);
                    CraftEventFactory.entityDamage = null;
                }
            }
        } else if (this.shooter != null) {
            this.shooter.enderTeleportTo(this.locX, this.locY, this.locZ);
            this.shooter.fallDistance = 0.0F;
        }
    }

    private void adjustLocationForAntiGlitch(Location location, Location playerLocation) {
        double diffX = location.getBlockX() - playerLocation.getBlockX();
        double diffY = location.getBlockY() - playerLocation.getBlockY();
        double diffZ = location.getBlockZ() - playerLocation.getBlockZ();

        if (diffY <= 0) {
            location.setY(location.getBlockY() + 0.5D);
        } else {
            location.setY(location.getBlockY() - 0.5D);
            if (diffX <= 0) {
                location.setX(location.getBlockX() + 0.5D);
            } else {
                location.setX(location.getBlockX() - 0.5D);
            }
            if (diffZ <= 0) {
                location.setZ(location.getBlockZ() + 0.5D);
            } else {
                location.setZ(location.getBlockZ() - 0.5D);
            }
        }
    }

    private void spawnEndermite() {
        EntityEndermite endermite = new EntityEndermite(this.world);
        endermite.a(true);
        endermite.setPositionRotation(this.shooter.locX, this.shooter.locY, this.shooter.locZ, this.shooter.yaw, this.shooter.pitch);
        this.world.addEntity(endermite);
    }

    @Override
    public void t_() {
        if (this.shooter instanceof EntityHuman && !this.shooter.isAlive()) {
            this.die();
        } else {
            super.t_();
        }
    }
}
