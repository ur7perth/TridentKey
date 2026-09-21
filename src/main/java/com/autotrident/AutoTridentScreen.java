package com.autotrident;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoTridentScreen extends Screen {

    private static final int BOX = 64;
    private static final long DOUBLE_CLICK_MS = 300;

    private boolean listening = false;
    private long lastClick = 0;

    public AutoTridentScreen() {
        super(Text.literal("TridentKey"));
    }

    private int boxX() { return this.width / 2 - BOX / 2; }
    private int boxY() { return this.height / 2 - BOX / 2; }

    private String keyName() {
        return InputUtil.Type.KEYSYM.createFromCode(AutoTridentConfig.key)
                .getLocalizedText().getString();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int bx = boxX();
        int by = boxY();

        context.drawCenteredTextWithShadow(this.textRenderer, "TridentKey", cx, by - 20, 0xFFFFFF);

        // لون الإطار: أصفر = تغيير الحرف، أخضر = شغال، أحمر = مطفي
        int border;
        if (listening) {
            border = 0xFFFFFF55;
        } else if (AutoTridentConfig.enabled) {
            border = 0xFF55FF55;
        } else {
            border = 0xFFFF5555;
        }

        // المربع
        context.fill(bx, by, bx + BOX, by + BOX, 0xCC000000);
        context.drawBorder(bx, by, BOX, BOX, border);

        // الحرف
        String text = listening ? "?" : keyName();
        int w = this.textRenderer.getWidth(text);
        float scale = (w * 2 <= BOX - 8) ? 2.0f : 1.0f;
        context.getMatrices().push();
        context.getMatrices().translate(cx, by + BOX / 2f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawText(this.textRenderer, text, -w / 2, -4, 0xFFFFFF, true);
        context.getMatrices().pop();

        // الشرح
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Click once: change key", cx, by + BOX + 10, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "In game: press the key to switch to trident", cx, by + BOX + 22, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean inside = mouseX >= boxX() && mouseX <= boxX() + BOX
                    && mouseY >= boxY() && mouseY <= boxY() + BOX;
            if (inside) {
                long now = System.currentTimeMillis();
                if (now - lastClick < DOUBLE_CLICK_MS) {
                    // ضغطتين: تشغيل/إطفاء بدون تغيير الحرف
                    AutoTridentConfig.enabled = !AutoTridentConfig.enabled;
                    AutoTridentConfig.save();
                    listening = false;
                    lastClick = 0;
                } else {
                    // ضغطة وحدة: تغيير الحرف
                    listening = true;
                    lastClick = now;
                }
                return true;
            } else {
                listening = false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                AutoTridentConfig.key = keyCode;
                AutoTridentConfig.save();
            }
            listening = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
