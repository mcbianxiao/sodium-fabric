package me.jellysquid.mods.sodium.mixin.features.world.biome;

import me.jellysquid.mods.sodium.client.world.biome.BiomeColorMaps;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Biome.class)
public abstract class BiomeMixin {
    @Shadow
    @Final
    private BiomeEffects effects;

    @Shadow
    @Final
    private Biome.Weather weather;

    @Unique
    private boolean hasCustomGrassColor;

    @Unique
    private int customGrassColor;

    @Unique
    private boolean hasCustomFoliageColor;

    @Unique
    private int customFoliageColor;

    @Unique
    private int defaultColorIndex;

    @Unique
    private BiomeEffects cachedSpecialEffects;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        setupColors();
    }

    @Unique
    private void setupColors() {
        this.cachedSpecialEffects = effects;

        var grassColor = this.effects.getGrassColor();

        if (grassColor.isPresent()) {
            this.hasCustomGrassColor = true;
            this.customGrassColor = grassColor.get();
        }

        var foliageColor = this.effects.getFoliageColor();

        if (foliageColor.isPresent()) {
            this.hasCustomFoliageColor = true;
            this.customFoliageColor = foliageColor.get();
        }

        this.defaultColorIndex = this.getDefaultColorIndex();
    }

    /**
     * @author JellySquid
     * @reason Avoid unnecessary pointer de-references and allocations
     */
    @Overwrite
    public int getGrassColorAt(double x, double z) {
        if (this.effects != this.cachedSpecialEffects) {
            setupColors();
        }

        int color;

        if (this.hasCustomGrassColor) {
            color = this.customGrassColor;
        } else {
            color = BiomeColorMaps.getGrassColor(this.defaultColorIndex);
        }

        var modifier = this.effects.getGrassColorModifier();

        if (modifier != BiomeEffects.GrassColorModifier.NONE) {
            color = modifier.getModifiedGrassColor(x, z, color);
        }

        return color;
    }

    /**
     * @author JellySquid
     * @reason Avoid unnecessary pointer de-references and allocations
     */
    @Overwrite
    public int getFoliageColor() {
        if (this.effects != this.cachedSpecialEffects) {
            setupColors();
        }

        int color;

        if (this.hasCustomFoliageColor) {
            color = this.customFoliageColor;
        } else {
            color = BiomeColorMaps.getFoliageColor(this.defaultColorIndex);
        }

        return color;
    }

    @Unique
    private int getDefaultColorIndex() {
        double temperature = MathHelper.clamp(this.weather.temperature(), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(this.weather.downfall(), 0.0F, 1.0F);

        return BiomeColorMaps.getIndex(temperature, humidity);
    }
}
