package de.unimuenster.imi.randimi.mapping.study;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.model.api.SiteResource;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import org.springframework.stereotype.Component;

@Component
public class SiteMapper {

    final NamesMapper namesMapper;

	public SiteMapper(final NamesMapper namesMapper) {
		this.namesMapper = namesMapper;
	}

	public SiteDTO toSiteDto(Site model) {
        SiteDTO dto = new SiteDTO();
        dto.setId(model.getId());

        namesMapper.toNamesDTO(model, dto);

        dto.setOrderNumber(model.getOrderNumber());
        dto.setCapacity(model.getCapacity());
        dto.setSeed(model.getSeed());
        dto.setPseudonymRegex(model.getPseudonymRegex());

        return dto;
    }

    /**
     * Converts a {@link SiteDTO} to a {@link Site}.
     * If a site with the same ID as dto is already present in the given study,
     * the fields of the present site will be overwritten.
     *
     * @param dto The DTO to convert.
     * @param study Study that might contain the original site.
     * @param orderNumber Order number of the converted site.
     * @return The converted site.
     */
    public Site toSite(final SiteDTO dto, final Study study, final int orderNumber) {
        final Site originalSite;

        if (dto.getId() != null && dto.getId() != 0) {
            originalSite = study.getSites().stream()
                                .filter(s -> s.getId() == dto.getId())
                                .findFirst()
                                .orElseGet(Site::new);
        } else {
            originalSite = new Site();
        }

        return toSite(dto, originalSite, orderNumber);
    }

    public Site toSite(SiteDTO dto, Site site, final int orderNumber) {
        if (dto.getId() != null && dto.getId() != 0) {
            site.setId(dto.getId());
        }

        namesMapper.toNamedEntity(dto, site);

        site.setOrderNumber(orderNumber);
        site.setCapacity(dto.getCapacity());
        site.setPseudonymRegex(dto.getPseudonymRegex());

        if (dto.getSeed() != null) {
            site.setSeed(dto.getSeed());
        } else {
            site.setSeed(System.currentTimeMillis());
        }
        return site;
    }

    /**
     * Converts a {@link Site} to a {@link SiteResource}.
     *
     * @param site The site to convert.
     * @return The converted site resource.
     */
    public SiteResource toSiteResource(final Site site) {
        return new SiteResource(site.getGuiName(), site.getApiId());
    }
}
