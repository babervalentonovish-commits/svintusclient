package com.lowcode.svintusclient.client.render;

import com.lowcode.svintusclient.client.SvintusclientClient;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.gl.UniformType;
import net.minecraft.util.Identifier;

public final class Pipelines {

    public static final VertexFormat SHAPE_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV1", VertexFormatElement.UV1)
            .add("UV2", VertexFormatElement.UV2)
            .build();

    public static final RenderPipeline ROUND_RECT = shape("round_rect", false);
    public static final RenderPipeline ROUND_SHADOW = shape("round_shadow", false);
    public static final RenderPipeline BLUR_RECT = shape("blur_rect", true);

    private Pipelines() {
    }

    private static RenderPipeline shape(String fragment, boolean sampler) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(id("pipeline/" + fragment))
                .withVertexShader(id("core/shape"))
                .withFragmentShader(id("core/" + fragment))
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withVertexFormat(SHAPE_FORMAT, VertexFormat.DrawMode.QUADS);
        if (sampler) builder.withSampler("Sampler0");
        return builder.build();
    }

    private static Identifier id(String path) {
        return Identifier.of(SvintusclientClient.MOD_ID, path);
    }
}