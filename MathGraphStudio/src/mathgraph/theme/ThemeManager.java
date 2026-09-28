package mathgraph.theme;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private Theme currentTheme;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    private ThemeManager() {
        this.currentTheme = Theme.DARK_CYBER;
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        if (theme != null && this.currentTheme != theme) {
            this.currentTheme = theme;
            notifyListeners();
        }
    }

    public void addListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ThemeChangeListener listener : listeners) {
            try {
                listener.onThemeChanged(currentTheme);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Theme[] getAllThemes() {
        return new Theme[]{
                Theme.DARK_CYBER,
                Theme.LIGHT_STUDIO,
                Theme.FOREST_PASTEL,
                Theme.MATRIX_GREEN,
                Theme.SOLARIZED_SUNSET
        };
    }
}
