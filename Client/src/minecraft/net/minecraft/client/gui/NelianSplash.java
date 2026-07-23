package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

public class NelianSplash
{
    private static final int NELIAN_PIXEL_SIZE = 10;
    private static final int NELIAN_SPACING = 3;
    private static final int NELIAN_LETTER_COUNT = 6;
    private static final int NELIAN_TOTAL_HEIGHT = 5 * NELIAN_PIXEL_SIZE;
    
    private Timer timer = new Timer(20.0F);
    private int displayWidth;
    private int displayHeight;
    
    private void drawEarlySplashScreen() 
    {
        float red = 255.0F / 255.0F;
        float green = 100.0F / 255.0F;
        float blue = 100.0F / 255.0F;

        GlStateManager.clearColor(red, green, blue, 1.0F);
        GlStateManager.clear(16640);
        this.updateDisplay();
    }

    private void drawSplashScreen(TextureManager textureManagerInstance, GuiScreen backgroundScreen) throws LWJGLException 
    {
        ScaledResolution scaledresolution = new ScaledResolution(this);
        int i = scaledresolution.getScaleFactor();
        int scaledW = scaledresolution.getScaledWidth();
        int scaledH = scaledresolution.getScaledHeight();
        Framebuffer framebuffer = new Framebuffer(scaledW * i, scaledH * i, true);
        boolean soundPlayed = false;
        long splashDuration = 6000L;
        long crossfadeDuration = 1000L;
        long totalDuration = splashDuration + crossfadeDuration;
        long animStart = System.currentTimeMillis();
        boolean cursorReleased = false;
        Mouse.setGrabbed(true);

        long moveDuration = 1000L;
        long moveStart = totalDuration - moveDuration;
        final float startPixelSize = 10.0F;
        final float startSpacing = 3.0F;
        final float targetPixelSize = 7.0F;
        final float targetSpacing = 5.0F;
        float targetCenterX = scaledW / 2.0F;
        float targetHeight = 5 * targetPixelSize;
        float targetCenterY = 45.0F + targetHeight / 2.0F;
        long nelianAppearStart = splashDuration + 1;

        while (true) 
        {
            long elapsed = System.currentTimeMillis() - animStart;
            if (elapsed >= totalDuration) break;

            if (!cursorReleased && elapsed >= splashDuration) 
            {
                cursorReleased = true;
                Mouse.setGrabbed(false);
                Mouse.setCursorPosition(this.displayWidth / 2, this.displayHeight / 2);
                if (!soundPlayed) 
                {
                    soundPlayed = true;
                    playSplashEndSound();
                }
            }

            float splashAlpha = 1.0F;
            if (elapsed >= splashDuration) 
            {
                splashAlpha = 1.0F - (float)(elapsed - splashDuration) / (float)crossfadeDuration;
                splashAlpha = MathHelper.clamp_float(splashAlpha, 0.0F, 1.0F);
            }

            float nelianMainMenuAlpha = 0.0F;
            if (elapsed >= nelianAppearStart) 
            {
                float totalFadeTime = crossfadeDuration + 500L;
                nelianMainMenuAlpha = (float)(elapsed - nelianAppearStart) / totalFadeTime;
                nelianMainMenuAlpha = MathHelper.clamp_float(nelianMainMenuAlpha, 0.0F, 1.0F);
            }

            framebuffer.bindFramebuffer(false);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, scaledW, scaledH, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

            if (backgroundScreen != null && splashAlpha < 1.0F) 
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                backgroundScreen.drawScreen(0, 0, this.timer.renderPartialTicks);
            }

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableTexture2D();

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(0.0D, (double)scaledH, 0.0D).color(255, 100, 100, (int)(255 * splashAlpha)).endVertex();
            worldrenderer.pos((double)scaledW, (double)scaledH, 0.0D).color(255, 100, 100, (int)(255 * splashAlpha)).endVertex();
            worldrenderer.pos((double)scaledW, 0.0D, 0.0D).color(255, 100, 100, (int)(255 * splashAlpha)).endVertex();
            worldrenderer.pos(0.0D, 0.0D, 0.0D).color(255, 100, 100, (int)(255 * splashAlpha)).endVertex();
            tessellator.draw();

            float textAnimDuration = 700L;
            float textT = MathHelper.clamp_float((float)elapsed / textAnimDuration, 0.0F, 1.0F);
            float introEased = easeOutBack(textT);
            float introAlpha = MathHelper.clamp_float(textT * 1.6F, 0.0F, 1.0F);

            long spinnerDelay = 2200L;
            long spinnerFadeIn = 400L;
            float spinnerAlpha = 0.0F;
            if (elapsed > spinnerDelay) 
            {
                spinnerAlpha = MathHelper.clamp_float((float)(elapsed - spinnerDelay) / (float)spinnerFadeIn, 0.0F, 1.0F);
            }
            spinnerAlpha *= splashAlpha;

            float nelianCenterX = scaledW / 2.0F;
            float nelianCenterY = scaledH / 2.0F;
            float nelianPixelSize = startPixelSize * introEased;
            float nelianSpacing = startSpacing * introEased;
            float nelianAlpha = introAlpha;

            if (elapsed >= moveStart) 
            {
                float moveT = MathHelper.clamp_float((float)(elapsed - moveStart) / (float)moveDuration, 0.0F, 1.0F);
                float moveEased = easeInOutCubic(moveT);

                nelianCenterX = (scaledW / 2.0F) + (targetCenterX - scaledW / 2.0F) * moveEased;
                nelianCenterY = (scaledH / 2.0F) + (targetCenterY - scaledH / 2.0F) * moveEased;
                nelianPixelSize = startPixelSize + (targetPixelSize - startPixelSize) * moveEased;
                nelianSpacing = startSpacing + (targetSpacing - startSpacing) * moveEased;
                nelianAlpha = 1.0F;
            }

            if (splashAlpha > 0.0F) 
            {
                this.drawNelianText(nelianCenterX, nelianCenterY, nelianAlpha * splashAlpha, nelianPixelSize, nelianSpacing);
            }

            if (spinnerAlpha > 0.0F) 
            {
                float spinnerCenterX = scaledW / 2.0F;
                float spinnerCenterY = scaledH / 2.0F + 160.0F;
                this.drawLoadingSpinner(spinnerCenterX, spinnerCenterY, 10.0F, elapsed - spinnerDelay, spinnerAlpha);
            }

            if (backgroundScreen instanceof GuiMainMenu && nelianMainMenuAlpha > 0.0F) 
            {
                GuiMainMenu mainMenu = (GuiMainMenu) backgroundScreen;
                GlStateManager.enableTexture2D();
                
                if (!mainMenu.isNelianTextureCreated()) 
                {
                    mainMenu.createNelianTexture();
                }
                
                mainMenu.drawNelianTitleWithAlpha(nelianMainMenuAlpha);
                GlStateManager.disableTexture2D();
            }

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            framebuffer.unbindFramebuffer();
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, (double)(scaledW * i), (double)(scaledH * i), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            framebuffer.framebufferRender(scaledW * i, scaledH * i);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1F);
            this.updateDisplay();

            try { Thread.sleep(8L); } catch (InterruptedException ignored) { }
        }

        if (backgroundScreen != null) 
        {
            this.currentScreen = backgroundScreen;
        }
    }

    private void drawLoadingSpinner(float centerX, float centerY, float radius, long elapsedMs, float alpha)
    {
        float cycleMs = 1333.0F;
        float t = (elapsedMs % (long)cycleMs) / cycleMs;
        long cycleCount = elapsedMs / (long)cycleMs;

        float baseRotation = (elapsedMs / 2000.0F) * 360.0F;

        float minArc = 15.0F;
        float maxArc = 280.0F;
        float deltaArc = maxArc - minArc;
        float cycleOffset = cycleCount * deltaArc;

        float startAngle;
        float arcLength;

        if (t < 0.5F)
        {
            float ease = easeInOutCubic(t * 2.0F);
            arcLength = minArc + deltaArc * ease;
            startAngle = baseRotation + cycleOffset;
        }
        else
        {
            float ease = easeInOutCubic((t - 0.5F) * 2.0F);
            arcLength = maxArc - deltaArc * ease;
            startAngle = baseRotation + cycleOffset + deltaArc * ease;
        }

        float thickness = 2.5F;
        float innerRadius = radius - thickness;
        float outerRadius = radius;

        int alphaByte = (int)(MathHelper.clamp_float(alpha, 0.0F, 1.0F) * 255.0F);
        int segments = Math.max(2, (int)(arcLength / 4.0F));
        float angleStep = arcLength / (float)segments;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);
        for (int s = 0; s <= segments; s++)
        {
            float angleDeg = startAngle + angleStep * s;
            double angleRad = Math.toRadians(angleDeg);
            float cos = (float)Math.cos(angleRad);
            float sin = (float)Math.sin(angleRad);

            worldrenderer.pos(centerX + cos * outerRadius, centerY + sin * outerRadius, 0.0D)
                          .color(255, 255, 255, alphaByte).endVertex();
            worldrenderer.pos(centerX + cos * innerRadius, centerY + sin * innerRadius, 0.0D)
                          .color(255, 255, 255, alphaByte).endVertex();
        }
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void drawNelianText(float centerX, float centerY, float alpha, float pixelSize, float spacing)
    {
        int[][][] letters = {
            {
                {1,0,0,0,1},
                {1,1,0,0,1},
                {1,0,1,0,1},
                {1,0,0,1,1},
                {1,0,0,0,1}
            },
            {
                {1,1,1,1,1},
                {1,0,0,0,0},
                {1,1,1,1,0},
                {1,0,0,0,0},
                {1,1,1,1,1}
            },
            {
                {1,0,0,0,0},
                {1,0,0,0,0},
                {1,0,0,0,0},
                {1,0,0,0,0},
                {1,1,1,1,1}
            },
            {
                {1,1,1,1,1},
                {0,0,1,0,0},
                {0,0,1,0,0},
                {0,0,1,0,0},
                {1,1,1,1,1}
            },
            {
                {0,1,1,1,0},
                {1,0,0,0,1},
                {1,1,1,1,1},
                {1,0,0,0,1},
                {1,0,0,0,1}
            },
            {
                {1,0,0,0,1},
                {1,1,0,0,1},
                {1,0,1,0,1},
                {1,0,0,1,1},
                {1,0,0,0,1}
            }
        };

        float totalWidth = letters.length * (5 * pixelSize + spacing) - spacing;
        float totalHeight = 5 * pixelSize;

        float startX = centerX - totalWidth / 2.0F;
        float startY = centerY - totalHeight / 2.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        int alphaByte = (int)(MathHelper.clamp_float(alpha, 0.0F, 1.0F) * 255.0F);

        float currentX = startX;
        for (int letter = 0; letter < letters.length; letter++)
        {
            for (int row = 0; row < 5; row++)
            {
                for (int col = 0; col < 5; col++)
                {
                    if (letters[letter][row][col] == 1)
                    {
                        float x = currentX + col * pixelSize;
                        float y = startY + row * pixelSize;

                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        worldrenderer.pos(x, y + pixelSize, 0.0D).color(255, 255, 255, alphaByte).endVertex();
                        worldrenderer.pos(x + pixelSize, y + pixelSize, 0.0D).color(255, 255, 255, alphaByte).endVertex();
                        worldrenderer.pos(x + pixelSize, y, 0.0D).color(255, 255, 255, alphaByte).endVertex();
                        worldrenderer.pos(x, y, 0.0D).color(255, 255, 255, alphaByte).endVertex();
                        tessellator.draw();
                    }
                }
            }
            currentX += 5 * pixelSize + spacing;
        }
    }

    private float easeInOutCubic(float t) 
    {
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0D) / 2.0F;
    }

    private static float easeOutBack(float t)
    {
        float c1 = 1.4F;
        float c3 = c1 + 1.0F;
        float x = t - 1.0F;
        return 1.0F + c3 * x * x * x + c1 * x * x;
    }

    private void playSplashEndSound() 
    {
        try 
        {
            if (this.mcSoundHandler != null) 
            {
                ResourceLocation soundLocation = new ResourceLocation("random.levelup");
                ISound sound = PositionedSoundRecord.create(soundLocation);
                this.mcSoundHandler.playSound(sound);
            }
        } 
        catch (Exception e) 
        {
        }
    }

    private void updateDisplay()
    {
    }

    private static class Timer 
    {
        public float renderPartialTicks;
        
        public Timer(float ticksPerSecond) 
        {
        }
    }
    
    private interface ISound {}
    private static class PositionedSoundRecord 
    {
        public static ISound create(ResourceLocation location) 
        {
            return null;
        }
    }
    
    private class SoundHandler 
    {
        public void playSound(ISound sound) {}
    }
    
    private SoundHandler mcSoundHandler;
    private GuiScreen currentScreen;
}
