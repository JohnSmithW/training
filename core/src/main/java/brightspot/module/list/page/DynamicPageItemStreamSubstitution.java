package brightspot.module.list.page;

import brightspot.itemstream.MemoizationDynamicQueryModifiable;
import brightspot.itemstream.PathedOnlyQueryModifiableWithField;
import brightspot.itemstream.SiteItemsQueryModifiableWithField;
import brightspot.l10n.LocaleDynamicQueryModifiable;
import brightspot.promo.page.PagePromotableDynamicQueryModifiable;
import brightspot.sponsor.SponsorDynamicQueryModifiable;
import com.psddev.dari.util.Substitution;

public class DynamicPageItemStreamSubstitution extends DynamicPageItemStream implements
        Substitution,
        LocaleDynamicQueryModifiable,
        MemoizationDynamicQueryModifiable,
        PagePromotableDynamicQueryModifiable,
        PathedOnlyQueryModifiableWithField,
        SiteItemsQueryModifiableWithField,
        SponsorDynamicQueryModifiable {

}
