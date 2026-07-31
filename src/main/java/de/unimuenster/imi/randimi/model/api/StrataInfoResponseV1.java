package de.unimuenster.imi.randimi.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.List;

@Value
@Schema(description = "Response containing information about the strata.")
public class StrataInfoResponseV1 {

    @JsonProperty
    @Schema(description = "Definitions of all strata in the study.",
            example = "[{\"name\":\"Color\",\"type\":\"ENUM\",\"values\":[\"red\",\"blue\",\"green\"]}]")
    List<Definition> strata;

    /**
     * Shared definition of stratum
     */
    @Schema(description = "Shared interface of a stratum definition which is implemented by FactorDefinition (see section 'Models' further down).",
            anyOf = {FactorDefinition.class})
    public interface Definition {
        @Schema(description = "Name of the Stratum")
        String getName();
        @Schema(description = "API ID of the Stratum")
        String getApiId();
        @Schema(description = "Type of the stratum", example = "ENUM")
        StratumType getType();
    }

//    @Value
//    @Schema(description = "Describes a stratum where values from one or more intervals are possible. 'type' MUST be 'INTERVAL'.")
//    public static class IntervalDefinition implements Definition {
//        String name;
//
//        @Schema(description = "List of interval parts.")
//        List<IntervalPart> values;
//
//		@Override
//        public StratumType getType() {
//            return StratumType.INTERVAL;
//        }
//    }
//
//    @Value
//    @Schema(description = "Describes an Interval [min, max]")
//    public static class IntervalPart {
//        float min;
//        float max;
//    }

    @Value
    @Schema(description = "Describes a stratum where different factorized values are possible. 'type' MUST be 'ENUM'.")
    public static class FactorDefinition implements Definition {
        String name;
        String apiId;
        @Schema(description = "List of possible factors, eg. colors ['red', 'blue', 'yellow'...]")
        List<String> values;
		
		@Override
        public StratumType getType() {
            return StratumType.ENUM;
        }
    }
}
