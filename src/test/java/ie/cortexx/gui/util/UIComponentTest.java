package ie.cortexx.gui.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.swing.*;
import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests every theme + font combination for:
 *   - no Swing/FlatLaf exceptions during L&F install or updateComponentTreeUI
 *   - palette colors are non-null and distinct per theme
 *   - fonts are rebuilt when family or size changes
 *   - all component factory methods produce valid components under every theme
 *   - JScrollPane wrapping tables does NOT use forbidden FlatLaf styles
 */
class UIComponentTest {

    @BeforeEach
    void initLaf() throws Exception {
        SwingUtilities.invokeAndWait(UI::init);
    }

    @AfterEach
    void resetDefaults() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UI.applyFontFamily(UI.FontFamily.OUTFIT);
            UI.applyFontSize(UI.FontSize.MEDIUM);
            UI.applyTheme(UI.Theme.LIGHT);
        });
    }

    // --- theme switching ---

    @ParameterizedTest
    @EnumSource(UI.Theme.class)
    void themeAppliesWithoutException(UI.Theme theme) throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertThatNoException().isThrownBy(() -> UI.applyTheme(theme))
        );
    }

    @ParameterizedTest
    @EnumSource(UI.Theme.class)
    void themeSetsPaletteColors(UI.Theme theme) throws Exception {
        SwingUtilities.invokeAndWait(() -> UI.applyTheme(theme));
        assertThat(UI.BG).isNotNull();
        assertThat(UI.BG_CARD).isNotNull();
        assertThat(UI.TEXT).isNotNull();
        assertThat(UI.ACCENT).isNotNull();
        assertThat(UI.BORDER).isNotNull();
    }

    @Test
    void darkAndLightProduceDistinctBackgrounds() throws Exception {
        SwingUtilities.invokeAndWait(() -> UI.applyTheme(UI.Theme.DARK));
        Color darkBg = UI.BG;

        SwingUtilities.invokeAndWait(() -> UI.applyTheme(UI.Theme.LIGHT));
        Color lightBg = UI.BG;

        assertThat(darkBg).isNotEqualTo(lightBg);
    }

    // --- font family switching ---

    @ParameterizedTest
    @EnumSource(UI.FontFamily.class)
    void fontFamilyAppliesWithoutException(UI.FontFamily family) throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertThatNoException().isThrownBy(() -> UI.applyFontFamily(family))
        );
    }

    @Test
    void fontFamilyChangeUpdatesFontObjects() throws Exception {
        SwingUtilities.invokeAndWait(() -> UI.applyFontFamily(UI.FontFamily.OUTFIT));
        String outfitName = UI.FONT.getFamily();

        SwingUtilities.invokeAndWait(() -> UI.applyFontFamily(UI.FontFamily.SYSTEM));
        String systemName = UI.FONT.getFamily();

        assertThat(outfitName).isNotEqualTo(systemName);
    }

    @ParameterizedTest
    @EnumSource(UI.FontFamily.class)
    void fontFamilySansNameAccessorWorks(UI.FontFamily family) {
        assertThat(family.sansName()).isNotNull().isNotEmpty();
    }

    // --- font size switching ---

    @ParameterizedTest
    @EnumSource(UI.FontSize.class)
    void fontSizeAppliesWithoutException(UI.FontSize size) throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertThatNoException().isThrownBy(() -> UI.applyFontSize(size))
        );
    }

    @Test
    void fontSizeChangesActualFontSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> UI.applyFontSize(UI.FontSize.SMALL));
        int small = UI.FONT.getSize();

        SwingUtilities.invokeAndWait(() -> UI.applyFontSize(UI.FontSize.LARGE));
        int large = UI.FONT.getSize();

        assertThat(large).isGreaterThan(small);
    }

    // --- full matrix: every theme × every font family × every font size ---

    @Test
    void fullThemeFontMatrixAppliesWithoutException() throws Exception {
        for (UI.Theme theme : UI.Theme.values()) {
            for (UI.FontFamily family : UI.FontFamily.values()) {
                for (UI.FontSize size : UI.FontSize.values()) {
                    SwingUtilities.invokeAndWait(() ->
                        assertThatNoException().describedAs(
                            "theme=%s family=%s size=%s", theme, family, size
                        ).isThrownBy(() -> {
                            UI.applyTheme(theme);
                            UI.applyFontFamily(family);
                            UI.applyFontSize(size);
                        })
                    );
                }
            }
        }
    }

    // --- component factories under every theme ---

    @ParameterizedTest
    @EnumSource(UI.Theme.class)
    void componentFactoriesProduceValidComponents(UI.Theme theme) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UI.applyTheme(theme);

            assertThat(UI.primaryButton("Test")).isInstanceOf(JButton.class);
            assertThat(UI.button("Test")).isInstanceOf(JButton.class);
            assertThat(UI.dangerButton("Test")).isInstanceOf(JButton.class);
            assertThat(UI.iconButton("Test", "icons/settings.svg", true)).isInstanceOf(JButton.class);
            assertThat(UI.inputField("placeholder")).isInstanceOf(JTextField.class);
            assertThat(UI.passwordField("placeholder")).isInstanceOf(JPasswordField.class);
            assertThat(UI.primaryButtonWide("Test")).isInstanceOf(JButton.class);
            assertThat(UI.heading("Test")).isInstanceOf(JLabel.class);
            assertThat(UI.title("Test")).isInstanceOf(JLabel.class);
            assertThat(UI.subtitle("Test")).isInstanceOf(JLabel.class);
            assertThat(UI.errorLabel()).isInstanceOf(JLabel.class);
            assertThat(UI.centeredPanel()).isInstanceOf(JPanel.class);
            assertThat(UI.badge("NORMAL")).isInstanceOf(JComponent.class);
            assertThat(UI.badge("SUSPENDED")).isInstanceOf(JComponent.class);
            assertThat(UI.badge(null)).isInstanceOf(JComponent.class);
        });
    }

    // --- updateComponentTreeUI safety (the actual crash scenario) ---

    @ParameterizedTest
    @EnumSource(UI.Theme.class)
    void updateComponentTreeUI_noExceptionOnTableScrollPane(UI.Theme theme) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UI.applyTheme(theme);

            // build a styled table like the real panels do
            UI.StyledTable styled = UI.table("Name", "Price");

            // this is the exact call that was crashing — updateComponentTreeUI
            // traverses the scroll pane and re-installs FlatLaf UI delegates
            assertThatNoException().isThrownBy(() ->
                SwingUtilities.updateComponentTreeUI(styled.scroll())
            );
        });
    }

    @ParameterizedTest
    @EnumSource(UI.Theme.class)
    void updateComponentTreeUI_noExceptionOnDataTable(UI.Theme theme) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UI.applyTheme(theme);

            var dt = UI.<String>table(
                UI.col("Col", s -> s)
            );

            assertThatNoException().isThrownBy(() ->
                SwingUtilities.updateComponentTreeUI(dt.scroll())
            );
        });
    }

    @ParameterizedTest
    @EnumSource(UI.Theme.class)
    void updateComponentTreeUI_noExceptionOnFormWithTextArea(UI.Theme theme) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UI.applyTheme(theme);

            // field() wraps a JScrollPane around JTextArea — this was the other crash path
            JPanel form = UI.textAreaField("Notes", "test text", 3);

            assertThatNoException().isThrownBy(() ->
                SwingUtilities.updateComponentTreeUI(form)
            );
        });
    }

    // --- theme switch + full component tree ---

    @Test
    void themeSwitchOnPopulatedFrameNoException() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UI.applyTheme(UI.Theme.DARK);

            JFrame frame = new JFrame();
            frame.setContentPane(buildSamplePanel());

            // switch to every theme and verify no exception during update
            for (UI.Theme theme : UI.Theme.values()) {
                assertThatNoException().describedAs("switching to " + theme).isThrownBy(() -> {
                    UI.applyTheme(theme);
                    SwingUtilities.updateComponentTreeUI(frame);
                });
            }

            frame.dispose();
        });
    }

    // helper — builds a JPanel containing every type of styled component
    private static JPanel buildSamplePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(UI.title("Title"));
        panel.add(UI.subtitle("Subtitle"));
        panel.add(UI.heading("Heading"));
        panel.add(UI.inputField("placeholder"));
        panel.add(UI.passwordField("password"));
        panel.add(UI.primaryButton("Primary"));
        panel.add(UI.button("Secondary"));
        panel.add(UI.dangerButton("Danger"));
        panel.add(UI.badge("NORMAL"));
        panel.add(UI.badge("SUSPENDED"));
        panel.add(UI.textAreaField("Label", "content", 2));

        UI.StyledTable styled = UI.table("A", "B");
        panel.add(styled.scroll());

        return panel;
    }
}
