package com.example.autowebgenerator.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Wraps a single AI text token (or a status string like "Building Vue project...").
 *
 * JSON shape: {@code {"type":"ai_response","data":"token text"}}
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AiResponseMessage extends StreamMessage {

    /** The partial AI text, or a status message emitted by the backend. */
    private String data;

    public AiResponseMessage(String data) {
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        this.data = data;
    }
}
