package mathgraph.theme;

import java.awt.Color;
import java.awt.Font;

public class Theme {
    private final String id;
    private final String name;
    private final boolean isDark;

    public final Color bgCanvas;
    public final Color bgSidebar;
    public final Color bgCard;
    public final Color bgInput;
    public final Color textPrimary;
    public final Color textSecondary;
    public final Color textAccent;
    public final Color border;

    public final Color gridMajor;
    public final Color gridMinor;
    public final Color axis;
    public final Color axisLabels;

    public final Color accentPrimary;
    public final Color accentSecondary;
    public final Color crosshair;
    public final Color tangentLine;

    public final Color rootBadge;
    public final Color maxBadge;
    public final Color minBadge;

    public final Color tableHeaderBg;
    public final Color tableRowEven;
    public final Color tableRowOdd;
    public final Color tableSelection;

    public final Color flowchartNodeBg;
    public final Color flowchartNodeBorder;
    public final Color flowchartLine;

    public Theme(String id, String name, boolean isDark,
                 Color bgCanvas, Color bgSidebar, Color bgCard, Color bgInput,
                 Color textPrimary, Color textSecondary, Color textAccent, Color border,
                 Color gridMajor, Color gridMinor, Color axis, Color axisLabels,
                 Color accentPrimary, Color accentSecondary, Color crosshair, Color tangentLine,
                 Color rootBadge, Color maxBadge, Color minBadge,
                 Color tableHeaderBg, Color tableRowEven, Color tableRowOdd, Color tableSelection,
                 Color flowchartNodeBg, Color flowchartNodeBorder, Color flowchartLine) {
        this.id = id;
        this.name = name;
        this.isDark = isDark;
        this.bgCanvas = bgCanvas;
        this.bgSidebar = bgSidebar;
        this.bgCard = bgCard;
        this.bgInput = bgInput;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.textAccent = textAccent;
        this.border = border;
        this.gridMajor = gridMajor;
        this.gridMinor = gridMinor;
        this.axis = axis;
        this.axisLabels = axisLabels;
        this.accentPrimary = accentPrimary;
        this.accentSecondary = accentSecondary;
        this.crosshair = crosshair;
        this.tangentLine = tangentLine;
        this.rootBadge = rootBadge;
        this.maxBadge = maxBadge;
        this.minBadge = minBadge;
        this.tableHeaderBg = tableHeaderBg;
        this.tableRowEven = tableRowEven;
        this.tableRowOdd = tableRowOdd;
        this.tableSelection = tableSelection;
        this.flowchartNodeBg = flowchartNodeBg;
        this.flowchartNodeBorder = flowchartNodeBorder;
        this.flowchartLine = flowchartLine;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isDark() { return isDark; }

    // Predefined Themes
    public static Theme DARK_CYBER = new Theme(
            "dark_cyber", "Cyber Dark", true,
            new Color(11, 15, 25), new Color(17, 24, 39), new Color(30, 41, 59), new Color(15, 23, 42),
            new Color(248, 250, 252), new Color(148, 163, 184), new Color(56, 189, 248), new Color(51, 65, 85),
            new Color(51, 65, 85, 120), new Color(30, 41, 59, 80), new Color(100, 116, 139), new Color(148, 163, 184),
            new Color(56, 189, 248), new Color(244, 63, 94), new Color(250, 204, 21), new Color(251, 146, 60),
            new Color(16, 185, 129), new Color(239, 68, 68), new Color(99, 102, 241),
            new Color(30, 41, 59), new Color(17, 24, 39), new Color(22, 30, 49), new Color(56, 189, 248, 60),
            new Color(24, 33, 50), new Color(56, 189, 248), new Color(100, 116, 139)
    );

    public static Theme LIGHT_STUDIO = new Theme(
            "light_studio", "Studio Light", false,
            new Color(255, 255, 255), new Color(241, 245, 249), new Color(255, 255, 255), new Color(248, 250, 252),
            new Color(15, 23, 42), new Color(100, 116, 139), new Color(37, 99, 235), new Color(203, 213, 225),
            new Color(203, 213, 225, 160), new Color(241, 245, 249, 180), new Color(71, 85, 105), new Color(51, 65, 85),
            new Color(37, 99, 235), new Color(124, 58, 237), new Color(217, 119, 6), new Color(234, 88, 12),
            new Color(5, 150, 105), new Color(220, 38, 38), new Color(79, 70, 229),
            new Color(226, 232, 240), new Color(255, 255, 255), new Color(248, 250, 252), new Color(37, 99, 235, 40),
            new Color(255, 255, 255), new Color(37, 99, 235), new Color(148, 163, 184)
    );

    public static Theme MATRIX_GREEN = new Theme(
            "matrix_green", "Matrix Emerald", true,
            new Color(5, 13, 10), new Color(10, 24, 18), new Color(15, 34, 25), new Color(7, 18, 13),
            new Color(0, 255, 136), new Color(0, 170, 85), new Color(0, 255, 204), new Color(0, 85, 40),
            new Color(0, 85, 40, 150), new Color(0, 45, 20, 100), new Color(0, 200, 100), new Color(0, 255, 136),
            new Color(0, 255, 136), new Color(0, 229, 255), new Color(255, 220, 0), new Color(255, 120, 0),
            new Color(0, 255, 136), new Color(255, 70, 70), new Color(0, 180, 255),
            new Color(15, 34, 25), new Color(7, 18, 13), new Color(10, 24, 18), new Color(0, 255, 136, 50),
            new Color(12, 28, 20), new Color(0, 255, 136), new Color(0, 150, 75)
    );

    public static Theme SOLARIZED_SUNSET = new Theme(
            "solarized_sunset", "Solarized Sunset", true,
            new Color(26, 16, 47), new Color(38, 24, 69), new Color(52, 33, 94), new Color(20, 12, 36),
            new Color(253, 246, 227), new Color(181, 137, 0), new Color(255, 107, 107), new Color(88, 56, 158),
            new Color(88, 56, 158, 140), new Color(52, 33, 94, 90), new Color(203, 75, 22), new Color(254, 202, 87),
            new Color(255, 107, 107), new Color(254, 202, 87), new Color(78, 205, 196), new Color(255, 159, 243),
            new Color(78, 205, 196), new Color(255, 107, 107), new Color(165, 94, 234),
            new Color(52, 33, 94), new Color(26, 16, 47), new Color(33, 21, 59), new Color(255, 107, 107, 50),
            new Color(42, 27, 75), new Color(255, 107, 107), new Color(136, 84, 208)
    );

    public static Theme FOREST_PASTEL = new Theme(
            "forest_pastel", "Forest Green & Pastel Yellow", true,
            new Color(10, 26, 18), new Color(15, 38, 26), new Color(22, 54, 38), new Color(12, 30, 21),
            new Color(254, 240, 138), new Color(167, 243, 208), new Color(253, 224, 71), new Color(34, 84, 61),
            new Color(34, 84, 61, 140), new Color(20, 50, 36, 90), new Color(74, 222, 128), new Color(254, 240, 138),
            new Color(253, 224, 71), new Color(74, 222, 128), new Color(254, 240, 138), new Color(250, 204, 21),
            new Color(74, 222, 128), new Color(248, 113, 113), new Color(96, 165, 250),
            new Color(22, 54, 38), new Color(15, 38, 26), new Color(18, 46, 32), new Color(253, 224, 71, 50),
            new Color(18, 46, 32), new Color(253, 224, 71), new Color(74, 222, 128)
    );
}
