package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiYesNo extends GuiScreen
{
    protected GuiYesNoCallback parentScreen;
    protected String messageLine1;
    private String messageLine2;
    private final List<String> field_175298_s = Lists.<String>newArrayList();
    protected String confirmButtonText;
    protected String cancelButtonText;
    protected int parentButtonClickedId;
    private int ticksUntilEnable;
    private float skullPulse = 0;
    private boolean pulseDirection = true;
    private boolean showSkull = false;

    public GuiYesNo(GuiYesNoCallback p_i1082_1_, String p_i1082_2_, String p_i1082_3_, int p_i1082_4_)
    {
        this.parentScreen = p_i1082_1_;
        this.messageLine1 = p_i1082_2_;
        this.messageLine2 = p_i1082_3_;
        this.parentButtonClickedId = p_i1082_4_;
        this.confirmButtonText = I18n.format("gui.yes", new Object[0]);
        this.cancelButtonText = I18n.format("gui.no", new Object[0]);
        

        if (p_i1082_2_.contains("death") || p_i1082_2_.contains("quit") || p_i1082_3_.contains("death"))
        {
            this.showSkull = true;
        }
    }

    public GuiYesNo(GuiYesNoCallback p_i1083_1_, String p_i1083_2_, String p_i1083_3_, String p_i1083_4_, String p_i1083_5_, int p_i1083_6_)
    {
        this.parentScreen = p_i1083_1_;
        this.messageLine1 = p_i1083_2_;
        this.messageLine2 = p_i1083_3_;
        this.confirmButtonText = p_i1083_4_;
        this.cancelButtonText = p_i1083_5_;
        this.parentButtonClickedId = p_i1083_6_;
        

        if (p_i1083_2_.contains("death") || p_i1083_2_.contains("quit") || p_i1083_3_.contains("death"))
        {
            this.showSkull = true;
        }
    }

    public void initGui()
    {
        this.buttonList.clear();
        

        ModernYesNoButton yesButton = new ModernYesNoButton(0, this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.confirmButtonText);
        yesButton.setPrimaryMode(true);
        this.buttonList.add(yesButton);
        
        ModernYesNoButton noButton = new ModernYesNoButton(1, this.width / 2 + 5, this.height / 6 + 96, 150, 20, this.cancelButtonText);
        noButton.setDangerMode(true);
        this.buttonList.add(noButton);
        
        this.field_175298_s.clear();
        this.field_175298_s.addAll(this.fontRendererObj.listFormattedStringToWidth(this.messageLine2, this.width - 50));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        this.parentScreen.confirmClicked(button.id == 0, this.parentButtonClickedId);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {

        if (this.mc.theWorld != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else
        {
            GlobalMenuBackground.get().render(this, this.width, this.height);
        }
        

        if (showSkull)
        {
            drawRect(0, 0, this.width, this.height, new Color(100, 0, 0, 80).getRGB());
        }
        else
        {
            drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 100).getRGB());
        }
        

        if (showSkull)
        {
            drawSkullIcon();
        }
        

        drawModernTitle();
        

        drawDecorativeLine();
        

        int i = 110;
        for (String s : this.field_175298_s)
        {
            this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, new Color(220, 220, 230).getRGB());
            i += this.fontRendererObj.FONT_HEIGHT;
        }
        

        for (Object obj : this.buttonList) {
            if (obj instanceof ModernYesNoButton) {
                ModernYesNoButton btn = (ModernYesNoButton) obj;
                boolean isHovered = mouseX >= btn.xPosition && mouseY >= btn.yPosition && 
                                   mouseX < btn.xPosition + btn.getButtonWidth() && 
                                   mouseY < btn.yPosition + btn.getButtonHeight();
                btn.setHovered(isHovered);
            }
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        

        drawCornerDecorations();
    }
    
    public void updateScreen()
    {
        super.updateScreen();
        

        if (showSkull)
        {
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
        }

        if (--this.ticksUntilEnable == 0)
        {
            for (GuiButton guibutton : this.buttonList)
            {
                guibutton.enabled = true;
            }
        }
    }
    
    private void drawSkullIcon() {
        int centerX = this.width / 2;
        int iconY = 45;
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
    
    private void drawModernTitle() {
        String title = this.messageLine1;
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        int titleX = this.width / 2 - titleWidth / 2;
        int titleY = showSkull ? 110 : 70;
        
   
        this.fontRendererObj.drawString(title, titleX + 2, titleY + 2, new Color(0, 0, 0, 100).getRGB());
        

        for (int i = 0; i < title.length(); i++) {
            String letter = String.valueOf(title.charAt(i));
            int xPos = titleX + this.fontRendererObj.getStringWidth(title.substring(0, i));
            float progress = (float)i / title.length();
            int r, g, b;
            
            if (showSkull) {
                r = (int)(220 + 35 * Math.sin(progress * Math.PI));
                g = (int)(60 + 30 * Math.cos(progress * Math.PI));
                b = (int)(50 + 25 * Math.sin(progress * Math.PI));
            } else {
                r = (int)(255 * (0.6 + 0.4 * Math.sin(progress * Math.PI)));
                g = (int)(255 * (0.7 + 0.3 * Math.cos(progress * Math.PI * 0.5)));
                b = (int)(255 * (0.8 + 0.2 * Math.sin(progress * Math.PI * 0.3)));
            }
            int color = new Color(r, g, b).getRGB();
            this.fontRendererObj.drawString(letter, xPos, titleY, color);
        }
    }
    
    private void drawDecorativeLine() {
        int lineWidth = 150;
        int lineY = showSkull ? 130 : 90;
        int lineStartX = this.width / 2 - lineWidth / 2;
        
        for (int i = 0; i < lineWidth; i++) {
            float progress = (float)i / lineWidth;
            int alpha = (int)(150 * Math.sin(progress * Math.PI));
            int color;
            if (showSkull) {
                color = new Color(200, 80, 70, alpha).getRGB();
            } else {
                color = new Color(255, 255, 255, alpha).getRGB();
            }
            drawRect(lineStartX + i, lineY, lineStartX + i + 1, lineY + 2, color);
        }
    }
    
    private void drawCornerDecorations() {
        int cornerSize = 40;
        int cornerThickness = 2;
        Color cornerColor = showSkull ? new Color(180, 50, 50, 80) : new Color(255, 255, 255, 60);
        
        drawRect(0, 0, cornerSize, cornerThickness, cornerColor.getRGB());
        drawRect(0, 0, cornerThickness, cornerSize, cornerColor.getRGB());
        drawRect(this.width - cornerSize, 0, this.width, cornerThickness, cornerColor.getRGB());
        drawRect(this.width - cornerThickness, 0, this.width, cornerSize, cornerColor.getRGB());
        drawRect(0, this.height - cornerThickness, cornerSize, this.height, cornerColor.getRGB());
        drawRect(0, this.height - cornerSize, cornerThickness, this.height, cornerColor.getRGB());
        drawRect(this.width - cornerSize, this.height - cornerThickness, this.width, this.height, cornerColor.getRGB());
        drawRect(this.width - cornerThickness, this.height - cornerSize, this.width, this.height, cornerColor.getRGB());
    }

    public void setButtonDelay(int p_146350_1_)
    {
        this.ticksUntilEnable = p_146350_1_;

        for (GuiButton guibutton : this.buttonList)
        {
            guibutton.enabled = false;
        }
    }


    class ModernYesNoButton extends GuiButton {
        private boolean isHovered = false;
        private float hoverProgress = 0;
        private boolean isPrimary = false;
        private boolean isDanger = false;
        
        public ModernYesNoButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
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
        
        public void setDangerMode(boolean danger) {
            this.isDanger = danger;
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
                } else if (isDanger) {
                    bgColor = new Color(160, 50, 50, (int)(200 + 55 * hoverProgress));
                    borderColor = new Color(220, 80, 70);
                    textColor = Color.WHITE;
                } else {
                    bgColor = new Color(30, 30, 40, (int)(180 + 75 * hoverProgress));
                    borderColor = new Color(100, 100, 120);
                    textColor = new Color(220, 220, 230);
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
