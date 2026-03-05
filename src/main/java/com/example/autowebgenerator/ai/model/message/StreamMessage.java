package com.example.autowebgenerator.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for all typed SSE stream messages.
 *
 * Every SSE event the backend sends has a {@code type} field so the frontend
 * can dispatch to the right handler without fragile string-prefix heuristics.
 *
 * NOTE: {@code @NoArgsConstructor} is required — Jackson needs a no-arg
 * constructor to deserialize these as JSON.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {
    private String type;
}
