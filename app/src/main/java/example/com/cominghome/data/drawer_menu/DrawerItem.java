package example.com.cominghome.data.drawer_menu;

/**
 * Created by Loner on 23.05.2015.
 */
public class DrawerItem {
    public int icon;
    public String name;

    public DrawerItem(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

}
