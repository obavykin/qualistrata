package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.DownloadSupport;
import com.bavykin.qualistrata.ui.driver.UiCapabilities;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.driver.UiSessionRegistry;
import com.bavykin.qualistrata.ui.driver.WindowSupport;
import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;
import com.microsoft.playwright.Page;

/**
 * Playwright-backed UI session bound to one page/window.
 */
public final class PlaywrightUiSession implements UiSession {

    private final Page page;
    private final UiRuntimeConfig config;
    private final UiCapabilities capabilities;
    private final PlaywrightLocatorResolver resolver;
    private final WindowSupport windows;
    private final DownloadSupport downloads;

    /**
     * Creates a session around an existing Playwright page.
     *
     * @param page native Playwright page.
     * @param config effective UI runtime configuration.
     * @param capabilities resolved browser capabilities.
     */
    public PlaywrightUiSession(Page page, UiRuntimeConfig config, UiCapabilities capabilities) {
        this.page = page;
        this.config = config;
        this.capabilities = capabilities;
        this.resolver = new PlaywrightLocatorResolver(page);
        this.windows = new PlaywrightWindowSupport(page, config, capabilities);
        this.downloads = new PlaywrightDownloadSupport(page, config.timeouts().downloadTimeout());
        this.page.setDefaultTimeout(config.timeouts().defaultTimeout().toMillis());
        this.page.setDefaultNavigationTimeout(config.timeouts().navigationTimeout().toMillis());
    }

    @Override
    public void open(String urlOrPath) {
        String url = resolveUrl(urlOrPath);
        page.navigate(url, new Page.NavigateOptions().setTimeout(config.timeouts().navigationTimeout().toMillis()));
    }

    @Override
    public String url() {
        return page.url();
    }

    @Override
    public String title() {
        return page.title();
    }

    @Override
    public UiElement element(UiLocator locator) {
        return new UiElement(new PlaywrightElementAdapter(resolver.resolve(locator)));
    }

    @Override
    public UiElementCollection elements(UiLocator locator) {
        return new UiElementCollection(new PlaywrightElementAdapter(resolver.resolve(locator)));
    }

    @Override
    public WindowSupport windows() {
        return windows;
    }

    @Override
    public DownloadSupport downloads() {
        return downloads;
    }

    @Override
    public UiCapabilities capabilities() {
        return capabilities;
    }

    /**
     * Returns native Playwright page for adapter-specific framework extensions.
     *
     * @return native Playwright page for adapter-specific framework extensions.
     */
    public Page nativePage() {
        return page;
    }

    @Override
    public void close() {
        try {
            page.close();
        } finally {
            UiSessionRegistry.unregister(this);
        }
    }

    private String resolveUrl(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.isBlank()) {
            return config.baseUrl();
        }
        if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
            return urlOrPath;
        }

        String base = config.baseUrl().endsWith("/") ? config.baseUrl().substring(0, config.baseUrl().length() - 1) : config.baseUrl();
        String path = urlOrPath.startsWith("/") ? urlOrPath : "/" + urlOrPath;
        return base + path;
    }
}
