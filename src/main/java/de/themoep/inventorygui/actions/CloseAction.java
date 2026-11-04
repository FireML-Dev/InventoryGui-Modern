package de.themoep.inventorygui.actions;

import de.themoep.inventorygui.InventoryGui;

public interface CloseAction {

    Result onClose(InventoryGui.Close close);

    /**
     * The result of this close action.
     */
    enum Result {
        /**
         * When this action is complete and nothing else needs to happen.
         */
        COMPLETE,
        /**
         * When this action is complete and the player should be sent to their previous GUI.
         * <p>
         * Note: Only works if {@link InventoryGui#ENABLE_HISTORY} is true.
         */
        GO_BACK
    }

}
