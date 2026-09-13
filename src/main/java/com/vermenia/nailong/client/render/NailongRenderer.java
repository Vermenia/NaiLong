package com.vermenia.nailong.client.render;

import com.vermenia.nailong.client.model.NailongModel;
import com.vermenia.nailong.entity.NailongEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class NailongRenderer extends GeoEntityRenderer<NailongEntity> {
    public NailongRenderer(EntityRendererProvider.Context context) {
        super(context, new NailongModel());
        this.withScale(NailongEntity.MODEL_SCALE);
        this.shadowRadius = 0.55F;
    }
}
