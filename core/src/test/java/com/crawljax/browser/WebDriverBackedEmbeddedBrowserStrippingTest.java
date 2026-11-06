package com.crawljax.browser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class WebDriverBackedEmbeddedBrowserStrippingTest {

    private static WebDriverBackedEmbeddedBrowser browserFor(String page, ImmutableSortedSet<String> filtered) {
        WebDriver driver = mock(WebDriver.class);
        when(driver.getPageSource()).thenReturn(page);
        return WebDriverBackedEmbeddedBrowser.withDriver(driver, filtered, 100, 2500);
    }

    @Test
    public void theWaitsKeepTheirMeaning() {
        WebDriverBackedEmbeddedBrowser browser = browserFor("", ImmutableSortedSet.of());
        assertThat(browser.getCrawlWaitEvent(), is(100L));
        assertThat(browser.getCrawlWaitReload(), is(2500L));
    }

    @Test
    public void everyFilteredAttributeIsRemoved() {
        WebDriverBackedEmbeddedBrowser browser =
                browserFor("<DIV class=\"a\" style=\"b\" title=\"c\">x</DIV>", ImmutableSortedSet.of("class", "style"));
        assertThat(browser.getStrippedDomWithoutIframeContent(), is("<DIV title=\"c\">x</DIV>"));
    }

    @Test
    public void ignoredPatternsAreRemovedBeforeComparison() {
        WebDriverBackedEmbeddedBrowser browser =
                browserFor("<BUTTON aria-labelledby=\"tooltip-:ro:\">x</BUTTON>", ImmutableSortedSet.of());
        browser.ignoreInComparison(List.of(":r[0-9a-z]+:"));
        assertThat(browser.getStrippedDomWithoutIframeContent(), is("<BUTTON aria-labelledby=\"tooltip-\">x</BUTTON>"));
    }

    @Test
    public void nothingIsIgnoredByDefault() {
        WebDriverBackedEmbeddedBrowser browser = browserFor("<P id=\":ro:\">x</P>", ImmutableSortedSet.of());
        assertThat(browser.getStrippedDomWithoutIframeContent(), is("<P id=\":ro:\">x</P>"));
    }
}
