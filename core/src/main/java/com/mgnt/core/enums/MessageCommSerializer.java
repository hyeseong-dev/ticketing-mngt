package com.mgnt.core.enums;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * MessageCommInterface를 위한 커스텀 JSON 직렬화 클래스
 * <p>
 * 이 클래스는 MessageCommInterface 객체를 JSON으로 직렬화할 때 사용됩니다.
 */
public class MessageCommSerializer extends StdSerializer<MessageCommInterface> {

    /**
     * 생성자
     *
     * @param t 직렬화할 클래스 타입
     */
    protected MessageCommSerializer(Class<MessageCommInterface> t) {
        super(t);
    }

    /**
     * MessageCommInterface 객체를 JSON으로 직렬화하는 메서드
     *
     * @param value    직렬화할 객체
     * @param gen      JSON 생성기
     * @param provider 직렬화 제공자
     * @throws IOException 입출력 예외
     */
    @Override
    public void serialize(MessageCommInterface value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("code", value.getCode());
        gen.writeStringField("message", value.getMessage());
        gen.writeEndObject();
    }
}
