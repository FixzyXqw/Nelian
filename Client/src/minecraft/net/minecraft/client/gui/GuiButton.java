package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.awt.Color;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;
    
    // Modern button properties
    private ButtonType buttonType = ButtonType.NEUTRAL;
    private boolean isHovered = false;
    private float hoverProgress = 0;

    public enum ButtonType {
        PRIMARY,    // Green - Accept/Confirm/Yes
        DANGER,     // Red - Cancel/Delete/No/Quit
        NEUTRAL     // Blue - Other buttons
    }

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
        this.buttonType = determineButtonType(buttonText);
    }

    private ButtonType determineButtonType(String text)
    {
        String lowerText = text.toLowerCase();
        

        if (lowerText.contains("respawn") || 
            lowerText.contains("confirm") || 
            lowerText.contains("yes") || 
            lowerText.contains("accept") || 
            lowerText.contains("ok") ||
            lowerText.contains("done") ||
            lowerText.contains("save") ||
            lowerText.contains("apply") ||
            lowerText.contains("continue") ||
            lowerText.contains("play") ||
            lowerText.contains("join") ||
            lowerText.contains("create") ||
            lowerText.contains("add") ||
            lowerText.contains("purchase") ||
            lowerText.contains("buy") ||
            lowerText.contains("proceed") ||
            lowerText.contains("select") ||
            lowerText.contains("connect") ||
            lowerText.contains("start") ||
            lowerText.contains("enable") ||
            lowerText.contains("allow") ||
            lowerText.contains("agree") ||
            lowerText.contains("approve") ||
            lowerText.contains("open") ||
            lowerText.contains("lan") ||
            lowerText.contains("host") ||
            lowerText.contains("new") ||
            lowerText.contains("generate") ||
            lowerText.contains("refresh"))
        {
            return ButtonType.PRIMARY;
        }
        

        if (lowerText.contains("quit") || 
            lowerText.contains("cancel") || 
            lowerText.contains("delete") || 
            lowerText.contains("remove") ||
            lowerText.contains("no") ||
            lowerText.contains("exit") ||
            lowerText.contains("stop") ||
            lowerText.contains("disconnect") ||
            lowerText.contains("kick") ||
            lowerText.contains("ban") ||
            lowerText.contains("reset") ||
            lowerText.contains("clear") ||
            lowerText.contains("leave") ||
            lowerText.contains("abort") ||
            lowerText.contains("skip") ||
            lowerText.contains("close") ||
            lowerText.contains("reject") ||
            lowerText.contains("refuse") ||
            lowerText.contains("deny") ||
            lowerText.contains("disable") ||
            lowerText.contains("forbid") ||
            lowerText.contains("block") ||
            lowerText.contains("back") ||
            lowerText.contains("title") && lowerText.contains("screen") ||
            lowerText.contains("menu") && lowerText.contains("return") ||
            lowerText.contains("close") ||
            lowerText.contains("logout") ||
            lowerText.contains("shutdown"))
        {
            return ButtonType.DANGER;
        }
        

        return ButtonType.NEUTRAL;
    }

    public void setPrimaryMode(boolean primary)
    {
        if (primary)
        {
            this.buttonType = ButtonType.PRIMARY;
        }
    }

    public void setDangerMode(boolean danger)
    {
        if (danger)
        {
            this.buttonType = ButtonType.DANGER;
        }
    }

    public void setButtonType(ButtonType type)
    {
        this.buttonType = type;
    }

    public ButtonType getButtonType()
    {
        return this.buttonType;
    }

    public void setHovered(boolean hovered)
    {
        if (hovered != this.isHovered)
        {
            this.isHovered = hovered;
        }
        
        if (hovered && hoverProgress < 1.0f)
        {
            hoverProgress += 0.1f;
            if (hoverProgress > 1.0f) hoverProgress = 1.0f;
        }
        else if (!hovered && hoverProgress > 0.0f)
        {
            hoverProgress -= 0.1f;
            if (hoverProgress < 0.0f) hoverProgress = 0.0f;
        }
    }

    public float getHoverProgress()
    {
        return hoverProgress;
    }

    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {

            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && 
                           mouseX < this.xPosition + this.width && 
                           mouseY < this.yPosition + this.height;
            

            this.setHovered(this.hovered);
            

            drawModernButton(mc);
        }
    }

    private void drawModernButton(Minecraft mc)
    {
        Color bgColor, borderColor, textColor;
        int alpha;
        
        if (!this.enabled)
        {
            bgColor = new Color(60, 60, 70, 150);
            borderColor = new Color(80, 80, 90, 200);
            textColor = new Color(120, 120, 130);
        }
        else
        {
            alpha = (int)(200 + 55 * hoverProgress);
            
            switch (buttonType)
            {
                case PRIMARY:
                    bgColor = new Color(50, 180, 80, alpha);
                    borderColor = new Color(80, 220, 110);
                    textColor = Color.WHITE;
                    break;
                case DANGER:
                    bgColor = new Color(180, 50, 50, alpha);
                    borderColor = new Color(220, 80, 70);
                    textColor = Color.WHITE;
                    break;
                default: // NEUTRAL
                    bgColor = new Color(40, 80, 180, alpha);
                    borderColor = new Color(70, 120, 220);
                    textColor = Color.WHITE;
                    break;
            }
        }
        

        drawRect(this.xPosition, this.yPosition, 
                 this.xPosition + this.width, 
                 this.yPosition + this.height, 
                 bgColor.getRGB());
        

        drawHorizontalLine(this.xPosition, this.xPosition + this.width, 
                          this.yPosition, borderColor.getRGB());
        

        drawHorizontalLine(this.xPosition, this.xPosition + this.width, 
                          this.yPosition + this.height - 1, borderColor.getRGB());
        
 
        drawVerticalLine(this.xPosition, this.yPosition, 
                        this.yPosition + this.height, borderColor.getRGB());
        

        drawVerticalLine(this.xPosition + this.width - 1, this.yPosition, 
                        this.yPosition + this.height, borderColor.getRGB());
        

        FontRenderer fontrenderer = mc.fontRendererObj;
        int textWidth = fontrenderer.getStringWidth(this.displayString);
        int textX = this.xPosition + (this.width - textWidth) / 2;
        int textY = this.yPosition + (this.height - 8) / 2;
        

        fontrenderer.drawString(this.displayString, textX + 1, textY + 1, 
                               new Color(0, 0, 0, 100).getRGB());
        

        fontrenderer.drawString(this.displayString, textX, textY, 
                               textColor.getRGB());
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && 
               mouseX >= this.xPosition && mouseY >= this.yPosition && 
               mouseX < this.xPosition + this.width && 
               mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(
            new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
