package top.steve3184.mapvnc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class VNCKeyMap {

    private static final Map<String, Integer> KEY_MAP = new ConcurrentHashMap<>();

    static {
        KEY_MAP.put("A", 0x0041);
        KEY_MAP.put("B", 0x0042);
        KEY_MAP.put("C", 0x0043);
        KEY_MAP.put("D", 0x0044);
        KEY_MAP.put("E", 0x0045);
        KEY_MAP.put("F", 0x0046);
        KEY_MAP.put("G", 0x0047);
        KEY_MAP.put("H", 0x0048);
        KEY_MAP.put("I", 0x0049);
        KEY_MAP.put("J", 0x004a);
        KEY_MAP.put("K", 0x004b);
        KEY_MAP.put("L", 0x004c);
        KEY_MAP.put("M", 0x004d);
        KEY_MAP.put("N", 0x004e);
        KEY_MAP.put("O", 0x004f);
        KEY_MAP.put("P", 0x0050);
        KEY_MAP.put("Q", 0x0051);
        KEY_MAP.put("R", 0x0052);
        KEY_MAP.put("S", 0x0053);
        KEY_MAP.put("T", 0x0054);
        KEY_MAP.put("U", 0x0055);
        KEY_MAP.put("V", 0x0056);
        KEY_MAP.put("W", 0x0057);
        KEY_MAP.put("X", 0x0058);
        KEY_MAP.put("Y", 0x0059);
        KEY_MAP.put("Z", 0x005a);

        KEY_MAP.put("0", 0x0030);
        KEY_MAP.put("1", 0x0031);
        KEY_MAP.put("2", 0x0032);
        KEY_MAP.put("3", 0x0033);
        KEY_MAP.put("4", 0x0034);
        KEY_MAP.put("5", 0x0035);
        KEY_MAP.put("6", 0x0036);
        KEY_MAP.put("7", 0x0037);
        KEY_MAP.put("8", 0x0038);
        KEY_MAP.put("9", 0x0039);

        KEY_MAP.put("BACK_QUOTE", 0x0060);    // ` (XK_grave)
        KEY_MAP.put("MINUS", 0x002d);         // - (XK_minus)
        KEY_MAP.put("EQUALS", 0x003d);        // = (XK_equal)
        KEY_MAP.put("OPEN_BRACKET", 0x005b);  // [ (XK_bracketleft)
        KEY_MAP.put("CLOSE_BRACKET", 0x005d); // ] (XK_bracketright)
        KEY_MAP.put("BACK_SLASH", 0x005c);    // \ (XK_backslash)
        KEY_MAP.put("SEMICOLON", 0x003b);     // ; (XK_semicolon)
        KEY_MAP.put("QUOTE", 0x0027);         // ' (XK_apostrophe)
        KEY_MAP.put("COMMA", 0x002c);         // , (XK_comma)
        KEY_MAP.put("PERIOD", 0x002e);        // . (XK_period)
        KEY_MAP.put("SLASH", 0x002f);         // / (XK_slash)

        KEY_MAP.put("F1", 0xffbe);
        KEY_MAP.put("F2", 0xffbf);
        KEY_MAP.put("F3", 0xffc0);
        KEY_MAP.put("F4", 0xffc1);
        KEY_MAP.put("F5", 0xffc2);
        KEY_MAP.put("F6", 0xffc3);
        KEY_MAP.put("F7", 0xffc4);
        KEY_MAP.put("F8", 0xffc5);
        KEY_MAP.put("F9", 0xffc6);
        KEY_MAP.put("F10", 0xffc7);
        KEY_MAP.put("F11", 0xffc8);
        KEY_MAP.put("F12", 0xffc9);

        KEY_MAP.put("ENTER", 0xff0d);          // (XK_Return)
        KEY_MAP.put("BACKSPACE", 0xff08);      // (XK_BackSpace)
        KEY_MAP.put("TAB", 0xff09);            // (XK_Tab)
        KEY_MAP.put("SHIFT", 0xffe1);          // (XK_Shift_L)
        KEY_MAP.put("CONTROL", 0xffe3);        // (XK_Control_L)
        KEY_MAP.put("ALT", 0xffe9);            // (XK_Alt_L)
        KEY_MAP.put("WINDOWS", 0xffeb);        // (XK_Super_L)
        KEY_MAP.put("ESCAPE", 0xff1b);         // (XK_Escape)
        KEY_MAP.put("SPACE", 0x0020);          // (XK_space)
        KEY_MAP.put("PAGE_UP", 0xff55);        // (XK_Page_Up)
        KEY_MAP.put("PAGE_DOWN", 0xff56);      // (XK_Page_Down)
        KEY_MAP.put("END", 0xff57);            // (XK_End)
        KEY_MAP.put("HOME", 0xff50);           // (XK_Home)
        KEY_MAP.put("INSERT", 0xff63);         // (XK_Insert)
        KEY_MAP.put("DELETE", 0xffff);         // (XK_Delete)
        KEY_MAP.put("CAPS_LOCK", 0xffe5);      // (XK_Caps_Lock)
        KEY_MAP.put("NUM_LOCK", 0xff7f);       // (XK_Num_Lock)
        KEY_MAP.put("SCROLL_LOCK", 0xff14);    // (XK_Scroll_Lock)
        KEY_MAP.put("PRINT_SCREEN", 0xff61);   // (XK_Print)
        KEY_MAP.put("PAUSE", 0xff13);          // (XK_Pause)
        KEY_MAP.put("MENU", 0xff67);           // (XK_Menu)

        KEY_MAP.put("LEFT", 0xff51);           // (XK_Left)
        KEY_MAP.put("UP", 0xff52);             // (XK_Up)
        KEY_MAP.put("RIGHT", 0xff53);          // (XK_Right)
        KEY_MAP.put("DOWN", 0xff54);           // (XK_Down)

        KEY_MAP.put("NUMPAD0", 0xffb0);         // (XK_KP_0)
        KEY_MAP.put("NUMPAD1", 0xffb1);         // (XK_KP_1)
        KEY_MAP.put("NUMPAD2", 0xffb2);         // (XK_KP_2)
        KEY_MAP.put("NUMPAD3", 0xffb3);         // (XK_KP_3)
        KEY_MAP.put("NUMPAD4", 0xffb4);         // (XK_KP_4)
        KEY_MAP.put("NUMPAD5", 0xffb5);         // (XK_KP_5)
        KEY_MAP.put("NUMPAD6", 0xffb6);         // (XK_KP_6)
        KEY_MAP.put("NUMPAD7", 0xffb7);         // (XK_KP_7)
        KEY_MAP.put("NUMPAD8", 0xffb8);         // (XK_KP_8)
        KEY_MAP.put("NUMPAD9", 0xffb9);         // (XK_KP_9)
        KEY_MAP.put("NUMPAD_DIVIDE", 0xffaf);   // (XK_KP_Divide)
        KEY_MAP.put("NUMPAD_MULTIPLY", 0xffaa); // (XK_KP_Multiply)
        KEY_MAP.put("NUMPAD_SUBTRACT", 0xffad); // (XK_KP_Subtract)
        KEY_MAP.put("NUMPAD_ADD", 0xffab);      // (XK_KP_Add)
        KEY_MAP.put("NUMPAD_DECIMAL", 0xffae);  // (XK_KP_Decimal)
        KEY_MAP.put("NUMPAD_ENTER", 0xff8d);    // (XK_KP_Enter)
    }

    public static Integer getKeyCode(String name) {
        if (name == null) {
            return null;
        }
        return KEY_MAP.get(name.toUpperCase());
    }

    public static List<String> getKeyNames() {
        return KEY_MAP.keySet().stream().sorted().collect(Collectors.toList());
    }

    private VNCKeyMap() {}
}