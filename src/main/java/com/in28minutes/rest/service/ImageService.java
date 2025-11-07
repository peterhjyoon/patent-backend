package com.in28minutes.rest.service;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private final OpenAiImageModel imageClient;

    public ImageService(OpenAiImageModel imageClient) {
        this.imageClient = imageClient;
    }

    public ImageResponse generateImage(String prompt) {
        ImageOptions imageOptions = ImageOptionsBuilder.builder()
                .withN(1) //Number of images to be generated
                .withHeight(1024)
                .withWidth(1024)
                .build();

        // This can be used for some additional options specific to OpenAI, but it is not portable abstraction unlike 'ImageOptions' above,
        // so it will need to be updated when switching AI models
        //    OpenAiImageOptions openAiImageOptions = OpenAiImageOptions.builder()
        //    .withQuality("hd")
        //    .withN(1)
        //    .withHeight(1024)
        //    .withWidth(1024)
        //    .build();


        return imageClient.call(new ImagePrompt(prompt, imageOptions));
    }
}
