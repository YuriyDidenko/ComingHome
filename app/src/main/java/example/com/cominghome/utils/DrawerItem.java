package example.com.cominghome.utils;

/**
 * Created by Loner on 23.05.2015.
 */
public class DrawerItem {
    public int icon;
    public String name;

    // Constructor.
    public DrawerItem(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
