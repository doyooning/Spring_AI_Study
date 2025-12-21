package com.dynii.springai.domain.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Component
@RequiredArgsConstructor
public class RagSeedIngestor implements ApplicationRunner {

    private final RagIngestService ragIngestService;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Resource[] resources =
                new PathMatchingResourcePatternResolver(resourceLoader)
                        .getResources("classpath:rag/seed/*");

        for (Resource resource : resources) {
            ragIngestService.ingest(resource);
            log.info("Ingested : " + resource.getFilename());
        }
    }
}

