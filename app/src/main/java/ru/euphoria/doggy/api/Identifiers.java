package ru.euphoria.doggy.api;

/**
 * IDs of messaging apps
 */
public class Identifiers {
    /**
     * Official clients
     */
    public static final int ANDROID_OFFICIAL = 2274003;
    public static final int IPHONE_OFFICIAL = 3140623;
    public static final int IPAD_OFFICIAL = 3682744;
    public static final int WP_OFFICIAL = 3502557;
    public static final int WP_OFFICIAL_NEW = 3502561;
    public static final int WINDOWS_OFFICIAL = 3697615;

    /**
     * Unofficial client, mods and messengers
     */
    public static final int KATE_MOBILE = 2685278;
    public static final int EUPHORIA = 4510232;
    public static final int LYNT = 3469984;
    public static final int SWEET = 4856309;
    public static final int AMBERFOG = 4445970;
    public static final int PHOENIX = 4994316;
    public static final int MESSENGER = 4894723;
    public static final int ZEUS = 4831060;
    public static final int ROCKET = 4757672;
    public static final int VK_MD = 4967124;
    public static final int MP3_MOD = 4996844;
    public static final int FAST = 5021699;
    public static final int DOGGY = 6418289;


    public static String toString(int appId) {
        String name = "";
        switch (appId) {
            case ANDROID_OFFICIAL:
                name = "Android";
                break;
            case IPHONE_OFFICIAL:
                name = "iPhone";
                break;
            case IPAD_OFFICIAL:
                name = "iPad";
                break;
            case WP_OFFICIAL:
            case WP_OFFICIAL_NEW:
                name = "WP";
                break;
            case WINDOWS_OFFICIAL:
                name = "Win";
                break;

            case KATE_MOBILE:
                name = "Kate Mobile";
                break;
            case EUPHORIA:
                name = "Euphoria";
                break;
            case LYNT:
                name = "Lynt";
                break;
            case SWEET:
                name = "Sweet";
                break;
            case AMBERFOG:
                name = "Amberfog";
                break;
            case PHOENIX:
                name = "Phoenix";
                break;
            case MESSENGER:
                name = "Messenger";
                break;
            case ZEUS:
                name = "Zeus";
                break;
            case ROCKET:
                name = "Rocket";
                break;
            case VK_MD:
                name = "VK MD";
                break;
            case MP3_MOD:
                name = "MP3 Mod";
                break;
            case FAST:
                name = "Fast";
                break;
            case DOGGY:
                name = "Doggy";
                break;

            case 0:
                name = "Mobile";
        }
        return name;
    }
}