package com.github.argon.sos.moreoptions.game.ui;

import com.github.argon.sos.moreoptions.game.util.UiUtil;
import lombok.Builder;
import org.jetbrains.annotations.Nullable;
import snake2d.util.gui.GuiSection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class ButtonMenu<Key> extends GuiSection {

    private final Map<Key, Button> buttons;

    @Builder.Default
    private boolean horizontal = false;
    @Builder.Default
    private boolean sameWidth = false;

    @Builder.Default
    private boolean clickable = true;
    @Builder.Default
    private boolean hoverable = true;
    @Builder.Default
    private boolean spacer = false;
    @Builder.Default
    private int margin = 0;

    @Builder.Default
    private List<Integer> widths = null;

    public ButtonMenu(Map<Key, Button> buttons) {
        this(buttons, false, true, true, true, false, 0 ,null);
    }

    public ButtonMenu(
        Map<Key, Button> buttons,
        boolean horizontal,
        boolean sameWidth,
        boolean clickable,
        boolean hoverable,
        boolean spacer,
        int margin,
        @Nullable List<Integer> widths
    ) {
        this.buttons = buttons;
        int maxWidth = 0;
        if (widths == null) maxWidth = UiUtil.getMaxWidth(buttons.values());
        Collection<Button> buttonList = this.buttons.values();

        int pos = 0;
        for (Button button : buttonList) {

            int width = button.body().width();
            if (sameWidth && maxWidth > 0) {
                // adjust with by widest
                width = maxWidth;
            } else if (widths != null && pos < widths.size()) {
                // adjust width by given widths
                width = widths.get(pos);
            }

            button.body().setWidth(width);
            button.clickable(clickable);
            button.hoverable(hoverable);

            // add buttons in correct directions
            if (horizontal) {
                if (spacer) {
                    addRightC(0, button);
                    if (pos < buttonList.size() - 1)
                        addRightC(0, new VerticalLine(margin, button.body.height(), 1));
                } else {
                    addRightC(margin, button);
                }
            } else {
                if (spacer) {
                    addDownC(0, button);
                    if (pos < buttonList.size() - 1)
                        addDownC(0, new VerticalLine(margin, button.body.height(), 1));
                } else {
                    addDownC(margin, button);
                }
            }

            pos++;
        }
    }

    public Button get(Key key) {
        return buttons.get(key);
    }



    public static class ButtonMenuBuilder<Key> {
        public static ButtonMenu<String> fromList(List<Button> infoList) {
            LinkedHashMap<String, Button> collect = infoList.stream().collect(Collectors.toMap(
                Object::toString, button -> button,
                (left, right) -> left, LinkedHashMap::new));

            return new ButtonMenu<>(collect);
        }
    }
}
