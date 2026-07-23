package net.minecraft.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class NelianRenderChunk extends RenderChunk {
    
    private static final float BACK_CULL_THRESHOLD = 0.0F;
    private static final float PARTIAL_CULL_THRESHOLD = 0.3F;
    private static final double MIN_DIST_FOR_CULL = 16.0D;
    private static final double MAX_DIST_FOR_CULL = 64.0D;
    
    private boolean isBackFaceCulled = false;
    private long lastCullCheckTime = 0;
    private static final long CULL_CHECK_INTERVAL = 50;
    
    private final RenderGlobal renderGlobal;
    private final int renderDistanceSq;
    
    public NelianRenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos pos, int index) {
        super(worldIn, renderGlobalIn, pos, index);
        this.renderGlobal = renderGlobalIn;
        int renderDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
        this.renderDistanceSq = renderDistance * renderDistance;
    }
    
    @Override
    public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator) {
        super.rebuildChunk(x, y, z, generator);
    }
    
    public boolean shouldRenderChunk(EntityPlayer player) {
        if (player == null) return true;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCullCheckTime > CULL_CHECK_INTERVAL) {
            this.lastCullCheckTime = currentTime;
            this.isBackFaceCulled = this.isChunkBehindCamera(player);
        }
        
        return !this.isBackFaceCulled;
    }
    
 
    private boolean isChunkBehindCamera(EntityPlayer player) {
        Vec3 lookVec = player.getLook(1.0F);
        BlockPos chunkPos = this.getPosition();
        
        double centerX = chunkPos.getX() + 8.0D;
        double centerY = chunkPos.getY() + 8.0D;
        double centerZ = chunkPos.getZ() + 8.0D;
        
        double eyeX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double eyeZ = player.posZ;
        
        double dx = centerX - eyeX;
        double dy = centerY - eyeY;
        double dz = centerZ - eyeZ;
        
        double distSq = dx*dx + dy*dy + dz*dz;
        
        if (distSq < MIN_DIST_FOR_CULL * MIN_DIST_FOR_CULL) {
            return false;
        }
        
        if (distSq > this.renderDistanceSq) {
            return true;
        }
        
        double dist = Math.sqrt(distSq);
        dx /= dist;
        dy /= dist;
        dz /= dist;
        
        double dot = dx * lookVec.xCoord + dy * lookVec.yCoord + dz * lookVec.zCoord;
        
        if (dot < BACK_CULL_THRESHOLD && dist > MIN_DIST_FOR_CULL) {
            return true;
        }
        
        if (dot < PARTIAL_CULL_THRESHOLD && dist > MAX_DIST_FOR_CULL) {
            return this.isPartialChunkBehindCamera(player, lookVec);
        }
        
        return false;
    }
    
    private boolean isPartialChunkBehindCamera(EntityPlayer player, Vec3 lookVec) {
        BlockPos chunkPos = this.getPosition();
        
        double[][] corners = {
            {chunkPos.getX(), chunkPos.getY(), chunkPos.getZ()},
            {chunkPos.getX() + 16, chunkPos.getY(), chunkPos.getZ()},
            {chunkPos.getX(), chunkPos.getY() + 16, chunkPos.getZ()},
            {chunkPos.getX(), chunkPos.getY(), chunkPos.getZ() + 16},
            {chunkPos.getX() + 16, chunkPos.getY() + 16, chunkPos.getZ()},
            {chunkPos.getX() + 16, chunkPos.getY(), chunkPos.getZ() + 16},
            {chunkPos.getX(), chunkPos.getY() + 16, chunkPos.getZ() + 16},
            {chunkPos.getX() + 16, chunkPos.getY() + 16, chunkPos.getZ() + 16}
        };
        
        int visibleCorners = 0;
        double eyeX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double eyeZ = player.posZ;
        
        for (double[] corner : corners) {
            double dx = corner[0] - eyeX;
            double dy = corner[1] - eyeY;
            double dz = corner[2] - eyeZ;
            
            double distSq = dx*dx + dy*dy + dz*dz;
            
            if (distSq < 4.0D) {
                visibleCorners++;
                continue;
            }
            
            double dist = Math.sqrt(distSq);
            dx /= dist;
            dy /= dist;
            dz /= dist;
            
            double dot = dx * lookVec.xCoord + dy * lookVec.yCoord + dz * lookVec.zCoord;
            
            if (dot > BACK_CULL_THRESHOLD) {
                visibleCorners++;
            }
        }
        
        return visibleCorners == 0;
    }
    
    public boolean shouldRenderBlock(BlockPos blockPos, EntityPlayer player) {
        if (player == null) return true;
        
        BlockPos chunkPos = this.getPosition();
        int localX = blockPos.getX() - chunkPos.getX();
        int localY = blockPos.getY() - chunkPos.getY();
        int localZ = blockPos.getZ() - chunkPos.getZ();
        
        double bx = blockPos.getX() + 0.5D;
        double by = blockPos.getY() + 0.5D;
        double bz = blockPos.getZ() + 0.5D;
        
        Vec3 lookVec = player.getLook(1.0F);
        double eyeX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double eyeZ = player.posZ;
        
        double dx = bx - eyeX;
        double dy = by - eyeY;
        double dz = bz - eyeZ;
        
        double distSq = dx*dx + dy*dy + dz*dz;
        
        if (distSq < 4.0D) {
            return true;
        }
        
        if (distSq > 64.0D * 64.0D) {
            return false;
        }
        
        double dist = Math.sqrt(distSq);
        dx /= dist;
        dy /= dist;
        dz /= dist;
        
        double dot = dx * lookVec.xCoord + dy * lookVec.yCoord + dz * lookVec.zCoord;
        
        float blockThreshold = -0.1F;
        
        if (localX == 0 || localX == 15 || localY == 0 || localY == 15 || localZ == 0 || localZ == 15) {
            blockThreshold = 0.1F;
        }
        
        return dot >= blockThreshold;
    }
    
    @Override
    public CompiledChunk getCompiledChunk() {
        CompiledChunk compiled = super.getCompiledChunk();
        
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && this.isBackFaceCulled) {
            return CompiledChunk.DUMMY;
        }
        
        return compiled;
    }
}
