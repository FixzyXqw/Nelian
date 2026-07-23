package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class NelianViewFrustum extends ViewFrustum {
    
    public NelianViewFrustum(World worldIn, int renderDistanceChunksIn, 
                             RenderGlobal renderGlobalIn, IRenderChunkFactory renderChunkFactoryIn) {
        super(worldIn, renderDistanceChunksIn, renderGlobalIn, renderChunkFactoryIn);
    }

}
