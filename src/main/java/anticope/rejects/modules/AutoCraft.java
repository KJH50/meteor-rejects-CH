package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;

public class AutoCraft extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("物品")
        .description("你想要自动合成的物品。")
        .defaultValue(List.of())
        .build()
    );

    private final Setting<Boolean> antiDesync = sgGeneral.add(new BoolSetting.Builder()
            .name("防失步")
            .description("尝试防止物品栏失步。")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> craftAll = sgGeneral.add(new BoolSetting.Builder()
            .name("全部合成")
            .description("每次合成时尽可能多地合成物品（按住Shift点击）。")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> drop = sgGeneral.add(new BoolSetting.Builder()
            .name("自动丢弃")
            .description("自动丢弃合成后的物品（当物品栏空间不足时有用）。")
            .defaultValue(false)
            .build()
    );

    public AutoCraft() {
        super(MeteorRejectsAddon.CATEGORY, "自动合成", "自动合成物品。");
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate() || mc.interactionManager == null) return;

        if (items.get().isEmpty()) return;

        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler)) return;

        if (antiDesync.get()) 
            mc.player.getInventory().updateItems();

        // Danke schön GhostTypes
        // https://github.com/GhostTypes/orion/blob/main/src/main/java/me/ghosttypes/orion/modules/main/AutoBedCraft.java
        CraftingScreenHandler currentScreenHandler = (CraftingScreenHandler) mc.player.currentScreenHandler;
        List<Item> itemList = items.get();
        List<RecipeResultCollection> recipeResultCollectionList  = mc.player.getRecipeBook().getOrderedResults();
        for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
            // Get craftable recipes only
            List<RecipeDisplayEntry> craftRecipes = recipeResultCollection.filter(RecipeResultCollection.RecipeFilterMode.CRAFTABLE);
            for (RecipeDisplayEntry recipe : craftRecipes) {
                RecipeDisplay recipeDisplay = recipe.display();
                List<ItemStack> resultStacks = recipeDisplay.result().getStacks(SlotDisplayContexts.createParameters(mc.world));
                for (ItemStack resultStack : resultStacks) {
                    // Check if the result item is in the item list
                    if (!itemList.contains(resultStack.getItem())) continue;

                    mc.interactionManager.clickRecipe(currentScreenHandler.syncId, recipe.id(), craftAll.get());
                    mc.interactionManager.clickSlot(currentScreenHandler.syncId, 0, 1,
                            drop.get() ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, mc.player);
                }
            }
        }
    }
}
