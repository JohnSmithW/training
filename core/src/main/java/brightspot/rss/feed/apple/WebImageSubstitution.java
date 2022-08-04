package brightspot.rss.feed.apple;

import brightspot.image.WebImage;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.Substitution;

public class WebImageSubstitution extends WebImage implements RssWebImage, Substitution {

    @Override
    public Integer getRssWebImageWidth() {
        return getWidth();
    }

    @Override
    public Integer getRssWebImageHeight() {
        return getHeight();
    }

    @Override
    public StorageItem getRssWebImageAssetFile() {
        return getFile();
    }
}
