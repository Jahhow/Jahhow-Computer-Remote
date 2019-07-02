package c.jahhow.remotecontroller.Msg;

// Microsoft Virtual-Key Code
public final class MSVK {
	/*
	 * Virtual Keys, Standard Set
	 */
	static final byte VK_LBUTTON      = (byte) 0x01;
	static final byte VK_RBUTTON      = (byte) 0x02;
	static final byte VK_CANCEL       = (byte) 0x03;
	static final byte VK_MBUTTON      = (byte) 0x04;    /* NOT contiguous with L & RBUTTON */

	// #if(_WIN32_WINNT >= 0x0500)
	static final byte VK_XBUTTON1     = (byte) 0x05;    /* NOT contiguous with L & RBUTTON */
	static final byte VK_XBUTTON2     = (byte) 0x06;    /* NOT contiguous with L & RBUTTON */
	// #endif /* _WIN32_WINNT >= 0x0500 */

	/*
	 * 0x07 : reserved
	 */


	static final byte VK_BACK         = (byte) 0x08;
	static final byte VK_TAB          = (byte) 0x09;

	/*
	 * 0x0A - 0x0B : reserved
	 */

	static final byte VK_CLEAR        = (byte) 0x0C;
	static final byte VK_RETURN       = (byte) 0x0D;

	/*
	 * 0x0E - 0x0F : unassigned
	 */

	static final byte VK_SHIFT        = (byte) 0x10;
	static final byte VK_CONTROL      = (byte) 0x11;
	static final byte VK_MENU         = (byte) 0x12;
	static final byte VK_PAUSE        = (byte) 0x13;
	static final byte VK_CAPITAL      = (byte) 0x14;

	static final byte VK_KANA         = (byte) 0x15;
	static final byte VK_HANGEUL      = (byte) 0x15;  /* old name - should be here for compatibility */
	static final byte VK_HANGUL       = (byte) 0x15;

	/*
	 * 0x16 : unassigned
	 */

	static final byte VK_JUNJA        = (byte) 0x17;
	static final byte VK_FINAL        = (byte) 0x18;
	static final byte VK_HANJA        = (byte) 0x19;
	static final byte VK_KANJI        = (byte) 0x19;

	/*
	 * 0x1A : unassigned
	 */

	static final byte VK_ESCAPE       = (byte) 0x1B;

	static final byte VK_CONVERT      = (byte) 0x1C;
	static final byte VK_NONCONVERT   = (byte) 0x1D;
	static final byte VK_ACCEPT       = (byte) 0x1E;
	static final byte VK_MODECHANGE   = (byte) 0x1F;

	static final byte VK_SPACE        = (byte) 0x20;
	static final byte VK_PRIOR        = (byte) 0x21;
	static final byte VK_NEXT         = (byte) 0x22;
	static final byte VK_END          = (byte) 0x23;
	static final byte VK_HOME         = (byte) 0x24;
	static final byte VK_LEFT         = (byte) 0x25;
	static final byte VK_UP           = (byte) 0x26;
	static final byte VK_RIGHT        = (byte) 0x27;
	static final byte VK_DOWN         = (byte) 0x28;
	static final byte VK_SELECT       = (byte) 0x29;
	static final byte VK_PRINT        = (byte) 0x2A;
	static final byte VK_EXECUTE      = (byte) 0x2B;
	static final byte VK_SNAPSHOT     = (byte) 0x2C;
	static final byte VK_INSERT       = (byte) 0x2D;
	static final byte VK_DELETE       = (byte) 0x2E;
	static final byte VK_HELP         = (byte) 0x2F;

	/*
	 * VK_0 - VK_9 are the same as ASCII '0' - '9' (0x30 - 0x39)
	 * 0x3A - 0x40 : unassigned
	 * VK_A - VK_Z are the same as ASCII 'A' - 'Z' (0x41 - 0x5A)
	 */

	static final byte VK_LWIN         = (byte) 0x5B;
	static final byte VK_RWIN         = (byte) 0x5C;
	static final byte VK_APPS         = (byte) 0x5D;

	/*
	 * 0x5E : reserved
	 */

	static final byte VK_SLEEP        = (byte) 0x5F;

	static final byte VK_NUMPAD0      = (byte) 0x60;
	static final byte VK_NUMPAD1      = (byte) 0x61;
	static final byte VK_NUMPAD2      = (byte) 0x62;
	static final byte VK_NUMPAD3      = (byte) 0x63;
	static final byte VK_NUMPAD4      = (byte) 0x64;
	static final byte VK_NUMPAD5      = (byte) 0x65;
	static final byte VK_NUMPAD6      = (byte) 0x66;
	static final byte VK_NUMPAD7      = (byte) 0x67;
	static final byte VK_NUMPAD8      = (byte) 0x68;
	static final byte VK_NUMPAD9      = (byte) 0x69;
	static final byte VK_MULTIPLY     = (byte) 0x6A;
	static final byte VK_ADD          = (byte) 0x6B;
	static final byte VK_SEPARATOR    = (byte) 0x6C;
	static final byte VK_SUBTRACT     = (byte) 0x6D;
	static final byte VK_DECIMAL      = (byte) 0x6E;
	static final byte VK_DIVIDE       = (byte) 0x6F;
	static final byte VK_F1           = (byte) 0x70;
	static final byte VK_F2           = (byte) 0x71;
	static final byte VK_F3           = (byte) 0x72;
	static final byte VK_F4           = (byte) 0x73;
	static final byte VK_F5           = (byte) 0x74;
	static final byte VK_F6           = (byte) 0x75;
	static final byte VK_F7           = (byte) 0x76;
	static final byte VK_F8           = (byte) 0x77;
	static final byte VK_F9           = (byte) 0x78;
	static final byte VK_F10          = (byte) 0x79;
	static final byte VK_F11          = (byte) 0x7A;
	static final byte VK_F12          = (byte) 0x7B;
	static final byte VK_F13          = (byte) 0x7C;
	static final byte VK_F14          = (byte) 0x7D;
	static final byte VK_F15          = (byte) 0x7E;
	static final byte VK_F16          = (byte) 0x7F;
	static final byte VK_F17          = (byte) 0x80;
	static final byte VK_F18          = (byte) 0x81;
	static final byte VK_F19          = (byte) 0x82;
	static final byte VK_F20          = (byte) 0x83;
	static final byte VK_F21          = (byte) 0x84;
	static final byte VK_F22          = (byte) 0x85;
	static final byte VK_F23          = (byte) 0x86;
	static final byte VK_F24          = (byte) 0x87;

	// #if(_WIN32_WINNT >= 0x0604)

	/*
	 * 0x88 - 0x8F : UI navigation
	 */

	static final byte VK_NAVIGATION_VIEW   = (byte) 0x88; // reserved
	static final byte VK_NAVIGATION_MENU   = (byte) 0x89; // reserved
	static final byte VK_NAVIGATION_UP     = (byte) 0x8A; // reserved
	static final byte VK_NAVIGATION_DOWN   = (byte) 0x8B; // reserved
	static final byte VK_NAVIGATION_LEFT   = (byte) 0x8C; // reserved
	static final byte VK_NAVIGATION_RIGHT  = (byte) 0x8D; // reserved
	static final byte VK_NAVIGATION_ACCEPT = (byte) 0x8E; // reserved
	static final byte VK_NAVIGATION_CANCEL = (byte) 0x8F; // reserved

	// #endif /* _WIN32_WINNT >= 0x0604 */

	static final byte VK_NUMLOCK      = (byte) 0x90;
	static final byte VK_SCROLL       = (byte) 0x91;

	/*
	 * NEC PC-9800 kbd definitions
	 */
	static final byte VK_OEM_NEC_EQUAL= (byte) 0x92;   // '=' key on numpad

	/*
	 * Fujitsu/OASYS kbd definitions
	 */
	static final byte VK_OEM_FJ_JISHO   = (byte) 0x92;   // 'Dictionary' key
	static final byte VK_OEM_FJ_MASSHOU = (byte) 0x93;   // 'Unregister word' key
	static final byte VK_OEM_FJ_TOUROKU = (byte) 0x94;   // 'Register word' key
	static final byte VK_OEM_FJ_LOYA    = (byte) 0x95;   // 'Left OYAYUBI' key
	static final byte VK_OEM_FJ_ROYA    = (byte) 0x96;   // 'Right OYAYUBI' key

	/*
	 * 0x97 - 0x9F : unassigned
	 */

	/*
	 * VK_L* & VK_R* - left and right Alt, Ctrl and Shift virtual keys.
	 * Used only as parameters to GetAsyncKeyState() and GetKeyState().
	 * No other API or message will distinguish left and right keys in this way.
	 */
	static final byte VK_LSHIFT       = (byte) 0xA0;
	static final byte VK_RSHIFT       = (byte) 0xA1;
	static final byte VK_LCONTROL     = (byte) 0xA2;
	static final byte VK_RCONTROL     = (byte) 0xA3;
	static final byte VK_LMENU        = (byte) 0xA4;
	static final byte VK_RMENU        = (byte) 0xA5;

	// #if(_WIN32_WINNT >= 0x0500)
	static final byte VK_BROWSER_BACK        = (byte) 0xA6;
	static final byte VK_BROWSER_FORWARD     = (byte) 0xA7;
	static final byte VK_BROWSER_REFRESH     = (byte) 0xA8;
	static final byte VK_BROWSER_STOP        = (byte) 0xA9;
	static final byte VK_BROWSER_SEARCH      = (byte) 0xAA;
	static final byte VK_BROWSER_FAVORITES   = (byte) 0xAB;
	static final byte VK_BROWSER_HOME        = (byte) 0xAC;

	static final byte VK_VOLUME_MUTE         = (byte) 0xAD;
	static final byte VK_VOLUME_DOWN         = (byte) 0xAE;
	static final byte VK_VOLUME_UP           = (byte) 0xAF;
	static final byte VK_MEDIA_NEXT_TRACK    = (byte) 0xB0;
	static final byte VK_MEDIA_PREV_TRACK    = (byte) 0xB1;
	static final byte VK_MEDIA_STOP          = (byte) 0xB2;
	static final byte VK_MEDIA_PLAY_PAUSE    = (byte) 0xB3;
	static final byte VK_LAUNCH_MAIL         = (byte) 0xB4;
	static final byte VK_LAUNCH_MEDIA_SELECT = (byte) 0xB5;
	static final byte VK_LAUNCH_APP1         = (byte) 0xB6;
	static final byte VK_LAUNCH_APP2         = (byte) 0xB7;

	// #endif /* _WIN32_WINNT >= 0x0500 */

	/*
	 * 0xB8 - 0xB9 : reserved
	 */

	static final byte VK_OEM_1        = (byte) 0xBA;   // ';:' for US
	static final byte VK_OEM_PLUS     = (byte) 0xBB;   // '+' any country
	static final byte VK_OEM_COMMA    = (byte) 0xBC;   // ',' any country
	static final byte VK_OEM_MINUS    = (byte) 0xBD;   // '-' any country
	static final byte VK_OEM_PERIOD   = (byte) 0xBE;   // '.' any country
	static final byte VK_OEM_2        = (byte) 0xBF;   // '/?' for US
	static final byte VK_OEM_3        = (byte) 0xC0;   // '`~' for US

	/*
	 * 0xC1 - 0xC2 : reserved
	 */

	// #if(_WIN32_WINNT >= 0x0604)

	/*
	 * 0xC3 - 0xDA : Gamepad input
	 */

	static final byte VK_GAMEPAD_A                       = (byte) 0xC3; // reserved
	static final byte VK_GAMEPAD_B                       = (byte) 0xC4; // reserved
	static final byte VK_GAMEPAD_X                       = (byte) 0xC5; // reserved
	static final byte VK_GAMEPAD_Y                       = (byte) 0xC6; // reserved
	static final byte VK_GAMEPAD_RIGHT_SHOULDER          = (byte) 0xC7; // reserved
	static final byte VK_GAMEPAD_LEFT_SHOULDER           = (byte) 0xC8; // reserved
	static final byte VK_GAMEPAD_LEFT_TRIGGER            = (byte) 0xC9; // reserved
	static final byte VK_GAMEPAD_RIGHT_TRIGGER           = (byte) 0xCA; // reserved
	static final byte VK_GAMEPAD_DPAD_UP                 = (byte) 0xCB; // reserved
	static final byte VK_GAMEPAD_DPAD_DOWN               = (byte) 0xCC; // reserved
	static final byte VK_GAMEPAD_DPAD_LEFT               = (byte) 0xCD; // reserved
	static final byte VK_GAMEPAD_DPAD_RIGHT              = (byte) 0xCE; // reserved
	static final byte VK_GAMEPAD_MENU                    = (byte) 0xCF; // reserved
	static final byte VK_GAMEPAD_VIEW                    = (byte) 0xD0; // reserved
	static final byte VK_GAMEPAD_LEFT_THUMBSTICK_BUTTON  = (byte) 0xD1; // reserved
	static final byte VK_GAMEPAD_RIGHT_THUMBSTICK_BUTTON = (byte) 0xD2; // reserved
	static final byte VK_GAMEPAD_LEFT_THUMBSTICK_UP      = (byte) 0xD3; // reserved
	static final byte VK_GAMEPAD_LEFT_THUMBSTICK_DOWN    = (byte) 0xD4; // reserved
	static final byte VK_GAMEPAD_LEFT_THUMBSTICK_RIGHT   = (byte) 0xD5; // reserved
	static final byte VK_GAMEPAD_LEFT_THUMBSTICK_LEFT    = (byte) 0xD6; // reserved
	static final byte VK_GAMEPAD_RIGHT_THUMBSTICK_UP     = (byte) 0xD7; // reserved
	static final byte VK_GAMEPAD_RIGHT_THUMBSTICK_DOWN   = (byte) 0xD8; // reserved
	static final byte VK_GAMEPAD_RIGHT_THUMBSTICK_RIGHT  = (byte) 0xD9; // reserved
	static final byte VK_GAMEPAD_RIGHT_THUMBSTICK_LEFT   = (byte) 0xDA; // reserved

	// #endif /* _WIN32_WINNT >= 0x0604 */


	static final byte VK_OEM_4        = (byte) 0xDB;  //  '[{' for US
	static final byte VK_OEM_5        = (byte) 0xDC;  //  '\|' for US
	static final byte VK_OEM_6        = (byte) 0xDD;  //  ']}' for US
	static final byte VK_OEM_7        = (byte) 0xDE;  //  ''"' for US
	static final byte VK_OEM_8        = (byte) 0xDF;

	/*
	 * 0xE0 : reserved
	 */

	/*
	 * Various extended or enhanced keyboards
	 */
	static final byte VK_OEM_AX       = (byte) 0xE1;  //  'AX' key on Japanese AX kbd
	static final byte VK_OEM_102      = (byte) 0xE2;  //  "<>" or "\|" on RT 102-key kbd.
	static final byte VK_ICO_HELP     = (byte) 0xE3;  //  Help key on ICO
	static final byte VK_ICO_00       = (byte) 0xE4;  //  00 key on ICO

	// #if(WINVER >= 0x0400)
	static final byte VK_PROCESSKEY   = (byte) 0xE5;
	// #endif /* WINVER >= 0x0400 */

	static final byte VK_ICO_CLEAR    = (byte) 0xE6;


	// #if(_WIN32_WINNT >= 0x0500)
	static final byte VK_PACKET       = (byte) 0xE7;
	// #endif /* _WIN32_WINNT >= 0x0500 */

	/*
	 * 0xE8 : unassigned
	 */

	/*
	 * Nokia/Ericsson definitions
	 */
	static final byte VK_OEM_RESET    = (byte) 0xE9;
	static final byte VK_OEM_JUMP     = (byte) 0xEA;
	static final byte VK_OEM_PA1      = (byte) 0xEB;
	static final byte VK_OEM_PA2      = (byte) 0xEC;
	static final byte VK_OEM_PA3      = (byte) 0xED;
	static final byte VK_OEM_WSCTRL   = (byte) 0xEE;
	static final byte VK_OEM_CUSEL    = (byte) 0xEF;
	static final byte VK_OEM_ATTN     = (byte) 0xF0;
	static final byte VK_OEM_FINISH   = (byte) 0xF1;
	static final byte VK_OEM_COPY     = (byte) 0xF2;
	static final byte VK_OEM_AUTO     = (byte) 0xF3;
	static final byte VK_OEM_ENLW     = (byte) 0xF4;
	static final byte VK_OEM_BACKTAB  = (byte) 0xF5;

	static final byte VK_ATTN         = (byte) 0xF6;
	static final byte VK_CRSEL        = (byte) 0xF7;
	static final byte VK_EXSEL        = (byte) 0xF8;
	static final byte VK_EREOF        = (byte) 0xF9;
	static final byte VK_PLAY         = (byte) 0xFA;
	static final byte VK_ZOOM         = (byte) 0xFB;
	static final byte VK_NONAME       = (byte) 0xFC;
	static final byte VK_PA1          = (byte) 0xFD;
	static final byte VK_OEM_CLEAR    = (byte) 0xFE;

	/*
	 * 0xFF : reserved
	 */
}
