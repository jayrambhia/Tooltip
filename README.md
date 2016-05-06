## Tooltip

This is a lightweight library to show Tooltips dynamically in your app. This tooltip does
not require any custom layout. It works out of the box with all the layouts.

Here's a short gif showing how it works.

![Demo](https://raw.githubusercontent.com/jayrambhia/Tooltip/master/art/demo.gif)

## Tooltip.Builder

Tooltip uses Builder pattern to created immutable Tooltips.

### Usage

    Tooltip tooltip = new Tooltip.Builder(contenxt)
                        .anchor(anchorView, Tooltip.BOTTOM)
                        .content(contentView)
                        .into(root)
                        .withTip(new Tip(tipWidth, tipHeight, tipColor))
                        .show();
                        
That's it. It's that simple. You can customize the size and color of the tip to match color of the content view.

## How To Install

### Maven

    repositories {
        maven {
            url  "http://dl.bintray.com/jayrambhia/maven"
        }
    }

### JCenter

    repositories {
        jcenter()
    }

### Dependency

    dependencies {
        compile 'com.fenchtose.tooltip:0.1.2'
    }

### Useful Methods:

 - `anchor(View view)` - set anchor view with position as `Tooltip.TOP`
 - `anchor(View view, @Position int position)` - set anchor view with position
 - `content(View view)` - set content view of the tooltip
 - `withTip(@Nullable Tip tip)` - set `Tip` of the tooltip. If null, it doesn't show the tip 
 - `into(ViewGroup viewGroup)` - set ViewGroup into which the tip is to be shown
 - `autoAdjust(boolean adjust)` - if you want the tooltip to adjust itself if going out of bound
 - `cancelable(boolean cancelable)` - if you want the tooltip to dismiss automatically if clicked outside. Default is true
 - `withPadding(int padding)` - distance from the anchor and screen boundaries
 - `autoCancel(int timeInMS)` - if tooltip should be dismissed automatically after given time. If value is <= 0, auto cancel is off
 - `withListener(@NonNull Listener listener)` - Attach dismiss listener.
 - `debug(boolean debug)` - Enable debugging mode. Default is false.

### Tip

Tip is drawn as an isosceles triangle. The length of the base is defined by width and perpendicular length between top vertex and base is defined by height.

 - `width` - length of the base of isosceles triangle
 - `height` - length of the perpendicular from top vertex to the base
 - `color` - Color of the tip
 
### Future Work
 
 - Customizable tips
 - Animation in show and dismiss

## Licenses and Release History

**[CHANGELOG](https://github.com/jayrambhia/Tooltip/blob/master/Changelog.md)**

NoCropper binaries and source code can be used according to the [Apache License, Version 2.0](https://github.com/jayrambhia/Tooltip/blob/master/License).