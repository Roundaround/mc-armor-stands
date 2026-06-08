package me.roundaround.armorstands.client.render;

/**
 * Immutable per-stand mannequin render settings. Holds the boolean toggles that the mannequin
 * renderer reads each frame, plus the {@code enabled} master override.
 *
 * <p>Stored as a synched-data int bitmask on the armor stand (see {@code ArmorStandMannequinDataMixin}),
 * edited through the server-side editor — so the toggles support undo/redo and persist with the
 * world — and replicated to every client. {@link #toBits()} / {@link #fromBits(int)} convert to and
 * from that bitmask; the bit order must stay stable for save compatibility.
 *
 * <p>Use {@link #DEFAULT} for an unconfigured stand, and the {@code with...} helpers to derive a
 * copy with one flag changed (e.g. {@code settings.withShowCape(false)}).
 *
 * @param enabled        master override; <b>off by default</b> — mannequin mode is opt-in per stand.
 *                       When {@code false} the stand renders as the normal vanilla stick model (with
 *                       its name + vanilla layers) and <b>no skin lookup is attempted at all</b>, so
 *                       stands the player has not opted in cost nothing extra.
 * @param animations     whether the idle arm bob plays.
 * @param showCape       whether the cape is rendered (only when the skin has one).
 * @param showHat        whether the hat (second head layer) is rendered.
 * @param showJacket     whether the jacket (second body layer) is rendered.
 * @param showLeftSleeve whether the left sleeve (second left-arm layer) is rendered.
 * @param showRightSleeve whether the right sleeve (second right-arm layer) is rendered.
 * @param showLeftPants  whether the left pants (second left-leg layer) is rendered.
 * @param showRightPants whether the right pants (second right-leg layer) is rendered.
 * @param slim           arm-width override; {@code false} follows the skin's natural model type,
 *                       {@code true} forces the slim (3px) arm model.
 * @param autoRefresh    whether this stand's skin auto-refreshes; <b>off by default</b> — the
 *                       periodic re-fetch only runs when this is on (and the global
 *                       {@code disableAutoSkinRefresh} kill-switch is not set).
 */
public record MannequinSettings(
    boolean enabled,
    boolean animations,
    boolean showCape,
    boolean showHat,
    boolean showJacket,
    boolean showLeftSleeve,
    boolean showRightSleeve,
    boolean showLeftPants,
    boolean showRightPants,
    boolean slim,
    boolean autoRefresh
) {
  /**
   * Defaults: mannequin mode DISABLED (opt-in per stand, so no skin lookup happens until the player
   * turns it on), but otherwise animated with every skin layer shown and arm width following the
   * skin — so enabling it on a named stand "just works".
   */
  public static final MannequinSettings DEFAULT =
      new MannequinSettings(false, true, true, true, true, true, true, true, true, false, false);

  /** Packs the flags into an int bitmask (bit order must stay stable for save compatibility). */
  public int toBits() {
    int bits = 0;
    if (this.enabled) bits |= 1;
    if (this.animations) bits |= 1 << 1;
    if (this.showCape) bits |= 1 << 2;
    if (this.showHat) bits |= 1 << 3;
    if (this.showJacket) bits |= 1 << 4;
    if (this.showLeftSleeve) bits |= 1 << 5;
    if (this.showRightSleeve) bits |= 1 << 6;
    if (this.showLeftPants) bits |= 1 << 7;
    if (this.showRightPants) bits |= 1 << 8;
    if (this.slim) bits |= 1 << 9;
    if (this.autoRefresh) bits |= 1 << 10;
    return bits;
  }

  /** Unpacks the int bitmask produced by {@link #toBits()}. */
  public static MannequinSettings fromBits(int bits) {
    return new MannequinSettings(
        (bits & 1) != 0,
        (bits & 1 << 1) != 0,
        (bits & 1 << 2) != 0,
        (bits & 1 << 3) != 0,
        (bits & 1 << 4) != 0,
        (bits & 1 << 5) != 0,
        (bits & 1 << 6) != 0,
        (bits & 1 << 7) != 0,
        (bits & 1 << 8) != 0,
        (bits & 1 << 9) != 0,
        (bits & 1 << 10) != 0);
  }

  public MannequinSettings withEnabled(boolean enabled) {
    return new MannequinSettings(enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withAnimations(boolean animations) {
    return new MannequinSettings(this.enabled, animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowCape(boolean showCape) {
    return new MannequinSettings(this.enabled, this.animations, showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowHat(boolean showHat) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowJacket(boolean showJacket) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowLeftSleeve(boolean showLeftSleeve) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowRightSleeve(boolean showRightSleeve) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowLeftPants(boolean showLeftPants) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, showLeftPants,
        this.showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withShowRightPants(boolean showRightPants) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        showRightPants, this.slim, this.autoRefresh);
  }

  public MannequinSettings withSlim(boolean slim) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, slim, this.autoRefresh);
  }

  public MannequinSettings withAutoRefresh(boolean autoRefresh) {
    return new MannequinSettings(this.enabled, this.animations, this.showCape, this.showHat,
        this.showJacket, this.showLeftSleeve, this.showRightSleeve, this.showLeftPants,
        this.showRightPants, this.slim, autoRefresh);
  }
}
