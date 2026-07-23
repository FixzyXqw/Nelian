package net.minecraft.client.gui;

import java.io.IOException;
import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public class GuiGameOver extends GuiScreen implements GuiYesNoCallback
{
    private int enableButtonsTimer;
    private boolean field_146346_f = false;
    private float fadeProgress = 0;
    private float skullPulse = 0;
    private boolean pulseDirection = true;

    public void initGui()
    {
        this.fadeProgress = 0;
        this.buttonList.clear();

        if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
        {
            if (this.mc.isIntegratedServerRunning())
            {
                this.buttonList.add(new ModernGameOverButton(1, this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.format("deathScreen.deleteWorld", new Object[0])));
            }
            else
            {
                this.buttonList.add(new ModernGameOverButton(1, this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.format("deathScreen.leaveServer", new Object[0])));
            }
        }
        else
        {

            ModernGameOverButton respawnButton = new ModernGameOverButton(0, this.width / 2 - 100, this.height / 4 + 96, 200, 20, I18n.format("deathScreen.respawn", new Object[0]));
            respawnButton.setPrimaryMode(true);
            this.buttonList.add(respawnButton);
            
            this.buttonList.add(new ModernGameOverButton(1, this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.format("deathScreen.titleScreen", new Object[0])));

            if (this.mc.getSession() == null)
            {
                ((GuiButton)this.buttonList.get(1)).enabled = false;
            }
        }

        for (GuiButton guibutton : this.buttonList)
        {
            guibutton.enabled = false;
        }
    }
    
    public void updateScreen()
    {
        super.updateScreen();
        ++this.enableButtonsTimer;
        
        if (fadeProgress < 1.0f) {
            fadeProgress += 0.05f;
            if (fadeProgress > 1.0f) fadeProgress = 1.0f;
        }
        
        if (pulseDirection) {
            skullPulse += 0.03f;
            if (skullPulse >= 1.0f) {
                skullPulse = 1.0f;
                pulseDirection = false;
            }
        } else {
            skullPulse -= 0.03f;
            if (skullPulse <= 0.5f) {
                skullPulse = 0.5f;
                pulseDirection = true;
            }
        }

        if (this.enableButtonsTimer == 20)
        {
            for (GuiButton guibutton : this.buttonList)
            {
                guibutton.enabled = true;
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                this.mc.thePlayer.respawnPlayer();
                this.mc.displayGuiScreen((GuiScreen)null);
                break;

            case 1:
                if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
                {
                    this.mc.displayGuiScreen(new GuiMainMenu());
                }
                else
                {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm", new Object[0]), "", I18n.format("deathScreen.titleScreen", new Object[0]), I18n.format("deathScreen.respawn", new Object[0]), 0);
                    this.mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result)
        {
            this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.loadWorld((WorldClient)null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else
        {
            this.mc.thePlayer.respawnPlayer();
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
       
        drawRect(0, 0, this.width, this.height, new Color(150, 0, 0, 80).getRGB());
        

        drawSkullIcon();
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        boolean flag = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
        String s = flag ? I18n.format("deathScreen.title.hardcore", new Object[0]) : I18n.format("deathScreen.title", new Object[0]);
        

        drawGradientTitle(s, this.width / 2 / 2, 45);
        
        GlStateManager.popMatrix();

        if (flag)
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.hardcoreInfo", new Object[0]), this.width / 2, 160, new Color(200, 100, 100).getRGB());
        }


        String scoreText = I18n.format("deathScreen.score", new Object[0]) + ": ";
        String scoreValue = "" + this.mc.thePlayer.getScore();
        int scoreTextWidth = this.fontRendererObj.getStringWidth(scoreText);
        int scoreValueWidth = this.fontRendererObj.getStringWidth(scoreValue);
        int totalWidth = scoreTextWidth + scoreValueWidth;
        int scoreX = this.width / 2 - totalWidth / 2;
        int scoreY = 115;
        
        this.fontRendererObj.drawString(scoreText, scoreX, scoreY, new Color(180, 180, 200).getRGB());
        this.fontRendererObj.drawString(scoreValue, scoreX + scoreTextWidth, scoreY, new Color(255, 200, 100).getRGB());
        

        for (Object obj : this.buttonList) {
            if (obj instanceof ModernGameOverButton) {
                ModernGameOverButton btn = (ModernGameOverButton) obj;
                boolean isHovered = mouseX >= btn.xPosition && mouseY >= btn.yPosition && 
                                   mouseX < btn.xPosition + btn.getButtonWidth() && 
                                   mouseY < btn.yPosition + btn.getButtonHeight();
                btn.setHovered(isHovered);
            }
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        

        drawCornerDecorations();
    }
    
    
    
    private void drawSkullIcon() {
        int centerX = this.width / 2;
        int iconY = 55;
        int iconSize = (int)(42 * (0.7f + skullPulse * 0.3f));
        

        Color skullColor = new Color(200, 200, 220, 220);
        

        drawCircle(centerX - iconSize/2, iconY, iconSize, iconSize, skullColor);
        

        int eyeSize = iconSize / 5;
        int eyeY = iconY + iconSize / 3;
        drawCircle(centerX - iconSize/3 - eyeSize/2, eyeY, eyeSize, eyeSize, new Color(40, 40, 50));
        drawCircle(centerX + iconSize/3 - eyeSize/2, eyeY, eyeSize, eyeSize, new Color(40, 40, 50));
        

        int mouthY = iconY + iconSize * 2 / 3;
        int mouthWidth = iconSize / 2;
        drawRect(centerX - mouthWidth/2, mouthY, centerX + mouthWidth/2, mouthY + 3, new Color(40, 40, 50).getRGB());
        

        for (int i = -2; i <= 2; i++) {
            drawRect(centerX + i * 4, mouthY, centerX + i * 4 + 2, mouthY + 6, new Color(200, 200, 220).getRGB());
        }
    }
    
    private void drawCircle(int x, int y, int width, int height, Color color) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int dx = i - width/2;
                int dy = j - height/2;
                if (dx*dx + dy*dy <= (width/2)*(width/2)) {
                    drawRect(x + i, y + j, x + i + 1, y + j + 1, color.getRGB());
                }
            }
        }
    }
    
    private void drawGradientTitle(String title, int x, int y) {
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        int titleX = x - titleWidth / 2;
        

        this.fontRendererObj.drawString(title, titleX + 1, y + 1, new Color(0, 0, 0, 150).getRGB());
        

        for (int i = 0; i < title.length(); i++) {
            String letter = String.valueOf(title.charAt(i));
            int xPos = titleX + this.fontRendererObj.getStringWidth(title.substring(0, i));
            float progress = (float)i / title.length();
            int r = (int)(220 + 35 * Math.sin(progress * Math.PI));
            int g = (int)(60 + 30 * Math.cos(progress * Math.PI));
            int b = (int)(50 + 25 * Math.sin(progress * Math.PI));
            int color = new Color(r, g, b).getRGB();
            this.fontRendererObj.drawString(letter, xPos, y, color);
        }
    }
    
    private void drawCornerDecorations() {
        int cornerSize = 40;
        int cornerThickness = 2;
        Color cornerColor = new Color(180, 50, 50, 100);
        
        drawRect(0, 0, cornerSize, cornerThickness, cornerColor.getRGB());
        drawRect(0, 0, cornerThickness, cornerSize, cornerColor.getRGB());
        drawRect(this.width - cornerSize, 0, this.width, cornerThickness, cornerColor.getRGB());
        drawRect(this.width - cornerThickness, 0, this.width, cornerSize, cornerColor.getRGB());
        drawRect(0, this.height - cornerThickness, cornerSize, this.height, cornerColor.getRGB());
        drawRect(0, this.height - cornerSize, cornerThickness, this.height, cornerColor.getRGB());
        drawRect(this.width - cornerSize, this.height - cornerThickness, this.width, this.height, cornerColor.getRGB());
        drawRect(this.width - cornerThickness, this.height - cornerSize, this.width, this.height, cornerColor.getRGB());
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }


    class ModernGameOverButton extends GuiButton {
        private boolean isHovered = false;
        private float hoverProgress = 0;
        private boolean isPrimary = false;
        
        public ModernGameOverButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
        
        public int getButtonWidth() {
            return this.width;
        }
        
        public int getButtonHeight() {
            return this.height;
        }
        
        public void setPrimaryMode(boolean primary) {
            this.isPrimary = primary;
        }
        
        public void setHovered(boolean hovered) {
            if (hovered != this.isHovered) {
                this.isHovered = hovered;
            }
            
            if (hovered && hoverProgress < 1.0f) {
                hoverProgress += 0.1f;
                if (hoverProgress > 1.0f) hoverProgress = 1.0f;
            } else if (!hovered && hoverProgress > 0.0f) {
                hoverProgress -= 0.1f;
                if (hoverProgress < 0.0f) hoverProgress = 0.0f;
            }
        }
        
        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                Color bgColor, borderColor, textColor;
                
                if (!this.enabled) {
                    bgColor = new Color(60, 60, 70, 150);
                    borderColor = new Color(80, 80, 90, 200);
                    textColor = new Color(120, 120, 130);
                } else if (isPrimary) {
                    bgColor = new Color(50, 150, 80, (int)(200 + 55 * hoverProgress));
                    borderColor = new Color(80, 200, 110);
                    textColor = Color.WHITE;
                } else {
                    bgColor = new Color(160, 50, 50, (int)(200 + 55 * hoverProgress));
                    borderColor = new Color(220, 80, 70);
                    textColor = Color.WHITE;
                }
                
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, bgColor.getRGB());
                drawHorizontalLine(this.xPosition, this.xPosition + this.width, this.yPosition, borderColor.getRGB());
                drawHorizontalLine(this.xPosition, this.xPosition + this.width, this.yPosition + this.height - 1, borderColor.getRGB());
                drawVerticalLine(this.xPosition, this.yPosition, this.yPosition + this.height, borderColor.getRGB());
                drawVerticalLine(this.xPosition + this.width - 1, this.yPosition, this.yPosition + this.height, borderColor.getRGB());
                
                int textWidth = mc.fontRendererObj.getStringWidth(this.displayString);
                int textX = this.xPosition + (this.width - textWidth) / 2;
                int textY = this.yPosition + (this.height - 8) / 2;
                
                mc.fontRendererObj.drawString(this.displayString, textX + 1, textY + 1, new Color(0, 0, 0, 100).getRGB());
                mc.fontRendererObj.drawString(this.displayString, textX, textY, textColor.getRGB());
            }
        }
    }
}
