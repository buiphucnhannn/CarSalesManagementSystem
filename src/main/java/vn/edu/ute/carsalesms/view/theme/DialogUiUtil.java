package vn.edu.ute.carsalesms.view.theme;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;

/**
 * Tien ich canh giua tat ca dialog/thong bao theo cua so app goc.
 */
public final class DialogUiUtil {

    private DialogUiUtil() {
    }

    public static Component appDialogParent(Component source) {
        if (source == null) {
            return null;
        }

        Window window = source instanceof Window w ? w : SwingUtilities.getWindowAncestor(source);
        while (window instanceof Dialog dialog && dialog.getOwner() != null) {
            window = dialog.getOwner();
        }
        return window != null ? window : source;
    }
}

