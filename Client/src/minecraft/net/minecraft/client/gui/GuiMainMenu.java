package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.optifine.reflect.Reflector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {

    private static final AtomicInteger field_175373_f = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    private float updateCounter;
    private String splashText;
    private float globeRotation = 0F;
    private int panoramaTimer;
    private DynamicTexture viewportTexture;
    private boolean field_175375_v = true;
    private final Object threadLock = new Object();
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{
            new ResourceLocation("textures/gui/title/background/panorama_0.png"),
            new ResourceLocation("textures/gui/title/background/panorama_1.png"),
            new ResourceLocation("textures/gui/title/background/panorama_2.png"),
            new ResourceLocation("textures/gui/title/background/panorama_3.png"),
            new ResourceLocation("textures/gui/title/background/panorama_4.png"),
            new ResourceLocation("textures/gui/title/background/panorama_5.png")
    };
    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";

    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private int field_92024_r;
    private ResourceLocation backgroundTexture;
    private boolean field_183502_L;
    private GuiScreen field_183503_M;
    private GuiButton modButton;
    private GuiScreen modUpdateNotification;

    private List<NelianInterface.Snowflake> snowflakes = new ArrayList<>();
    private List<NelianInterface.MouseTrailPoint> mouseTrail = new ArrayList<>();
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    private ResourceLocation nelianTexture;
    private int nelianTextureWidth;
    private int nelianTextureHeight;
    private int nelianTextureX;
    private int nelianTextureY;
    private boolean nelianTextureCreated = false;

    private AccountPanel accountPanel;

    private static class GuiButtonModern extends NelianInterface.ModernButton {
        public GuiButtonModern(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!this.visible) return;
            
            boolean isHovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                    mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.setHovered(isHovered);
            
            super.drawButton(mc, mouseX, mouseY);
        }
    }


    public GuiMainMenu() {
        this.openGLWarning2 = field_96138_a;
        this.field_183502_L = false;
        this.splashText = "";
        this.updateCounter = RANDOM.nextFloat();
        this.openGLWarning1 = "";

        for (int i = 0; i < 120; i++) {
            snowflakes.add(new NelianInterface.Snowflake(
                    RANDOM.nextFloat() * 1920,
                    RANDOM.nextFloat() * 1080,
                    0.4f + RANDOM.nextFloat() * 1.2f,
                    1 + RANDOM.nextInt(2)
            ));
        }

        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            this.openGLWarning1 = I18n.format("title.oldgl1");
            this.openGLWarning2 = I18n.format("title.oldgl2");
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }
    }

    private boolean func_183501_a() {
        return Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS)
                && this.field_183503_M != null;
    }

    @Override
    public void updateScreen() {
        globeRotation += 0.5F;
        ++this.panoramaTimer;

        for (NelianInterface.Snowflake s : snowflakes) {
            s.update(this.width, this.height);
        }

        long now = System.currentTimeMillis();
        mouseTrail.removeIf(p -> now - p.life > 400);

        if (this.func_183501_a()) this.field_183503_M.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    @Override
    public void initGui() {
        if (accountPanel == null) {
            accountPanel = new AccountPanel(8, 8);
        }
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);

        if (!nelianTextureCreated) {
            createNelianTexture();
        }

        this.buttonList.clear();

        int buttonWidth = 240;
        int buttonHeight = 28;
        int spacing = 7;
        int startX = (this.width - buttonWidth) / 2;
        int totalHeight = (buttonHeight * 4) + (spacing * 3);
        int startY = (this.height - totalHeight) / 2 + 35;

        this.buttonList.add(new GuiButtonModern(1, startX, startY, buttonWidth, buttonHeight, I18n.format("menu.singleplayer", new Object[0])));
        this.buttonList.add(new GuiButtonModern(2, startX, startY + (buttonHeight + spacing), buttonWidth, buttonHeight, I18n.format("menu.multiplayer", new Object[0])));
        this.buttonList.add(new GuiButtonModern(
        	    0,
        	    startX,
        	    startY + (buttonHeight + spacing) * 2,
        	    buttonWidth,
        	    buttonHeight,
        	    I18n.format("menu.options", new Object[0]).replace(".", "")
        	));
        this.buttonList.add(new GuiButtonModern(4, startX, startY + (buttonHeight + spacing) * 3, buttonWidth, buttonHeight, I18n.format("menu.quit", new Object[0])));

        nelianTextureX = (this.width - nelianTextureWidth) / 2;
        nelianTextureY = 45;

        this.mc.setConnectedToRealms(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case 2:
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 4:
                this.mc.shutdown();
                break;
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (result && id == 12) {
            this.mc.getSaveLoader().deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        } else if (id == 13 && result) {
            try {
                Class<?> oclass = Class.forName("java.awt.Desktop");
                Object desktop = oclass.getMethod("getDesktop").invoke(null);
                oclass.getMethod("browse", URI.class).invoke(desktop, new URI(this.openGLWarningLink));
            } catch (Throwable t) {
                logger.error("Couldn't open link", t);
            }
            this.mc.displayGuiScreen(this);
        }
    }

    private void renderSkybox(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, this.width, this.height, 0xFF0B0C10, 0xFF14161D);
        drawGradientRect(0, 0, this.width, this.height, 0x1A000000, 0x66000000);
    }

    public void createNelianTexture() {
        int pixelSize = 7;
        int spacing = 5;
        
        nelianTexture = NelianInterface.createNelianTexture(this.mc, pixelSize, spacing);
        
        int totalWidth = 6 * (5 * pixelSize + spacing) - spacing;
        int totalHeight = 5 * pixelSize;
        nelianTextureWidth = totalWidth;
        nelianTextureHeight = totalHeight;
        nelianTextureCreated = true;
    }

    public boolean isNelianTextureCreated() {
        return nelianTextureCreated;
    }

    private void drawNelianTitle() {
        drawNelianTitleWithAlpha(1.0F);
    }

    public void drawNelianTitleWithAlpha(float alpha) {
        NelianInterface.drawNelianTitle(
            this.mc, 
            nelianTexture, 
            nelianTextureX, 
            nelianTextureY, 
            nelianTextureWidth, 
            nelianTextureHeight, 
            alpha
        );
    }

    private void drawSnowflakes() {
        NelianInterface.drawSnowflakes(snowflakes, this.height);
    }

    private void drawMouseTrail() {
        NelianInterface.drawMouseTrail(mouseTrail);
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long now = System.currentTimeMillis();
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            mouseTrail.add(new NelianInterface.MouseTrailPoint(mouseX, mouseY, now));
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();

        if (this.mc.theWorld != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            GlobalMenuBackground.get().render(this, this.width, this.height);
        }

        this.drawSnowflakes();
        this.drawMouseTrail();

        int buttonWidth = 240;
        int buttonHeight = 28;
        int spacing = 7;
        int totalHeight = (buttonHeight * 4) + (spacing * 3);
        int startX = (this.width - buttonWidth) / 2;
        int startY = (this.height - totalHeight) / 2 + 35;

        int panelLeft   = startX - 25;
        int panelRight  = startX + buttonWidth + 25;
        int panelTop    = startY - 25;
        int panelBottom = startY + totalHeight + 25;
        int panelColor  = new Color(10, 10, 12, 160).getRGB();

        NelianInterface.drawRoundedRect(panelLeft, panelTop, panelRight, panelBottom, 14, panelColor);

        int outlineColor = new Color(63, 63, 70, 90).getRGB();
        NelianInterface.drawRoundedRectBorder(panelLeft, panelTop, panelRight, panelBottom, 14, outlineColor, 1.5f);

        if (accountPanel != null) {
            accountPanel.draw(mc, mouseX, mouseY);
        }

        this.drawNelianTitle();

        int infoPre2 = 0xFFFF5555;
        int infoPre   = 0x44AA0000;
        int infoColor = 0x44FFFFFF;

        if (Reflector.FMLCommonHandler_getBrandings.exists()) {
            Object fml = Reflector.call(Reflector.FMLCommonHandler_instance);
            List<String> brands = Lists.reverse((List) Reflector.call(fml, Reflector.FMLCommonHandler_getBrandings, true));
            for (int i = 0; i < brands.size(); i++) {
                String br = brands.get(i);
                if (!Strings.isNullOrEmpty(br)) {
                    this.drawString(this.fontRendererObj, br, 8,
                            this.height - 12 - i * (this.fontRendererObj.FONT_HEIGHT + 2), infoColor);
                }
            }
            if (Reflector.ForgeHooksClient_renderMainMenu.exists()) {
                Reflector.call(Reflector.ForgeHooksClient_renderMainMenu, this, this.fontRendererObj, this.width, this.height);
            }
        } else {
            if (Nelianinfo.isPre) {
                this.drawString(this.fontRendererObj,
                        "Pre-release " + Nelianinfo.ALL + " " + Nelianinfo.VERSION_NUMBER + " & " + Nelianinfo.BUILD_NUMBER,
                        8, this.height - 12, infoPre2);
            } else {
                this.drawString(this.fontRendererObj, Nelianinfo.VERSION, 8, this.height - 12, infoColor);
            }
        }

        String copyright;
        if (Nelianinfo.isPre) {
            copyright = "Restricted Source.";
            this.drawString(this.fontRendererObj, copyright,
                    this.width - this.fontRendererObj.getStringWidth(copyright) - 8,
                    this.height - 12, infoPre);
        } else {
            copyright = "Has nothing to do with Mojang!";
            this.drawString(this.fontRendererObj, copyright,
                    this.width - this.fontRendererObj.getStringWidth(copyright) - 8,
                    this.height - 12, infoColor);
        }

        if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty()) {
            drawRect(this.field_92022_t - 2, this.field_92021_u - 2,
                    this.field_92020_v + 2, this.field_92019_w - 1, 0x55000000);
            this.drawString(this.fontRendererObj, this.openGLWarning1,
                    this.field_92022_t, this.field_92021_u, -1);
            this.drawString(this.fontRendererObj, this.openGLWarning2,
                    (this.width - this.field_92024_r) / 2,
                    ((GuiButton) this.buttonList.get(0)).yPosition - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.func_183501_a()) this.field_183503_M.drawScreen(mouseX, mouseY, partialTicks);
        if (this.modUpdateNotification != null) this.modUpdateNotification.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (accountPanel != null) {
            accountPanel.mouseClicked(mouseX, mouseY, mouseButton, mc);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

        synchronized (this.threadLock) {
            if (this.openGLWarning1.length() > 0 &&
                    mouseX >= this.field_92022_t && mouseX <= this.field_92020_v &&
                    mouseY >= this.field_92021_u && mouseY <= this.field_92019_w) {
                GuiConfirmOpenLink gui = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                gui.disableSecurityWarning();
                this.mc.displayGuiScreen(gui);
            }
        }
        if (this.func_183501_a()) this.field_183503_M.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        if (this.field_183503_M != null) this.field_183503_M.onGuiClosed();
    }
}
