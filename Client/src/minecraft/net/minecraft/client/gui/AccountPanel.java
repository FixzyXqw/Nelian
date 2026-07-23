package net.minecraft.client.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountPanel {

    private static final Logger logger = LogManager.getLogger("AccountPanel");

    // ========== KONUM & BOYUT ==========
    private int x, y;
    private int width  = 210;
    private int height = 64;

    // ========== ANİMASYON ==========
    private float hover        = 0f;   // 0 to 1
    private float clickPulse   = 0f;   // 1 to 0
    private float copyAlpha    = 0f;   // 1 to 0
    private float ringPhase    = 0f;  
    private boolean copied     = false;

    // ========== GERÇEK SKIN SİSTEMİ ==========
    // In mainmenu, you cannot get players head because player hasn't joined to a world yet

    private volatile ResourceLocation resolvedSkin = null;
    private volatile boolean resolvedIsFlatFace = false; 
    private boolean skinFetchStarted = false;

    
    private static final int C_BG_TOP     = 0xE614192B; // upside gradient
    private static final int C_BG_BOTTOM  = 0xE60A0D18; // downside gradient
    private static final int C_BG_HOVER_T = 0xE61E2A42;
    private static final int C_BG_HOVER_B = 0xE6111827;

    private static final int C_ACCENT     = 0xFF4F8EFF;
    private static final int C_ACCENT_DIM = 0x554F8EFF;

    private static final int C_NAME       = 0xFFF1F5F9;
    private static final int C_HINT       = 0xFF7FA8FF;
    private static final int C_COPIED     = 0xFF3DD68C;
    private static final int C_UUID_DIM   = 0xFF5B6B85;
    private static final int C_ONLINE_DOT = 0xFF3DD68C;

    private static final int C_BORDER     = 0x24FFFFFF;
    private static final int C_SHADOW     = 0x55000000;

    private static final int C_AVATAR_BG  = 0xFF0C1120;
    private static final int C_AVATAR_RIM = 0xFF4F8EFF;

    // ========== BOYUT SABİTLERİ ==========
    private static final int CORNER_R  = 10;
    private static final int AVATAR_SZ = 36; 
    private static final int PAD       = 10;

    public AccountPanel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // ========== ANA ÇİZİM METODU ==========
    public void draw(Minecraft mc, int mouseX, int mouseY) {
        Session session = mc.getSession();

        boolean hovered = isHovered(mouseX, mouseY);
        float targetHover = hovered ? 1f : 0f;
        hover += (targetHover - hover) * 0.12f;
        hover = clamp01(hover);

        if (clickPulse > 0f) clickPulse = Math.max(0f, clickPulse - 0.06f);
        if (copyAlpha  > 0f) copyAlpha  = Math.max(0f, copyAlpha  - 0.018f);
        if (copyAlpha <= 0f) copied = false;
        ringPhase = (ringPhase + 1.6f) % 360f;


        drawShadow(x + 3, y + 4, x + width + 3, y + height + 4, CORNER_R, C_SHADOW);


        int topC    = lerpColor(C_BG_TOP, C_BG_HOVER_T, hover);
        int bottomC = lerpColor(C_BG_BOTTOM, C_BG_HOVER_B, hover);
        drawGradientRoundedRect(x, y, x + width, y + height, CORNER_R, topC, bottomC);


        if (clickPulse > 0f) {
            int pulseAlpha = (int)(0x44 * clickPulse);
            drawRoundedRect(x, y, x + width, y + height, CORNER_R, (pulseAlpha << 24));
        }


        drawRoundedRectBorder(x, y, x + width, y + height, CORNER_R, C_BORDER);


        drawRect(x, y + CORNER_R, x + 2, y + height - CORNER_R, C_ACCENT_DIM);


        int avatarX = x + PAD;
        int avatarY = y + (height - AVATAR_SZ) / 2;


        drawSpinningGlow(avatarX + AVATAR_SZ / 2, avatarY + AVATAR_SZ / 2,
                AVATAR_SZ / 2 + 5, ringPhase, 0.10f + 0.35f * hover);


        drawRoundedRect(avatarX - 2, avatarY - 2,
                        avatarX + AVATAR_SZ + 2, avatarY + AVATAR_SZ + 2,
                        6, C_AVATAR_BG);


        ResourceLocation skin = resolvePlayerSkin(mc);
        mc.getTextureManager().bindTexture(skin);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (resolvedIsFlatFace && mc.thePlayer == null) {
            Gui.drawScaledCustomSizeModalRect(
                    avatarX, avatarY,
                    0, 0,
                    64, 64,
                    AVATAR_SZ, AVATAR_SZ,
                    64, 64
            );
        } else {
            Gui.drawScaledCustomSizeModalRect(
                    avatarX, avatarY,
                    8, 8,
                    8, 8,
                    AVATAR_SZ, AVATAR_SZ,
                    64, 64
            );
            Gui.drawScaledCustomSizeModalRect(
                    avatarX, avatarY,
                    40, 8,
                    8, 8,
                    AVATAR_SZ, AVATAR_SZ,
                    64, 64
            );
        }

        GlStateManager.disableBlend();
        int dotSz = 8;
        int dotX = avatarX + AVATAR_SZ - dotSz + 3;
        int dotY = avatarY + AVATAR_SZ - dotSz + 3;
        drawFilledCircle(dotX + dotSz / 2, dotY + dotSz / 2, dotSz / 2 + 1, 0xFF0C1120);
        drawFilledCircle(dotX + dotSz / 2, dotY + dotSz / 2, dotSz / 2 - 1, C_ONLINE_DOT);
        int textX = avatarX + AVATAR_SZ + 12;
        int nameY = y + 13;
        mc.fontRendererObj.drawStringWithShadow(
                session.getUsername(),
                textX, nameY,
                C_NAME
        );

        int hintY = y + 29;
        if (copied && copyAlpha > 0f) {
            int ca = (int)(0xFF * copyAlpha);
            mc.fontRendererObj.drawStringWithShadow(
                    "\u2714 Copied!",
                    textX, hintY,
                    (ca << 24) | (C_COPIED & 0x00FFFFFF)
            );
        } else {
            int ha = 0x99 + (int)(0x66 * hover);
            mc.fontRendererObj.drawString(
                    "\u29C9 Click to copy username",
                    textX, hintY,
                    (ha << 24) | (C_HINT & 0x00FFFFFF)
            );
        }

        GameProfile profile = mc.getSession().getProfile();
        String rawUuid = profile.getId() != null
                ? profile.getId().toString().replace("-", "")
                : "unknown";
        String shortUuid = rawUuid.length() > 8
                ? rawUuid.substring(0, 8) + "\u2026"
                : rawUuid;

        int uuidW = mc.fontRendererObj.getStringWidth(shortUuid);
        mc.fontRendererObj.drawString(
                shortUuid,
                x + width - uuidW - 6,
                y + height - 10,
                C_UUID_DIM
        );
    }
    private ResourceLocation resolvePlayerSkin(final Minecraft mc) {
        if (mc.thePlayer != null) {
            return ((AbstractClientPlayer) mc.thePlayer).getLocationSkin();
        }

        if (resolvedSkin != null) {
            return resolvedSkin;
        }

        if (!skinFetchStarted) {
            skinFetchStarted = true;
            logger.info("[Nelian] Skin fetch started, user: " + mc.getSession().getUsername());
            fetchSkinFromMcHeads(mc, mc.getSession().getProfile());
        }

        return new ResourceLocation("textures/entity/steve.png");
    }

   
    private void fetchSkinFromMcHeads(final Minecraft mc, final GameProfile profile) {
        Thread fetchThread = new Thread("AccountPanel-McHeadsFetch") {
            public void run() {
                try {
                    String identifier = profile.getId() != null
                            ? profile.getId().toString()
                            : profile.getName();

                    if (identifier == null || identifier.isEmpty()) {
                        logger.warn("[Nelian] Failed to fetch, Mojang API");
                        fetchSkinFromMojang(mc, profile);
                        return;
                    }

                    String urlStr = "https://mc-heads.net/avatar/" + identifier + "/64";
                    logger.info("[Nelian] Sent request. " + urlStr);

                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(4000);
                    conn.setRequestProperty("User-Agent", "Minecraft-Client");
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    logger.info("[Nelian] mc-heads HTTP Respond: " + responseCode);

                    if (responseCode != 200) {
                        logger.warn("[Nelian] mc-heads failed (" + responseCode + "), Mojang API");
                        fetchSkinFromMojang(mc, profile);
                        return;
                    }

                    final BufferedImage image = ImageIO.read(conn.getInputStream());
                    if (image == null) {
                        logger.warn("[Nelian] mc-heads respond is Null");
                        fetchSkinFromMojang(mc, profile);
                        return;
                    }

                    logger.info("[Nelian] 1: " + image.getWidth() + "x" + image.getHeight());
                    mc.addScheduledTask(new Runnable() {
                        public void run() {
                            try {
                                DynamicTexture texture = new DynamicTexture(image);
                                ResourceLocation loc = mc.getTextureManager()
                                        .getDynamicTextureLocation("accountpanel_head", texture);
                                resolvedIsFlatFace = true;
                                resolvedSkin = loc;
                            } catch (Exception glEx) {
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error("[Nelian] mc-heads req. failed. Mojang API", e);
                    fetchSkinFromMojang(mc, profile);
                }
            }
        };
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    private void fetchSkinFromMojang(final Minecraft mc, GameProfile profile) {
        try {
            logger.info("[Nelian] switched to Mojang session-service " + profile.getName());
            GameProfile filled = mc.getSessionService().fillProfileProperties(profile, false);
            logger.info("[Nelian] property count: " + filled.getProperties().size());

            mc.getSkinManager().loadProfileTextures(filled, new SkinManager.SkinAvailableCallback() {
                public void skinAvailable(Type type, ResourceLocation location, MinecraftProfileTexture texture) {
                    if (type == Type.SKIN) {
                        resolvedIsFlatFace = false;
                        resolvedSkin = location;
                    }
                }
            }, true);
        } catch (Exception e) {
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button, Minecraft mc) {
        if (button != 0) return;
        if (!isHovered(mouseX, mouseY)) return;

        String uuid = mc.getSession().getPlayerID();
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(uuid), null);

        clickPulse = 1f;
        copyAlpha  = 1f;
        copied     = true;
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width
            && mouseY >= y && mouseY <= y + height;
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static int lerpColor(int a, int b, float t) {
        int aA = (a >> 24) & 0xFF, rA = (a >> 16) & 0xFF, gA = (a >> 8) & 0xFF, bA = a & 0xFF;
        int aB = (b >> 24) & 0xFF, rB = (b >> 16) & 0xFF, gB = (b >> 8) & 0xFF, bB = b & 0xFF;
        int ra = aA + (int)((aB - aA) * t);
        int rr = rA + (int)((rB - rA) * t);
        int rg = gA + (int)((gB - gA) * t);
        int rb = bA + (int)((bB - bA) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

  

    private static void drawRect(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y2, color);
    }

    
    public static void drawRoundedRect(int left, int top, int right, int bottom, int radius, int color) {
        if (((color >> 24) & 0xFF) == 0) return;
        drawGradientRoundedRect(left, top, right, bottom, radius, color, color);
    }

   
    private static void drawGradientRoundedRect(int left, int top, int right, int bottom, int radius, int colorTop, int colorBottom) {
        if (((colorTop >> 24) & 0xFF) == 0 && ((colorBottom >> 24) & 0xFF) == 0) return;

        float aT = ((colorTop >> 24) & 0xFF) / 255f, rT = ((colorTop >> 16) & 0xFF) / 255f, gT = ((colorTop >> 8) & 0xFF) / 255f, bT = (colorTop & 0xFF) / 255f;
        float aB = ((colorBottom >> 24) & 0xFF) / 255f, rB = ((colorBottom >> 16) & 0xFF) / 255f, gB = ((colorBottom >> 8) & 0xFF) / 255f, bB = (colorBottom & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425); // GL_SMOOTH

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

      
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(left + radius, top,     0).color(rT, gT, bT, aT).endVertex();
        wr.pos(right - radius, top,    0).color(rT, gT, bT, aT).endVertex();
        wr.pos(right - radius, bottom, 0).color(rB, gB, bB, aB).endVertex();
        wr.pos(left + radius,  bottom, 0).color(rB, gB, bB, aB).endVertex();
        tess.draw();

        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(left,          top    + radius, 0).color(rT, gT, bT, aT).endVertex();
        wr.pos(left + radius, top    + radius, 0).color(rT, gT, bT, aT).endVertex();
        wr.pos(left + radius, bottom - radius, 0).color(rB, gB, bB, aB).endVertex();
        wr.pos(left,          bottom - radius, 0).color(rB, gB, bB, aB).endVertex();
        tess.draw();
        
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(right - radius, top    + radius, 0).color(rT, gT, bT, aT).endVertex();
        wr.pos(right,          top    + radius, 0).color(rT, gT, bT, aT).endVertex();
        wr.pos(right,          bottom - radius, 0).color(rB, gB, bB, aB).endVertex();
        wr.pos(right - radius, bottom - radius, 0).color(rB, gB, bB, aB).endVertex();
        tess.draw();

        GlStateManager.shadeModel(7424); // GL_FLAT
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        int topColor = ((int)(aT * 255) << 24) | ((int)(rT * 255) << 16) | ((int)(gT * 255) << 8) | (int)(bT * 255);
        int bottomColor = ((int)(aB * 255) << 24) | ((int)(rB * 255) << 16) | ((int)(gB * 255) << 8) | (int)(bB * 255);
        drawFilledCircle(left  + radius, top    + radius, radius, topColor);
        drawFilledCircle(right - radius, top    + radius, radius, topColor);
        drawFilledCircle(left  + radius, bottom - radius, radius, bottomColor);
        drawFilledCircle(right - radius, bottom - radius, radius, bottomColor);
    }

    private static void drawRoundedRectBorder(int left, int top, int right, int bottom, int radius, int color) {
        if (((color >> 24) & 0xFF) == 0) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ((color      ) & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(3, DefaultVertexFormats.POSITION); // GL_LINE_STRIP

        int segs = 8;
        double[][] corners = {
            {right - radius, top    + radius,  -90},
            {right - radius, bottom - radius,    0},
            {left  + radius, bottom - radius,   90},
            {left  + radius, top    + radius,  180}
        };
        for (double[] c : corners) {
            double startAng = c[2];
            for (int i = 0; i <= segs; i++) {
                double ang = Math.toRadians(startAng + 90.0 * i / segs);
                wr.pos(c[0] + radius * Math.cos(ang), c[1] + radius * Math.sin(ang), 0).endVertex();
            }
        }
        double closeAng = Math.toRadians(-90);
        wr.pos(right - radius + radius * Math.cos(closeAng), top + radius + radius * Math.sin(closeAng), 0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void drawSpinningGlow(float cx, float cy, float radius, float phaseDeg, float alpha) {
        if (alpha <= 0f) return;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        int arcs = 3;
        float arcLen = 50f;
        float gap = 360f / arcs;

        for (int a = 0; a < arcs; a++) {
            float start = phaseDeg + a * gap;
            wr.begin(5, DefaultVertexFormats.POSITION_COLOR); // GL_TRIANGLE_STRIP
            int segs = 10;
            for (int i = 0; i <= segs; i++) {
                float t = i / (float) segs;
                double ang = Math.toRadians(start + arcLen * t);
                float fade = alpha * (1f - Math.abs(t - 0.5f) * 1.4f);
                fade = fade < 0f ? 0f : fade;
                float cos = (float) Math.cos(ang);
                float sin = (float) Math.sin(ang);
                wr.pos(cx + cos * radius, cy + sin * radius, 0)
                        .color(0.31f, 0.56f, 1f, fade).endVertex();
                wr.pos(cx + cos * (radius - 1.5f), cy + sin * (radius - 1.5f), 0)
                        .color(0.31f, 0.56f, 1f, fade).endVertex();
            }
            tess.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void drawFilledCircle(int cx, int cy, int rad, int color) {
        if (rad <= 0 || ((color >> 24) & 0xFF) == 0) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ((color      ) & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(6, DefaultVertexFormats.POSITION);
        wr.pos(cx, cy, 0).endVertex();
        int steps = Math.max(16, rad * 4);
        for (int i = 0; i <= steps; i++) {
            double ang = Math.toRadians(360.0 * i / steps);
            wr.pos(cx + rad * Math.cos(ang), cy + rad * Math.sin(ang), 0).endVertex();
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void drawShadow(int x1, int y1, int x2, int y2, int radius, int baseColor) {
        int layers = 6;
        for (int i = layers; i > 0; i--) {
            int ba = (baseColor >> 24) & 0xFF;
            int la = (int)(ba * (float)(layers - i) / layers * 0.35f);
            drawRoundedRect(x1 - i, y1 - i, x2 + i, y2 + i, radius + i,
                    (la << 24) | (baseColor & 0x00FFFFFF));
        }
    }
}
