package com.example.controller.converter;

import com.example.model.Visitor;
import com.example.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class VisitorConverter implements Converter<String, Visitor> {

    private final VisitorService visitorService;

    @Override
    public Visitor convert(@Nullable String source) {
        if (Objects.requireNonNullElse(source, "").isBlank()) {
            return null;
        }
        var visitor = visitorService.getByName(source);
        if (visitor != null) {
            return visitor;
        }
        visitor = new Visitor(source);
        return visitorService.create(visitor);
    }
}
