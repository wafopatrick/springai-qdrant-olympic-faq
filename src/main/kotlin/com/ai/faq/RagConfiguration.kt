package com.ai.faq

import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.reader.TextReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files
import java.nio.file.Paths

@Configuration
class RagConfiguration {

    private val logger = org.slf4j.LoggerFactory.getLogger(RagConfiguration::class.java)

    @Bean
    fun qdrantVectorStore(
        qdrantClient: QdrantClient,
        embeddingModel: EmbeddingModel,
        @Value("\${vectorstore.qdrant.collection-name}") collectionName: String
    ): QdrantVectorStore {
        val qdrantVectorStore = QdrantVectorStore(qdrantClient, collectionName, embeddingModel, true)
        qdrantVectorStore.afterPropertiesSet()
        if (isFaqsAlreadyLoaded()) {
            logger.info("FAQs already loaded")
        } else {
            val faqResource = ClassPathResource("data/olympic-faq.txt")
            val textReader = TextReader(faqResource)
            textReader.customMetadata["filename"] = faqResource.filename
            val documents = textReader.get()
            val tokenTextSplitter = TokenTextSplitter()
            val splitDocuments = tokenTextSplitter.apply(documents)
            qdrantVectorStore.add(splitDocuments)
            val faqLoadedFilePath = Paths.get("src", "main", "resources", "data", "faq-loaded.txt")
            Files.createFile(faqLoadedFilePath)
            logger.info("FAQs loaded")
        }
        return qdrantVectorStore
    }

    private fun isFaqsAlreadyLoaded(): Boolean {
        val resource = ClassPathResource("data/faq-loaded.txt")
        return resource.exists()
    }

    @Bean
    fun qdrantClient(
        @Value("\${vectorstore.qdrant.host}") host: String,
        @Value("\${vectorstore.qdrant.port}") port: Int,
        @Value("\${vectorstore.qdrant.api-key}") apiKey: String
    ): QdrantClient {
        val grpcClient = QdrantGrpcClient.newBuilder(host, port, false).withApiKey(apiKey).build()
        return QdrantClient(grpcClient)
    }
}