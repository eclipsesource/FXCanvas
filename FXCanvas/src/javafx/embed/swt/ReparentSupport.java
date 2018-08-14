package javafx.embed.swt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.sun.javafx.embed.EmbeddedStageInterface;

import javafx.embed.swt.FXCanvas.HostContainer;

@SuppressWarnings("restriction")
public abstract class ReparentSupport {
	@SuppressWarnings("unchecked")
	public ReparentSupport() throws Throwable {
		Class<?> cl_windowStage = Class.forName("com.sun.javafx.tk.quantum.WindowStage");
		Class<?> cl_embeddedStage = Class.forName("com.sun.javafx.tk.quantum.EmbeddedStage");

		Field field_platformWindows = cl_windowStage.getDeclaredField("platformWindows");
		Field field_owner = cl_windowStage.getDeclaredField("owner");
		Field field_host = cl_embeddedStage.getDeclaredField("host");

		field_platformWindows.setAccessible(true);
		field_owner.setAccessible(true);
		field_host.setAccessible(true);

		BiConsumer<com.sun.glass.ui.Window, Object> handleAttach = (key, value) -> {
			try {
				Object owner = field_owner.get(value);
				if (owner instanceof EmbeddedStageInterface) {
					HostContainer host = (HostContainer) field_host.get(owner);
					Shell shell = host.fxCanvas.getShell();
					attach(shell, key);
				}
			} catch (Throwable e) {
				// TODO Logging
				e.printStackTrace();
			}
		};
		
		BiConsumer<com.sun.glass.ui.Window, Object> handleDetach = (key, value) -> {
			try {
				Object owner = field_owner.get(value);
				if (owner instanceof EmbeddedStageInterface) {
					HostContainer host = (HostContainer) field_host.get(owner);
					Shell shell = host.fxCanvas.getShell();
					detach(shell, key);
				}
			} catch (Throwable e) {
				// TODO Logging
				e.printStackTrace();
			}
		};

		// Hook ourselves deep into JavaFX to get informed when new windows are created
		Map<com.sun.glass.ui.Window, Object/* com.sun.javafx.tk.quantum.WindowStage */> platformWindows = (Map<com.sun.glass.ui.Window, Object>) field_platformWindows
				.get(null);

		platformWindows.entrySet().forEach(e -> handleAttach.accept(e.getKey(), e.getValue()));

		// Overload methods called by upstream code
		Map<com.sun.glass.ui.Window, Object> replacement = new HashMap<com.sun.glass.ui.Window, Object>(
				platformWindows) {
			private static final long serialVersionUID = 1L;

			@Override
			public Object put(com.sun.glass.ui.Window key, Object value) {
				handleAttach.accept(key, value);
				return super.put(key, value);
			}

			@Override
			public Object remove(Object key) {
				Object remove = super.remove(key);
				if (remove != null) {
					handleDetach.accept((com.sun.glass.ui.Window) key, remove);
				}
				return remove;
			}
		};

		field_platformWindows.set(null, replacement);
	}
	
	protected abstract void attach(Shell shell, com.sun.glass.ui.Window window) throws Throwable;
	protected abstract void detach(Shell shell, com.sun.glass.ui.Window window) throws Throwable;
	
	public static void init() throws Throwable {
		if ("cocoa".equals(SWT.getPlatform())) {
			new OSXReparentSupport();
		} else if( "win32".equals(SWT.getPlatform()) ) {
			new Win32ReparentSupport();
		}
	}
}
