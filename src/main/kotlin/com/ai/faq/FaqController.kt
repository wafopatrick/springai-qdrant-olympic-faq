package com.ai.faq

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FaqController(
    chatClientBuilder: ChatClient.Builder,
    var vectorStore: VectorStore,
    @Value("classpath:/prompts/rag-prompt-template.st") var ragPromptTemplate: Resource
) {

    var chatClient: ChatClient = chatClientBuilder.build()

    @GetMapping("/faq")
    fun faq(@RequestParam(value = "message", defaultValue = "How many athletes compete in the olympic games paris 2024") message: String) : String? {
        val similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(2))
        val contentList = similarDocuments.map { it.content }.toList()
        val promptTemplate = PromptTemplate(ragPromptTemplate)
        val promptParameters = mapOf("input" to message, "documents" to contentList.joinToString("\n"))
        val prompt = promptTemplate.create(promptParameters)
        return chatClient.prompt(prompt).call().content()
    }
}