package de.unimuenster.imi.randimi.repository.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import de.unimuenster.imi.randimi.model.study.Site;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class SiteRepositoryImpl {

	@Autowired
	@Lazy
	SiteRepository siteRepository;

	public Pair<List<SiteDTO>, List<Site>> getNewAndDeletedSites(StudyDTO studyDTO) {
		List<SiteDTO> newSites;
		List<Site> deletedSites;

		if (studyDTO.getId() != null && studyDTO.getId() != 0) {
			newSites = new ArrayList<SiteDTO>();

			List<Site> currentSites = siteRepository.findByStudyId(studyDTO.getId());
			Map<Long, Site> currentSitesMap = IntStream.range(0, currentSites.size()).boxed()
					.collect(Collectors.toMap(i -> currentSites.get(i).getId(), currentSites::get));
			Map<Long, SiteDTO> dtoSitesMap = new HashMap<Long, SiteDTO>();

			for (SiteDTO siteDTO : studyDTO.getSites()) {
				if (siteDTO.getId() == null || siteDTO.getId() == 0)
					newSites.add(siteDTO);
				else
					dtoSitesMap.put(siteDTO.getId(), siteDTO);
			}

			HashSet<Long> deletedSiteIds = new HashSet<Long>(currentSitesMap.keySet());
			deletedSiteIds.removeAll(dtoSitesMap.keySet());
			deletedSites = deletedSiteIds.stream().map(currentSitesMap::get).collect(Collectors.toList());
		} else {
			newSites = studyDTO.getSites();
			deletedSites = new ArrayList<Site>();
		}

		return Pair.of(newSites, deletedSites);
	}

}
