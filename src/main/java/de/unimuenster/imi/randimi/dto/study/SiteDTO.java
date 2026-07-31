package de.unimuenster.imi.randimi.dto.study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.SelectInputOption;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@EqualsAndHashCode(callSuper = true)
@Setter @Getter
public class SiteDTO extends NamesDTO implements SelectInputOption {

    @JsonIgnore
    private Long id = 0L;

    private Integer orderNumber = 0;

    @JsonIgnore
    private Boolean empty = true;

    private String pseudonymRegex;

    @Nullable
    private Integer capacity;

    private Long seed;

    /**
     * {@inheritDoc}
     * @return
     */
    @JsonIgnore
    @Override
    public boolean isFormEmpty() {
        return super.isFormEmpty()
               && (id == null || id == 0)
               && capacity == null
               && (pseudonymRegex == null || pseudonymRegex.isBlank())
               && seed == null;
    }

    @Override
    public boolean lookupTranslation() {
        return false;
    }

    @JsonIgnore
    @Override
    public String getOptionName() {
        return getGuiName();
    }

    @JsonIgnore
    @Override
    public String getOptionValue() {
        return String.valueOf(id);
    }
}
