package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import java.awt.Color;

public class ColorPicker {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private float hue = 0.0f;
    private float saturation = 0.0f;
    private float brightness = 0.0f;
    private float alpha = 1.0f;
    private int currentColor = 0xFFFFFFFF;
    
    private boolean dragMainColorBox = false;
    private boolean dragHueSlider = false;
    
    private int x, y;
    private int width = 300;
    private int height = 280;
    private int mainColorBoxSize = 120;
    private int hueSelectorWidth = 20;
    private int paddingX = 15;
    private int paddingY = 15;
    
    private ColorChangeListener listener;
    private boolean closed = false;
    
    private int[] quickColors = {
        0xFFFFFFFF, 0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
        0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFFFF8800, 0xFF88FF00,
        0xFF0088FF, 0xFF8800FF, 0xFFFF4444, 0xFF44FF44, 0xFF4444FF,
        0xFFFFAA44, 0xFF44FFAA, 0xFFAA44FF, 0xFFFF8888, 0xFF88FF88
    };

    public interface ColorChangeListener {
        void onColorChanged(int color);
    }

    public ColorPicker(int x, int y, int initialColor) {
        this.x = x;
        this.y = y;
        setColor(initialColor);
    }

    public void setColor(int color) {
        this.currentColor = color;
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = ((color >> 24) & 0xFF) / 255.0f;
    }

    public int getColor() {
        return currentColor;
    }

    public void setListener(ColorChangeListener listener) {
        this.listener = listener;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isClosed() {
        return closed;
    }

    public void render(int mouseX, int mouseY) {
        int px = x;
        int py = y;


        drawRect(px - 8, py - 8, px + width + 8, py + height + 8, 0xCC000000);
        

        drawRect(px, py, px + width, py + height, 0xFF1A1A1A);
        drawRect(px, py, px + width, py + 3, 0xFF8A2BE2);

        
        mc.fontRendererObj.drawString("§5§lSelect Color", px + 20, py + 12, 0xFFFFFFFF);

        int startX = px + paddingX;
        int startY = py + paddingY + 20;
        int boxEndX = startX + mainColorBoxSize;
        int boxEndY = startY + mainColorBoxSize;

        // ===== Colorbox =====
        // 1. white
        drawRect(startX, startY, boxEndX, boxEndY, 0xFFFFFFFF);
        
        // 2. white to hue
        int hueColor = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        drawHorizontalGradient(startX, startY, boxEndX, boxEndY, 0xFFFFFFFF, hueColor);
        
        // 3. gradient
        drawVerticalGradient(startX, startY, boxEndX, boxEndY);

        
        int pickerX = startX + (int)(saturation * mainColorBoxSize);
        int pickerY = startY + (int)((1.0f - brightness) * mainColorBoxSize);
        drawCircle(pickerX, pickerY, 4, 0xFFFFFFFF);
        drawCircle(pickerX, pickerY, 3, 0xFF000000);

        // ===== HUE =====
        int hueX = boxEndX + 10;
        int hueY = startY;
        int hueHeight = mainColorBoxSize;

        for (int i = 0; i < hueHeight; i++) {
            float hueVal = (float) i / hueHeight;
            drawRect(hueX, hueY + i, hueX + hueSelectorWidth, hueY + i + 1, Color.HSBtoRGB(hueVal, 1.0f, 1.0f));
        }

        int indicatorY = hueY + (int)(hue * hueHeight);
        drawRect(hueX - 2, indicatorY - 2, hueX + hueSelectorWidth + 2, indicatorY + 2, 0xFFFFFFFF);
        drawRect(hueX - 1, indicatorY - 1, hueX + hueSelectorWidth + 1, indicatorY + 1, 0xFF000000);

        // ===== Quick color =====
        int quickStartX = hueX + hueSelectorWidth + 15;
        int quickSize = 16;
        int quickCols = 5;
        
        for (int i = 0; i < quickColors.length; i++) {
            int col = i % quickCols;
            int row = i / quickCols;
            int qx = quickStartX + col * (quickSize + 4);
            int qy = startY + row * (quickSize + 4);
            
            drawRect(qx, qy, qx + quickSize, qy + quickSize, quickColors[i]);
            drawRect(qx, qy, qx + quickSize, qy + 1, 0x33FFFFFF);
            drawRect(qx, qy, qx + 1, qy + quickSize, 0x33FFFFFF);
            
            if (mouseX >= qx && mouseX <= qx + quickSize && mouseY >= qy && mouseY <= qy + quickSize) {
                drawRect(qx - 2, qy - 2, qx + quickSize + 2, qy + quickSize + 2, 0x44FFFFFF);
            }
        }

        // ===== Picked color =====
        int previewY = startY + quickSize * 4 + 20;
        drawRect(startX, previewY, startX + mainColorBoxSize, previewY + 30, currentColor);
        drawRect(startX - 1, previewY - 1, startX + mainColorBoxSize + 1, previewY + 31, 0x33FFFFFF);
        
        Color color = new Color(currentColor);
        String rgbText = String.format("R:%d G:%d B:%d A:%d", 
            color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        mc.fontRendererObj.drawString(rgbText, startX, previewY + 35, 0xFFAAAAAA);
        
        // ===== close button =====
        int closeX = px + width - 35;
        int closeY = py + 8;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 27 && 
                               mouseY >= closeY && mouseY <= closeY + 27;
        
        drawRect(closeX, closeY, closeX + 27, closeY + 27, closeHovered ? 0x44FF4444 : 0x00000000);
        mc.fontRendererObj.drawString("✕", closeX + 8, closeY + 7, closeHovered ? 0xFFFF5555 : 0xFFFFFFFF);

        // ===== DRAG =====
        if (dragMainColorBox) {
            int relX = Math.max(0, Math.min(mainColorBoxSize, mouseX - startX));
            int relY = Math.max(0, Math.min(mainColorBoxSize, mouseY - startY));
            saturation = (float) relX / mainColorBoxSize;
            brightness = 1.0f - (float) relY / mainColorBoxSize;
            updateColor();
        }
        
        if (dragHueSlider) {
            int relY = Math.max(0, Math.min(hueHeight, mouseY - hueY));
            hue = (float) relY / hueHeight;
            updateColor();
        }

        if (!Mouse.isButtonDown(0)) {
            dragMainColorBox = false;
            dragHueSlider = false;
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;

        int px = x;
        int py = y;

        // close icon
        int closeX = px + width - 35;
        int closeY = py + 8;
        if (mouseX >= closeX && mouseX <= closeX + 27 && mouseY >= closeY && mouseY <= closeY + 27) {
            closed = true;
            return true;
        }

        int startX = px + paddingX;
        int startY = py + paddingY + 20;
        int boxEndX = startX + mainColorBoxSize;
        int boxEndY = startY + mainColorBoxSize;

        // Main colorbox
        if (mouseX >= startX && mouseX <= boxEndX && mouseY >= startY && mouseY <= boxEndY) {
            dragMainColorBox = true;
            int relX = Math.max(0, Math.min(mainColorBoxSize, mouseX - startX));
            int relY = Math.max(0, Math.min(mainColorBoxSize, mouseY - startY));
            saturation = (float) relX / mainColorBoxSize;
            brightness = 1.0f - (float) relY / mainColorBoxSize;
            updateColor();
            return false;
        }


        int hueX = boxEndX + 10;
        int hueY = startY;
        int hueHeight = mainColorBoxSize;
        if (mouseX >= hueX && mouseX <= hueX + hueSelectorWidth && mouseY >= hueY && mouseY <= hueY + hueHeight) {
            dragHueSlider = true;
            int relY = Math.max(0, Math.min(hueHeight, mouseY - hueY));
            hue = (float) relY / hueHeight;
            updateColor();
            return false;
        }

        //QUICK COLOR
        int quickStartX = hueX + hueSelectorWidth + 15;
        int quickSize = 16;
        int quickCols = 5;
        
        for (int i = 0; i < quickColors.length; i++) {
            int col = i % quickCols;
            int row = i / quickCols;
            int qx = quickStartX + col * (quickSize + 4);
            int qy = startY + row * (quickSize + 4);
            
            if (mouseX >= qx && mouseX <= qx + quickSize && mouseY >= qy && mouseY <= qy + quickSize) {
                int color = quickColors[i];
                setColor(color);
                if (listener != null) {
                    listener.onColorChanged(color);
                }
                return false;
            }
        }

        return false;
    }

    private void updateColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int alphaVal = (int)(alpha * 255);
        currentColor = (alphaVal << 24) | (rgb & 0x00FFFFFF);
        if (listener != null) {
            listener.onColorChanged(currentColor);
        }
    }

    // ========== DRAWINGS ==========


    private void drawRect(int left, int top, int right, int bottom, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glColor4f(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0).endVertex();
        worldRenderer.pos(right, bottom, 0).endVertex();
        worldRenderer.pos(right, top, 0).endVertex();
        worldRenderer.pos(left, top, 0).endVertex();
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawHorizontalGradient(int left, int top, int right, int bottom, int startColor, int endColor) {
        float sa = ((startColor >> 24) & 0xFF) / 255.0f;
        float sr = ((startColor >> 16) & 0xFF) / 255.0f;
        float sg = ((startColor >> 8) & 0xFF) / 255.0f;
        float sb = (startColor & 0xFF) / 255.0f;

        float ea = ((endColor >> 24) & 0xFF) / 255.0f;
        float er = ((endColor >> 16) & 0xFF) / 255.0f;
        float eg = ((endColor >> 8) & 0xFF) / 255.0f;
        float eb = (endColor & 0xFF) / 255.0f;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        worldRenderer.pos(right, top, 0).color(er, eg, eb, ea).endVertex();
        worldRenderer.pos(left, top, 0).color(sr, sg, sb, sa).endVertex();
        worldRenderer.pos(left, bottom, 0).color(sr, sg, sb, sa).endVertex();
        worldRenderer.pos(right, bottom, 0).color(er, eg, eb, ea).endVertex();

        tessellator.draw();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawVerticalGradient(int left, int top, int right, int bottom) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        worldRenderer.pos(right, top, 0).color(0f, 0f, 0f, 0f).endVertex();
        worldRenderer.pos(left, top, 0).color(0f, 0f, 0f, 0f).endVertex();
        worldRenderer.pos(left, bottom, 0).color(0f, 0f, 0f, 1f).endVertex();
        worldRenderer.pos(right, bottom, 0).color(0f, 0f, 0f, 1f).endVertex();

        tessellator.draw();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawCircle(int cx, int cy, int radius, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glColor4f(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(cx, cy, 0).endVertex();

        for (int i = 0; i <= 20; i++) {
            double angle = Math.PI * 2 * i / 20;
            worldRenderer.pos(cx + Math.sin(angle) * radius, cy + Math.cos(angle) * radius, 0).endVertex();
        }

        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
