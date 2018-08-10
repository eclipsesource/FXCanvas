package javafx.embed.swt;

import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Shell;

import com.sun.glass.ui.Window;

@SuppressWarnings("restriction")
public class Win32ReparentSupport extends ReparentSupport {
	private final Class<?> Class_OS;
	private final Method Method_SetWindowLongPtr;
	private final int GWL_HWNDPARENT;

	public Win32ReparentSupport() throws Throwable {
		super();
		Class_OS = Class.forName("org.eclipse.swt.internal.win32.OS");
		Method_SetWindowLongPtr = Class_OS.getDeclaredMethod("SetWindowLongPtr", long.class, int.class, long.class);
		GWL_HWNDPARENT = Class_OS.getDeclaredField("GWL_HWNDPARENT").getInt(null);
	}

	private long getWindowPointer(Shell shell) {
		return shell.handle;
	}

	@Override
	protected void attach(Shell shell, Window window) throws Throwable {
		long hWnd = window.getNativeHandle();
		Method_SetWindowLongPtr.invoke(null, hWnd, GWL_HWNDPARENT, getWindowPointer(shell));
//		OS.SetWindowLongPtr(hWnd, OS.GWL_HWNDPARENT, getWindowPointer(shell));
	}

	@Override
	protected void detach(Shell shell, Window window) throws Throwable {
		long hWnd = window.getNativeHandle();
		Method_SetWindowLongPtr.invoke(null, hWnd, GWL_HWNDPARENT, 0);
//		OS.SetWindowLongPtr(hWnd, OS.GWL_HWNDPARENT, 0);
	}

}
