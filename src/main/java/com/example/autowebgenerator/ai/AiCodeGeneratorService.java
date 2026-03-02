package com.example.autowebgenerator.ai;

import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * AI code generation service — declared as a plain Java interface.
 *
 * LangChain4j's AiServices.create() generates a runtime proxy that:
 *   1. Reads the @SystemMessage prompt from the classpath resource.
 *   2. Calls the configured OpenAI ChatModel / StreamingChatModel.
 *   3. For POJO return types  → appends a JSON schema instruction and parses the response.
 *   4. For TokenStream return → streams tokens back token-by-token via the StreamingChatModel.
 */
public interface AiCodeGeneratorService {

    // -------------------------------------------------------------------------
    // Synchronous (structured JSON output)
    // -------------------------------------------------------------------------

    /**
     * Generate a single-file HTML website (synchronous).
     *
     * @param userMessage natural-language description of the desired website
     * @return parsed result containing htmlCode and description fields
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * Generate a three-file website — HTML, CSS, JS — (synchronous).
     *
     * @param userMessage natural-language description of the desired website
     * @return parsed result containing htmlCode, cssCode, jsCode, and description
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    // -------------------------------------------------------------------------
    // Streaming (SSE / Server-Sent Events)
    // -------------------------------------------------------------------------

    /**
     * Generate a single-file HTML website (streaming).
     *
     * The AI responds in markdown with a ```html code block.
     * Tokens are delivered one-by-one via the returned TokenStream.
     * On completion the caller should parse the accumulated string with CodeParser.
     *
     * @param userMessage natural-language description of the desired website
     * @return TokenStream — attach handlers with .onPartialResponse / .onCompleteResponse
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * Generate a three-file website (streaming).
     *
     * The AI responds in markdown with ```html, ```css, and ```js code blocks.
     * On completion the caller should parse the accumulated string with CodeParser.
     *
     * @param userMessage natural-language description of the desired website
     * @return Flux<String> — each element is one token from the model
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
