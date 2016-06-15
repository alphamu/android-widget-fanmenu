[![Build Status](https://travis-ci.org/BCGDV-ASIA/android-widget-fanmenu.svg?branch=master)](https://travis-ci.org/BCGDV-ASIA/android-widget-fanmenu)
[![Download](https://api.bintray.com/packages/bcgdv/android/fanmenu/images/download.svg)](https://bintray.com/bcgdv/android/fanmenu/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FanMenu-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3555)

# FanMenu

A simple widget that can be used to display menu's which can be animated in and out.
The menu's can be animated in from behind a button like a FloatingActionButton
to give an effect of a submenu or can be placed on the screen to work as menus.

[![Build Status](https://travis-ci.org/BCGDV-ASIA/android-widget-fanmenu.svg?branch=master)](https://travis-ci.org/BCGDV-ASIA/android-widget-fanmenu) ![Fan Menu with FAB](http://i.giphy.com/l0DEKHNqfwwCTmS8U.gif) ![Fan Menu without FAB](http://i.giphy.com/eqek1YdF2UFR6.gif)

## Features

- Show as many menu items as you like, with or without icons.
- Icons can be changed when selected.
- Colors, text and icons can be specified in the xml.
- If a background color is set, this fades in/out when the menu opens/closes.
- The menu can be movable or fixed.
- Menu can reset if moved beyond a certain boundary.

## Adding it to your project

    compile 'com.bcgdv.asia.lib:fanmenu:1.2'
	
## Usage

### The menus needs to be configured in the XML.

    <com.bcgdv.asia.lib.fanmenu.FanMenuButtons
        android:background="#CCFFFFFF"
        android:id="@+id/myFABSubmenu"
        android:layout_alignBaseline="@+id/myFAB"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:textColor="@android:color/white"
        android:paddingRight="@dimen/fab_submenu_margin_right"
        android:paddingBottom="@dimen/fab_submenu_margin_bottom"
        android:textSize="14sp"
        app:fanMenuLabels="@array/fan_labels"
        app:fanButtonColors="@array/button_colors"
        app:fanLeftDrawable="@array/button_icons"
        app:fanRememberMovedPosition="true"
        app:fanMinBounceBackAngle="-30"
        app:fanMaxBounceBackAngle="50"
        android:visibility="gone" />

		
### Labels need to be defined as `string-array` resource.

    <string name="menu_top_stories">Top Stories</string>
    <string name="menu_sports">Sports News</string>
    <string name="menu_tech">Technology News</string>
    <string name="menu_local">Local News</string>
    <string-array name="fan_labels">
        <item>@string/menu_top_stories</item>
        <item>@string/menu_local</item>
        <item>@string/menu_sports</item>
        <item>@string/menu_tech</item>
    </string-array>


### Button colors need to be defined as an `integer-array` resource.

    <color name="red_1">#C2185B</color>
    <color name="red_2">#E91E63</color>
    <color name="red_3">#F44336</color>
    <color name="red_4">#FF5722</color>
    <integer-array name="button_colors">
        <item>@color/red_1</item>
        <item>@color/red_2</item>
        <item>@color/red_3</item>
        <item>@color/red_4</item>
    </integer-array>
	

### Icons need to be defined as an `array` resource.

    <array name="button_icons">
        <item>@drawable/bg_addgoal</item>
        <item>@drawable/ic_cashout</item>
        <item>@drawable/ic_invest</item>
        <item>@drawable/ic_qrcode</item>
    </array>


- `android:background` (Optional) the background drawable will fade in/out as the menu opens and closes. Can be `null`.
- `app:fanMenuLabels` (Required) a `string-array` from the resources folder. See above or the sample app for more.
- `app:fanButtonColors` (Required) a `integer-array` of colors for the buttons. Should be the same length as labels.
- `app:fanLeftDrawable` (Optional) an `array` of drawables should be the same length as labels.
- `app:fanRememberMovedPosition` (Optional) `true|false` if true, fan will not bounce back after being moved, unless a boundary angle is crossed. `false` by default.
- `app:fanMinBounceBackAngle` (Optional) `int` lower angle boundary, if this boundary is crossed when moving the fan, the fan will reset to original position. This is measured from the lowest (horizontal) button, "Top Stories" in our example.
- `app:fanMaxBounceBackAngle` (Optional) `int` upper angle boundary, if this boundary is crossed when moving the fan, the fan will reset to original position. This is measured from the lowest (horizontal) button, "Top Stories" in our example.
- `app:fanEnableTouchMovement` (Optional) `true|false` should the fan move when dragged. `true` by default.
- `app:fanButtonHeight` (Optional) `dp` the height of the buttons in DP. `32dp` by default.
- `app:fanAngleBetweenButtons` (Optional) `int` the angle between the buttons. `10 degrees` by default.
- `app:fanAnimationDuration` (Optional) `int` the time taken for the open and close animation. `200ms` by default.

## Callbacks

	final FanMenuButtons sub = (FanMenuButtons) findViewById(R.id.myFABSubmenu);
    if (sub != null) {
        sub.setOnFanButtonClickListener(new FanMenuButtons.OnFanClickListener() {
            @Override
            public void onFanButtonClicked(int index) {
            }
        });

		sub.setOnFanAnimationListener(new FanMenuButtons.OnFanAnimationListener() {
			@Override
			public void onAnimateInStarted() {
			}

			@Override
			public void onAnimateOutStarted() {
			}

			@Override
			public void onAnimateInFinished() {
			}

			@Override
			public void onAnimateOutFinished() {
			}
		});
	}
	
## Known Issues

- RTL support is not great.
- Motion may not be smooth when you release the fan and try to move it again.

License
=======

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
