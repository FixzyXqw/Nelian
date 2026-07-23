package net.minecraft.client.renderer.culling;

import net.minecraft.util.AxisAlignedBB;

public class Frustum implements ICamera
{
    private ClippingHelper clippingHelper;
    private double xPosition;
    private double yPosition;
    private double zPosition;


    private double lookX, lookY, lookZ;
    private final double backCullThreshold = -0.15D; 
    

    private final double minCullDistSq = 64.0D; 
    // ----------------------------------------------------------

    public Frustum()
    {
        this(ClippingHelperImpl.getInstance());
    }

    public Frustum(ClippingHelper p_i46196_1_)
    {
        this.clippingHelper = p_i46196_1_;
    }

    public void setPosition(double p_78547_1_, double p_78547_3_, double p_78547_5_)
    {
        this.xPosition = p_78547_1_;
        this.yPosition = p_78547_3_;
        this.zPosition = p_78547_5_;
    }


    public void setLookVector(double x, double y, double z) {
        this.lookX = x;
        this.lookY = y;
        this.lookZ = z;
    }

    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {

        double centerX = (minX + maxX) * 0.5D - this.xPosition;
        double centerY = (minY + maxY) * 0.5D - this.yPosition;
        double centerZ = (minZ + maxZ) * 0.5D - this.zPosition;

        double distSq = centerX * centerX + centerY * centerY + centerZ * centerZ;


        if (distSq > minCullDistSq) {
            double dot = centerX * this.lookX + centerY * this.lookY + centerZ * this.lookZ;
            
            if (dot < backCullThreshold * distSq) {
                return false; 
            }
        }


        return this.clippingHelper.isBoxInFrustum(
            minX - this.xPosition, minY - this.yPosition, minZ - this.zPosition, 
            maxX - this.xPosition, maxY - this.yPosition, maxZ - this.zPosition
        );
    }

    public boolean isBoundingBoxInFrustum(AxisAlignedBB p_78546_1_)
    {
        return this.isBoxInFrustum(p_78546_1_.minX, p_78546_1_.minY, p_78546_1_.minZ, p_78546_1_.maxX, p_78546_1_.maxY, p_78546_1_.maxZ);
    }

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return this.clippingHelper.isBoxInFrustumFully(
            minX - this.xPosition, minY - this.yPosition, minZ - this.zPosition, 
            maxX - this.xPosition, maxY - this.yPosition, maxZ - this.zPosition
        );
    }
}
