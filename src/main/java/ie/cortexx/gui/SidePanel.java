package ie.cortexx.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SidePanel extends JPanel {

	public record Page(String title, String iconPath) {}

	private final Consumer<Page> onTabChange;
	private final Map<Page, JToggleButton> navButtons = new LinkedHashMap<>();
	private final ButtonGroup navGroup = new ButtonGroup();

	public SidePanel(MainFrame mainFrame, List<Page> pages, Consumer<Page> onTabChange,
					 String username, String role, String activeTitle, Runnable onLogout) {
		this.onTabChange = onTabChange;

		setLayout(new BorderLayout());
		setBackground(UI.BG_CARD);
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UI.BORDER));
		setPreferredSize(new Dimension(260, 0));

		add(buildBrand(mainFrame), BorderLayout.NORTH);
		add(buildNav(pages), BorderLayout.CENTER);
		add(buildFooter(username, role, onLogout), BorderLayout.SOUTH);

		Page activePage = pages.stream()
			.filter(page -> page.title().equals(activeTitle))
			.findFirst()
			.orElse(pages.isEmpty() ? null : pages.get(0));
		if (activePage != null) {
			setActiveTab(activePage);
		}
	}

	private JPanel buildBrand(MainFrame mainFrame) {
		JPanel brand = UI.transparentPanel(2);
		brand.setBorder(new EmptyBorder(22, 18, 16, 18));

		JPanel text = new JPanel();
		text.setOpaque(false);
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		text.add(UI.label("IPOS-CA", UI.FONT_TITLE, UI.TEXT));
		text.add(UI.gap(2));
		text.add(UI.subtitle("COSYMED LTD"));
		brand.add(text, BorderLayout.CENTER);
		brand.add(ThemeSwitchButton.create(mainFrame), BorderLayout.EAST);
		return brand;
	}

	private JPanel buildNav(List<Page> pages) {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);

		JPanel nav = new JPanel();
		nav.setOpaque(false);
		nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
		nav.setBorder(new EmptyBorder(4, 10, 8, 10));

		for (Page page : pages) {
			JToggleButton button = createNavButton(page);
			navButtons.put(page, button);
			navGroup.add(button);

			button.addActionListener(e -> {
				setActiveTab(page);
				onTabChange.accept(page);
			});

			nav.add(button);
			nav.add(UI.gap(4));
		}

		wrapper.add(nav, BorderLayout.NORTH);
		return wrapper;
	}

	private JToggleButton createNavButton(Page page) {
		JToggleButton button = new JToggleButton(page.title());
		button.putClientProperty("FlatLaf.styleClass", "sidebarNav");
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
		button.setPreferredSize(new Dimension(220, 46));
		button.setIconTextGap(10);
		button.setFocusable(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setFont(UI.FONT_BOLD);
		applyNavVisual(button, page.iconPath(), false);
		return button;
	}

	private void setActiveTab(Page page) {
		for (Map.Entry<Page, JToggleButton> entry : navButtons.entrySet()) {
			boolean active = entry.getKey().equals(page);
			JToggleButton button = entry.getValue();
			button.setSelected(active);
			applyNavVisual(button, entry.getKey().iconPath(), active);
		}
	}

	private void applyNavVisual(AbstractButton button, String iconPath, boolean active) {
		Color colour = active ? Color.WHITE : UI.TEXT_DIM;
		button.setForeground(colour);
		button.setIcon(navIcon(iconPath, colour));
	}

	private FlatSVGIcon navIcon(String path, Color colour) {
		return UI.icon(path, 18, colour);
	}

	private JPanel buildFooter(String username, String role, Runnable onLogout) {
		JPanel footer = new JPanel(new BorderLayout(8, 0));
		footer.setOpaque(false);
		footer.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 0, 0, UI.BORDER),
			new EmptyBorder(12, 14, 12, 14)
		));

		JComponent avatar = UI.avatarBadge(safeUser(username), 42);

		JPanel userMeta = new JPanel();
		userMeta.setOpaque(false);
		userMeta.setLayout(new BoxLayout(userMeta, BoxLayout.Y_AXIS));
		userMeta.add(UI.label(safeUser(username), UI.FONT_BOLD, UI.TEXT));
		userMeta.add(UI.gap(2));
		userMeta.add(UI.label(safeRole(role), UI.FONT_SMALL, UI.TEXT_DIM));

		JButton logout = new JButton(new FlatSVGIcon("icons/log-out.svg", 18, 18));
		logout.putClientProperty("FlatLaf.styleClass", "sidebarIcon");
		logout.setToolTipText("Logout");
		logout.setFocusable(false);
		logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		logout.addActionListener(e -> onLogout.run());

		footer.add(avatar, BorderLayout.WEST);
		footer.add(userMeta, BorderLayout.CENTER);
		footer.add(logout, BorderLayout.EAST);
		return footer;
	}

	private String safeUser(String username) {
		if (username == null || username.isBlank()) {
			return "user";
		}
		return username;
	}

	private String safeRole(String role) {
		if (role == null || role.isBlank()) {
			return "STAFF";
		}
		return role.toUpperCase();
	}

}
