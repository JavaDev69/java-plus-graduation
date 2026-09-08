package ru.practicum.collector.mapper;

import org.mapstruct.Builder;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        builder = @Builder(disableBuilder = true),
        uses = {
                TimestampsMapper.class
        }
)
public interface UserActionMapper {
    UserActionAvro toAvro(UserActionProto userActionProto);

    @EnumMapping(
            nameTransformationStrategy = MappingConstants.STRIP_PREFIX_TRANSFORMATION,
            configuration = "ACTION_"
    )
    @ValueMapping(
            source = "UNRECOGNIZED",
            target = MappingConstants.THROW_EXCEPTION
    )
    ActionTypeAvro map(ActionTypeProto actionTypeProto);

}