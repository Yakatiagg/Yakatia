package net.minecraft.server;

// CraftBukkit start
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;
// CraftBukkit end

public class EntityEgg extends EntityProjectile {

    public EntityEgg(World world) {
        super(world);
    }

    public EntityEgg(World world, EntityLiving entityliving) {
        super(world, entityliving);
    }

    public EntityEgg(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null) {
            movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0.0F);
        }

        handleEggHatching();

        spawnEggParticles();

        if (!this.world.isClientSide) {
            this.die();
        }
    }

    private void handleEggHatching() {
        final boolean isHatching = !this.world.isClientSide && this.random.nextInt(8) == 0;
        int numHatching = isHatching && this.random.nextInt(32) == 0 ? 4 : 1;

        EntityType hatchingType = EntityType.CHICKEN;
        final Entity shooter = this.getShooter();

        if (shooter instanceof EntityPlayer) {
            Player player = (Player) shooter.getBukkitEntity();
            PlayerEggThrowEvent event = new PlayerEggThrowEvent(player, (org.bukkit.entity.Egg) this.getBukkitEntity(), isHatching, (byte) numHatching, hatchingType);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isHatching()) return;

            numHatching = event.getNumHatches();
            hatchingType = event.getHatchingType();
        }

        if (isHatching) {
            hatchEntities(hatchingType, numHatching);
        }
    }

    private void hatchEntities(EntityType hatchingType, int numHatching) {
        for (int k = 0; k < numHatching; k++) {
            Entity entity = world.getWorld().createEntity(
                    new org.bukkit.Location(world.getWorld(), this.locX, this.locY, this.locZ, this.yaw, 0.0F),
                    hatchingType.getEntityClass()
            );

            if (entity.getBukkitEntity() instanceof Ageable) {
                ((Ageable) entity.getBukkitEntity()).setBaby();
            }

            world.getWorld().addEntity(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.EGG);
        }
    }

    private void spawnEggParticles() {
        final double particleSpeed = 0.08D;
        for (int j = 0; j < 8; ++j) {
            this.world.addParticle(EnumParticle.ITEM_CRACK, this.locX, this.locY, this.locZ,
                    ((double) this.random.nextFloat() - 0.5D) * particleSpeed,
                    ((double) this.random.nextFloat() - 0.5D) * particleSpeed,
                    ((double) this.random.nextFloat() - 0.5D) * particleSpeed,
                    new int[] { Item.getId(Items.EGG) }
            );
        }
    }
}
