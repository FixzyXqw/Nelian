package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;

import java.io.IOException;

public class NelianHudEditor extends GuiScreen {

    private static final int C_BG = 0x80000000;
    private static final int C_ELEMENT_SELECTED = 0xCC8A2BE2;
    private static final int C_TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int C_SCALE_HANDLE = 0xFFFFFFFF;

    private static final int ELEMENT_FPS = 0;
    private static final int ELEMENT_CPS = 1;
    private static final int ELEMENT_KEYSTROKES = 2;

    public static final CpsCounter INSTANCE = new CpsCounter();

    private int draggingElement = -1;
    private int selectedElement = -1;

    private int dragOffsetX;
    private int dragOffsetY;

    private float targetX;
    private float targetY;

    private boolean scalingElement = false;

    private int scaleStartMouseX;
    private int scaleStartMouseY;
    private int scaleStartKeySize;

    private int dragElementWidth;
    private int dragElementHeight;

    private final GuiScreen parent;

    public NelianHudEditor(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {

        drawRect(
                0,
                0,
                width,
                height,
                C_BG
        );

        updateSmoothPosition();

        renderFPS();
        renderCPS();
        renderKeystrokes();

        mc.fontRendererObj.drawString(
                "Drag HUD elements to move them",
                10,
                height - 25,
                C_TEXT_SECONDARY
        );

        mc.fontRendererObj.drawString(
                "Drag bottom-right corner to resize",
                10,
                height - 12,
                C_TEXT_SECONDARY
        );

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    private void updateSmoothPosition() {

        if (
                draggingElement == -1
                        || scalingElement
        ) {
            return;
        }

        float currentX =
                getElementX(
                        draggingElement
                );

        float currentY =
                getElementY(
                        draggingElement
                );

        float smoothSpeed = 0.45F;

        float newX =
                currentX
                        + (
                        targetX
                                - currentX
                )
                        * smoothSpeed;

        float newY =
                currentY
                        + (
                        targetY
                                - currentY
                )
                        * smoothSpeed;

        if (
                Math.abs(
                        targetX
                                - newX
                ) < 0.5F
        ) {
            newX =
                    targetX;
        }

        if (
                Math.abs(
                        targetY
                                - newY
                ) < 0.5F
        ) {
            newY =
                    targetY;
        }

        setElementPosition(
                draggingElement,
                Math.round(newX),
                Math.round(newY)
        );
    }

    private void renderFPS() {

        if (!Nelianoptions.fpsEnabled) {
            return;
        }

        String fps =
                "["
                        + Minecraft.getDebugFPS()
                        + " FPS]";

        int x =
                Nelianoptions.fpsX;

        int y =
                Nelianoptions.fpsY;

        int elementWidth =
                mc.fontRendererObj.getStringWidth(
                        fps
                );

        int elementHeight =
                mc.fontRendererObj.FONT_HEIGHT;

        mc.fontRendererObj.drawStringWithShadow(
                fps,
                x,
                y,
                0xFFFFFF
        );

        if (
                selectedElement
                        == ELEMENT_FPS
        ) {

            drawSelectionOutline(
                    x,
                    y,
                    elementWidth,
                    elementHeight
            );
        }
    }

    private void renderCPS() {

        if (!Nelianoptions.cpsEnabled) {
            return;
        }

        int leftCPS =
                INSTANCE.getLeftCPS();

        int rightCPS =
                INSTANCE.getRightCPS();

        String fullText =
                "["
                        + leftCPS
                        + " | "
                        + rightCPS
                        + "]";

        int x =
                Nelianoptions.cpsX;

        int y =
                Nelianoptions.cpsY;

        int elementWidth =
                mc.fontRendererObj.getStringWidth(
                        fullText
                );

        int elementHeight =
                mc.fontRendererObj.FONT_HEIGHT;

        int currentX =
                x;

        currentX +=
                drawCpsPart(
                        "[",
                        currentX,
                        y,
                        0xFFFFFF
                );

        currentX +=
                drawCpsPart(
                        String.valueOf(
                                leftCPS
                        ),
                        currentX,
                        y,
                        0xFFFFFF
                );

        currentX +=
                drawCpsPart(
                        " | ",
                        currentX,
                        y,
                        0xAAAAAA
                );

        currentX +=
                drawCpsPart(
                        String.valueOf(
                                rightCPS
                        ),
                        currentX,
                        y,
                        0xFFFFFF
                );

        drawCpsPart(
                "]",
                currentX,
                y,
                0xFFFFFF
        );

        if (
                selectedElement
                        == ELEMENT_CPS
        ) {

            drawSelectionOutline(
                    x,
                    y,
                    elementWidth,
                    elementHeight
            );
        }
    }

    private int drawCpsPart(
            String text,
            int x,
            int y,
            int color
    ) {

        mc.fontRendererObj.drawStringWithShadow(
                text,
                x,
                y,
                color
        );

        return mc.fontRendererObj.getStringWidth(
                text
        );
    }

    private void renderKeystrokes() {

        if (!Nelianoptions.keystrokesEnabled) {
            return;
        }

        int keySize =
                Nelianoptions.keystrokesKeySize;

        int gap =
                Nelianoptions.keystrokesGap;

        int x =
                Nelianoptions.keystrokesX;

        int y =
                Nelianoptions.keystrokesY;

        if (Nelianoptions.keystrokesShowLMB) {

            drawEditorKey(
                    x,
                    y,
                    keySize,
                    keySize,
                    "LMB",
                    5
            );
        }

        if (Nelianoptions.keystrokesShowWASD) {

            drawEditorKey(
                    x + keySize + gap,
                    y,
                    keySize,
                    keySize,
                    "W",
                    0
            );
        }

        if (Nelianoptions.keystrokesShowRMB) {

            drawEditorKey(
                    x + (
                            keySize
                                    + gap
                    ) * 2,
                    y,
                    keySize,
                    keySize,
                    "RMB",
                    6
            );
        }

        if (Nelianoptions.keystrokesShowWASD) {

            drawEditorKey(
                    x,
                    y + keySize + gap,
                    keySize,
                    keySize,
                    "A",
                    1
            );

            drawEditorKey(
                    x + keySize + gap,
                    y + keySize + gap,
                    keySize,
                    keySize,
                    "S",
                    2
            );

            drawEditorKey(
                    x + (
                            keySize
                                    + gap
                    ) * 2,
                    y + keySize + gap,
                    keySize,
                    keySize,
                    "D",
                    3
            );
        }

        if (Nelianoptions.keystrokesShowSpace) {

            drawEditorKey(
                    x,
                    y + (
                            keySize
                                    + gap
                    ) * 2,
                    keySize * 3 + gap * 2,
                    keySize,
                    "SPACE",
                    4
            );
        }

        if (
                selectedElement
                        == ELEMENT_KEYSTROKES
        ) {

            int totalWidth =
                    getElementWidth(
                            ELEMENT_KEYSTROKES
                    );

            int totalHeight =
                    getElementHeight(
                            ELEMENT_KEYSTROKES
                    );

            drawSelectionOutline(
                    x,
                    y,
                    totalWidth,
                    totalHeight
            );

            drawScaleHandle(
                    x + totalWidth,
                    y + totalHeight
            );
        }
    }

    private void drawEditorKey(
            int x,
            int y,
            int width,
            int height,
            String text,
            int key
    ) {

        int color =
                Nelianoptions.keystrokesColorNormal;

        boolean pressed = false;

        switch (key) {

            case 0:

                pressed =
                        org.lwjgl.input.Keyboard.isKeyDown(
                                org.lwjgl.input.Keyboard.KEY_W
                        );

                break;

            case 1:

                pressed =
                        org.lwjgl.input.Keyboard.isKeyDown(
                                org.lwjgl.input.Keyboard.KEY_A
                        );

                break;

            case 2:

                pressed =
                        org.lwjgl.input.Keyboard.isKeyDown(
                                org.lwjgl.input.Keyboard.KEY_S
                        );

                break;

            case 3:

                pressed =
                        org.lwjgl.input.Keyboard.isKeyDown(
                                org.lwjgl.input.Keyboard.KEY_D
                        );

                break;

            case 4:

                pressed =
                        org.lwjgl.input.Keyboard.isKeyDown(
                                org.lwjgl.input.Keyboard.KEY_SPACE
                        );

                break;

            case 5:

                pressed =
                        org.lwjgl.input.Mouse.isButtonDown(
                                0
                        );

                break;

            case 6:

                pressed =
                        org.lwjgl.input.Mouse.isButtonDown(
                                1
                        );

                break;
        }

        if (pressed) {

            color =
                    Nelianoptions.keystrokesColorPressed;
        }

        if (Nelianoptions.keystrokesBackground) {

            if (
                    Nelianoptions.keystrokesRoundedCorners
            ) {

                drawRoundedRect(
                        x,
                        y,
                        x + width,
                        y + height,
                        4,
                        color
                );

            } else {

                drawRect(
                        x,
                        y,
                        x + width,
                        y + height,
                        color
                );
            }
        }

        int textWidth =
                mc.fontRendererObj.getStringWidth(
                        text
                );

        int textX =
                x
                        + (
                        width
                                - textWidth
                ) / 2;

        int textY =
                y
                        + (
                        height
                                - mc.fontRendererObj.FONT_HEIGHT
                ) / 2;

        mc.fontRendererObj.drawString(
                text,
                textX + 1,
                textY + 1,
                0xAA000000
        );

        mc.fontRendererObj.drawString(
                text,
                textX,
                textY,
                Nelianoptions.keystrokesColorText
        );
    }

    private void drawSelectionOutline(
            int x,
            int y,
            int width,
            int height
    ) {

        int color =
                C_ELEMENT_SELECTED;

        drawRect(
                x - 2,
                y - 2,
                x + width + 2,
                y,
                color
        );

        drawRect(
                x - 2,
                y + height,
                x + width + 2,
                y + height + 2,
                color
        );

        drawRect(
                x - 2,
                y,
                x,
                y + height,
                color
        );

        drawRect(
                x + width,
                y,
                x + width + 2,
                y + height,
                color
        );
    }

    private void drawScaleHandle(
            int x,
            int y
    ) {

        drawRect(
                x - 5,
                y - 5,
                x + 5,
                y + 5,
                C_SCALE_HANDLE
        );

        drawRect(
                x - 3,
                y - 3,
                x + 3,
                y + 3,
                C_ELEMENT_SELECTED
        );
    }

    private boolean isScaleHandleHovered(
            int mouseX,
            int mouseY
    ) {

        if (
                selectedElement
                        != ELEMENT_KEYSTROKES
        ) {
            return false;
        }

        int x =
                Nelianoptions.keystrokesX
                        + getElementWidth(
                        ELEMENT_KEYSTROKES
                );

        int y =
                Nelianoptions.keystrokesY
                        + getElementHeight(
                        ELEMENT_KEYSTROKES
                );

        return mouseX >= x - 7
                && mouseX <= x + 7
                && mouseY >= y - 7
                && mouseY <= y + 7;
    }

    private void drawRoundedRect(
            int left,
            int top,
            int right,
            int bottom,
            int radius,
            int color
    ) {

        drawRect(
                left + radius,
                top,
                right - radius,
                bottom,
                color
        );

        drawRect(
                left,
                top + radius,
                right,
                bottom - radius,
                color
        );

        drawRect(
                left,
                top,
                left + radius,
                top + radius,
                color
        );

        drawRect(
                right - radius,
                top,
                right,
                top + radius,
                color
        );

        drawRect(
                left,
                bottom - radius,
                left + radius,
                bottom,
                color
        );

        drawRect(
                right - radius,
                bottom - radius,
                right,
                bottom,
                color
        );
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {

        if (mouseButton != 0) {

            super.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton
            );

            return;
        }

        if (
                selectedElement
                        == ELEMENT_KEYSTROKES
                        && isScaleHandleHovered(
                        mouseX,
                        mouseY
                )
        ) {

            scalingElement = true;

            scaleStartMouseX =
                    mouseX;

            scaleStartMouseY =
                    mouseY;

            scaleStartKeySize =
                    Nelianoptions.keystrokesKeySize;

            return;
        }

        int element =
                getElementAt(
                        mouseX,
                        mouseY
                );

        if (element != -1) {

            draggingElement =
                    element;

            selectedElement =
                    element;

            dragOffsetX =
                    mouseX
                            - getElementX(
                            element
                    );

            dragOffsetY =
                    mouseY
                            - getElementY(
                            element
                    );

            targetX =
                    getElementX(
                            element
                    );

            targetY =
                    getElementY(
                            element
                    );

            dragElementWidth =
                    getElementWidth(
                            element
                    );

            dragElementHeight =
                    getElementHeight(
                            element
                    );

            return;
        }

        selectedElement = -1;

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    @Override
    protected void mouseClickMove(
            int mouseX,
            int mouseY,
            int clickedMouseButton,
            long timeSinceLastClick
    ) {

        if (
                scalingElement
                        && clickedMouseButton == 0
        ) {

            int deltaX =
                    mouseX
                            - scaleStartMouseX;

            int deltaY =
                    mouseY
                            - scaleStartMouseY;

            int delta =
                    Math.max(
                            deltaX,
                            deltaY
                    );

            int newKeySize =
                    scaleStartKeySize
                            + delta;

            newKeySize =
                    Math.max(
                            10,
                            Math.min(
                                    100,
                                    newKeySize
                            )
                    );

            Nelianoptions.keystrokesKeySize =
                    newKeySize;

            int elementWidth =
                    getElementWidth(
                            ELEMENT_KEYSTROKES
                    );

            int elementHeight =
                    getElementHeight(
                            ELEMENT_KEYSTROKES
                    );

            int currentX =
                    Nelianoptions.keystrokesX;

            int currentY =
                    Nelianoptions.keystrokesY;

            if (
                    currentX
                            + elementWidth
                            > width
            ) {

                Nelianoptions.keystrokesX =
                        Math.max(
                                0,
                                width
                                        - elementWidth
                        );
            }

            if (
                    currentY
                            + elementHeight
                            > height
            ) {

                Nelianoptions.keystrokesY =
                        Math.max(
                                0,
                                height
                                        - elementHeight
                        );
            }

            return;
        }

        if (
                draggingElement != -1
                        && clickedMouseButton == 0
        ) {

            targetX =
                    mouseX
                            - dragOffsetX;

            targetY =
                    mouseY
                            - dragOffsetY;

            float maxX =
                    Math.max(
                            0,
                            width
                                    - dragElementWidth
                    );

            float maxY =
                    Math.max(
                            0,
                            height
                                    - dragElementHeight
                    );

            targetX =
                    Math.max(
                            0,
                            Math.min(
                                    maxX,
                                    targetX
                            )
                    );

            targetY =
                    Math.max(
                            0,
                            Math.min(
                                    maxY,
                                    targetY
                            )
                    );
        }

        super.mouseClickMove(
                mouseX,
                mouseY,
                clickedMouseButton,
                timeSinceLastClick
        );
    }

    @Override
    protected void mouseReleased(
            int mouseX,
            int mouseY,
            int state
    ) {

        if (state == 0) {

            if (scalingElement) {

                scalingElement = false;

                Nelianoptions.save();

                return;
            }

            if (draggingElement != -1) {

                targetX =
                        Math.max(
                                0,
                                Math.min(
                                        width
                                                - dragElementWidth,
                                        targetX
                                )
                        );

                targetY =
                        Math.max(
                                0,
                                Math.min(
                                        height
                                                - dragElementHeight,
                                        targetY
                                )
                        );

                setElementPosition(
                        draggingElement,
                        Math.round(targetX),
                        Math.round(targetY)
                );

                Nelianoptions.save();

                draggingElement = -1;
            }
        }

        super.mouseReleased(
                mouseX,
                mouseY,
                state
        );
    }

    private int getElementAt(
            int mouseX,
            int mouseY
    ) {

        if (
                Nelianoptions.fpsEnabled
                        && isInside(
                        mouseX,
                        mouseY,
                        Nelianoptions.fpsX,
                        Nelianoptions.fpsY,
                        getElementWidth(
                                ELEMENT_FPS
                        ),
                        getElementHeight(
                                ELEMENT_FPS
                        )
                )
        ) {

            return ELEMENT_FPS;
        }

        if (
                Nelianoptions.cpsEnabled
                        && isInside(
                        mouseX,
                        mouseY,
                        Nelianoptions.cpsX,
                        Nelianoptions.cpsY,
                        getElementWidth(
                                ELEMENT_CPS
                        ),
                        getElementHeight(
                                ELEMENT_CPS
                        )
                )
        ) {

            return ELEMENT_CPS;
        }

        if (
                Nelianoptions.keystrokesEnabled
                        && isInside(
                        mouseX,
                        mouseY,
                        Nelianoptions.keystrokesX,
                        Nelianoptions.keystrokesY,
                        getElementWidth(
                                ELEMENT_KEYSTROKES
                        ),
                        getElementHeight(
                                ELEMENT_KEYSTROKES
                        )
                )
        ) {

            return ELEMENT_KEYSTROKES;
        }

        return -1;
    }

    private boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    private int getElementX(
            int element
    ) {

        switch (element) {

            case ELEMENT_FPS:

                return Nelianoptions.fpsX;

            case ELEMENT_CPS:

                return Nelianoptions.cpsX;

            case ELEMENT_KEYSTROKES:

                return Nelianoptions.keystrokesX;

            default:

                return 0;
        }
    }

    private int getElementY(
            int element
    ) {

        switch (element) {

            case ELEMENT_FPS:

                return Nelianoptions.fpsY;

            case ELEMENT_CPS:

                return Nelianoptions.cpsY;

            case ELEMENT_KEYSTROKES:

                return Nelianoptions.keystrokesY;

            default:

                return 0;
        }
    }

    private int getElementWidth(
            int element
    ) {

        switch (element) {

            case ELEMENT_FPS:

                return mc.fontRendererObj.getStringWidth(
                        "["
                                + Minecraft.getDebugFPS()
                                + " FPS]"
                );

            case ELEMENT_CPS:

                return mc.fontRendererObj.getStringWidth(
                        "[0 | 0]"
                );

            case ELEMENT_KEYSTROKES:

                return Nelianoptions.keystrokesKeySize * 3
                        + Nelianoptions.keystrokesGap * 2;

            default:

                return 0;
        }
    }

    private int getElementHeight(
            int element
    ) {

        switch (element) {

            case ELEMENT_FPS:

                return mc.fontRendererObj.FONT_HEIGHT;

            case ELEMENT_CPS:

                return mc.fontRendererObj.FONT_HEIGHT;

            case ELEMENT_KEYSTROKES:

                return Nelianoptions.keystrokesKeySize * 3
                        + Nelianoptions.keystrokesGap * 2;

            default:

                return 0;
        }
    }

    private void setElementPosition(
            int element,
            int x,
            int y
    ) {

        switch (element) {

            case ELEMENT_FPS:

                Nelianoptions.fpsX =
                        x;

                Nelianoptions.fpsY =
                        y;

                break;

            case ELEMENT_CPS:

                Nelianoptions.cpsX =
                        x;

                Nelianoptions.cpsY =
                        y;

                break;

            case ELEMENT_KEYSTROKES:

                Nelianoptions.keystrokesX =
                        x;

                Nelianoptions.keystrokesY =
                        y;

                break;
        }
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode
    ) throws IOException {

        if (keyCode == 1) {

            Nelianoptions.save();

            Minecraft.getMinecraft()
                    .displayGuiScreen(
                            parent
                    );

            return;
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {

        Nelianoptions.save();

        super.onGuiClosed();
    }
}
