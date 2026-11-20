package dev.nxnative.android;

/**
 * The launcher's design tokens (Ui.java builds every view from these, so screens stay the same): the 4/8 dp spacing grid, the corner radii, the type scale, the
 * smallest touch target, the components' fixed measures, and the few values the system needs off the grid — each with its why (nothing loose in the helpers).
 * Public so dev.peanuts.ui.PUi, the verbatim copy of Ui, can import them like it imports R. Colors are not here on purpose: they live in res/values/colors.xml
 * (day) and values-night (dark), because resources switch with the system's mode and constants cannot. The look is flat on purpose: surfaces are told apart by
 * their fill and hairline edge, so there are no shadow tokens (dialogs get their elevation from the theme).
 */
public final class Tokens {
    private Tokens() {}

    // Spacing on the 4/8 grid: gaps between cards, inside cards, around sections (SPACE_1 = 4 dp up to SPACE_8 = 32 dp)
    public static final int SPACE_1 = 4, SPACE_2 = 8, SPACE_3 = 12, SPACE_4 = 16,
            SPACE_5 = 20, SPACE_6 = 24, SPACE_7 = 28, SPACE_8 = 32;

    // Corner radii: small controls (rows, tiles), buttons and icon buttons, cards; ROW and INNER are the two in-between tiers the system draws
    // (a row's touch ripple, a card inside a card and a game tile). RADIUS_PILL (999) stays a literal in Ui: a fully rounded pill, not a tier
    public static final int RADIUS_SM = 8, RADIUS_ROW = 12, RADIUS_MD = 14, RADIUS_INNER = 16, RADIUS_LG = 20;

    // Type scale (sp): the five sizes every text is set in (section labels and tags, secondary text, body and buttons, headings, page titles)
    public static final float TYPE_CAPTION = 12, TYPE_SMALL = 13, TYPE_BODY = 15, TYPE_HEADING = 18, TYPE_TITLE = 24;

    // The two named steps the fork's own cards need off the five above: a card's title above its value (TYPE_CARD_TITLE) and a cover's name / a short state line on
    // the bottom screen (TYPE_LABEL). Named, so same-role text stays one size across Library, the build card, DriverActivity and the second screen instead of 14/16/17.
    public static final float TYPE_CARD_TITLE = 17, TYPE_LABEL = 14;
    // The launch screen's big name (TYPE_DISPLAY, TYPE_DISPLAY_SMALL on a narrow panel): larger than a page title on purpose (it is the game, full width)
    public static final float TYPE_DISPLAY = 26, TYPE_DISPLAY_SMALL = 22;
    // The companion development screen's brand line (CompanionPage text view): a watermark, its own size above a page title
    public static final float TYPE_BRAND = 30;

    // Touch targets (dp): the smallest tappable area of a control (buttons and icon buttons size themselves to it, so a finger never misses and a
    // screen reader's cursor has something to land on)
    public static final int TOUCH_MIN = 48;

    // Components' fixed measures (dp, on the 4/8 grid but named, so no helper holds a bare number):
    public static final int HEADER_DP = 56;       // the page header's height (Ui.header)
    public static final int ACTION_DP = 56;       // a card's big action's height (Ui.Action), the header's twin
    public static final int ROW_DP = 52;          // a list row's height (Ui.Setting): room for a title and its value line
    public static final int TILE_DP = 36;         // a row's emoji tile (Ui.Setting)
    public static final int SUB_TILE_DP = 28;     // a sub-row's smaller tile (Ui.Setting.sub)
    public static final int ICON_DP = 24;         // the big action's leading icon (Ui.Action)
    public static final int CHEVRON_DP = 20;      // a row's chevron (Ui.Setting.chevron)
    public static final int EMPTY_BANANA_DP = 72; // the empty state's banana (Ui.emptyState)
    public static final int BUTTON_MIN_W_DP = 64; // a button's smallest width (Ui.button): a short label still reads as a button

    // The values off the grid the system still needs, each with its why:
    public static final int HAIRLINE_DP = 1;       // the hairline: card edges, dividers, the outline of a SOFT button, and the tightest gap (a row's value under its title)
    public static final int FOCUS_EDGE_DP = 2;     // the focus edge's width (Ui.style, Ui.touchable): a hairline reads as a card's edge, this reads as focus
    public static final int FOCUS_RING_GAP_DP = 4; // the room a cover's focus ring needs on each side (Library.coverCell): the cell is the cover plus 2 x this
    public static final int OVERLAY_SP = 8;        // the in-game overlay's text (GameActivity): at 11-12 sp it covered the touch buttons games put in the corners
    public static final int PANEL_TAB_MIN_SP = 10; // the in-game panel's tab label's floor (SidePanel.tabTextSize): four labels share one bar the player can resize; below 12 sp only here
    public static final int HEADER_CAPTION_SP = 10; // Home's header icon captions (Library.headerAction): three captions (FAQ, Settings, Screens off) share one 56 dp header row with the title; below 12 sp only here

    // A page's readable maximum width (Ui.page(MainActivity, frame, maxWidthDp)): the same cap the cover grid derives its cell from, and Library.render's column maths
    public static final int PAGE_MAX_DP = 900;      // every page but a game's settings
    public static final int PAGE_MAX_WIDE_DP = 1040; // a game's settings page on a wide window, for its side rail

    // The status-bar scrim's alpha: the page's background color held at this alpha over the clock (Ui.page), so scrolled cards never show through
    public static final int SCRIM_ALPHA = 0xEE;
}
