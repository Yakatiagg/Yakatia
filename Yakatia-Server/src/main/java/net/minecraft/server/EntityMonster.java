package net.minecraft.server;

import org.bukkit.event.entity.EntityCombustByEntityEvent; // CraftBukkit

public abstract class EntityMonster extends EntityCreature implements IMonster {

    public EntityMonster(World world) {
        super(world);
        this.b_ = 5;
    }

    @Override
    public void m() {
        this.bx();
        float visibility = this.c(1.0F);

        if (visibility > 0.5F) {
            this.ticksFarFromPlayer += 2;
        }

        super.m();
    }

    @Override
    public void t_() {
        super.t_();
        if (!this.world.isClientSide && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.die();
        }
    }

    @Override
    protected String P() {
        return "game.hostile.swim";
    }

    @Override
    protected String aa() {
        return "game.hostile.swim.splash";
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damage) {
        if (this.isInvulnerable(damageSource)) {
            return false;
        } else if (super.damageEntity(damageSource, damage)) {
            Entity attacker = damageSource.getEntity();
            if (this.passenger != attacker && this.vehicle != attacker) {
                // Handle fire aspect enchantment
                return handleFireAspect(attacker);
            }
        }
        return false;
    }

    private boolean handleFireAspect(Entity attacker) {
        int fireAspectLevel = EnchantmentManager.getFireAspectEnchantmentLevel(this);

        if (fireAspectLevel > 0) {
            // CraftBukkit start - Call a combust event when hitting with a fire enchanted item
            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), attacker.getBukkitEntity(), fireAspectLevel * 4);
            org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

            if (!combustEvent.isCancelled()) {
                attacker.setOnFire(combustEvent.getDuration());
            }
            // CraftBukkit end
        }

        this.a((EntityLiving) this, attacker);
        return true;
    }

    @Override
    public float a(BlockPosition position) {
        return 0.5F - this.world.o(position);
    }

    @Override
    protected boolean n_() {
        BlockPosition position = new BlockPosition(this.locX, this.getBoundingBox().b, this.locZ);
        int lightLevel = this.world.b(EnumSkyBlock.SKY, position);

        if (lightLevel > this.random.nextInt(32)) {
            return false;
        }

        boolean isDark;
        if (this.world.R()) {
            int prevLightLevel = this.world.ab();
            this.world.c(10);
            isDark = !world.isLightLevel(position, this.random.nextInt(9));
            this.world.c(prevLightLevel);
        } else {
            isDark = !world.isLightLevel(position, this.random.nextInt(9));
        }

        return isDark;
    }

    @Override
    public boolean bR() {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && this.n_() && super.bR();
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE);
    }

    @Override
    protected boolean ba() {
        return true;
    }
}
