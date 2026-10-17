// Copyright 2026 Viachaslau Lyskouski. All rights reserved.
// Use of this source code is governed by an MIT license that can be found in the LICENSE file.

package io.github.lyskouski.screenshot

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public enum class TargetPlatform { IOS, ANDROID, MACOS, WINDOWS, LINUX }

public enum class DeviceType { PHONE, TABLET, DESKTOP }

/** System bars reserved by the device, in logical pixels. */
public data class SafeArea(
    val top: Dp = 0.dp,
    val bottom: Dp = 0.dp,
    val left: Dp = 0.dp,
    val right: Dp = 0.dp,
) {
    public companion object {
        public val Zero: SafeArea = SafeArea()
    }
}

/**
 * Screen to render the application on.
 *
 * [width] and [height] are physical pixels: the size of the produced image.
 */
public data class DeviceConfig(
    val name: String,
    val width: Int,
    val height: Int,
    val devicePixelRatio: Float,
    val platform: TargetPlatform,
    val type: DeviceType,
    val safeArea: SafeArea = SafeArea.Zero,
    val navigationBarHeight: Dp = if (type == DeviceType.DESKTOP) 0.dp else 56.dp,
) {
    val logicalWidth: Dp get() = (width / devicePixelRatio).dp
    val logicalHeight: Dp get() = (height / devicePixelRatio).dp

    /** Folder name of the device inside a locale folder. */
    val folderName: String get() = sanitizeFileName(name)

    override fun toString(): String = name
}

/** Platform-specific device configurations. */
public object PlatformDevices {
    private val iosPhoneSafeArea = SafeArea(top = 47.dp, bottom = 34.dp)
    private val iosTabletSafeArea = SafeArea(top = 24.dp, bottom = 20.dp)

    // iOS Devices
    public val iphone15ProMax: DeviceConfig = DeviceConfig(
        name = "iPhone 15 Pro Max", // Apple Marketplace [iPhone 6.9" Display]
        width = 1290, height = 2796, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iPhone: DeviceConfig = DeviceConfig(
        name = "iPhone 6.5dm", // Apple Marketplace [iPhone 6.5" Display]
        width = 1284, height = 2778, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iphone15Pro: DeviceConfig = DeviceConfig(
        name = "iPhone 15 Pro", // Apple Marketplace [iPhone 6.3" Display]
        width = 1179, height = 2556, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iphone14: DeviceConfig = DeviceConfig(
        name = "iPhone 14", // Apple Marketplace [iPhone 6.1" Display]
        width = 1170, height = 2532, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iphoneDuo: DeviceConfig = DeviceConfig(
        name = "iPhone Duo",
        width = 1398, height = 2034, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iphoneDuoOpened: DeviceConfig = DeviceConfig(
        name = "iPhone Duo opened",
        width = 2853, height = 2007, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iPhone55: DeviceConfig = DeviceConfig(
        name = "iPhone 5.5dm", // Apple Marketplace [iPhone 5.5" Display]
        width = 1242, height = 2208, devicePixelRatio = 3f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = iosPhoneSafeArea,
    )
    public val iphoneSE: DeviceConfig = DeviceConfig(
        name = "iPhone SE 3rd Gen", // Apple Marketplace [iPhone 4.7" Display]
        width = 750, height = 1334, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = SafeArea(top = 20.dp),
    )
    public val iPhone4dm: DeviceConfig = DeviceConfig(
        name = "iPhone 4dm", // Apple Marketplace [iPhone 4" Display]
        width = 640, height = 1136, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE, safeArea = SafeArea(top = 20.dp),
    )
    public val iPhone3dm: DeviceConfig = DeviceConfig(
        name = "iPhone 3.5dm", // Apple Marketplace [iPhone 3.5" Display]
        width = 640, height = 920, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.PHONE,
    )
    public val ipadPro: DeviceConfig = DeviceConfig(
        name = "iPad Pro 12.9", // Apple Marketplace [iPad 13" Display]
        width = 2048, height = 2732, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.TABLET, safeArea = iosTabletSafeArea,
    )
    public val iPad11dm: DeviceConfig = DeviceConfig(
        name = "iPad 11dm", // Apple Marketplace [iPad 11" Display]
        width = 1668, height = 2420, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.TABLET, safeArea = iosTabletSafeArea,
    )
    public val iPadPro2nd: DeviceConfig = DeviceConfig(
        name = "iPad Pro 2nd Gen", // Apple Marketplace [iPad 12.9" Display]
        width = 2048, height = 2732, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.TABLET, safeArea = iosTabletSafeArea,
    )
    public val iPad10_5dm: DeviceConfig = DeviceConfig(
        name = "iPad 10.5dm", // Apple Marketplace [iPad 10.5" Display]
        width = 1668, height = 2224, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.TABLET, safeArea = iosTabletSafeArea,
    )
    public val iPad9_7dm: DeviceConfig = DeviceConfig(
        name = "iPad 9.7dm", // Apple Marketplace [iPad 9.7" Display]
        width = 1536, height = 2008, devicePixelRatio = 2f,
        platform = TargetPlatform.IOS, type = DeviceType.TABLET, safeArea = iosTabletSafeArea,
    )

    // Android Devices
    public val pixel8Pro: DeviceConfig = DeviceConfig(
        name = "Pixel 8 Pro",
        width = 1344, height = 2992, devicePixelRatio = 2.8f,
        platform = TargetPlatform.ANDROID, type = DeviceType.PHONE, safeArea = SafeArea(top = 24.dp),
    )
    public val pixel7a: DeviceConfig = DeviceConfig(
        name = "Pixel 7a",
        width = 1080, height = 2400, devicePixelRatio = 2.2f,
        platform = TargetPlatform.ANDROID, type = DeviceType.PHONE, safeArea = SafeArea(top = 24.dp),
    )
    public val galaxyS24: DeviceConfig = DeviceConfig(
        name = "Galaxy S24",
        width = 1080, height = 2340, devicePixelRatio = 3f,
        platform = TargetPlatform.ANDROID, type = DeviceType.PHONE, safeArea = SafeArea(top = 28.dp),
    )
    public val galaxyS24Ultra: DeviceConfig = DeviceConfig(
        name = "Galaxy S24 Ultra",
        width = 1440, height = 3120, devicePixelRatio = 3f,
        platform = TargetPlatform.ANDROID, type = DeviceType.PHONE, safeArea = SafeArea(top = 34.dp),
    )
    public val galaxyTabS9: DeviceConfig = DeviceConfig(
        name = "Galaxy Tab S9",
        width = 1600, height = 2560, devicePixelRatio = 2f,
        platform = TargetPlatform.ANDROID, type = DeviceType.TABLET, safeArea = SafeArea(top = 24.dp),
    )

    // Desktop Devices
    public val macBook: DeviceConfig = DeviceConfig(
        name = "MacBook", // Apple Marketplace
        width = 2880, height = 1800, devicePixelRatio = 2f,
        platform = TargetPlatform.MACOS, type = DeviceType.DESKTOP,
    )
    public val macBookPro14: DeviceConfig = DeviceConfig(
        name = "MacBook Pro 14dm",
        width = 3024, height = 1964, devicePixelRatio = 2f,
        platform = TargetPlatform.MACOS, type = DeviceType.DESKTOP,
    )
    public val macBookAir13: DeviceConfig = DeviceConfig(
        name = "MacBook Air 13dm",
        width = 2560, height = 1664, devicePixelRatio = 2f,
        platform = TargetPlatform.MACOS, type = DeviceType.DESKTOP,
    )
    public val windows1080p: DeviceConfig = DeviceConfig(
        name = "Windows 1080p",
        width = 1920, height = 1080, devicePixelRatio = 1f,
        platform = TargetPlatform.WINDOWS, type = DeviceType.DESKTOP,
    )
    public val windows1440p: DeviceConfig = DeviceConfig(
        name = "Windows 1440p",
        width = 2560, height = 1440, devicePixelRatio = 1.25f,
        platform = TargetPlatform.WINDOWS, type = DeviceType.DESKTOP,
    )
    public val linux1080p: DeviceConfig = DeviceConfig(
        name = "Linux 1080p",
        width = 1920, height = 1080, devicePixelRatio = 1f,
        platform = TargetPlatform.LINUX, type = DeviceType.DESKTOP,
    )

    // Grouped collections
    public val iPhones: List<DeviceConfig> = listOf(
        iPhone, iphone15Pro, iphone15ProMax, iphone14, iphoneDuo, iphoneDuoOpened, iPhone55, iphoneSE, iPhone4dm, iPhone3dm,
    )
    public val iPads: List<DeviceConfig> = listOf(ipadPro, iPad11dm, iPadPro2nd, iPad10_5dm, iPad9_7dm)
    public val androidPhones: List<DeviceConfig> = listOf(pixel8Pro, pixel7a, galaxyS24, galaxyS24Ultra)
    public val androidTablets: List<DeviceConfig> = listOf(galaxyTabS9)
    public val desktops: List<DeviceConfig> =
        listOf(macBook, macBookPro14, macBookAir13, windows1080p, windows1440p, linux1080p)

    public val allDevices: List<DeviceConfig> = iPhones + iPads + androidPhones + androidTablets + desktops
    public val mobileDevices: List<DeviceConfig> = iPhones + androidPhones
    public val tabletDevices: List<DeviceConfig> = iPads + androidTablets
}

/** Replaces characters that are unsafe in file names, keeping letters, digits, '-', '_' and '.'. */
public fun sanitizeFileName(value: String): String = value.replace(Regex("[^\\w\\-.]"), "_")
