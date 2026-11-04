package de.themoep.inventorygui.actions;

import de.themoep.inventorygui.GuiElement;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ClickAction {

    Result onClick(GuiElement.Click click);

    /**
     * The result of this click action.
     */
    enum Result {
        /**
         * When this action is complete and nothing else needs to happen.
         */
        COMPLETE,
        /**
         * When this action is complete and the event should be cancelled.
         */
        CANCEL_EVENT
    }

}
