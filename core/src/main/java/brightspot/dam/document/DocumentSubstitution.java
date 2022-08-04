package brightspot.dam.document;

import java.util.Optional;

import brightspot.image.WebImageAsset;
import brightspot.promo.attachment.AttachmentPromotableWithOverrides;
import com.psddev.cms.db.Site;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.Substitution;
import org.apache.commons.io.FileUtils;

public class DocumentSubstitution extends Document implements Substitution, AttachmentPromotableWithOverrides {

    @Override
    public String getAttachmentPromotableTitleFallback() {
        return getAttachmentTitle();
    }

    @Override
    public String getAttachmentPromotableDescriptionFallback() {
        return getDescription();
    }

    @Override
    public WebImageAsset getAttachmentPromotableImageFallback() {
        return null;
    }

    @Override
    public String getAttachmentPromotableFileSizeFallback() {
        return Optional.ofNullable(getFile())
                .map(StorageItem::getHttpHeaders)
                .map(headersMap -> headersMap.get("Content-Length"))
                .map(sizes -> sizes.get(0))
                .map(size -> FileUtils.byteCountToDisplaySize(Long.parseLong(size)))
                .orElse(null);
    }

    @Override
    public String getAttachmentPromotableDownloadUrl(Site site) {
        return getAttachmentFileUrl();
    }

    @Override
    public String getAttachmentPromotableFileName() {
        return getAttachmentFilename();
    }

    @Override
    public String getAttachmentPromotableFileMimeType() {
        return getAttachmentFileMimeType();
    }
}
