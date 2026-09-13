package com.vermenia.nailong.client.model;

import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.entity.NailongEntity;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class NailongModel extends GeoModel<NailongEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(NailongMod.MOD_ID, "geo/nailong.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NailongMod.MOD_ID, "textures/entity/nailong.png");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(NailongMod.MOD_ID, "animations/nailong.animation.json");

    @Override
    public ResourceLocation getModelResource(NailongEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NailongEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NailongEntity animatable) {
        return ANIMATIONS;
    }
}
