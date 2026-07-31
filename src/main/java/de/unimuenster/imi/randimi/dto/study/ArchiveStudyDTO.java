package de.unimuenster.imi.randimi.dto.study;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ArchiveStudyDTO {

	@NotNull(message = "{validator.general.mustNotBeNull}")
	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long studyId;

	@Nullable
	@Future(message = "{validator.archiveStudy.retentionPeriodMustBeInTheFuture}")
	private LocalDate retentionPeriod;
}
