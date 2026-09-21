Enterpackage com.autotrident;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;

public class AutoTridentClient implements ClientModInitializer {

    /** عدد التكات اللازمة قبل ما يصير الرمي ممكن. لو السيرفر لاق زدها لـ 11 */
    private static final int THROW_TICKS = 10;

    private static boolean keyWasDown = false;
    private static boolean menuKeyWasDown = false;
    private static boolean openMenuRequested = false;

    @Override
    public void onInitializeClient() {
        AutoTridentConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(AutoTridentClient::tick);

        // بديل لفتح القائمة: الأمر /autotrident
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("autotrident").executes(ctx -> {
                    openMenuRequested = true;
                    return 1;
                })));
    }

    private static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        long handle = mc.getWindow().getHandle();

        boolean menuDown = InputUtil.isKeyPressed(handle, AutoTridentConfig.OPEN_MENU_KEY);
        boolean keyDown = InputUtil.isKeyPressed(handle, AutoTridentConfig.key);

        boolean menuPressed = menuDown && !menuKeyWasDown;
        boolean keyPressed = keyDown && !keyWasDown;
        menuKeyWasDown = menuDown;
        keyWasDown = keyDown;

        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null) return;

        // فتح القائمة
        if ((menuPressed || openMenuRequested) && mc.currentScreen == null) {
            openMenuRequested = false;
            mc.setScreen(new AutoTridentScreen());
            return;
        }
        if (mc.currentScreen != null) return;

        // ضغطة الحرف: ينقلك للترايدنت فقط
        if (keyPressed) {
            int slot = nextTridentSlot(player, player.getInventory().selectedSlot);
            if (slot != -1) {
                player.getInventory().selectedSlot = slot;
            }
        }

        // الرمي التلقائي
        if (!AutoTridentConfig.enabled) return;
        if (!mc.options.useKey.isPressed()) return;
        if (!player.isUsingItem()) return;

        ItemStack active = player.getActiveItem();
        if (!(active.getItem() instanceof TridentItem)) return;
        // ترايدنت الـ Riptide نتركه للسلوك الطبيعي
        if (EnchantmentHelper.getTridentSpinAttackStrength(active, player) > 0) return;
        if (player.getItemUseTime() < THROW_TICKS) return;

        Hand hand = player.getActiveHand();

        // رمي الترايدنت (نفس رفع اليد عن الرايت كلك)
        mc.interactionManager.stopUsingItem(player);

        // التحويل للترايدنت اللي بعده، وطالما الرايت كلك مضغوط يبدأ يشحنه ويرميه تلقائي
        if (hand == Hand.MAIN_HAND) {
            int slot = nextTridentSlot(player, player.getInventory().selectedSlot);
            if (slot != -1) {
                player.getInventory().selectedSlot = slot;
            }
        }
    }

    /** يبحث في الهوتبار عن ترايدنت بعد السلوت الحالي (يلف دائري) */
    private static int nextTridentSlot(PlayerEntity player, int current) {
        for (int i = 1; i < 9; i++) {
            int slot = (current + i) % 9;
            if (player.getInventory().getStack(slot).getItem() instanceof TridentItem) {
                return slot;
            }
        }
        return -1;
    }
}
