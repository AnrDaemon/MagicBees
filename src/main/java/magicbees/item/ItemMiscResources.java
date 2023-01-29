package magicbees.item;

import java.util.List;

import magicbees.item.types.ResourceType;
import magicbees.main.CommonProxy;
import magicbees.main.utils.TabMagicBees;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMiscResources extends Item {

    private IIcon[] icons = new IIcon[ResourceType.values().length];

    public ItemMiscResources() {
        super();
        this.setCreativeTab(TabMagicBees.tabMagicBees);
        this.setHasSubtypes(true);
        this.setUnlocalizedName("miscResources");
        GameRegistry.registerItem(this, "miscResources");
    }

    public ItemStack getStackForType(ResourceType type) {
        return new ItemStack(this, 1, type.ordinal());
    }

    public ItemStack getStackForType(ResourceType type, int count) {
        return new ItemStack(this, count, type.ordinal());
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
        for (ResourceType type : ResourceType.values()) {
            if (type.showInList) {
                list.add(this.getStackForType(type));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        for (int i = 0; i < ResourceType.values().length; i++) {
            this.icons[i] = par1IconRegister
                    .registerIcon(CommonProxy.DOMAIN + ":" + ResourceType.values()[i].getName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return icons[meta];
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return ResourceType.values()[stack.getItemDamage()].getLocalizedName();
    }
}
