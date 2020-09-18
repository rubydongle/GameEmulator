#ifndef SETTINGS_H_
#define SETTINGS_H_

#define BRIDGE_PACKAGE(x) Java_com_midas_game_emulator_JniBridge_ ## x
#define BUFFER_TYPE unsigned char
#define PALETTE_TYPE unsigned int

#define GET_PIXEL(buf, idx) emuPalette[buf[idx]]
#endif
