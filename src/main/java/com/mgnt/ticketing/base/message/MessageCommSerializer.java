package com.mgnt.ticketing.base.message;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MessageCommSerializer extends StdSerializer<MessageCommInterface> {

    protected MessageCommSerializer(Class<MessageCommInterface> t) {
        super(t);
    }

    @Override
    public void serialize(MessageCommInterface value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("code", value.getCode());
        gen.writeStringField("message", value.getMessage());
        gen.writeEndObject();
    }
}
