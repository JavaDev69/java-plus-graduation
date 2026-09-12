package ru.practicum.analyzer.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.practicum.analyzer.dal.model.EventSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                TimestampsMapper.class
        }
)
public interface EventSimilarityMapper {
    @Mapping(target = "firstEvent", source = "eventA")
    @Mapping(target = "secondEvent", source = "eventB")
    @Mapping(target = "similarity", source = "score")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "id", ignore = true)
    EventSimilarity toEntity(EventSimilarityAvro eventSimilarityAvro);

    @Mapping(source = "firstEvent", target = "eventA")
    @Mapping(source = "secondEvent", target = "eventB")
    @Mapping(source = "similarity", target = "score")
    @Mapping(source = "timestamp", target = "timestamp")
    EventSimilarityAvro toDto(EventSimilarity eventSimilarity);
}