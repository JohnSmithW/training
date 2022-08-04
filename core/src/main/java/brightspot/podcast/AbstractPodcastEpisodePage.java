package brightspot.podcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import brightspot.breadcrumbs.HasBreadcrumbs;
import brightspot.cascading.CascadingPageElements;
import brightspot.embargo.Embargoable;
import brightspot.image.ImagePreviewHtml;
import brightspot.image.WebImage;
import brightspot.image.WebImageAsset;
import brightspot.landing.LandingCascadingData;
import brightspot.landing.LandingPageElements;
import brightspot.module.ModulePlacement;
import brightspot.page.Page;
import brightspot.permalink.AbstractPermalinkRule;
import brightspot.permalink.DefaultPermalinkRule;
import brightspot.podcast.feed.PodcastFeedItem;
import brightspot.promo.page.PagePromotableWithOverrides;
import brightspot.promo.podcast.PodcastEpisodePromotable;
import brightspot.rss.feed.RssFeedItemWithFields;
import brightspot.rss.feed.apple.AppleRssFeedItemWithFields;
import brightspot.rte.LargeRichTextToolbar;
import brightspot.rte.SmallRichTextToolbar;
import brightspot.rte.TinyRichTextToolbar;
import brightspot.search.boost.HasSiteSearchBoostIndexes;
import brightspot.search.modifier.exclusion.SearchExcludable;
import brightspot.search.sortalphabetical.AlphabeticallySortable;
import brightspot.section.HasSecondarySectionsWithField;
import brightspot.section.HasSection;
import brightspot.section.Section;
import brightspot.seo.SeoWithFields;
import brightspot.share.Shareable;
import brightspot.sharedcontent.SharedContent;
import brightspot.site.DefaultSiteMapItem;
import brightspot.tag.HasTagsWithField;
import brightspot.urlslug.HasUrlSlugWithField;
import brightspot.util.RichTextUtils;
import brightspot.util.Truncate;
import com.psddev.cms.db.Content;
import com.psddev.cms.db.Site;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.ui.form.DynamicNoteMethod;
import com.psddev.dari.html.Node;
import com.psddev.dari.util.Utils;
import com.psddev.feed.FeedItem;
import com.psddev.suggestions.Suggestable;

public abstract class AbstractPodcastEpisodePage extends Content implements
        AlphabeticallySortable,
        AppleRssFeedItemWithFields,
        CascadingPageElements,
        Embargoable,
        DefaultSiteMapItem,
        FeedItem,
        HasBreadcrumbs,
        HasPodcastWithField,
        HasSecondarySectionsWithField,
        HasSection,
        HasSiteSearchBoostIndexes,
        HasTagsWithField,
        HasUrlSlugWithField,
        LandingPageElements,
        Page,
        PagePromotableWithOverrides,
        PodcastEpisode,
        PodcastEpisodePromotable,
        PodcastFeedItem,
        RssFeedItemWithFields,
        SearchExcludable,
        SeoWithFields,
        Shareable,
        SharedContent,
        Suggestable {

    private String episodeNumber;

    @Required
    @ToolUi.RichText(toolbar = TinyRichTextToolbar.class)
    private String title;
    @ToolUi.RichText(toolbar = SmallRichTextToolbar.class)
    private String description;

    @DynamicNoteMethod("getCoverImageFallbackNote")
    private WebImage coverImageOverride;

    @ToolUi.RichText(toolbar = LargeRichTextToolbar.class, lines = 10, inline = false)
    private String body;

    public String getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(String episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WebImage getCoverImageOverride() {
        return coverImageOverride;
    }

    public void setCoverImageOverride(WebImage coverImageOverride) {
        this.coverImageOverride = coverImageOverride;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // --- Has Breadcrumbs Support ---

    @Override
    public List<?> getBreadcrumbs() {
        List<Section> ancestors = getSectionAncestors();
        Collections.reverse(ancestors);
        return ancestors;
    }

    // --- HasSiteSearchBoostIndexes support ---

    @Override
    public String getSiteSearchBoostTitle() {
        return getTitle();
    }

    @Override
    public String getSiteSearchBoostDescription() {
        return getDescription();
    }

    // --- Linkable support ---

    @Override
    public String getLinkableText() {
        return getPagePromotableTitle();
    }

    // --- SeoHooks support ---
    @Override
    public String getSeoTitleFallback() {
        return RichTextUtils.richTextToPlainText(getPagePromotableTitle());
    }

    @Override
    public String getSeoDescriptionFallback() {
        return RichTextUtils.richTextToPlainText(getPagePromotableDescription());
    }

    // --- HasUrlSlugWithField support ---

    @Override
    public String getUrlSlugFallback() {
        return Utils.toNormalized(RichTextUtils.richTextToPlainText(getTitle()));
    }

    // --- Promotable implementation ---

    @Override
    public String getPagePromotableTitleFallback() {
        return getTitle();
    }

    @Override
    public String getPagePromotableDescriptionFallback() {
        return getDescription();
    }

    @Override
    public WebImageAsset getPagePromotableImageFallback() {
        return getPodcastEpisodeCoverImage();
    }

    @Override
    public String getPagePromotableCategoryFallback() {
        return Optional.ofNullable(asHasSectionData().getSectionParent())
            .map(Section::getSectionDisplayNameRichText)
            .orElse(null);
    }

    @Override
    public String getPagePromotableCategoryUrlFallback(Site site) {
        return Optional.ofNullable(asHasSectionData().getSectionParent())
            .map(section -> section.getLinkableUrl(site))
            .orElse(null);
    }

    // --- Shareable support ---

    @Override
    public String getShareableTitleFallback() {
        return RichTextUtils.richTextToPlainText(getPagePromotableTitleFallback());
    }

    @Override
    public String getShareableDescriptionFallback() {
        return RichTextUtils.richTextToPlainText(getPagePromotableDescriptionFallback());
    }

    @Override
    public WebImageAsset getShareableImageFallback() {
        return getPagePromotableImageFallback();
    }

    // --- Recordable support ---

    @Override
    public String getLabel() {
        return RichTextUtils.richTextToPlainText(getTitle());
    }

    // --- Directory.Item support ---

    @Override
    public String createPermalink(Site site) {
        return AbstractPermalinkRule.create(site, this, DefaultPermalinkRule.class);
    }

    // --- FeedElement support ---

    @Override
    public String getFeedTitle() {
        return getSeoTitle();
    }

    @Override
    public String getFeedDescription() {
        return getSeoDescription();
    }

    @Override
    public String getFeedLink(Site site) {
        return getPagePromotableUrl(site);
    }

    @Override
    public String getSuggestableText() {
        return Optional.ofNullable(RichTextUtils.richTextToPlainText(getTitle())).orElse("") + " "
                + Optional.ofNullable(getDescription())
                .map(RichTextUtils::stripRichTextElements)
                .map(RichTextUtils::richTextToPlainText)
                .orElse("");
    }

    @Override
    public String getPodcastEpisodePromotableTitle() {
        return getPagePromotableTitle();
    }

    @Override
    public String getPodcastEpisodePromotableDescription() {
        return getPagePromotableDescription();
    }

    @Override
    public WebImageAsset getPodcastEpisodePromotableImage() {
        return getPagePromotableImage();
    }

    // ---Image Fallback ---
    public WebImage getPodcastEpisodeCoverImageFallback() {
        return Optional.ofNullable(getPodcast())
                .filter(PodcastPage.class::isInstance)
                .map(PodcastPage.class::cast)
                .map(PodcastPage::getCoverArt)
                .orElse(null);
    }

    public WebImage getPodcastEpisodeCoverImage() {
        return Optional.ofNullable(getPodcastEpisodeCoverImageOverride())
                .orElseGet(this::getPodcastEpisodeCoverImageFallback);
    }

    public WebImage getPodcastEpisodeCoverImageOverride() {
        return getCoverImageOverride();
    }

    private Node getCoverImageFallbackNote() {
        if (getCoverImageOverride() == null) {
            return Optional.ofNullable(getPodcastEpisodeCoverImageFallback())
                .map(WebImageAsset::getWebImageAssetFile)
                .map(ImagePreviewHtml::createPreviewImageHtml)
                .orElse(null);
        }
        return null;
    }

    public List<ModulePlacement> getContents() {
        return Optional.ofNullable(as(LandingCascadingData.class)
                .getContent(as(Site.ObjectModification.class).getOwner()))
            .orElseGet(ArrayList::new);
    }

    @Override
    public Section getSectionParent() {
        Podcast parent = getPodcast();
        if (parent instanceof Section) {
            return (Section) parent;
        }
        return null;
    }

    // - AlphabeticallySortable

    @Override
    public String getAlphabeticallySortableIndexValue() {
        return RichTextUtils.richTextToPlainText(getPagePromotableTitle());
    }

    @Override
    public String getRssFeedItemTitleFallback() {

        return RichTextUtils.richTextToPlainText(getPagePromotableTitleFallback());
    }

    @Override
    public String getRssFeedItemDescriptionFallback() {

        return Optional.ofNullable(getDescription())
                .map(RichTextUtils::richTextToPlainText)
                .map(text -> Truncate.truncate(text, 1500, true))
                .orElse(null);
    }

    @Override
    public String getRssFeedItemGuidFallback() {
        return Optional.ofNullable(getPodcastFeedItemId())
            .map(UUID::toString)
            .orElse(null);
    }

    @Override
    public String getAppleRssFeedItemTitleFallback() {
        return getRssFeedItemTitleFallback();
    }

    @Override
    public String getAppleRssFeedItemSummaryFallback() {

        return getRssFeedItemDescriptionFallback();
    }

    @Override
    public String getAppleRssFeedItemSubtitleFallback() {
        return Optional.ofNullable(getDescription())
            .map(RichTextUtils::richTextToPlainText)
            .map(text -> Truncate.truncate(text, 80, true))
            .orElse(null);
    }
}
