package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class NelianRenderChunkFactory implements IRenderChunkFactory {
    
    @Override
    public RenderChunk makeRenderChunk(World world, RenderGlobal renderGlobal, BlockPos pos, int index) {
        return new NelianRenderChunk(world, renderGlobal, pos, index);
    }
}
