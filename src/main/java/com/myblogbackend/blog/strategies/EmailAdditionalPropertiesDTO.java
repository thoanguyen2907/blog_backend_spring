package com.myblogbackend.blog.strategies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAdditionalPropertiesDTO {

    private String confirmationToken;
}
