package com.example.autowebgenerator.ai;

import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

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
    TokenStream generateHtmlCodeStream(String userMessage);

    /**
     * Generate a three-file website (streaming).
     *
     * The AI responds in markdown with ```html, ```css, and ```js code blocks.
     * On completion the caller should parse the accumulated string with CodeParser.
     *
     * @param userMessage natural-language description of the desired website
     * @return TokenStream — converted to Flux<String> in AiCodeGeneratorFacade
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    TokenStream generateMultiFileCodeStream(String userMessage);

    /**
     * Generate a Vue3 project (streaming).
     *
     * The AI outputs every file using [FILE:path]...[/FILE] delimiters and then a
     * plain-text summary. Tokens stream back via the returned TokenStream.
     * On completion, the caller parses the accumulated buffer with
     * CodeParser.parseVueProjectFiles(), saves the files, and runs npm build.
     *
     * @param userMessage natural-language description of the desired app
     * @return TokenStream — attach handlers with .onPartialResponse / .onCompleteResponse
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

    /**
     * Auto-fix a Vue3 project after a build failure (synchronous).
     *
     * The npm error output is included in errorContext so the AI can identify and
     * fix the broken files by calling writeFile() for each corrected file.
     *
     * @param appId        the app's ID (used to select the correct chat memory)
     * @param errorContext the build error + instruction to fix
     * @return AI response (the side-effect is writeFile() tool calls)
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    String fixVueProjectCode(@MemoryId long appId, @UserMessage String errorContext);
}
