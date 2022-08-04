package brightspot.blog;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import brightspot.ad.injection.rte.SupportsAdInjection;
import brightspot.article.ArticleLead;
import brightspot.author.HasAuthorsWithField;
import brightspot.breadcrumbs.HasBreadcrumbs;
import brightspot.cascading.CascadingPageElements;
import brightspot.commenting.HasCommenting;
import brightspot.commenting.coral.HasCoralPageMetadata;
import brightspot.commenting.disqus.HasDisqusPageMetadata;
import brightspot.embargo.Embargoable;
import brightspot.image.WebImageAsset;
import brightspot.l10n.LocaleProvider;
import brightspot.mediatype.HasMediaTypeWithOverride;
import brightspot.mediatype.MediaType;
import brightspot.opengraph.article.OpenGraphArticle;
import brightspot.page.Page;
import brightspot.permalink.AbstractPermalinkRule;
import brightspot.permalink.DefaultPermalinkRule;
import brightspot.permalink.Permalink;
import brightspot.promo.page.PagePromotableWithOverrides;
import brightspot.rte.LargeRichTextToolbar;
import brightspot.rte.TinyRichTextToolbar;
import brightspot.rte.image.ImageRichTextElement;
import brightspot.search.boost.HasSiteSearchBoostIndexes;
import brightspot.search.modifier.exclusion.SearchExcludable;
import brightspot.section.HasSecondarySectionsWithField;
import brightspot.section.HasSection;
import brightspot.section.Section;
import brightspot.seo.SeoWithFields;
import brightspot.share.Shareable;
import brightspot.sharedcontent.SharedContent;
import brightspot.site.DefaultSiteMapItem;
import brightspot.tag.HasTagsWithField;
import brightspot.tag.Tag;
import brightspot.update.LastUpdatedProvider;
import brightspot.urlslug.HasUrlSlugWithField;
import brightspot.util.RichTextUtils;
import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Site;
import com.psddev.cms.db.SiteSettings;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Utils;
import com.psddev.feed.FeedItem;
import com.psddev.sitemap.NewsSiteMapItem;
import com.psddev.sitemap.SiteMapEntry;
import com.psddev.sitemap.SiteMapNews;
import com.psddev.sitemap.SiteMapSettingsModification;
import com.psddev.suggestions.Suggestable;
import com.psddev.theme.StyleEmbeddedContentCreator;
import org.apache.commons.lang3.StringUtils;

@ToolUi.FieldDisplayOrder({
        "hasBlogWithField.blog",
        "headline",
        "subheadline",
        "hasUrlSlug.urlSlug",
        "hasAuthorsWithField.authors",
        "lead",
        "body",
        "hasTags.tags",
        "embargoable.embargo"
})
@Recordable.DisplayName("Blog Post")
@ToolUi.IconName("rate_review")
public class BlogPostPage extends Content implements
        CascadingPageElements,
        Embargoable,
        DefaultSiteMapItem,
        FeedItem,
        HasAuthorsWithField,
        HasBlogWithField,
        HasBreadcrumbs,
        HasCommenting,
        HasCoralPageMetadata,
        HasDisqusPageMetadata,
        HasMediaTypeWithOverride,
        HasSecondarySectionsWithField,
        HasSection,
        HasSiteSearchBoostIndexes,
        HasTagsWithField,
        HasUrlSlugWithField,
        NewsSiteMapItem,
        OpenGraphArticle,
        Page,
        PagePromotableWithOverrides,
        SearchExcludable,
        SeoWithFields,
        Shareable,
        SharedContent,
        Suggestable,
        SupportsAdInjection, Recordable {

    @Indexed
    @Required
    @ToolUi.NoteHtml("<span data-dynamic-html=\"${content.getSeoTitleNoteHtml()}\"></span>")
    @ToolUi.RichText(toolbar = TinyRichTextToolbar.class)
    private String headline;

    @ToolUi.RichText(toolbar = TinyRichTextToolbar.class)
    private String subheadline;

    @ToolUi.RichText(inline = false, toolbar = LargeRichTextToolbar.class, lines = 10)
    private String body;

    @ToolUi.EmbeddedContentCreatorClass(StyleEmbeddedContentCreator.class)
    private ArticleLead lead;

    public String getFullBody() {
        return getBody();
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSubheadline() {
        return subheadline;
    }

    public void setSubheadline(String subheadline) {
        this.subheadline = subheadline;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ArticleLead getLead() {
        return lead;
    }

    public void setLead(ArticleLead lead) {
        this.lead = lead;
    }

    // --- HasBreadcrumbs support ---

    @Override
    public List<?> getBreadcrumbs() {
        List<Section> ancestors = getSectionAncestors();
        Collections.reverse(ancestors);
        return ancestors;
    }

    // --- HasSiteSearchBoostIndexes support ---

    @Override
    public String getSiteSearchBoostTitle() {
        return getHeadline();
    }

    @Override
    public String getSiteSearchBoostDescription() {
        return getSubheadline();
    }

    // --- Linkable support ---

    @Override
    public String getLinkableText() {
        return getPagePromotableTitle();
    }

    // --- SeoWithFields support ---

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
        return Utils.toNormalized(RichTextUtils.richTextToPlainText(getHeadline()));
    }

    // --- OpenGraphArticle support ---

    @Override
    public Date getOpenGraphArticleModifiedDate() {
        return getUpdateDate();
    }

    @Override
    public Date getOpenGraphArticlePublishedDate() {
        return getPublishDate();
    }

    @Override
    public List<String> getOpenGraphArticleAuthorUrls(Site site) {
        return Optional.ofNullable(asHasAuthorsWithFieldData().getAuthors())
                .flatMap(authors -> Optional.ofNullable(authors.stream()))
                .orElseGet(Stream::empty)
                .map(author -> Permalink.getPermalink(site, author))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String getOpenGraphArticleSection() {
        return Optional.ofNullable(asHasSectionData().getSectionParent())
                .map(Section::getSectionDisplayNamePlainText)
                .orElse(null);
    }

    @Override
    public List<String> getOpenGraphArticleTags() {
        return getTags()
                .stream()
                .map(Tag::getTagDisplayNamePlainText)
                .collect(Collectors.toList());
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
        return Permalink.getPermalink(site, this);
    }

    // --- NewsSiteMapItem support ---

    @Override
    public List<SiteMapEntry> getNewsSiteMapEntries(Site site) {
        String sitePermalinkPath = as(Directory.ObjectModification.class).getSitePermalinkPath(site);
        if (StringUtils.isBlank(sitePermalinkPath)) {
            return Collections.emptyList();
        }

        Locale locale = ObjectUtils.firstNonNull(
                LocaleProvider.getModelLocale(site, this),
                LocaleProvider.DEFAULT_LOCALE);

        SiteMapEntry siteMapEntry = new SiteMapEntry();
        siteMapEntry.setUpdateDate(
                ObjectUtils.firstNonNull(
                        LastUpdatedProvider.getMostRecentUpdateDate(getState()),
                        getState().as(Content.ObjectModification.class).getPublishDate()
                )
        );
        siteMapEntry.setPermalink(SiteSettings.get(
                site,
                f -> f.as(SiteMapSettingsModification.class).getSiteMapDefaultUrl()
                        + StringUtils.prependIfMissing(sitePermalinkPath, "/")));

        SiteMapNews siteMapNews = new SiteMapNews();
        siteMapNews.setName(site != null ? site.getName() : "Global");

        siteMapNews.setLanguage(locale.getISO3Language());
        siteMapNews.setPublicationDate(this.getPublishDate());
        siteMapNews.setTitle(ObjectUtils.firstNonBlank(
                getSeoTitle(),
                RichTextUtils.richTextToPlainText(this.getHeadline())
        ));
        List<String> keywords = getTags()
                .stream()
                .map(Tag::getTagDisplayNamePlainText)
                .collect(Collectors.toList());

        if (!ObjectUtils.isBlank(keywords)) {
            if (keywords.size() > 10) {
                keywords = keywords.subList(0, 10);
            }
            siteMapNews.setKeywords(keywords);
        }

        siteMapEntry.setNews(Collections.singletonList(siteMapNews));

        return Collections.singletonList(siteMapEntry);
    }

    // --- Suggestable support ---

    @Override
    public String getSuggestableText() {
        return Optional.ofNullable(RichTextUtils.richTextToPlainText(getHeadline())).orElse("") + " "
                + Optional.ofNullable(RichTextUtils.richTextToPlainText(getSubheadline())).orElse("") + " "
                + Optional.ofNullable(getFullBody())
                .map(RichTextUtils::stripRichTextElements)
                .map(RichTextUtils::richTextToPlainText)
                .orElse("");
    }

    // --- Recordable support ---

    @Override
    public String getLabel() {
        return RichTextUtils.richTextToPlainText(getHeadline());
    }

    // *** Promotable implementation *** //

    @Override
    public String getPagePromotableTitleFallback() {
        return getHeadline();
    }

    @Override
    public String getPagePromotableDescriptionFallback() {
        return getSubheadline();
    }

    @Override
    public WebImageAsset getPagePromotableImageFallback() {
        return Optional.ofNullable(getLead())
                .map(ArticleLead::getArticleLeadImage)
                .orElseGet(() -> ImageRichTextElement.getFirstImageFromRichText(getFullBody()));
    }

    @Override
    public String getPagePromotableType() {
        return Optional.ofNullable(getPrimaryMediaType())
                .map(MediaType::getIconName)
                .orElse(null);
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
                .orElseGet(null);
    }

    public String getReadDuration() {
        long characterCount = Optional.ofNullable(getFullBody())
                .map(RichTextUtils::stripRichTextElements)
                .map(RichTextUtils::richTextToPlainText)
                .map(String::length)
                .orElse(0);

        // TODO: localization needed. This implementation is highly dependent on language!
        // Average adult reading time given: 275 wpm. Average number of characters per English
        // word is 4-5 characters. Therefore, average is 1100-1375 CPM. 1200 CPM used.
        long readTime = characterCount / 1_200;

        if (readTime < 1) {
            readTime = 1;
        }

        return Long.toString(readTime) + " Min Read";
    }

    // --- Shareable support ---'

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

    // --- FeedItem support ---

    @Override
    public String getFullContentEncoded() {
        return Optional.ofNullable(getFullBody())
                .map(RichTextUtils::stripRichTextElements)
                .map(RichTextUtils::richTextToPlainText)
                .orElse(null);
    }

    // --- Section support ---

    @Override
    public Section getSectionParent() {
        Blog parent = getBlog();
        if (parent instanceof Section) {
            return (Section) parent;
        }
        return null;
    }
}
