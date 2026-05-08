package com.cinq.vestaboard.infrastructure.api.vestaboard;

import com.cinq.vestaboard.configuration.properties.VestaBoardProperties;
import com.cinq.vestaboard.configuration.properties.VestaBoardVbmlProperties;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@AllArgsConstructor
public class VestaBoardGateway {
    private RestClient restClient;
    private VestaBoardProperties vestaBoardProperties;
    private VestaBoardVbmlProperties vestaBoardVbmlProperties;

    public GetCurrentMessageResponse getCurrentMessage() {
        return restClient.get()
                .uri(vestaBoardProperties.getBaseUrl())
                .header("X-Vestaboard-Token", vestaBoardProperties.getToken())
                .retrieve()
                .body(GetCurrentMessageResponse.class);
    }

    public SetMessageResponse setMessage(SetMessageRequest message) {
        return restClient.post()
                .uri(vestaBoardProperties.getBaseUrl())
                .header("X-Vestaboard-Token", vestaBoardProperties.getToken())
                .body(message)
                .retrieve()
                .body(SetMessageResponse.class);
    }

    public int[][] compose(VestaboardMessage message) {
        RestClient client = restClient.mutate().baseUrl(vestaBoardVbmlProperties.getBaseUrl()).build();

        return client.post()
                .header("X-Vestaboard-Token", vestaBoardProperties.getToken())
                .body(message)
                .retrieve()
                .body(int[][].class);
    }

    public SetMessageResponse setMessage(SetMessageCharactersRequest request) {
        return restClient.post()
                .uri(vestaBoardProperties.getBaseUrl())
                .header("X-Vestaboard-Token", vestaBoardProperties.getToken())
                .body(request)
                .retrieve()
                .body(SetMessageResponse.class);
    }
}
