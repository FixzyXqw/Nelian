package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Nelian Client - Lunar Style Modern Premium UI
 * Advanced animations, modern gradients, premium design
 */
public class GuiNelianOverlay extends GuiScreen {

    // ========== MOD STATES ==========
    public static boolean hitboxesEnabled   = false;
    public static boolean crosshairEnabled  = false;
    public static boolean nametagsEnabled   = true;
    public static boolean fpsEnabled        = true;
    public static boolean cpsEnabled        = false;
    public static boolean freelookEnabled   = false;
    public static boolean shakeEnabled      = false;

    // ========== PREMIUM COLOR PALETTE (Lunar-inspired) ==========
    private static final int C_BG_DARK      = 0xFF0D0D12;
    private static final int C_PANEL_BG     = 0xFF131820;
    private static final int C_CARD_BG      = 0xFF1A202C;
    private static final int C_CARD_HOV     = 0xFF252F3F;
    private static final int C_PRIMARY      = 0xFF00D4FF;
    private static final int C_SECONDARY    = 0xFF7C3AED;
    private static final int C_ACCENT       = 0xFFFF6B9D;
    private static final int C_SUCCESS      = 0xFF10B981;
    private static final int C_DANGER       = 0xFFEF4444;
    private static final int C_TEXT_PRIMARY = 0xFFFAFAFA;
    private static final int C_TEXT_SECONDARY = 0xFFA0AEC0;
    private static final int C_BORDER       = 0x1AFFFFFF;
    private static final int C_SHADOW       = 0x80000000;

    // ========== MODERN LAYOUT ==========
    private static final int PANEL_W        = 820;
    private static final int PANEL_H        = 560;
    private static final int HEADER_H       = 80;
    private static final int CONTENT_PAD    = 24;
    private static final int CORNER_R       = 20;
    private static final int CARD_R         = 14;
    
    // Card grid layout
    private static final int CARDS_PER_ROW  = 2;
    private static final int CARD_W         = 340;
    private static final int CARD_H         = 140;
    private static final int CARD_GAP       = 20;
    
    private static final int TOGGLE_W       = 56;
    private static final int TOGGLE_H       = 32;
    private static final int SCROLL_W       = 6;

    // ========== ANIMATION & STATE ==========
    private float scrollOffset              = 0f;
    private float targetScroll              = 0f;
    private float maxScroll                 = 0f;
    private boolean draggingScrollbar       = false;

    private long openTime;
    private float animProgress              = 0f;
    private static final int ANIM_MS        = 500;
    
    private int hoveredCard                 = -1;
    private boolean hoverClose              = false;
    
    private float[] cardAnimProgress;
    private float[] cardHoverProgress;

    // ========== MOD LIST ==========
    private final List<ModItem> mods        = new ArrayList<>();
    private int selectedCategory            = 0; // 0 = All, 1 = Utility, etc.

    public GuiNelianOverlay() {
        Nelianoptions.load();
        syncFromOptions();

        mods.add(new ModItem("Hitboxes", "Show entity collision boxes for precise combat", 0, "🎯", 0));
        mods.add(new ModItem("Crosshair", "Custom crosshair with advanced customization", 1, "✦", 0));
        mods.add(new ModItem("Nametags", "Enhanced player name tags and information", 2, "👤", 0));
        mods.add(new ModItem("FPS Counter", "Real-time FPS display with history graph", 3, "⚡", 1));
        mods.add(new ModItem("CPS Counter", "Clicks per second tracking and statistics", 4, "◆", 1));
        mods.add(new ModItem("Freelook", "Free camera movement without turning body", 5, "📷", 2));
        mods.add(new ModItem("Shake Reducer", "Minimize knockback animation intensity", 6, "🛡️", 2));

        openTime = System.currentTimeMillis();
        cardAnimProgress = new float[mods.size()];
        cardHoverProgress = new float[mods.size()];
    }

    public static void syncFromOptions() {
        hitboxesEnabled   = Nelianoptions.hitboxesEnabled;
        crosshairEnabled  = Nelianoptions.crosshairEnabled;
        nametagsEnabled   = Nelianoptions.nametagsEnabled;
        fpsEnabled        = Nelianoptions.fpsEnabled;
        cpsEnabled        = Nelianoptions.cpsEnabled;
        freelookEnabled   = Nelianoptions.freelookEnabled;
        shakeEnabled      = Nelianoptions.minimalizeShakeEnabled;
    }

    private int getContentHeight() {
        int rows = (int)Math.ceil((double)mods.size() / CARDS_PER_ROW);
        return rows * (CARD_H + CARD_GAP) + 40;
    }

    private int panelX() { return (width - PANEL_W) / 2; }
    private int panelY() { return (height - PANEL_H) / 2; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Smooth entrance animation
        float t = Math.min(1f, (System.currentTimeMillis() - openTime) / (float) ANIM_MS);
        animProgress += (t - animProgress) * 0.12f;
        float easeOut = 1f - (float)Math.pow(1f - animProgress, 3);
        float scale = 0.85f + 0.15f * easeOut;
        float alpha = Math.min(1f, animProgress * 1.2f);

        int px = panelX(), py = panelY();
        int contentTop = py + HEADER_H + CONTENT_PAD;
        int contentHeight = PANEL_H - HEADER_H - CONTENT_PAD * 2;
        float totalH = getContentHeight();
        maxScroll = Math.max(0, totalH - contentHeight);

        scrollOffset += (targetScroll - scrollOffset) * 0.18f;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

        // Dark background with vignette
        drawVignetteBackground(width, height, alpha);

        GlStateManager.pushMatrix();
        float cx = px + PANEL_W * 0.5f, cy = py + PANEL_H * 0.5f;
        GlStateManager.translate(cx, cy, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-cx, -cy, 0);

        // Premium shadow with blur effect
        drawPremiumShadow(px, py, PANEL_W, PANEL_H, (int)(alpha * 255));

        // Main panel background with gradient
        drawPanelBackground(px, py, PANEL_W, PANEL_H, alpha);

        // Accent top border
        drawTopAccentBar(px, py, PANEL_W, alpha);

        // ---- HEADER ----
        drawHeader(px, py, mouseX, mouseY, alpha);

        // ---- CONTENT WITH SCISSOR ----
        int listX = px + CONTENT_PAD;
        int listW = PANEL_W - CONTENT_PAD * 2 - (maxScroll > 0 ? SCROLL_W + 12 : 0);
        enableScissor(listX, contentTop, listW, contentHeight);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffset, 0);

        drawCards(listX, listW, contentTop, mouseX, mouseY, alpha);

        GlStateManager.popMatrix();
        disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            drawScrollbar(px, py, contentTop, contentHeight, alpha);
        }

        // Footer
        drawFooter(px, py, alpha);

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawVignetteBackground(int w, int h, float alpha) {
        // Dark overlay
        drawRect(0, 0, w, h, applyAlpha(0xFF000000, alpha * 0.65f));
        
        // Vignette effect
        drawRadialGradient(w / 2f, h / 2f, 0, w / 2f, 
            applyAlpha(0x00000000, 0),
            applyAlpha(0xFF000000, alpha * 0.3f));
    }

    private void drawPanelBackground(int x, int y, int w, int h, float alpha) {
        // Main panel with subtle gradient
        drawRoundedRect(x, y, x + w, y + h, CORNER_R, applyAlpha(C_PANEL_BG, alpha));
        
        // Subtle border glow
        drawRoundedRectBorder(x, y, x + w, y + h, CORNER_R, applyAlpha(C_BORDER, alpha * 0.5f));
        
        // Inner highlight for premium feel
        drawRoundedRect(x + 1, y + 1, x + w - 1, y + 3, CORNER_R, 
            applyAlpha(0x18FFFFFF, alpha * 0.6f));
    }

    private void drawTopAccentBar(int x, int y, int w, float alpha) {
        // Gradient accent bar
        drawHorizontalGradient(x + CORNER_R, y + 2, x + w - CORNER_R, y + 6,
            applyAlpha(C_PRIMARY, alpha), applyAlpha(C_SECONDARY, alpha));
    }

    private void drawHeader(int x, int y, int mx, int my, float alpha) {
        int headerY = y + HEADER_H / 2;
        
        // Logo with glow
        drawTextWithGlow("✦ NELIAN", x + CONTENT_PAD, headerY - 12, applyAlpha(C_TEXT_PRIMARY, alpha), 2, alpha);
        fontRendererObj.drawString("Lunar-Style Client", x + CONTENT_PAD, headerY + 6, applyAlpha(C_TEXT_SECONDARY, alpha * 0.8f));
        
        // Stats badge with gradient background
        int enabledCount = countEnabled();
        String stats = enabledCount + "/" + mods.size() + " ACTIVE";
        int badgeW = fontRendererObj.getStringWidth(stats) + 20;
        int badgeX = x + PANEL_W - CONTENT_PAD - badgeW - 50;
        
        drawRoundedRect(badgeX - 2, headerY - 14, badgeX + badgeW + 2, headerY + 14, 10, 
            applyAlpha(C_SECONDARY, alpha * 0.15f));
        fontRendererObj.drawString(stats, badgeX + 10, headerY - 4, applyAlpha(C_PRIMARY, alpha));
        
        // Close button with premium styling
        int closeX = x + PANEL_W - CONTENT_PAD - 35, closeY = y + 18, closeSz = 32;
        hoverClose = mx >= closeX && mx <= closeX + closeSz && my >= closeY && my <= closeY + closeSz;
        
        if (hoverClose) {
            drawRoundedRect(closeX - 2, closeY - 2, closeX + closeSz + 2, closeY + closeSz + 2, 10,
                applyAlpha(C_DANGER, alpha * 0.2f));
        }
        drawRoundedRect(closeX, closeY, closeX + closeSz, closeY + closeSz, 10,
            applyAlpha(hoverClose ? C_DANGER : 0x18FFFFFF, alpha));
        fontRendererObj.drawStringWithShadow("✕", closeX + closeSz / 2 - 3, closeY + closeSz / 2 - 5,
            applyAlpha(hoverClose ? 0xFFFFFFFF : C_TEXT_SECONDARY, alpha));
        
        // Separator line
        drawRect(x + CONTENT_PAD, y + HEADER_H - 1, x + PANEL_W - CONTENT_PAD, y + HEADER_H,
            applyAlpha(0x1AFFFFFF, alpha * 0.3f));
    }

    private void drawCards(int listX, int listW, int contentTop, int mx, int my, float alpha) {
        hoveredCard = -1;
        int cols = CARDS_PER_ROW;
        int cardW = (listW - (cols - 1) * CARD_GAP) / cols;
        int totalRowW = cols * cardW + (cols - 1) * CARD_GAP;
        int offsetX = (listW - totalRowW) / 2;

        for (int i = 0; i < mods.size(); i++) {
            ModItem mod = mods.get(i);
            int row = i / cols;
            int col = i % cols;
            int cardX = listX + offsetX + col * (cardW + CARD_GAP);
            int cardY = contentTop + 16 + row * (CARD_H + CARD_GAP);
            boolean enabled = getModValue(mod.id);

            boolean cardHov = mx >= cardX && mx <= cardX + cardW &&
                    my + scrollOffset >= cardY && my + scrollOffset <= cardY + CARD_H;
            if (cardHov) hoveredCard = i;

            // Smooth hover animation
            cardHoverProgress[i] += (cardHov ? 1f : -1f) * 0.15f;
            cardHoverProgress[i] = Math.max(0, Math.min(1, cardHoverProgress[i]));

            drawModCard(cardX, cardY, cardW, CARD_H, mod, enabled, cardHoverProgress[i], alpha);
        }
    }

    private void drawModCard(int x, int y, int w, int h, ModItem mod, boolean enabled, float hoverProg, float alpha) {
        // Card background with hover effect
        int bgColor = lerpColor(C_CARD_BG, C_CARD_HOV, hoverProg);
        drawRoundedRect(x, y, x + w, y + h, CARD_R, applyAlpha(bgColor, alpha));
        
        // Border glow based on state
        int borderColor = enabled ? lerpColor(C_BORDER, C_PRIMARY, hoverProg) : C_BORDER;
        drawRoundedRectBorder(x, y, x + w, y + h, CARD_R, applyAlpha(borderColor, alpha * (0.4f + hoverProg * 0.4f)));
        
        // Hover glow
        if (hoverProg > 0) {
            drawRoundedRect(x - 1, y - 1, x + w + 1, y + h + 1, CARD_R, 
                applyAlpha(enabled ? C_PRIMARY : C_SECONDARY, alpha * hoverProg * 0.1f));
        }

        // Icon background
        int iconBgColor = enabled ? applyAlpha(C_PRIMARY, alpha * 0.15f) : applyAlpha(0x18FFFFFF, alpha);
        drawRoundedRect(x + 16, y + 16, x + 60, y + 60, 10, iconBgColor);
        
        // Icon with glow
        fontRendererObj.drawString(mod.icon, x + 35, y + 28, 
            applyAlpha(enabled ? C_PRIMARY : C_TEXT_SECONDARY, alpha));

        // Text content
        fontRendererObj.drawStringWithShadow(mod.name, x + 70, y + 20, applyAlpha(C_TEXT_PRIMARY, alpha));
        fontRendererObj.drawString(mod.desc, x + 70, y + 35, applyAlpha(C_TEXT_SECONDARY, alpha * 0.8f));

        // Toggle button
        int toggleX = x + w - TOGGLE_W - 16;
        int toggleY = y + (h - TOGGLE_H) / 2;
        drawPremiumToggle(toggleX, toggleY, enabled, alpha, hoverProg > 0.5f);
    }

    private void drawPremiumToggle(int x, int y, boolean enabled, float alpha, boolean hovered) {
        // Background - animated color change
        int bgColor = enabled ? C_SUCCESS : C_DANGER;
        drawRoundedRect(x, y, x + TOGGLE_W, y + TOGGLE_H, TOGGLE_H / 2, 
            applyAlpha(bgColor, alpha * 0.25f));
        
        // Border
        drawRoundedRectBorder(x, y, x + TOGGLE_W, y + TOGGLE_H, TOGGLE_H / 2, 
            applyAlpha(bgColor, alpha * (0.5f + (hovered ? 0.3f : 0f))));
        
        // Knob with shadow
        int knobR = (TOGGLE_H - 6) / 2;
        int knobX = enabled ? x + TOGGLE_W - knobR - 4 : x + knobR + 4;
        int knobY = y + TOGGLE_H / 2;
        
        // Knob shadow
        drawFilledCircle(knobX + 1, knobY + 1, knobR, applyAlpha(0x60000000, alpha));
        // Knob
        drawFilledCircle(knobX, knobY, knobR, applyAlpha(0xFFFFFFFF, alpha));
        // Knob highlight
        drawFilledCircle(knobX - 2, knobY - 2, knobR / 2, applyAlpha(0x40FFFFFF, alpha));
    }

    private void drawScrollbar(int px, int py, int contentTop, int contentHeight, float alpha) {
        int scrollX = px + PANEL_W - SCROLL_W - 12;
        int scrollY = contentTop;
        int scrollH = contentHeight;
        
        // Track
        drawRoundedRect(scrollX, scrollY, scrollX + SCROLL_W, scrollY + scrollH, SCROLL_W / 2, 
            applyAlpha(0x18FFFFFF, alpha * 0.3f));
        
        // Thumb
        float totalH = getContentHeight();
        float thumbRatio = Math.min(1f, contentHeight / totalH);
        int thumbH = Math.max(24, (int)(contentHeight * thumbRatio));
        int thumbY = scrollY + (int)((scrollOffset / maxScroll) * (contentHeight - thumbH));
        
        drawRoundedRect(scrollX, thumbY, scrollX + SCROLL_W, thumbY + thumbH, SCROLL_W / 2, 
            applyAlpha(C_PRIMARY, alpha * 0.7f));
    }

    private void drawFooter(int x, int y, float alpha) {
        int footerY = y + PANEL_H - 28;
        drawRect(x + CONTENT_PAD, footerY, x + PANEL_W - CONTENT_PAD, footerY + 1, 
            applyAlpha(0x1AFFFFFF, alpha * 0.2f));
        
        String hint = "Press ESC or RSHIFT to close";
        int hintW = fontRendererObj.getStringWidth(hint);
        fontRendererObj.drawString(hint, x + (PANEL_W - hintW) / 2, footerY + 6, 
            applyAlpha(C_TEXT_SECONDARY, alpha * 0.5f));
    }

    private int countEnabled() {
        int c = 0;
        for (ModItem m : mods) if (getModValue(m.id)) c++;
        return c;
    }

    private void enableScissor(int x, int y, int w, int h) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        double scale = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(x * scale), (int)((Minecraft.getMinecraft().displayHeight - (y + h) * scale)), 
                       (int)(w * scale), (int)(h * scale));
    }

    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            targetScroll -= Math.signum(wheel) * 25;
            targetScroll = Math.max(0, Math.min(maxScroll, targetScroll));
        }
    }

    private boolean getModValue(int id) {
        switch (id) {
            case 0: return Nelianoptions.hitboxesEnabled;
            case 1: return Nelianoptions.crosshairEnabled;
            case 2: return Nelianoptions.nametagsEnabled;
            case 3: return Nelianoptions.fpsEnabled;
            case 4: return Nelianoptions.cpsEnabled;
            case 5: return Nelianoptions.freelookEnabled;
            case 6: return Nelianoptions.minimalizeShakeEnabled;
            default: return false;
        }
    }

    private void setModValue(int id, boolean value) {
        switch (id) {
            case 0: Nelianoptions.hitboxesEnabled = value; hitboxesEnabled = value; break;
            case 1: Nelianoptions.crosshairEnabled = value; crosshairEnabled = value; break;
            case 2: Nelianoptions.nametagsEnabled = value; nametagsEnabled = value; break;
            case 3: Nelianoptions.fpsEnabled = value; fpsEnabled = value; break;
            case 4: Nelianoptions.cpsEnabled = value; cpsEnabled = value; break;
            case 5: Nelianoptions.freelookEnabled = value; freelookEnabled = value; break;
            case 6: Nelianoptions.minimalizeShakeEnabled = value; shakeEnabled = value; break;
        }
        Nelianoptions.save();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int px = panelX(), py = panelY();

            // Close button
            int closeX = px + PANEL_W - CONTENT_PAD - 35, closeY = py + 18, closeSz = 32;
            if (mouseX >= closeX && mouseX <= closeX + closeSz && mouseY >= closeY && mouseY <= closeY + closeSz) {
                Minecraft.getMinecraft().displayGuiScreen(null);
                return;
            }

            int contentTop = py + HEADER_H + CONTENT_PAD;
            int contentHeight = PANEL_H - HEADER_H - CONTENT_PAD * 2;

            // Scrollbar
            if (maxScroll > 0) {
                int scrollX = px + PANEL_W - SCROLL_W - 12;
                if (mouseX >= scrollX && mouseX <= scrollX + SCROLL_W && mouseY >= contentTop && mouseY <= contentTop + contentHeight) {
                    draggingScrollbar = true;
                    updateScrollFromMouse(mouseY, contentTop, contentHeight);
                    return;
                }
            }

            int mouseYAdj = (int)(mouseY + scrollOffset);
            int listX = px + CONTENT_PAD;
            int listW = PANEL_W - CONTENT_PAD * 2 - (maxScroll > 0 ? SCROLL_W + 12 : 0);
            int startY = contentTop + 16;

            int cols = CARDS_PER_ROW;
            int cardW = (listW - (cols - 1) * CARD_GAP) / cols;
            int totalRowW = cols * cardW + (cols - 1) * CARD_GAP;
            int offsetX = (listW - totalRowW) / 2;

            for (int i = 0; i < mods.size(); i++) {
                int row = i / cols;
                int col = i % cols;
                int cardX = listX + offsetX + col * (cardW + CARD_GAP);
                int cardY = startY + row * (CARD_H + CARD_GAP);

                if (mouseX >= cardX && mouseX <= cardX + cardW && mouseYAdj >= cardY && mouseYAdj <= cardY + CARD_H) {
                    setModValue(mods.get(i).id, !getModValue(mods.get(i).id));
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingScrollbar) {
            int py = panelY();
            int contentTop = py + HEADER_H + CONTENT_PAD;
            int contentHeight = PANEL_H - HEADER_H - CONTENT_PAD * 2;
            updateScrollFromMouse(mouseY, contentTop, contentHeight);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingScrollbar = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void updateScrollFromMouse(int mouseY, int trackTop, int trackHeight) {
        float percent = Math.max(0, Math.min(1, (float)(mouseY - trackTop) / trackHeight));
        targetScroll = percent * maxScroll;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RSHIFT || keyCode == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private static boolean wasKeyDown = false;
    public static void onTick() {
        boolean current = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (current && !wasKeyDown) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen instanceof GuiNelianOverlay) mc.displayGuiScreen(null);
            else if (mc.currentScreen == null) mc.displayGuiScreen(new GuiNelianOverlay());
        }
        wasKeyDown = current;
    }

    // ========== RENDERING UTILITIES ==========

    private static int applyAlpha(int color, float a) {
        int base = (color >> 24) & 0xFF;
        return ((int)(base * Math.min(1f, a)) << 24) | (color & 0x00FFFFFF);
    }

    private int lerpColor(int colorA, int colorB, float t) {
        float aA = ((colorA >> 24) & 0xFF) / 255f;
        float rA = ((colorA >> 16) & 0xFF) / 255f;
        float gA = ((colorA >> 8) & 0xFF) / 255f;
        float bA = (colorA & 0xFF) / 255f;
        
        float aB = ((colorB >> 24) & 0xFF) / 255f;
        float rB = ((colorB >> 16) & 0xFF) / 255f;
        float gB = ((colorB >> 8) & 0xFF) / 255f;
        float bB = (colorB & 0xFF) / 255f;
        
        int a = (int)(255 * (aA + (aB - aA) * t));
        int r = (int)(255 * (rA + (rB - rA) * t));
        int g = (int)(255 * (gA + (gB - gA) * t));
        int b = (int)(255 * (bA + (bB - bA) * t));
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawPremiumShadow(int x, int y, int w, int h, int baseAlpha) {
        int[] spreads = {30, 20, 10};
        for (int spread : spreads) {
            float ratio = (float)(spread) / 30f;
            int alpha = (int)(baseAlpha * 0.3f * ratio);
            if (alpha > 0) {
                drawRoundedRect(x - spread, y - spread, x + w + spread, y + h + spread, 
                    CORNER_R + spread, (alpha << 24) | 0x000000);
            }
        }
    }

    private void drawRadialGradient(float cx, float cy, float r1, float r2, int color1, int color2) {
        float a1 = ((color1 >> 24) & 0xFF) / 255f;
        float r1c = ((color1 >> 16) & 0xFF) / 255f;
        float g1 = ((color1 >> 8) & 0xFF) / 255f;
        float b1 = (color1 & 0xFF) / 255f;
        
        float a2 = ((color2 >> 24) & 0xFF) / 255f;
        float r2c = ((color2 >> 16) & 0xFF) / 255f;
        float g2 = ((color2 >> 8) & 0xFF) / 255f;
        float b2 = (color2 & 0xFF) / 255f;
        
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(6, DefaultVertexFormats.POSITION_COLOR);
        
        int segments = 64;
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(360.0 * i / segments);
            double ox = Math.cos(angle);
            double oy = Math.sin(angle);
            
            wr.pos(cx + ox * r1, cy + oy * r1, 0).color(r1c, g1, b1, a1).endVertex();
            wr.pos(cx + ox * r2, cy + oy * r2, 0).color(r2c, g2, b2, a2).endVertex();
        }
        tess.draw();
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawTextWithGlow(String text, int x, int y, int color, int glowSize, float alpha) {
        for (int i = glowSize; i > 0; i--) {
            int glowAlpha = (int)(((color >> 24) & 0xFF) * (1f - (i / (float)glowSize)) * 0.3f * alpha);
            fontRendererObj.drawString(text, x - i / 2, y, (glowAlpha << 24) | (color & 0xFFFFFF));
            fontRendererObj.drawString(text, x + i / 2, y, (glowAlpha << 24) | (color & 0xFFFFFF));
        }
        fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

    private void drawHorizontalGradient(int left, int top, int right, int bottom, int colorLeft, int colorRight) {
        float aL = ((colorLeft >> 24) & 0xFF) / 255f;
        float rL = ((colorLeft >> 16) & 0xFF) / 255f;
        float gL = ((colorLeft >> 8) & 0xFF) / 255f;
        float bL = (colorLeft & 0xFF) / 255f;
        
        float aR = ((colorRight >> 24) & 0xFF) / 255f;
        float rR = ((colorRight >> 16) & 0xFF) / 255f;
        float gR = ((colorRight >> 8) & 0xFF) / 255f;
        float bR = (colorRight & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(right, top, 0).color(rR, gR, bR, aR).endVertex();
        wr.pos(left, top, 0).color(rL, gL, bL, aL).endVertex();
        wr.pos(left, bottom, 0).color(rL, gL, bL, aL).endVertex();
        wr.pos(right, bottom, 0).color(rR, gR, bR, aR).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawRoundedRect(int left, int top, int right, int bottom, int radius, int color) {
        if (((color >> 24) & 0xFF) == 0) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        
        wr.begin(7, DefaultVertexFormats.POSITION);
        wr.pos(left + radius, top, 0).endVertex();
        wr.pos(right - radius, top, 0).endVertex();
        wr.pos(right - radius, bottom, 0).endVertex();
        wr.pos(left + radius, bottom, 0).endVertex();
        tess.draw();
        
        wr.begin(7, DefaultVertexFormats.POSITION);
        wr.pos(left, top + radius, 0).endVertex();
        wr.pos(left + radius, top + radius, 0).endVertex();
        wr.pos(left + radius, bottom - radius, 0).endVertex();
        wr.pos(left, bottom - radius, 0).endVertex();
        tess.draw();
        
        wr.begin(7, DefaultVertexFormats.POSITION);
        wr.pos(right - radius, top + radius, 0).endVertex();
        wr.pos(right, top + radius, 0).endVertex();
        wr.pos(right, bottom - radius, 0).endVertex();
        wr.pos(right - radius, bottom - radius, 0).endVertex();
        tess.draw();
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        
        drawFilledCircle(left + radius, top + radius, radius, color);
        drawFilledCircle(right - radius, top + radius, radius, color);
        drawFilledCircle(left + radius, bottom - radius, radius, color);
        drawFilledCircle(right - radius, bottom - radius, radius, color);
    }

    private void drawRoundedRectBorder(int left, int top, int right, int bottom, int radius, int color) {
        if (((color >> 24) & 0xFF) == 0) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(1.2f);
        
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        
        int segments = 20;
        double[][] corners = {
            {right - radius, top + radius, 0},
            {right - radius, bottom - radius, 90},
            {left + radius, bottom - radius, 180},
            {left + radius, top + radius, 270}
        };
        
        for (double[] corner : corners) {
            for (int i = 0; i <= segments; i++) {
                double angle = Math.toRadians(corner[2] + 90.0 * i / segments);
                double cx = corner[0] + radius * Math.cos(angle);
                double cy = corner[1] + radius * Math.sin(angle);
                wr.pos(cx, cy, 0).endVertex();
            }
        }
        tess.draw();
        
        GL11.glLineWidth(1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawFilledCircle(int cx, int cy, int rad, int color) {
        if (rad <= 0 || ((color >> 24) & 0xFF) == 0) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(cx, cy, 0).endVertex();
        
        int segments = Math.max(24, rad * 5);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(360.0 * i / segments);
            wr.pos(cx + rad * Math.cos(angle), cy + rad * Math.sin(angle), 0).endVertex();
        }
        tess.draw();
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static class ModItem {
        final String name, desc, icon;
        final int id, category;
        
        ModItem(String name, String desc, int id, String icon, int category) {
            this.name = name;
            this.desc = desc;
            this.id = id;
            this.icon = icon;
            this.category = category;
        }
    }
}
