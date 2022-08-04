package brightspot.podcast;

import java.time.Duration;
import java.util.Optional;

import brightspot.audio.Audio;
import brightspot.audio.file.AudioFile;
import brightspot.audio.file.AudioStorageItemWrapper;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.StorageItem;

@ToolUi.IconName("settings_voice")
@Recordable.DisplayName("Podcast Episode")
@ToolUi.FieldDisplayOrder({
    "hasPodcastWithField.podcast",
    "episodeNumber",
    "title",
    "description",
    "hasUrlSlug.urlSlug",
    "primaryAudio",
    "coverImageOverride",
    "body",
    "landingCascading.content",
    "hasTags.tags",
    "embargoable.embargo"
})
public class AudioPodcastEpisodePage extends AbstractPodcastEpisodePage {

    private Audio primaryAudio;

    public Audio getPrimaryAudio() {
        return primaryAudio;
    }

    public void setPrimaryAudio(Audio primaryAudio) {
        this.primaryAudio = primaryAudio;
    }

    @Override
    public Object getMedia() {
        return getPrimaryAudio();
    }

    @Override
    public String getRssFeedItemEnclosureUrlFallback() {
        //TODO this only supports AudioFile
        return Optional.ofNullable(getPrimaryAudio())
            .filter(AudioFile.class::isInstance)
            .map(AudioFile.class::cast)
            .map(AudioFile::getItems)
            .flatMap(sources -> sources.stream().map(AudioStorageItemWrapper::getFile).findFirst())
            .map(StorageItem::getSecurePublicUrl)
            .orElse(null);
    }

    @Override
    public Long getRssFeedItemEnclosureLengthFallback() {
        //TODO this only supports AudioFile
        return Optional.ofNullable(getPrimaryAudio())
            .filter(AudioFile.class::isInstance)
            .map(AudioFile.class::cast)
            .map(AudioFile::getItems)
            .flatMap(sources -> sources.stream().filter(wrapper -> wrapper.getFile() != null).findFirst())
            .map(AudioStorageItemWrapper::getLength)
            .orElse(null);
    }

    @Override
    public String getRssFeedItemEnclosureTypeFallback() {
        //TODO this only supports AudioFile
        return Optional.ofNullable(getPrimaryAudio())
            .filter(AudioFile.class::isInstance)
            .map(AudioFile.class::cast)
            .map(AudioFile::getItems)
            .flatMap(sources -> sources.stream().filter(wrapper -> wrapper.getFile() != null).findFirst())
            .map(AudioStorageItemWrapper::getMimeType)
            .orElse(null);
    }

    @Override
    public Long getAppleRssFeedItemDurationFallback() {
        //TODO this only supports AudioFile
        return Optional.ofNullable(getPrimaryAudio())
            .filter(AudioFile.class::isInstance)
            .map(AudioFile.class::cast)
            .map(AudioFile::getItems)
            .flatMap(sources -> sources.stream().filter(wrapper -> wrapper.getFile() != null).findFirst())
            .map(AudioStorageItemWrapper::getDuration)
            .map(Duration::getSeconds)
            .orElse(null);
    }
}
