package ru.practicum.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.analyzer.dal.model.Interaction;
import ru.practicum.analyzer.properties.WeightProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                TimestampsMapper.class
        }
)
public abstract class InteractionMapper {
    @Autowired
    protected WeightProperties weightProperties;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "rating", source = "actionType", qualifiedByName = "getWeight")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract Interaction toEntity(UserActionAvro userActionAvro);

    @Named("getWeight")
    protected double getWeight(ActionTypeAvro actionType) {
        if (actionType == null) {
            return 0.;
        }

        return switch (actionType) {
            case VIEW -> weightProperties.getView();
            case REGISTER -> weightProperties.getRegister();
            case LIKE -> weightProperties.getLike();
        };
    }
}