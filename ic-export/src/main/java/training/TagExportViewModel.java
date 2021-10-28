package training;

import brightspot.core.tag.Tag;
import com.psddev.cms.view.JsonView;
import com.psddev.cms.view.ViewInterface;
import com.psddev.cms.view.ViewKey;
import com.psddev.dari.util.ObjectUtils;
import org.apache.commons.text.StringEscapeUtils;

@JsonView
@ViewInterface
public class TagExportViewModel extends PageExportViewModel<Tag> implements ExportEntryView {

    @ViewKey("displayName")
    public CharSequence getDisplayName() {
        return StringEscapeUtils.escapeHtml4(model.getDisplayName());
    }

    @ViewKey("internalName")
    public String getInternalName() {
        return ObjectUtils.to(String.class, model.getState().get("name"));
    }

    @ViewKey("description")
    public CharSequence getDescription() {
        return ExportUtils.processRichText(model, Tag::getDescription);
    }

    @ViewKey("parent")
    public RefView getParent() {
        return createView(RefView.class, model.getParent());
    }

    @ViewKey("hidden")
    public boolean getHidden() {
        return model.isHidden();
    }

    @ViewKey("lead")
    public ModulePageLeadView getLead() {
        ModulePageLeadView leadView = createView(ModulePageLeadView.class, model.getLead());
        if (leadView == null) {
            leadView = createView(DefaultModulePageLeadViewModel.class, model);
        }
        return leadView;
    }

    @ViewKey("hasUrlSlug.urlSlug")
    public String getUrlSlug() {
        return ObjectUtils.to(String.class, model.getState().get("sluggable.slug"));
    }
}
