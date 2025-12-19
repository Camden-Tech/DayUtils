package me.BaddCamden.DayUtils.integration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Detects the optional SessionLibrary plugin and queries its SessionManager for session state.
 */
public class SessionLibraryHook {

    private static final Duration RESOLUTION_BACKOFF = Duration.ofSeconds(30);

    private final Plugin plugin;
    private Method hasActiveSession;
    private long nextResolutionAttempt;

    public SessionLibraryHook(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns whether time should advance based on SessionLibrary state.
     *
     * @return true if time should advance, false if there is no active session
     */
    public boolean shouldAdvanceTime() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled("SessionLibrary")) {
            resetResolution();
            return true;
        }

        Plugin sessionLibrary = pluginManager.getPlugin("SessionLibrary");
        if (!resolveSessionManager(sessionLibrary)) {
            return true;
        }

        try {
            Object result = hasActiveSession.invoke(null);
            return !(result instanceof Boolean bool) || bool;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            plugin.getLogger().log(Level.FINE, "Unable to query SessionLibrary session state", ex);
            return true;
        }
    }

    public void resetResolution() {
        this.hasActiveSession = null;
        this.nextResolutionAttempt = 0L;
    }

    private boolean resolveSessionManager(Plugin sessionLibrary) {
        if (hasActiveSession != null) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now < nextResolutionAttempt) {
            return false;
        }
        nextResolutionAttempt = now + RESOLUTION_BACKOFF.toMillis();

        Class<?> sessionManagerClass = locateSessionManagerClass(sessionLibrary);
        if (sessionManagerClass == null) {
            return false;
        }

        try {
            Method method = sessionManagerClass.getDeclaredMethod("hasActiveSession");
            if (method.getParameterCount() != 0 || !Modifier.isStatic(method.getModifiers())) {
                plugin.getLogger().fine("SessionManager.hasActiveSession() is not a static no-arg method.");
                return false;
            }
            method.setAccessible(true);
            this.hasActiveSession = method;
            return true;
        } catch (NoSuchMethodException ex) {
            plugin.getLogger().log(Level.FINE, "SessionManager.hasActiveSession() could not be located", ex);
            return false;
        }
    }

    private Class<?> locateSessionManagerClass(Plugin sessionLibrary) {
        ClassLoader loader = sessionLibrary != null
            ? sessionLibrary.getClass().getClassLoader()
            : plugin.getClass().getClassLoader();

        for (String candidate : candidateClassNames(sessionLibrary)) {
            try {
                return Class.forName(candidate, false, loader);
            } catch (ClassNotFoundException ignored) {
                // continue searching
            }
        }

        if (sessionLibrary == null) {
            return null;
        }

        CodeSource codeSource = sessionLibrary.getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            return null;
        }

        try (JarFile jarFile = new JarFile(new File(codeSource.getLocation().toURI()))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith("SessionManager.class")) {
                    continue;
                }
                String className = entry.getName()
                    .replace('/', '.')
                    .replace(".class", "");
                try {
                    return Class.forName(className, false, loader);
                } catch (ClassNotFoundException ignored) {
                    // keep trying
                }
            }
        } catch (IOException | URISyntaxException ex) {
            plugin.getLogger().log(Level.FINE, "Failed to inspect SessionLibrary jar for SessionManager", ex);
        }

        return null;
    }

    private List<String> candidateClassNames(Plugin sessionLibrary) {
        List<String> candidates = new ArrayList<>();
        if (sessionLibrary != null) {
            String mainClass = sessionLibrary.getDescription().getMain();
            int lastDot = mainClass.lastIndexOf('.');
            if (lastDot > 0) {
                String basePackage = mainClass.substring(0, lastDot);
                candidates.add(basePackage + ".SessionManager");
            }
            String pluginPackage = sessionLibrary.getClass().getPackageName();
            if (!pluginPackage.isEmpty()) {
                candidates.add(pluginPackage + ".SessionManager");
            }
        }
        return candidates;
    }
}
