package javafx.embed.swt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class OSXReparentSupport extends ReparentSupport {
	private final Class<?> Class_OS;
	private final Method Method_objc_msgSend_attach;
	private final Method Method_objc_msgSend_detach;
	private final long Selector_sel_addChildWindow_ordered_;
	private final long Selector_sel_removeChildWindow_;
	
	private final Field Field_Shell_window;
	private final Field Field_id_field_id;

	OSXReparentSupport() throws Throwable {
		Class_OS = Class.forName("org.eclipse.swt.internal.cocoa.OS");
		Method_objc_msgSend_attach = Class_OS.getDeclaredMethod("objc_msgSend", long.class, long.class, long.class,
				long.class);
		Method_objc_msgSend_detach = Class_OS.getDeclaredMethod("objc_msgSend", long.class, long.class, long.class);
		Field_Shell_window = Shell.class.getDeclaredField("window");
		Field_Shell_window.setAccessible(true);
		Class<?> cl_id = Class.forName("org.eclipse.swt.internal.cocoa.id");
		Field_id_field_id = cl_id.getDeclaredField("id");
		Selector_sel_addChildWindow_ordered_ = ((Number) Class_OS.getField("sel_addChildWindow_ordered_").get(null)).longValue();
		Selector_sel_removeChildWindow_ = ((Number) Class_OS.getField("sel_removeChildWindow_").get(null)).longValue();
	}

	private long nsWindowPointer(Shell shell) throws Throwable {
		Object nsWindow = Field_Shell_window.get(shell);
		return ((Number) Field_id_field_id.get(nsWindow)).longValue();
	}

	protected void attach(Shell shell, com.sun.glass.ui.Window fxWindow) throws Throwable {
		Method_objc_msgSend_attach.invoke(null, nsWindowPointer(shell), Selector_sel_addChildWindow_ordered_,
					fxWindow.getNativeWindow(), 1 /* OS.NSWindowAbove */);
		// OS.objc_msgSend(shell.view.window().id, OS.sel_addChildWindow_ordered_,
		// fxWindow.getNativeWindow(),
		// OS.NSWindowAbove);
	}

	protected void detach(Shell shell, com.sun.glass.ui.Window fxWindow) throws Throwable {
		Method_objc_msgSend_detach.invoke(null, nsWindowPointer(shell), Selector_sel_removeChildWindow_,
					fxWindow.getNativeWindow());
		// OS.objc_msgSend(shell.view.window().id, OS.sel_removeChildWindow_,
		// fxWindow.getNativeWindow());
	}

}
